import { before, after, beforeEach, test } from "node:test";
import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { createRequire } from "node:module";
const { initializeTestEnvironment, assertFails } = createRequire(import.meta.url)("@firebase/rules-unit-testing");
const { doc, getDoc, getDocFromServer, setDoc, updateDoc, deleteDoc, writeBatch, runTransaction, collection, query, where, getDocs } = createRequire(import.meta.url)("firebase/firestore");

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
      tx.set(h, { userId: uid, points: 15, reason: "CHECK_IN", eventId: "event", eventTitle: event.title, createdAt: new Date() });
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

test("standalone counters, forged QR history, role escalation and deleting a checked-in attendance are denied", async () => {
  const db = env.authenticatedContext("attendee").firestore();
  await assertFails(updateDoc(doc(db, "events/event"), { attendanceCount: 99 }));
  await assertFails(updateDoc(doc(db, "events/event"), { interestCount: 99 }));
  await assertFails(updateDoc(doc(db, "users/attendee"), { role: "MODERATOR" }));
  await assertFails(setDoc(doc(db, "pointsHistory/checkin_event_attendee"), { userId: "attendee", reason: "CHECK_IN", eventId: "event", points: 15 }));
  await complete(db);
  await assertFails(deleteDoc(doc(db, "attendances/event_attendee")));
  await assertFails(updateDoc(doc(db, "attendances/event_attendee"), { checkedIn: false }));
  await assertFails(deleteDoc(doc(db, "pointsHistory/checkin_event_attendee")));
});

test("legitimate confirmation/cancellation and interest/uninterest require matching counters", async () => {
  const db = env.authenticatedContext("attendee").firestore();
  const event = doc(db, "events/event"), attendance = doc(db, "attendances/event_attendee"), interest = doc(db, "interests/event_attendee");
  let batch = writeBatch(db);
  batch.set(attendance, { eventId: "event", userId: "attendee", checkedIn: false });
  batch.update(event, { attendanceCount: 1 });
  await batch.commit();
  batch = writeBatch(db); batch.delete(attendance); batch.update(event, { attendanceCount: 0 }); await batch.commit();
  batch = writeBatch(db); batch.set(interest, { eventId: "event", userId: "attendee" }); batch.update(event, { interestCount: 1 }); await batch.commit();
  batch = writeBatch(db); batch.delete(interest); batch.update(event, { interestCount: 0 }); await batch.commit();
  assert.equal((await getDoc(event)).data().attendanceCount, 0);
  assert.equal((await getDoc(event)).data().interestCount, 0);
});

test("moderation permits status/reward marker but not arbitrary content or counters", async () => {
  await env.withSecurityRulesDisabled(async c => {
    await setDoc(doc(c.firestore(), "users/mod"), { role: "MODERATOR", points: 0 });
    await updateDoc(doc(c.firestore(), "events/event"), { status: "PENDING", verificationRewarded: false });
  });
  const db = env.authenticatedContext("mod").firestore();
  const event = doc(db, "events/event");
  await updateDoc(event, { status: "VERIFIED", rejectionReason: null });
  await updateDoc(event, { verificationRewarded: true });
  await assertFails(updateDoc(event, { attendanceCount: 100 }));
  await assertFails(updateDoc(event, { organizerId: "mod" }));
  await assertFails(updateDoc(event, { status: "REJECTED", rejectionReason: "" }));
  await updateDoc(event, { status: "REJECTED", rejectionReason: "Fuera de alcance" });
  await updateDoc(event, { status: "FINISHED", rejectionReason: null });
});

test("known residual risk: generic reputation and non-QR history remain writable by authenticated clients", async () => {
  // Regresión explícita del riesgo que necesita operaciones de servidor; NO prueba de seguridad completa.
  const db = env.authenticatedContext("other").firestore();
  await updateDoc(doc(db, "users/attendee"), { points: 999, badges: ["FIRST_EVENT"] });
  await setDoc(doc(db, "pointsHistory/forged-generic"), { userId: "attendee", reason: "COMMENT", points: 999 });
  assert.equal((await getDoc(doc(db, "users/attendee"))).data().points, 999);
});

test("rules themselves reject own/full/non-public enrollment, even when bypassing client prechecks", async () => {
  async function join(uid) {
    const db = env.authenticatedContext(uid).firestore();
    const batch = writeBatch(db);
    const event = (await getDoc(doc(db, "events/event"))).data();
    batch.set(doc(db, `attendances/event_${uid}`), { userId: uid, eventId: "event", checkedIn: false });
    batch.update(doc(db, "events/event"), { attendanceCount: event.attendanceCount + 1 });
    await batch.commit();
  }
  await assertFails(join("organizer"));
  await env.withSecurityRulesDisabled(async c => { await updateDoc(doc(c.firestore(), "events/event"), { attendanceCount: 2 }); });
  await assertFails(join("attendee"));
  for (const status of ["FINISHED", "PENDING", "REJECTED"]) {
    await env.withSecurityRulesDisabled(async c => { await updateDoc(doc(c.firestore(), "events/event"), { status, attendanceCount: 0 }); });
    await assertFails(join("attendee"));
  }
});

test("only a recent identity can start deletion; personal edits cannot change another profile", async () => {
  const old = env.authenticatedContext("attendee", { auth_time: 1 }).firestore();
  await assertFails(updateDoc(doc(old, "users/attendee"), { deletionPending: true }));
  const recent = env.authenticatedContext("attendee", { auth_time: Math.floor(Date.now() / 1000) }).firestore();
  await updateDoc(doc(recent, "users/attendee"), { deletionPending: true });
  await updateDoc(doc(recent, "users/attendee"), { name: "Edited profile" });
  await assertFails(updateDoc(doc(recent, "users/other"), { email: "forged@example.test" }));
});

test("organizer deletion removes event and other users' related records in one batch", async () => {
  await env.withSecurityRulesDisabled(async c => {
    await setDoc(doc(c.firestore(), "attendances/event_attendee"), { eventId: "event", userId: "attendee", checkedIn: true });
    await setDoc(doc(c.firestore(), "interests/event_attendee"), { eventId: "event", userId: "attendee" });
    await setDoc(doc(c.firestore(), "comments/comment"), { eventId: "event", authorId: "attendee" });
  });
  const db = env.authenticatedContext("organizer").firestore();
  const batch = writeBatch(db);
  for (const path of ["events/event", "attendances/event_attendee", "interests/event_attendee", "comments/comment"]) batch.delete(doc(db, path));
  await batch.commit();
  assert.equal((await getDoc(doc(db, "events/event"))).exists(), false);
});
