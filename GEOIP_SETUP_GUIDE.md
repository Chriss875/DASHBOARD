# GeoIP Database Setup Guide

## 🎯 Complete Integration Checklist

Follow these steps to integrate the MaxMind GeoLite2-City database into your backend.

---

## Step 1: Download GeoLite2-City Database

### 1.1 Create MaxMind Account (5 minutes)

1. Visit: https://dev.maxmind.com/geoip/geolite2-free-geolocation-data
2. Click **"Sign Up for GeoLite2"**
3. Fill in your details:
   - Email address
   - Password
   - First/Last name
   - Company (can use "Personal" or "UDSM")
4. Verify your email address

### 1.2 Generate License Key (2 minutes)

1. Log in to your MaxMind account
2. Navigate to **"Account"** → **"Manage License Keys"**
3. Click **"Generate New License Key"**
4. Configure:
   - **Description**: `OJS Analytics Development`
   - **Will this key be used for GeoIP Update?**: Select **"No"**
5. Click **"Confirm"**
6. **IMPORTANT**: Copy and save your license key immediately (you won't see it again!)

### 1.3 Download the Database (2 minutes)

**Option A: Direct Download (Recommended)**

1. Go to: https://www.maxmind.com/en/accounts/current/geoip/downloads
2. Find **"GeoLite2 City"** in the list
3. Click **"Download GZIP"** (for `.mmdb` format)
4. The file will be named something like: `GeoLite2-City_YYYYMMDD.tar.gz`

**Option B: Using wget/curl (Advanced)**

```bash
# You'll need your license key from step 1.2
LICENSE_KEY="your_license_key_here"

curl -o GeoLite2-City.tar.gz \
  "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-City&license_key=${LICENSE_KEY}&suffix=tar.gz"
```

---

## Step 2: Extract and Place the Database

### 2.1 Extract the Downloaded File

The downloaded file is a `.tar.gz` archive. Extract it:

**On macOS (using Finder):**
1. Double-click the downloaded `GeoLite2-City_YYYYMMDD.tar.gz` file
2. It will automatically extract to a folder
3. Inside, you'll find `GeoLite2-City.mmdb`

**On macOS (using Terminal):**
```bash
cd ~/Downloads
tar -xzf GeoLite2-City_*.tar.gz
```

### 2.2 Copy to Your Project

Now copy the `.mmdb` file to your project directory:

```bash
# Navigate to your project
cd /Users/apple/Desktop/UDSM_HACKATHON2026

# Copy the database file
cp ~/Downloads/GeoLite2-City_*/GeoLite2-City.mmdb src/main/resources/geoip/

# Verify it's there
ls -lh src/main/resources/geoip/
```

**Expected output:**
```
-rw-r--r--  1 apple  staff    70M Feb 14 16:00 GeoLite2-City.mmdb
```

---

## Step 3: Verify Configuration

### 3.1 Check application.properties

The configuration has already been updated to:

```properties
app.geoip.database-path=src/main/resources/geoip/GeoLite2-City.mmdb
```

✅ **This is already configured!**

### 3.2 Update API Key (Important!)

Edit `src/main/resources/application.properties` and change:

```properties
app.ingestion.api-key=your-secure-api-key-change-this-in-production
```

To something secure, for example:

```properties
app.ingestion.api-key=ojs-analytics-2026-secure-key-xyz789
```

**💡 Tip**: Generate a random API key:
```bash
openssl rand -base64 32
```

---

## Step 4: Build and Test

### 4.1 Build the Project

```bash
cd /Users/apple/Desktop/UDSM_HACKATHON2026
./gradlew clean build
```

**Expected output:**
```
BUILD SUCCESSFUL in 15s
```

### 4.2 Run the Application

```bash
./gradlew bootRun
```

**Look for this in the logs:**
```
INFO  GeoIPService - GeoIP database loaded successfully from: src/main/resources/geoip/GeoLite2-City.mmdb
```

✅ **If you see this, GeoIP is working!**

❌ **If you see an error:**
```
WARN  GeoIPService - GeoIP database not found at: src/main/resources/geoip/GeoLite2-City.mmdb
```
→ Go back to Step 2.2 and verify the file location.

---

## Step 5: Test the Integration

### 5.1 Test GeoIP Resolution

Once the application is running, test the ingestion endpoint:

```bash
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -H "X-API-Key: ojs-analytics-2026-secure-key-xyz789" \
  -d '{
    "eventType": "READ",
    "timestamp": "2026-02-14T16:53:00Z",
    "ip": "8.8.8.8",
    "userAgent": "Test Client",
    "journalPath": "test",
    "journalTitle": "Test Journal",
    "articleId": 1,
    "articleTitle": "Test Article",
    "doi": "10.1234/test.1",
    "sectionTitle": "Test Section",
    "authors": ["Test Author"]
  }'
```

**Expected Response (202 Accepted):**
```json
{
  "eventType": "READ",
  "timestamp": "2026-02-14T16:53:00Z",
  "ip": "8.8.8.8",
  "articleId": 1,
  "articleTitle": "Test Article",
  "country": "United States",
  "countryCode": "US",
  "city": "Mountain View",
  "continent": "North America",
  "latitude": 37.386,
  "longitude": -122.0838
}
```

✅ **If you see geo data (country, city, lat/lng), GeoIP is working perfectly!**

### 5.2 Test with Different IPs

Try different IP addresses to verify geo resolution:

**Tanzania IP:**
```bash
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -H "X-API-Key: ojs-analytics-2026-secure-key-xyz789" \
  -d '{
    "eventType": "READ",
    "timestamp": "2026-02-14T16:53:00Z",
    "ip": "154.118.224.1",
    "articleId": 1,
    "articleTitle": "Test Article"
  }'
```

**Expected**: Should return Tanzania location data.

**Kenya IP:**
```bash
# Use IP: 105.163.0.1
# Expected: Kenya location data
```

---

## Step 6: Production Deployment (Optional)

For production, you may want to use `/opt/geoip` instead:

### 6.1 Create System Directory

```bash
sudo mkdir -p /opt/geoip
sudo cp src/main/resources/geoip/GeoLite2-City.mmdb /opt/geoip/
sudo chmod 644 /opt/geoip/GeoLite2-City.mmdb
```

### 6.2 Update Configuration for Production

Create `application-prod.properties`:

```properties
app.geoip.database-path=/opt/geoip/GeoLite2-City.mmdb
app.ingestion.api-key=${INGESTION_API_KEY}
```

Run with production profile:
```bash
java -jar build/libs/UDSM_HACKATHON2026-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## 🔄 Database Updates

MaxMind updates GeoLite2 databases monthly. To keep your data fresh:

### Manual Update (Every Month)

1. Download the latest database from MaxMind
2. Replace the old `.mmdb` file
3. Restart your application

### Automated Update (Advanced)

Use MaxMind's `geoipupdate` tool:

```bash
# Install geoipupdate
brew install geoipupdate

# Configure with your license key
# Edit /usr/local/etc/GeoIP.conf

# Run update
geoipupdate
```

---

## 📊 Monitoring

### Check Database Info

```bash
# Check file size (should be ~70MB)
ls -lh src/main/resources/geoip/GeoLite2-City.mmdb

# Check last modified date
stat src/main/resources/geoip/GeoLite2-City.mmdb
```

### Application Logs

Monitor logs for GeoIP-related messages:

```bash
# Watch logs while running
./gradlew bootRun | grep -i geoip
```

---

## ❓ Troubleshooting

### Problem: "GeoIP database not found"

**Solution:**
1. Verify file exists: `ls src/main/resources/geoip/GeoLite2-City.mmdb`
2. Check path in `application.properties`
3. Ensure file has read permissions: `chmod 644 src/main/resources/geoip/GeoLite2-City.mmdb`

### Problem: "Could not resolve IP"

**Possible causes:**
- Private IP address (127.0.0.1, 192.168.x.x, 10.x.x.x) - These are not in the database
- Invalid IP format
- Database file is corrupted

**Solution:** The system handles this gracefully and returns "Unknown" location.

### Problem: Build fails with "File too large"

**Solution:** Add to `.gitignore`:
```
src/main/resources/geoip/*.mmdb
```

The `.mmdb` file should NOT be committed to Git (it's 70MB+).

---

## ✅ Integration Complete!

Once you see this in your logs:
```
INFO  GeoIPService - GeoIP database loaded successfully
```

And your test curl returns geo data, **you're all set!** 🎉

---

## Next Steps

1. ✅ Download and place GeoLite2-City.mmdb
2. ✅ Update API key in application.properties
3. ✅ Build and run the application
4. ✅ Test with curl
5. 🔜 Create OJS PHP plugin
6. 🔜 Build frontend dashboard with WebSocket
7. 🔜 Deploy to production

---

**Need help?** Check the main documentation: `REALTIME_ANALYTICS_GUIDE.md`
