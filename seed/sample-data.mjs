// Datos de ejemplo de AKJ Events para cargar en Firestore. Son los mismos de `data/demo/` en la app
// (SampleEvents, SampleUsers y SampleParticipation); las fechas se calculan a partir del día de la carga.

/** Fecha a [days] días de hoy, a la hora indicada (hora del computador que ejecuta el script). */
function at(days, hour, minute = 0) {
  const date = new Date();
  date.setDate(date.getDate() + days);
  date.setHours(hour, minute, 0, 0);
  return date;
}

function ago({ days = 0, hours = 0, minutes = 0 }) {
  return new Date(Date.now() - ((days * 24 + hours) * 60 + minutes) * 60_000);
}

const ARMENIA = { latitude: 4.5339, longitude: -75.6811 };

function organizer(id, name, points) {
  return {
    id,
    name,
    email: `${id}@akjevents.co`,
    city: 'Armenia, Quindío',
    address: 'Armenia, Quindío',
    ...ARMENIA,
    role: 'USER',
    points,
    interests: [],
    badges: [],
    profilePictureUrl: null,
  };
}

// Perfiles de los organizadores de ejemplo. No tienen cuenta en Authentication: nadie inicia sesión con ellos.
export const users = [
  {
    id: 'user-1',
    name: 'Camilo Rodríguez',
    email: 'camilo.rodriguez@akjevents.co',
    city: 'Armenia, Quindío',
    address: 'Cra. 14 # 21-15, Centro',
    latitude: 4.5389,
    longitude: -75.6725,
    role: 'USER',
    points: 480,
    interests: ['SPORTS', 'CULTURE', 'VOLUNTEERING'],
    badges: ['FIRST_EVENT', 'FIRST_COMMENT'],
    profilePictureUrl: null,
  },
  organizer('organizer-1', 'Carlos Mario Gómez', 1250),
  organizer('organizer-2', 'Corporación Cultural del Quindío', 420),
  organizer('organizer-3', 'Colectivo Armenia Verde', 380),
  organizer('organizer-4', 'Laura Restrepo', 180),
  organizer('organizer-5', 'Junta de Acción Comunal Centro', 410),
  organizer('organizer-6', 'Andrés Valencia', 120),
  organizer('organizer-7', 'Mariana Ospina', 20),
];

function event(id, data) {
  const { start, end, createdAt, ...rest } = data;
  return {
    id,
    capacity: null,
    attendanceCount: 0,
    interestCount: 0,
    rejectionReason: null,
    ...rest,
    imageUrls: [`https://picsum.photos/seed/${data.image}/900/500`],
    startsAt: start,
    endsAt: end,
    // Igual que en la app: si no se indica, el evento se creó una semana antes de empezar
    createdAt: createdAt ?? new Date(start.getTime() - 7 * 24 * 60 * 60_000),
  };
}

