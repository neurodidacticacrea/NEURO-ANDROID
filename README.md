# NEURO Android 0.2.1 — Float Fix

Esta versión corrige el arranque del cerebro flotante en Android recientes.

## Cambio principal
El servicio flotante ya NO se declara como servicio de micrófono. El micrófono sólo se usa cuando el usuario inicia reconocimiento de voz.

## Cómo probar
1. Instala la app.
2. Abre NEURO.
3. Pulsa `🧠 Activar flotante`.
4. Si Android abre ajustes, activa `Mostrar sobre otras apps`.
5. Regresa a NEURO.
6. Pulsa otra vez `🧠 Activar flotante`.
7. Debe aparecer:
   - el cerebro en la zona superior izquierda
   - una notificación persistente: `NEURO flotante activo`

## Si no aparece
- Verifica Ajustes > Apps > Acceso especial > Mostrar sobre otras apps > NEURO.
- Verifica que la notificación `NEURO flotante activo` exista.
- En algunos fabricantes puede ser necesario permitir ejecución en segundo plano/batería sin restricciones.

## Seguridad
El overlay sigue siendo sólo visual/táctil. No usa Accessibility ni lee contenido de otras apps.
