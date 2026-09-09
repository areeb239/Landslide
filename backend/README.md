# 🏔️ NER Landslide Early Warning & Prediction Backend (FastAPI)

Production-grade, asynchronous backend designed for the **North Eastern Region Landslide & Slope Hazard Monitoring Platform**.

---

## 🌟 Capabilities

1. **AI / ML Landslide Prediction (`/api/v1/predict`)**:
   - Supports pre-trained scikit-learn models (`landslide_model.joblib`) or falls back dynamically to calibrated GSI / Caine Himalayan geotechnical physics heuristics.
   - Outputs: failure probability, risk classification (`LOW`, `MODERATE`, `HIGH`, `CRITICAL`), factor contribution breakdown, and actionable mitigation recommendations.
2. **Automated Real-Time Weather Prediction (`/api/v1/predict/location`)**:
   - Integrates with Open-Meteo's free public weather API (zero cost, zero API keys required).
   - Fetches 24h precipitation, 3-day antecedent rainfall, and soil moisture for any GPS coordinate across Sikkim, Assam, Meghalaya, Arunachal Pradesh, Nagaland, Manipur, Mizoram, and Tripura.
3. **Regional Hotspots Surveillance (`/api/v1/predict/hotspots`)**:
   - Precomputed live risk assessment for 8 key disaster-prone corridors (NH-10 Sevoke-Gangtok, Dzongu, Haflong Dima Hasao, Guwahati Hill Cuts, Kohima-Dimapur NH-29, etc.).
4. **FCM Push Notification Broadcasting (`/api/v1/alerts`)**:
   - Broadcasts real-time early warnings to citizen Android phones subscribed to topic `ner-alerts`.
   - Uses Firebase Admin SDK server-side (cannot be done directly from Android for security).
5. **IoT Sensor Telemetry Ingestion (`/api/v1/sensors/telemetry`)**:
   - Ingests tiltmeter angle, soil moisture, pore water pressure, and rainfall intensity.
   - Evaluates danger thresholds and automatically triggers early warning broadcasts.
6. **Common Alerting Protocol (CAP v1.2) (`/api/v1/alerts/cap.xml`)**:
   - Standardized XML alert feed compliant with NDMA (National Disaster Management Authority) and WMO requirements.
7. **Emergency SOS Distress Responder (`/api/v1/sos`)**:
   - Dispatches priority push notifications to emergency response teams and control rooms.

---

## 🚀 Quickstart Guide

### 1. Prerequisites
- Python 3.10+ installed.

### 2. Setup Virtual Environment
Open PowerShell or Terminal in this `backend/` folder:

```powershell
python -m venv venv
.\venv\Scripts\Activate.ps1   # On Windows
# source venv/bin/activate    # On Linux / Mac
```

### 3. Install Dependencies
```powershell
pip install -r requirements.txt
```

### 4. (Optional) Train the Machine Learning Model
Generate and train the Random Forest model:
```powershell
python train_model.py
```
*This produces `landslide_model.joblib` which the server automatically detects and loads!*

### 5. Run the Server
```powershell
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```
- Interactive Swagger UI Docs: **http://localhost:8000/docs**
- Alternative Redoc: **http://localhost:8000/redoc**
- Health Check: **http://localhost:8000/api/v1/health**

---

## 📱 Connecting Android App to this Backend

In your Android app's `app/build.gradle.kts`:
- **For Android Emulator**: Use `http://10.0.2.2:8000/`
- **For Physical Android Device (Wi-Fi)**: Use your computer's local LAN IP, e.g. `http://192.168.1.15:8000/`
- **For Free Cloud Hosting**: Deploy on [Render](https://render.com) or [HuggingFace Spaces](https://huggingface.co/spaces) and use `https://your-app.onrender.com/`

---

## 🔑 Firebase Admin SDK Setup (Optional for Live Firebase)

The backend works **100% out-of-the-box in fallback/demo mode** without Firebase credentials — ML inference, weather correlation, and sensor simulation are fully functional!

To enable live Firestore database persistence and real FCM push notifications:
### Option A: Local Development
1. Go to [Firebase Console](https://console.firebase.google.com/) -> Project Settings -> **Service accounts**.
2. Click **Generate new private key**.
3. Save the downloaded JSON file as `serviceAccountKey.json` inside this `backend/` folder.
4. Restart the server.

### Option B: Cloud Hosting (Render / Railway / Heroku)
Because `serviceAccountKey.json` is not committed to git for security:
1. Open your downloaded `serviceAccountKey.json` in a text editor and copy the entire JSON content.
2. Go to your **Render Dashboard** -> Select your Web Service -> **Environment**.
3. Add an Environment Variable:
   - **Key**: `FIREBASE_SERVICE_ACCOUNT_JSON`
   - **Value**: *(Paste the entire contents of your serviceAccountKey.json)*
4. Save Changes — Render will redeploy and automatically connect to Firebase!
