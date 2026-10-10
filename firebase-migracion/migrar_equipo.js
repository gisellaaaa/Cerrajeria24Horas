const admin = require('firebase-admin');

const aplicar = process.argv.includes('--apply');

const COL_USUARIOS = 'usuarios';
const COL_SERVICIOS = 'servicios';

function iniciar() {
  if (!process.env.GOOGLE_APPLICATION_CREDENTIALS) {
    console.error(
      'Falta la variable GOOGLE_APPLICATION_CREDENTIALS apuntando al JSON de la cuenta de servicio.'
    );
    process.exit(1);
  }
  admin.initializeApp({ credential: admin.credential.applicationDefault() });
}

async function migrar() {
  const db = admin.firestore();

  console.log(
    aplicar
      ? 'Modo APLICAR: los cambios se escribirán en Firestore.'
      : 'Modo SIMULACRO: no se escribe nada. Usa --apply para aplicar los cambios.'
  );

  let usuariosMigrados = 0;
  const usuarios = await db.collection(COL_USUARIOS).get();
  for (const doc of usuarios.docs) {
    const datos = doc.data();
    const cambios = {};
    if (!datos.tallerId) cambios.tallerId = doc.id;
    if (!datos.rol) cambios.rol = 'admin';
    if (Object.keys(cambios).length > 0) {
      usuariosMigrados++;
      console.log(`  usuario ${doc.id} -> ${JSON.stringify(cambios)}`);
      if (aplicar) await doc.ref.update(cambios);
    }
  }

  let serviciosMigrados = 0;
  const servicios = await db.collection(COL_SERVICIOS).get();
  for (const doc of servicios.docs) {
    const datos = doc.data();
    if (datos.asignadoA === undefined) {
      serviciosMigrados++;
      console.log(`  servicio ${doc.id} -> { asignadoA: "" }`);
      if (aplicar) await doc.ref.update({ asignadoA: '' });
    }
  }

  console.log('');
  console.log(`Usuarios por migrar: ${usuariosMigrados}.`);
  console.log(`Servicios por inicializar: ${serviciosMigrados}.`);
  console.log(aplicar ? 'Migración aplicada.' : 'Simulación finalizada.');
}

iniciar();
migrar()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error('La migración falló:', error);
    process.exit(1);
  });
