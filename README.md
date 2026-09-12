# NEURO Android

**NEURO** es la inteligencia personal de Neurodidáctica.

Esta versión inicia desde cero con una arquitectura Android nativa y un cerebro flotante animado.

## Qué incluye esta primera base

- Cerebro flotante animado y movible.
- Panel principal NEURO.
- Entrada por voz y texto.
- Respuesta hablada.
- Apertura de Spotify, WhatsApp, YouTube, Maps y otras apps.
- Alarmas.
- Agenda/calendario.
- Marcador telefónico.
- Galería de fotos.
- Router de comandos.
- Cliente preparado para conectar IA a través de un backend seguro.

## Importante sobre la IA

No coloques una clave de OpenAI dentro del APK.

Configura `NEURO_API_URL` en `gradle.properties` para apuntar a un backend seguro, por ejemplo:

- Python + FastAPI en Render
- Vercel Functions
- Supabase Edge Functions

El backend será quien guarde la clave y llame al modelo.

## Compilar con GitHub Actions

Este proyecto incluye `.github/workflows/build-apk.yml`.

1. Crea un repositorio de GitHub.
2. Sube **el contenido** de esta carpeta.
3. Ve a **Actions**.
4. Ejecuta `Build NEURO Android Beta`.
5. Descarga el artifact `NEURO-APK`.
6. Dentro estará `NEURO-BETA.apk`.

## Instalar

En Android:

1. Copia `NEURO-BETA.apk` a tu carpeta `NEURO`.
2. Tócalo.
3. Permite instalar apps desconocidas para el gestor de archivos si Android te lo solicita.
4. Abre NEURO.
5. Concede micrófono.
6. Pulsa `🧠 Flotante`.
7. Activa `Mostrar sobre otras apps`.
8. Regresa a NEURO y vuelve a pulsar `🧠 Flotante`.

## Filosofía de seguridad

NEURO puede automatizar mucho, pero acciones sensibles deben pedir confirmación:
- borrar archivos o fotos
- enviar mensajes
- realizar llamadas no solicitadas
- compras/pagos
- compartir información privada
- cambiar configuraciones críticas
