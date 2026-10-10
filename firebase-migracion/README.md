# Migración de datos: modelo de equipo

Este script habilita el modelo de equipo en los datos existentes de Firestore:

- A cada documento de `usuarios` sin `tallerId` le asigna su propio `uid` (el administrador es su
  propio taller).
- A cada documento de `usuarios` sin `rol` le asigna `admin` (los usuarios actuales son los dueños
  de su información).
- A cada documento de `servicios` sin `asignadoA` le inicializa el campo en `""` (sin asignar).

No modifica ni comparte clientes: cada taller sigue siendo dueño de sus propios datos.

## Requisitos

- Node.js 18 o superior.
- Una cuenta de servicio de Firebase con permisos sobre Firestore.

## Preparación

1. Descarga el JSON de la cuenta de servicio desde Firebase Console
   (Configuración del proyecto > Cuentas de servicio > Generar nueva clave privada).
2. Define la variable de entorno apuntando a ese archivo.

   PowerShell:

   ```powershell
   $env:GOOGLE_APPLICATION_CREDENTIALS = "C:\ruta\cuenta-servicio.json"
   ```

   Bash:

   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS="/ruta/cuenta-servicio.json"
   ```

3. Instala la dependencia:

   ```bash
   npm install
   ```

## Ejecución

Simulación (por defecto, **no escribe** nada):

```bash
npm run migrar
```

Aplicar los cambios de verdad:

```bash
npm run migrar:aplicar
```

Ejecuta primero la simulación, revisa la lista de documentos que aparecerá en consola y, si todo es
correcto, vuelve a ejecutar con `--apply`.
