# NEURO Security Model — 0.1.1 Secure Beta

## What the floating brain can do
The floating brain is a `TYPE_APPLICATION_OVERLAY`. It draws NEURO's own image above other apps and receives touches only on its own small window.

## What it deliberately cannot do
This build does NOT request:
- Accessibility Service access
- SMS read/write
- contacts access
- call-log access
- notification-listener access
- device-admin access
- broad photo/media access
- direct CALL_PHONE permission

It therefore cannot silently inspect banking apps, read passwords, read messages, browse contacts, or directly delete arbitrary photos.

## Private mode
Private mode is ON by default. In this mode, unrecognized conversational prompts are not sent to the Internet.

## Financial mode
Commands that look like payments, transfers, banking, cards, or purchases are refused by the command router.

## Destructive actions
Delete/remove commands are refused in this beta. A later version may use Android's official confirmation flows.

## Persistent availability
Auto-start after reboot is OFF by default. It only activates after the user explicitly enables "Siempre disponible".

## Backend
The APK contains no OpenAI key. Online AI must use an HTTPS backend. Cleartext HTTP traffic is disabled.