export const events = [
  event('ciclopaseo', {
    organizerId: 'organizer-1', title: 'Ciclopaseo nocturno por Armenia', category: 'SPORTS',
    description: 'Recorre la ciudad en comunidad. Trae bicicleta, casco y luces. Habrá puntos de hidratación durante el recorrido.',
    address: 'Plaza de Bolívar, Armenia, Quindío', latitude: 4.5339, longitude: -75.6811, image: 'akj-ciclopaseo',
    start: at(1, 19), end: at(1, 21, 30), capacity: 100, attendanceCount: 84, interestCount: 39, status: 'VERIFIED',
  }),
  event('feria-cultural', {
    organizerId: 'organizer-2', title: 'Feria cultural y de emprendimiento', category: 'CULTURE',
    description: 'Conoce artistas, sabores y emprendimientos locales en una jornada abierta para toda la familia.',
    address: 'Parque Sucre, Armenia, Quindío', latitude: 4.5382, longitude: -75.6731, image: 'akj-feria',
    start: at(3, 9), end: at(3, 16), attendanceCount: 142, interestCount: 71, status: 'VERIFIED',
  }),
  event('jornada-verde', {
    organizerId: 'organizer-3', title: 'Jornada de limpieza Parque de la Vida', category: 'VOLUNTEERING',
    description: 'Ayúdanos a cuidar el parque con vecinos y voluntarios. Lleva ropa cómoda y una botella de agua.',
    address: 'Parque de la Vida, Armenia, Quindío', latitude: 4.5511, longitude: -75.6598, image: 'akj-voluntariado',
    start: at(5, 8), end: at(5, 11), capacity: 50, attendanceCount: 38, interestCount: 25, status: 'VERIFIED',
  }),
  event('taller-kotlin', {
    organizerId: 'organizer-4', title: 'Taller introductorio de Kotlin', category: 'ACADEMIC',
    description: 'Un espacio gratuito para aprender fundamentos de programación móvil en compañía de la comunidad.',
    address: 'Centro de Innovación, Armenia, Quindío', latitude: 4.5320, longitude: -75.6750, image: 'akj-kotlin',
    start: at(6, 17), end: at(6, 19), capacity: 35, attendanceCount: 31, interestCount: 31, status: 'VERIFIED',
  }),
  event('encuentro-vecinal', {
    organizerId: 'organizer-5', title: 'Encuentro de vecinos y café', category: 'SOCIAL',
    description: 'Un encuentro para conocernos y conversar sobre ideas para el barrio.',
    address: 'Casa comunal, Armenia, Quindío', latitude: 4.5410, longitude: -75.6700, image: 'akj-cafe',
    start: at(7, 16), end: at(7, 18), capacity: 25, attendanceCount: 25, interestCount: 8, status: 'VERIFIED',
  }),
  event('cine-foro', {
    organizerId: 'user-1', title: 'Cine foro al parque', category: 'CULTURE',
    description: 'Proyección al aire libre de cine colombiano y conversación con la comunidad. Trae tu cobija o silla plegable.',
    address: 'Biblioteca Pública Municipal, Armenia, Quindío', latitude: 4.5356, longitude: -75.6762, image: 'akj-cine',
    start: at(3, 18, 30), end: at(3, 21), capacity: 60, attendanceCount: 18, interestCount: 22, status: 'VERIFIED',
    createdAt: ago({ days: 6 }),
  }),
  event('caminata-cocora', {
    organizerId: 'organizer-3', title: 'Caminata ecológica Valle de Cocora', category: 'SPORTS',
    description: 'Recorrido guiado de 12 km entre palmas de cera. Nivel intermedio; lleva botas, impermeable y refrigerio.',
    address: 'Valle de Cocora, Salento, Quindío', latitude: 4.6380, longitude: -75.4870, image: 'akj-cocora',
    start: at(10, 6, 30), end: at(10, 13), capacity: 40, attendanceCount: 27, interestCount: 44, status: 'VERIFIED',
  }),
  event('hackaton-utp', {
    organizerId: 'organizer-4', title: 'Hackatón universitario de soluciones cívicas', category: 'ACADEMIC',
    description: '24 horas para crear soluciones tecnológicas para la región. Equipos de 3 a 5 personas; hay mentorías y comida.',
    address: 'Universidad Tecnológica de Pereira, Pereira, Risaralda', latitude: 4.7946, longitude: -75.6900, image: 'akj-hackaton',
    start: at(13, 8), end: at(14, 8), capacity: 120, attendanceCount: 64, interestCount: 58, status: 'VERIFIED',
  }),
  event('pendiente-privado', {
    organizerId: 'organizer-6', title: 'Evento en revisión', category: 'SOCIAL',
    description: 'Este ejemplo sirve para comprobar que un evento pendiente no aparezca en el feed público.',
    address: 'Armenia, Quindío', ...ARMENIA, image: 'akj-pendiente',
    start: at(8, 10), end: at(8, 12), status: 'PENDING', createdAt: ago({ days: 1 }),
  }),
  event('huertos-urbanos', {
    organizerId: 'user-1', title: 'Taller de Huertos Urbanos y Compostaje', category: 'VOLUNTEERING',
    description: 'Aprenderemos a cultivar alimentos orgánicos en balcones y terrazas reducidas, a preparar compost casero ' +
      'y técnicas de reciclaje de sustrato para la comunidad.',
    address: 'Parque de la Vida, Armenia, Quindío', latitude: 4.5511, longitude: -75.6598, image: 'akj-huertos',
    start: at(9, 9), end: at(9, 12, 30), capacity: 35, status: 'PENDING', createdAt: ago({ hours: 2 }),
  }),
  event('microfutbol', {
    organizerId: 'organizer-5', title: 'Torneo Relámpago de Microfútbol', category: 'SPORTS',
    description: 'Torneo de un día para equipos barriales de 5 jugadores. Inscripción por equipos en la casa comunal.',
    address: 'Estadio Centenario, Armenia, Quindío', latitude: 4.5208, longitude: -75.6858, image: 'akj-microfutbol',
    start: at(11, 14), end: at(11, 19), capacity: 60, status: 'PENDING', createdAt: ago({ minutes: 40 }),
  }),
  event('muestra-artesanal', {
    organizerId: 'organizer-2', title: 'Muestra Artesanal y Café Quindiano', category: 'SOCIAL',
    description: 'Artesanos y caficultores de la región muestran su trabajo. Habrá catas guiadas de café de origen.',
    address: 'Plaza principal de Calarcá, Calarcá, Quindío', latitude: 4.5296, longitude: -75.6431, image: 'akj-artesanal',
    start: at(15, 10), end: at(15, 18), capacity: 120, status: 'PENDING', createdAt: ago({ hours: 5 }),
  }),
  event('rechazado-privado', {
    organizerId: 'organizer-7', title: 'Evento no publicado', category: 'SPORTS',
    description: 'Este evento rechazado tampoco debe ser visible en el feed público.',
    address: 'Armenia, Quindío', ...ARMENIA, image: 'akj-rechazado',
    start: at(9, 10), end: at(9, 12), status: 'REJECTED',
    rejectionReason: 'Falta información sobre el lugar del evento',
  }),
  event('voleibol-callejero', {
    organizerId: 'user-1', title: 'Torneo Nocturno de Voleibol Callejero', category: 'SPORTS',
    description: 'Torneo abierto de voleibol 6 contra 6 en la cancha del barrio, con premiación para los tres primeros equipos.',
    address: 'Cancha Barrio Granada, Armenia, Quindío', latitude: 4.5150, longitude: -75.6920, image: 'akj-voleibol',
    start: at(4, 19), end: at(4, 22), status: 'REJECTED',
    rejectionReason: 'Lugar no disponible o sin permiso municipal: la cancha tiene reserva prioritaria de las escuelas ' +
      'deportivas los viernes por la noche. Elige otro horario o adjunta la autorización de la junta de acción comunal.',
    createdAt: ago({ days: 3 }),
  }),
  event('reforestacion-rio', {
    organizerId: 'user-1', title: 'Jornada de Reforestación Río Quindío', category: 'VOLUNTEERING',
    description: 'Siembra de árboles nativos en la cuenca alta del río Quindío junto con la comunidad y guardabosques.',
    address: 'Cuenca Alta del Río Quindío, Salento, Quindío', latitude: 4.6290, longitude: -75.5300, image: 'akj-reforestacion',
    start: at(-12, 7), end: at(-12, 13), capacity: 50, attendanceCount: 45, interestCount: 33, status: 'FINISHED',
    createdAt: ago({ days: 25 }),
  }),
];

