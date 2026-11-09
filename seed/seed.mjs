// Carga los datos de ejemplo de AKJ Events en Cloud Firestore con el SDK de administración de Firebase,
// que no pasa por las reglas de seguridad.
//
// Emulador local:  FIRESTORE_EMULATOR_HOST=127.0.0.1:8080 node seed.mjs
// Proyecto real:   GOOGLE_APPLICATION_CREDENTIALS=<ruta a la clave de la cuenta de servicio> node seed.mjs
//
// Si la colección `events` ya tiene datos no hace nada, salvo que se pase --force (que sobrescribe los
// documentos de ejemplo, pero no borra los demás).
import { initializeApp, applicationDefault } from 'firebase-admin/app';
import { getFirestore, Timestamp } from 'firebase-admin/firestore';
import { attendances, comments, events, interests, users } from './sample-data.mjs';

const PROJECT_ID = 'akj---events';
const force = process.argv.includes('--force');
const emulator = process.env.FIRESTORE_EMULATOR_HOST;

if (!emulator && !process.env.GOOGLE_APPLICATION_CREDENTIALS) {
  console.error('Indica FIRESTORE_EMULATOR_HOST (emulador) o GOOGLE_APPLICATION_CREDENTIALS (proyecto real).');
  process.exit(1);
}

initializeApp(emulator ? { projectId: PROJECT_ID } : { credential: applicationDefault(), projectId: PROJECT_ID });
const db = getFirestore();

/** Igual que `String.hashCode()` de Java/Kotlin: entero de 32 bits con signo. */
function javaHashCode(text) {
  let hash = 0;
  for (let i = 0; i < text.length; i++) hash = (Math.imul(31, hash) + text.charCodeAt(i)) | 0;
  return hash;
}

/** El mismo código de QR que calcula `CommunityEvent.checkInCode` en la app. */
function checkInCode(eventId) {
  return 'AKJ-' + (javaHashCode(eventId) >>> 0).toString(36).toUpperCase().padStart(6, '0').slice(0, 6);
}

const timestamp = (date) => Timestamp.fromDate(date);

async function main() {
  const existing = await db.collection('events').limit(1).get();
  if (!existing.empty && !force) {
    console.log('La colección events ya tiene datos; no se cargó nada. Usa --force para sobrescribir los ejemplos.');
    return;
  }

  const batch = db.batch();
  for (const user of users) {
    batch.set(db.collection('users').doc(user.id), user);
  }
  for (const { id, image, startsAt, endsAt, createdAt, ...data } of events) {
    batch.set(db.collection('events').doc(id), {
      ...data,
      startsAt: timestamp(startsAt),
      endsAt: timestamp(endsAt),
      createdAt: timestamp(createdAt),
      checkInCode: checkInCode(id),
      // Los ejemplos ya aprobados parten con su recompensa de verificación entregada
      verificationRewarded: data.status === 'VERIFIED' || data.status === 'FINISHED',
    });
  }
  for (const { id, createdAt, ...data } of comments) {
    batch.set(db.collection('comments').doc(id), { ...data, createdAt: timestamp(createdAt) });
  }
  for (const attendance of attendances) {
    batch.set(db.collection('attendances').doc(`${attendance.eventId}_${attendance.userId}`), attendance);
  }
  for (const interest of interests) {
    batch.set(db.collection('interests').doc(`${interest.eventId}_${interest.userId}`), interest);
  }
  await batch.commit();

  console.log(
    `Listo (${emulator ? 'emulador ' + emulator : 'proyecto ' + PROJECT_ID}): ${users.length} perfiles, ` +
    `${events.length} eventos, ${comments.length} comentarios, ${attendances.length} asistencias y ${interests.length} intereses.`
  );
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
