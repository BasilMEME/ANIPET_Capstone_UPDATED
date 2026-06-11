Android build instructions for ANIPET_Capstone_UPDATED

Prerequisites
- Install Android Studio (recommended) which includes the Android SDK.
- Install Java JDK 11+ if not bundled with Android Studio.
- Install Git.

Quick setup (recommended: use Android Studio)
1. Open Android Studio → Open an existing project → select this `android` folder.
2. Let Android Studio download required SDK components and Gradle.
3. Click the Run button and choose an emulator or connected device.

Command-line build (for CI or advanced users)
1. Ensure `JAVA_HOME` and `ANDROID_HOME`/`ANDROID_SDK_ROOT` are set and `adb` is in PATH.
2. From repository root:
   ```powershell
   cd C:\xampp\htdocs\Anipet\android
   .\gradlew.bat assembleDebug --no-daemon --console=plain --stacktrace
   ```
3. The debug APK will be in `app\build\outputs\apk\debug\app-debug.apk`.
4. To install on a connected device/emulator:
   ```powershell
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

Important project notes
- The app's API base URL was updated to `http://10.0.2.2/Anipet/php-backend/` for emulator use in `app/src/main/java/com/example/anipet_capstone/network/ApiClient.kt`.
- For physical devices, replace the URL with `http://<your-pc-ip>/Anipet/php-backend/` and ensure firewall allows incoming connections.
 - To make builds portable, the app reads the base URL from `BuildConfig.API_BASE_URL`. You can override it without editing source by adding this line to `android/gradle.properties` (project) or to your global `~/.gradle/gradle.properties`:
    ```properties
    API_BASE_URL=http://<your-pc-ip>/Anipet/php-backend/
    ```
    If not set, the default used is `http://10.0.2.2/Anipet/php-backend/` (emulator).

Backend (PHP) setup summary
1. Install XAMPP and start Apache + MySQL.
2. Import `php-backend/anipet_db.sql` into phpMyAdmin or via CLI:
   ```powershell
   & 'C:\xampp\mysql\bin\mysql.exe' -u root -e "source C:/xampp/htdocs/Anipet/php-backend/anipet_db.sql"
   ```
3. Verify `php-backend` is accessible at `http://localhost/Anipet/php-backend/` and test endpoints like `get_pets.php`.

Troubleshooting
- Gradle download errors: use Android Studio which manages downloads automatically, or download the Gradle distribution manually and place it under `android/gradle/wrapper/dists/`.
- SDK/NDK missing: open Android Studio and let it install required components.

If you'd like, I can prepare an `app-debug.apk` here and add it to the repo once a successful build runs in this environment. Currently the build didn't produce a usable APK here due to environment limitations (no emulator/device and some Gradle execution behavior). Alternatively, you can open the project in Android Studio on your machine and run it; it should work after installing the SDK components.