export const comments = [
  { id: 'comentario-1', eventId: 'ciclopaseo', authorId: 'organizer-4', text: '¿El recorrido es apto para niños de 10 años en bicicleta?', createdAt: ago({ days: 2 }) },
  { id: 'comentario-2', eventId: 'ciclopaseo', authorId: 'organizer-1', text: '¡Sí! Vamos a un ritmo suave y habrá adultos acompañando al final del grupo.', createdAt: ago({ days: 2, hours: -1 }) },
  { id: 'comentario-3', eventId: 'ciclopaseo', authorId: 'user-1', text: 'Allá estaremos con los vecinos del barrio. ¡Gracias por organizarlo!', createdAt: ago({ days: 1 }) },
  { id: 'comentario-4', eventId: 'cine-foro', authorId: 'organizer-5', text: '¿Hay que llevar silla o habrá dónde sentarse?', createdAt: ago({ hours: 3 }) },
  { id: 'comentario-5', eventId: 'reforestacion-rio', authorId: 'organizer-3', text: 'Gran jornada, sembramos más de 300 árboles. ¡Gracias a todos!', createdAt: ago({ days: 11 }) },
];

export const attendances = [
  { eventId: 'feria-cultural', userId: 'user-1', checkedIn: false },
  { eventId: 'cine-foro', userId: 'organizer-4', checkedIn: false },
  { eventId: 'cine-foro', userId: 'organizer-7', checkedIn: false },
  { eventId: 'reforestacion-rio', userId: 'organizer-3', checkedIn: true },
  { eventId: 'reforestacion-rio', userId: 'organizer-6', checkedIn: true },
];

export const interests = [
  { eventId: 'jornada-verde', userId: 'user-1' },
  { eventId: 'cine-foro', userId: 'organizer-4' },
];
