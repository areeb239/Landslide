# NER Landslide Early Warning System — Android App

## Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **DI:** Hilt
- **Backend (AI):** FastAPI (your teammate's service)
- **Database (Remote):** Firebase Firestore (real-time)
- **Database (Offline):** Room + WorkManager
- **Auth:** Firebase Auth (Email + Google)
- **Storage:** Firebase Storage (photos)
- **Notifications:** Firebase Cloud Messaging (FCM)
- **Maps:** Google Maps SDK for Android + Maps Compose
- **Weather:** Open-Meteo (free, no key needed)

---

## Setup Steps

### 1. Firebase Setup (Free)
1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Create a project: **NER-Landslide-Watch**
3. Add an Android app with package name: `com.ner.landslide`
4. Download `google-services.json` and place it in `app/` directory
5. Enable the following in Firebase Console:
   - **Authentication** → Sign-in methods → Email/Password ✅ → Google ✅
   - **Firestore Database** → Create in test mode (for dev)
   - **Storage** → Get started
   - **Cloud Messaging** → Enabled by default

### 2. Google Maps API Key
1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. Enable **Maps SDK for Android**
3. Create an API key (restrict to your app's SHA-1)
4. In `local.properties`, add:
   ```
   MAPS_API_KEY=your_api_key_here
   ```

### 3. FastAPI Backend (Your Teammate)
Update `PREDICTION_API_BASE_URL` in `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "PREDICTION_API_BASE_URL", "\"https://your-fastapi-url.com/\"")
```
**Expected endpoint:** `POST /api/v1/predict`
```json
// Request
{
  "rainfall_mm": 120.0,
  "slope_deg": 35.0,
  "soil_moisture_pct": 78.0,
  "antecedent_rain_3d": 250.0
}

// Response
{
  "risk_level": "HIGH",
  "probability": 0.84,
  "confidence": 0.91,
  "factors": {"rainfall": 0.45, "slope_angle": 0.30, "soil_moisture": 0.25},
  "recommendation": "Immediate evacuation recommended."
}
```
> **Note:** If the FastAPI server is not available, the app automatically falls back to a **local mock prediction model** so your demo never fails.

### 4. Admin Setup
In Firebase Firestore, manually set the role for your admin user:
```
Collection: users
Document: {your_firebase_uid}
Field: role → "ADMIN"
```

### 5. Firestore Collections Required
Create these collections with test data for the demo:
- `alerts` — with severity (LOW/MODERATE/HIGH/CRITICAL), title, description, affectedDistrict
- `risk_zones` — with polygonPoints (list of lat/lng maps), severity
- `road_segments` — with points (list of lat/lng maps), status (OPEN/BLOCKED)
- `reports` — auto-populated when users submit
- `sos_alerts` — auto-populated on SOS

### 6. Build & Run
```bash
./gradlew assembleDebug
```
Or open in Android Studio → Run.

---

## App Architecture

```
Presentation (Compose Screens + ViewModels)
     ↓
Domain (Use Cases + Repository Interfaces)
     ↓
Data (Repositories → Firestore / Room / Retrofit)
```

---

## Features Checklist
- [x] Firebase Auth (Email + Google Sign-In)
- [x] 3 User Roles (Citizen / Field Officer / Admin)
- [x] Real-time Alert Feed from Firestore
- [x] GIS Map with Risk Heatmap polygons
- [x] Road Status Layer (Blocked / Open)
- [x] Field Incident Reporting (with photo upload)
- [x] Offline-first: Room queue + WorkManager auto-sync
- [x] AI Prediction via FastAPI (with mock fallback)
- [x] Weather Forecast (Open-Meteo)
- [x] SOS button (GPS + Firestore)
- [x] FCM Push Notifications
- [x] Admin Broadcast Alert
- [x] Admin SOS Alerts Dashboard
- [x] Multilingual (English + Hindi + Assamese)
