# NEURO — capacidades planeadas

## Beta 0.1 incluida
- Cerebro flotante animado.
- Reconocimiento de voz.
- Voz TTS en español de México.
- Abrir Spotify, WhatsApp, YouTube y Maps.
- Abrir marcador telefónico.
- Crear/abrir alarmas.
- Abrir agenda/calendario.
- Abrir fotos.
- Enrutador de comandos.
- Cliente preparado para IA mediante backend seguro.
- Inicio del cerebro flotante después de reiniciar, si Android lo permite.

## Fase 0.2
- Conversación continua con IA.
- Memoria personal.
- Agenda real con Google Calendar.
- Recordatorios.
- Búsqueda de contactos.
- Música por voz.
- Rutinas: mañana, trabajo, conducir, dormir.

## Fase 0.3
- Álbumes y organización de fotografías mediante MediaStore.
- Eliminación de fotos con confirmación del sistema.
- Compartir fotos.
- Dictado y redacción de mensajes.
- Llamadas desde contactos.
- Navegación contextual.

## Fase 0.4
- Rol de asistente predeterminado.
- Integración de VoiceInteractionService.
- Acciones multimodales.
- Panel de permisos y auditoría.
- Centro de actualizaciones.

## Acciones que requieren restricciones de Android
- Colgar llamadas: requiere ser app de teléfono/dialer predeterminada o permisos/roles especiales.
- Borrar fotos: Android obliga a mostrar confirmación del usuario en versiones modernas.
- Controlar interfaces de otras apps: depende de APIs/deep links; Accessibility no debe usarse como atajo general.
- “Siempre escuchando”: requiere diseño cuidadoso por batería, privacidad y políticas del sistema.
