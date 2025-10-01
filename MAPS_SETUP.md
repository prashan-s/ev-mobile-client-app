# Google Maps API Setup Guide

## Issue Fixed
The app was crashing with:
```
java.lang.IllegalStateException: API key not found. Check that <meta-data android:name="com.google.android.geo.API_KEY" android:value="your API key"/> is in the <application> element of AndroidManifest.xml
```

## How to Get Your Google Maps API Key

### Step 1: Go to Google Cloud Console
1. Visit: https://console.cloud.google.com/
2. Sign in with your Google account

### Step 2: Create or Select a Project
1. Click the project dropdown at the top
2. Click "New Project" or select an existing one
3. Give it a name (e.g., "ChargeHere App")

### Step 3: Enable Google Maps SDK for Android
1. Go to: https://console.cloud.google.com/apis/library
2. Search for "Maps SDK for Android"
3. Click on it and press "Enable"

### Step 4: Create API Key
1. Go to: https://console.cloud.google.com/apis/credentials
2. Click "Create Credentials" → "API Key"
3. Copy the API key that appears

### Step 5: (Recommended) Restrict Your API Key
1. Click "Edit API key" (pencil icon)
2. Under "Application restrictions":
   - Select "Android apps"
   - Click "Add an item"
   - Package name: `lk.chargehere.app`
   - SHA-1 certificate fingerprint: Get it by running:
     ```bash
     cd /Users/prashansamarathunge/AndroidStudioProjects/ChargeHere
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
3. Under "API restrictions":
   - Select "Restrict key"
   - Check "Maps SDK for Android"
4. Click "Save"

### Step 6: Add API Key to Your Project
1. Open `/Users/prashansamarathunge/AndroidStudioProjects/ChargeHere/local.properties`
2. Replace `YOUR_API_KEY_HERE` with your actual API key:
   ```properties
   MAPS_API_KEY=AIzaSyC1234567890abcdefghijklmnopqrstuv
   ```

### Step 7: Rebuild and Run
1. Sync Gradle (File → Sync Project with Gradle Files)
2. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```
3. Run the app

## What Was Changed

### 1. `local.properties`
Added a secure place to store the API key (this file is not committed to git):
```properties
MAPS_API_KEY=YOUR_API_KEY_HERE
```

### 2. `app/build.gradle.kts`
Added code to read the API key from `local.properties` and inject it into the manifest:
```kotlin
val properties = org.jetbrains.kotlin.konan.properties.Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { properties.load(it) }
}
val mapsApiKey = properties.getProperty("MAPS_API_KEY") ?: "YOUR_API_KEY_HERE"
manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
```

### 3. `AndroidManifest.xml`
Added the meta-data tag that Google Maps requires:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

## Important Notes

- ✅ `local.properties` is already in `.gitignore` - your API key won't be committed
- ⚠️  Never commit API keys to version control
- 🔒 Always restrict your API keys in production
- 💰 Set up billing alerts in Google Cloud Console to avoid unexpected charges

## Troubleshooting

### Still getting "API key not found" error?
1. Make sure you've added the API key to `local.properties`
2. Sync Gradle files
3. Clean and rebuild the project
4. Restart Android Studio

### Maps not loading?
1. Check that the API key is valid
2. Verify "Maps SDK for Android" is enabled in Google Cloud Console
3. Check the API key restrictions (package name and SHA-1)
4. Check logcat for specific error messages

### Need help with SHA-1 fingerprint?
Run this command in your project directory:
```bash
./gradlew signingReport
```

Look for the SHA-1 under "Task :app:signingReport → Variant: debug"
