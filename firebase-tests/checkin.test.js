import { before, after, beforeEach, test } from "node:test";
import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { createRequire } from "node:module";
const { initializeTestEnvironment, assertFails } = createRequire(import.meta.url)("@firebase/rules-unit-testing");
const { doc, getDoc, getDocFromServer, setDoc, runTransaction, collection, query, where, getDocs } = createRequire(import.meta.url)("firebase/firestore");

if (!process.env.FIRESTORE_EMULATOR_HOST?.startsWith("127.0.0.1:")) throw new Error("LOCAL_FIRESTORE_EMULATOR_REQUIRED");
let env;
before(async () => { env = await initializeTestEnvironment({ projectId: "demo-akj-checkin", firestore: { rules: await readFile(new URL("../firestore.rules", import.meta.url), "utf8") } }); });
after(async () => { await env?.cleanup(); });
beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, "events/event"), { organizerId: "organizer", title: "Evento de prueba", status: "VERIFIED", capacity: 2, attendanceCount: 0, interestCount: 0 });
    for (const uid of ["attendee", "organizer", "other"]) await setDoc(doc(db, `users/${uid}`), { id: uid, role: "USER", points: 100, badges: [] });
  });
});

// Mismo protocolo de lecturas/escrituras de FirestoreCheckInRepository. Solo emulador/demo.
async function complete(db, uid = "attendee") {
  const e = doc(db, "events/event"), a = doc(db, `attendances/event_${uid}`), u = doc(db, `users/${uid}`);
  const h = doc(db, `pointsHistory/checkin_event_${uid}`);
  try { return await runTransaction(db, async (tx) => {
    const event = (await tx.get(e)).data();
    if (event.status !== "VERIFIED") return "closed";
    if (event.organizerId === uid) return "own";
    const attendance = (await tx.get(a)).data();
    const user = (await tx.get(u)).data();
    if (attendance?.checkedIn) return "already";
    if (!attendance && event.attendanceCount >= event.capacity) return "full";
    if (attendance) tx.update(a, { checkedIn: true });
    else { tx.update(e, { attendanceCount: event.attendanceCount + 1 }); tx.set(a, { eventId: "event", userId: uid, checkedIn: true }); }
    if (user.role !== "MODERATOR") {
      tx.update(u, { points: user.points + 15 });
      tx.set(h, { userId: uid, points: 15, reason: "CHECK_IN", eventTitle: event.title, createdAt: new Date() });
    }
    return "registered";
  }); } catch (error) {
    if (["permission-denied", "aborted", "unavailable"].includes(error.code) && (await getDocFromServer(a)).data()?.checkedIn === true) return "already";
    throw error;
  }
}

test("two clients with the same account commit one attendance, one balance increment and one history item", async () => {
  const a = env.authenticatedContext("attendee").firestore();
  const b = env.authenticatedContext("attendee").firestore();
  const results = await Promise.all([complete(a), complete(b)]);
  assert.deepEqual(results.sort(), ["already", "registered"]);
  assert.equal((await getDoc(doc(a, "users/attendee"))).data().points, 115);
  assert.equal((await getDoc(doc(a, "events/event"))).data().attendanceCount, 1);
  assert.equal((await getDoc(doc(a, "attendances/event_attendee"))).data().checkedIn, true);
  const history = await getDocs(query(collection(a, "pointsHistory"), where("userId", "==", "attendee")));
  assert.equal(history.size, 1);
  assert.equal(history.docs[0].id, "checkin_event_attendee");
  assert.equal(await complete(b), "already");
  assert.equal((await getDoc(doc(a, "users/attendee"))).data().points, 115);
});

test("an existing reward cannot be overwritten and a rejected transaction rolls back balance and attendance", async () => {
  await env.withSecurityRulesDisabled(async (c) => { await setDoc(doc(c.firestore(), "pointsHistory/checkin_event_attendee"), { userId: "attendee", points: 15, reason: "CHECK_IN" }); });
  const db = env.authenticatedContext("attendee").firestore();
  await assertFails(complete(db));
  assert.equal((await getDoc(doc(db, "users/attendee"))).data().points, 100);
  assert.equal((await getDoc(doc(db, "events/event"))).data().attendanceCount, 0);
  await env.withSecurityRulesDisabled(async (c) => { assert.equal((await getDoc(doc(c.firestore(), "attendances/event_attendee"))).exists(), false); });
});

test("organizer cannot check in, closed/full events do not award, and confirmed attendance keeps its spot", async () => {
  const owner = env.authenticatedContext("organizer").firestore();
  assert.equal(await complete(owner, "organizer"), "own");
  await env.withSecurityRulesDisabled(async (c) => {
    await setDoc(doc(c.firestore(), "events/event"), { organizerId: "organizer", status: "VERIFIED", title: "Evento", capacity: 2, attendanceCount: 2 });
  });
  const db = env.authenticatedContext("attendee").firestore();
  assert.equal(await complete(db), "full");
  await env.withSecurityRulesDisabled(async (c) => { await setDoc(doc(c.firestore(), "attendances/event_attendee"), { eventId: "event", userId: "attendee", checkedIn: false }); });
  assert.equal(await complete(db), "registered");
  assert.equal((await getDoc(doc(db, "events/event"))).data().attendanceCount, 2);
  assert.equal((await getDoc(doc(db, "users/attendee"))).data().points, 115);
  await env.withSecurityRulesDisabled(async (c) => {
    const ref = doc(c.firestore(), "events/event");
    const old = (await getDoc(ref)).data();
    await setDoc(ref, { ...old, status: "FINISHED" });
  });
  assert.equal(await complete(db), "closed");
  assert.equal((await getDoc(doc(db, "users/attendee"))).data().points, 115);
});

test("unsigned requests cannot write participation/rewards and one account cannot read another history", async () => {
  await assertFails(complete(env.unauthenticatedContext().firestore()));
  const a = env.authenticatedContext("attendee").firestore();
  await complete(a);
  await assertFails(getDoc(doc(env.authenticatedContext("other").firestore(), "pointsHistory/checkin_event_attendee")));
});

test("moderator participation preserves the zero-reputation policy", async () => {
  await env.withSecurityRulesDisabled(async (c) => { await setDoc(doc(c.firestore(), "users/moderator"), { id: "moderator", role: "MODERATOR", points: 100, badges: [] }); });
  const db = env.authenticatedContext("moderator").firestore();
  assert.equal(await complete(db, "moderator"), "registered");
  assert.equal((await getDoc(doc(db, "users/moderator"))).data().points, 100);
  assert.equal((await getDocs(query(collection(db, "pointsHistory"), where("userId", "==", "moderator")))).size, 0);
});
