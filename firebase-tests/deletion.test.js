import { before, after, beforeEach, test } from "node:test";
import assert from "node:assert/strict";
import { createRequire } from "node:module";
import { readFile } from "node:fs/promises";
import { randomUUID } from "node:crypto";
const require = createRequire(import.meta.url);
const { initializeApp, deleteApp } = require("firebase/app");
const { getAuth, connectAuthEmulator, createUserWithEmailAndPassword, reauthenticateWithCredential, EmailAuthProvider, deleteUser, signInWithEmailAndPassword } = require("firebase/auth");
const { getFirestore, connectFirestoreEmulator, doc, setDoc, updateDoc, getDoc, getDocs, collection, query, where, writeBatch, increment } = require("firebase/firestore");
const { initializeTestEnvironment } = require("@firebase/rules-unit-testing");

if (process.env.FIRESTORE_EMULATOR_HOST !== "127.0.0.1:8180" || process.env.FIREBASE_AUTH_EMULATOR_HOST !== "127.0.0.1:9199") throw new Error("LOCAL_EMULATORS_REQUIRED");
let env, app, auth, db, uid, email, password;
before(async () => { env = await initializeTestEnvironment({ projectId: "demo-akj-checkin", firestore: { rules: await readFile(new URL("../firestore.rules", import.meta.url), "utf8") } }); });
after(async () => { if (app) await deleteApp(app); await env?.cleanup(); });
beforeEach(async () => {
  if (app) await deleteApp(app);
  await env.clearFirestore();
  app = initializeApp({ projectId: "demo-akj-checkin", apiKey: "demo-only", authDomain: "demo-akj-checkin.firebaseapp.com" }, randomUUID());
  auth = getAuth(app); connectAuthEmulator(auth, "http://127.0.0.1:9199", { disableWarnings: true });
  db = getFirestore(app); connectFirestoreEmulator(db, "127.0.0.1", 8180);
  email = `${randomUUID()}@example.test`; password = `LocalOnly-${randomUUID()}`;
  uid = (await createUserWithEmailAndPassword(auth, email, password)).user.uid;
  await setDoc(doc(db, `users/${uid}`), { id: uid, role: "USER", points: 15, badges: [] });
  await env.withSecurityRulesDisabled(async c => {
    await setDoc(doc(c.firestore(), "events/other"), { organizerId: "other", status: "VERIFIED", attendanceCount: 1, interestCount: 0 });
    await setDoc(doc(c.firestore(), `attendances/other_${uid}`), { userId: uid, eventId: "other", checkedIn: true });
    await setDoc(doc(c.firestore(), `pointsHistory/checkin_other_${uid}`), { userId: uid, points: 15, reason: "CHECK_IN" });
    await setDoc(doc(c.firestore(), `notifications/${uid}`), { userId: uid, isRead: false });
  });
});

// Reproduce la secuencia de DeleteAccountUseCase y el final/compensación de FirebaseUserRepository.
// SDK JS real contra Auth/Firestore locales; no sustituye la prueba instrumentada Android.
async function remove(candidate, failAt) {
  await reauthenticateWithCredential(auth.currentUser, EmailAuthProvider.credential(email, candidate));
  await updateDoc(doc(db, `users/${uid}`), { deletionPending: true });
  const participation = await getDocs(query(collection(db, "attendances"), where("userId", "==", uid)));
  let batch = writeBatch(db);
  for (const p of participation.docs) {
    batch.delete(p.ref); batch.update(doc(db, `events/${p.data().eventId}`), { attendanceCount: increment(-1) });
  }
  await batch.commit();
  if (failAt === "cleanup") throw new Error("INJECTED_CLEANUP_FAILURE");
  batch = writeBatch(db);
  for (const name of ["notifications", "pointsHistory"]) {
    for (const item of (await getDocs(query(collection(db, name), where("userId", "==", uid)))).docs) batch.delete(item.ref);
  }
  const profile = (await getDoc(doc(db, `users/${uid}`))).data();
  batch.delete(doc(db, `users/${uid}`)); await batch.commit();
  try {
    if (failAt === "auth") throw new Error("INJECTED_FINAL_AUTH_FAILURE");
    await deleteUser(auth.currentUser);
  } catch (error) {
    await setDoc(doc(db, `users/${uid}`), profile);
    throw error;
  }
}

async function assertIntact() {
  assert.equal((await getDoc(doc(db, `users/${uid}`))).data().deletionPending, undefined);
  assert.equal((await getDocs(query(collection(db, "attendances"), where("userId", "==", uid)))).size, 1);
  assert.equal((await getDocs(query(collection(db, "pointsHistory"), where("userId", "==", uid)))).size, 1);
  assert.equal((await getDoc(doc(db, "events/other"))).data().attendanceCount, 1);
}

test("cancel and wrong password leave profile, history, participation and counters intact", async () => {
  // Cancelar el diálogo no invoca el caso de uso.
  await assertIntact();
  await assert.rejects(remove("wrong-password"));
  await assertIntact();
});

test("reauthenticated successful deletion removes data and the disposable Auth account", async () => {
  await remove(password);
  await assert.rejects(signInWithEmailAndPassword(auth, email, password));
  await env.withSecurityRulesDisabled(async c => {
    assert.equal((await getDoc(doc(c.firestore(), `users/${uid}`))).exists(), false);
    assert.equal((await getDoc(doc(c.firestore(), "events/other"))).data().attendanceCount, 0);
    for (const name of ["pointsHistory", "attendances", "notifications"]) assert.equal((await getDocs(query(collection(c.firestore(), name), where("userId", "==", uid)))).size, 0);
  });
});

test("failure after cleanup preserves profile and same-account retry does not decrement twice", async () => {
  await assert.rejects(remove(password, "cleanup"));
  assert.equal((await getDoc(doc(db, `users/${uid}`))).data().deletionPending, true);
  assert.equal((await getDoc(doc(db, "events/other"))).data().attendanceCount, 0);
  await remove(password);
  await env.withSecurityRulesDisabled(async c => { assert.equal((await getDoc(doc(c.firestore(), "events/other"))).data().attendanceCount, 0); });
});

test("final Auth failure restores profile with pending marker and permits retry", async () => {
  await assert.rejects(remove(password, "auth"));
  assert.equal((await getDoc(doc(db, `users/${uid}`))).data().deletionPending, true);
  assert.equal(auth.currentUser.uid, uid);
  await remove(password);
});
