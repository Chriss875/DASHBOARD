# 🚀 Quick Start - GeoIP Integration

## TL;DR - 3 Simple Steps

### 1️⃣ Download GeoLite2-City.mmdb

Visit: https://dev.maxmind.com/geoip/geolite2-free-geolocation-data
- Create free account
- Download GeoLite2-City database
- Extract the `.tar.gz` file

### 2️⃣ Place the Database File

```bash
cd /Users/apple/Desktop/UDSM_HACKATHON2026
cp ~/Downloads/GeoLite2-City_*/GeoLite2-City.mmdb src/main/resources/geoip/
```

### 3️⃣ Run the Application

```bash
./gradlew bootRun
```

---

## ✅ Verify It's Working

Look for this in the logs:
```
✅ INFO  GeoIPService - GeoIP database loaded successfully from: src/main/resources/geoip/GeoLite2-City.mmdb
```

---

## 🧪 Test with curl

```bash
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-secure-api-key-change-this-in-production" \
  -d '{
    "eventType": "READ",
    "timestamp": "2026-02-14T16:53:00Z",
    "ip": "8.8.8.8",
    "articleId": 1,
    "articleTitle": "Test Article"
  }'
```

**Expected Response:**
```json
{
  "country": "United States",
  "city": "Mountain View",
  "latitude": 37.386,
  "longitude": -122.0838
}
```

---

## 📚 Full Documentation

- **Detailed Setup**: See `GEOIP_SETUP_GUIDE.md`
- **API Documentation**: See `REALTIME_ANALYTICS_GUIDE.md`
- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 🆘 Troubleshooting

**Database not found?**
```bash
ls -lh src/main/resources/geoip/GeoLite2-City.mmdb
```

**Still not working?**
Check the detailed guide: `GEOIP_SETUP_GUIDE.md`
