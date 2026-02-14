# 🚀 Quick Pull & Run Guide

This guide is for frontend developers who want to quickly pull and run the OJS Analytics backend.

## Prerequisites

- Docker Desktop installed (includes Docker and Docker Compose)
  - [Download for Mac](https://www.docker.com/products/docker-desktop/)
  - [Download for Windows](https://www.docker.com/products/docker-desktop/)
  - [Download for Linux](https://docs.docker.com/desktop/install/linux-install/)

## 🎯 Quick Start (3 Steps)

### Step 1: Clone or Pull the Repository

```bash
git clone <repository-url>
cd UDSM_HACKATHON2026
```

Or if pulling updates:
```bash
git pull origin main
```

### Step 2: Start Everything

```bash
docker-compose up -d
```

**That's it!** Docker will:
- ✅ Build the Spring Boot application
- ✅ Start MariaDB database
- ✅ Start Redis cache
- ✅ Configure networking
- ✅ Set up CORS for your frontend

### Step 3: Verify It's Running

```bash
docker-compose ps
```

You should see 3 services running:
- `ojs-analytics-app` (port 8080)
- `ojs-analytics-db` (port 3306)
- `ojs-analytics-redis` (port 6379)

**Test the API:**
```bash
curl http://localhost:8080/actuator/health
```

**Open Swagger UI:**
```
http://localhost:8080/swagger-ui/index.html
```

## 📡 Connect Your Frontend

### REST API

Set your API base URL to:
```typescript
const API_BASE_URL = 'http://localhost:8080';
```

### WebSocket Connection

```typescript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  // Subscribe to real-time updates
  stompClient.subscribe('/topic/reads', (message) => {
    console.log('New read event:', JSON.parse(message.body));
  });
  
  stompClient.subscribe('/topic/downloads', (message) => {
    console.log('New download event:', JSON.parse(message.body));
  });
  
  stompClient.subscribe('/topic/stats', (message) => {
    console.log('Stats update:', JSON.parse(message.body));
  });
});
```

### API Authentication

For event ingestion endpoint, include the API key:
```typescript
const headers = {
  'X-API-Key': 'your-api-key-here',  // Replace with actual API key
  'Content-Type': 'application/json'
};
```

## 🔧 Common Commands

### View Logs
```bash
# All services
docker-compose logs -f

# Just the app
docker-compose logs -f app
```

### Stop Services
```bash
docker-compose stop
```

### Start Again
```bash
docker-compose start
```

### Restart After Code Changes
```bash
docker-compose down
docker-compose up -d --build
```

### Complete Cleanup
```bash
docker-compose down -v  # Warning: deletes database!
```

## 🌐 Frontend CORS Configuration

The backend is already configured to accept requests from:
- `http://localhost:4200` (Angular default)
- `http://localhost:3000` (React default)
- `http://localhost:8080` (Swagger UI)

**Need to add your frontend URL?**

Edit `compose.yaml` and add your URL:
```yaml
SPRING_WEB_CORS_ALLOWED_ORIGINS: http://localhost:4200,http://your-frontend-url:port
```

Then restart:
```bash
docker-compose restart app
```

## 📊 Available Endpoints

### Event Ingestion
```bash
POST http://localhost:8080/api/v1/events/ingest
Headers: X-API-Key: your-api-key-here  # Replace with actual API key
Body: {
  "galley": {
    "pubObjectId": "12345",
    "assocType": 1048585,  // 1048585=reads, 515=downloads
    "canonicalUrl": "https://example.com/article/12345"
  },
  "ipAddress": "197.186.6.145"
}
```

### Analytics Queries
```bash
# Get reads for an article
GET http://localhost:8080/api/v1/analytics/reads?articleId=12345

# Get downloads with date range
GET http://localhost:8080/api/v1/analytics/downloads?articleId=12345&from=2026-01-01&to=2026-02-14

# Get overall statistics
GET http://localhost:8080/api/v1/analytics/stats
```

### WebSocket Topics
- `/topic/reads` - Real-time read events
- `/topic/downloads` - Real-time download events  
- `/topic/stats` - Stats updates every 5 seconds

## 🐛 Troubleshooting

### "Port already in use"
Another service is using port 8080, 3306, or 6379.

**Solution:** Stop the conflicting service or change ports in `compose.yaml`

### "Connection refused"
Services are still starting up.

**Solution:** Wait 30-60 seconds and try again. Check status:
```bash
docker-compose ps
```

### "CORS error"
Your frontend URL is not in the allowed origins.

**Solution:** Add your URL to `compose.yaml` as shown above

### "Container exits immediately"
There's a configuration error.

**Solution:** Check logs:
```bash
docker-compose logs app
```

## 🎨 Frontend Example (React/TypeScript)

```typescript
import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const API_BASE = 'http://localhost:8080';

function Analytics() {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    // Fetch initial stats
    fetch(`${API_BASE}/api/v1/analytics/stats`)
      .then(res => res.json())
      .then(data => setStats(data));

    // Connect to WebSocket for real-time updates
    const socket = new SockJS(`${API_BASE}/ws`);
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        client.subscribe('/topic/stats', (message) => {
          setStats(JSON.parse(message.body));
        });
      }
    });
    client.activate();

    return () => client.deactivate();
  }, []);

  return (
    <div>
      <h1>Real-time Analytics</h1>
      {stats && (
        <div>
          <p>Total Reads: {stats.totalReads}</p>
          <p>Total Downloads: {stats.totalDownloads}</p>
        </div>
      )}
    </div>
  );
}

export default Analytics;
```

## 📦 What's Running?

| Service | Purpose | Port | Container Name |
|---------|---------|------|----------------|
| Spring Boot App | API & WebSocket | 8080 | ojs-analytics-app |
| MariaDB | Database | 3306 | ojs-analytics-db |
| Redis | Cache | 6379 | ojs-analytics-redis |

## 🔐 Default Credentials

**Database:**
- Host: `localhost:3306`
- Database: `tjpsd32`
- Username: `root`
- Password: `rootpassword`

**API Key:**
- Set your API key in `compose.yaml` or use environment variable

⚠️ **Note:** Change default passwords and API keys before using in production!

## 📚 More Documentation

- `DOCKER_SETUP.md` - Detailed Docker configuration
- `REALTIME_INTEGRATION_GUIDE.md` - WebSocket integration details
- `QUICK_START.md` - Application features and usage
- `GEOIP_SETUP_GUIDE.md` - GeoIP database setup

## 💡 Tips

1. **First time setup?** It takes 2-3 minutes to build the application
2. **Making code changes?** Use `docker-compose up -d --build` to rebuild
3. **Database not persisting?** Data is stored in Docker volumes and survives restarts
4. **Need to reset everything?** Use `docker-compose down -v` (deletes data!)

## ✅ Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Database connection
docker exec ojs-analytics-db mysql -u root -prootpassword -e "SELECT 1"

# Redis connection
docker exec ojs-analytics-redis redis-cli ping
```

---

**Need help?** Check the logs:
```bash
docker-compose logs -f app
```

**Ready to go!** Your backend is running at `http://localhost:8080` 🎉
