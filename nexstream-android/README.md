# NEXSTREAM — Android / Android TV App

Aplicación WebView que empaqueta NEXSTREAM para Android y Android TV.
Resuelve el problema de CORS que impide reproducir streams .ts en el navegador.

---

## Requisitos

- **Android Studio** (versión Hedgehog 2023.1.1 o superior)
  Descargar: https://developer.android.com/studio
- Java 17+ (incluido con Android Studio)

---

## Cómo compilar

### Opción A — Android Studio (recomendado)

1. Abrir Android Studio
2. **File → Open** → seleccionar esta carpeta `nexstream-android`
3. Esperar que sincronice Gradle (descarga dependencias ~2 min)
4. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. El APK queda en: `app/build/outputs/apk/debug/app-debug.apk`

### Opción B — Línea de comandos (si ya tenés Android SDK)

```bash
cd nexstream-android
chmod +x gradlew
./gradlew assembleDebug
# APK en: app/build/outputs/apk/debug/app-debug.apk
```

---

## Instalar en Android TV

### Método 1 — ADB por red (sin cable)

1. En el Android TV: **Ajustes → Preferencias del dispositivo → Acerca de → Compilación**
   Presionar 7 veces para activar modo desarrollador
2. **Ajustes → Opciones de desarrollador → Depuración por ADB** → activar
3. Anotar la IP del Android TV (está en Ajustes → Red)
4. En tu PC:
```bash
adb connect 192.168.x.x:5555
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Método 2 — Pendrive / Archivo manager

1. Copiar el APK a un pendrive USB
2. En el Android TV instalar un file manager (ej: FX File Explorer desde Play Store)
3. Abrir el APK desde el file manager
4. Permitir "instalar fuentes desconocidas" cuando lo pida

### Método 3 — Desactivar verificación de apps (algunos TV)

Algunos Android TV piden confirmar en el teléfono vinculado a la cuenta Google.
Simplemente aceptar en el teléfono cuando aparezca la notificación.

---

## Actualizar NEXSTREAM

Cuando haya una nueva versión del HTML:

1. Reemplazar `app/src/main/assets/nexstream.html` con el nuevo archivo
2. Recompilar: **Build → Build APK(s)**
3. Reinstalar el APK

---

## Estructura del proyecto

```
nexstream-android/
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   └── nexstream.html        ← NEXSTREAM completo
│   │   ├── java/com/nexstream/app/
│   │   │   └── MainActivity.java     ← WebView con CORS deshabilitado
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml
│   │   │   ├── values/styles.xml
│   │   │   ├── xml/network_security_config.xml
│   │   │   └── mipmap-*/             ← íconos
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## Por qué funciona en Android y no en el navegador

El navegador aplica **CORS** (Cross-Origin Resource Sharing): bloquea peticiones
a servidores que no tienen el header `Access-Control-Allow-Origin`.
Los streams IPTV generalmente no tienen ese header.

En esta app, el WebView tiene `setAllowUniversalAccessFromFileURLs(true)` que
desactiva esa restricción, permitiendo cargar cualquier URL igual que VLC.

---

## Notas

- La app funciona en **Android 5.0+** (API 21) y **Android TV**
- Compatible con control remoto: flechas, OK, Back, botones numéricos
- Pantalla siempre encendida mientras reproduce
- Modo inmersivo: sin barras de sistema
