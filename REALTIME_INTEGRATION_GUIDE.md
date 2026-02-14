# Real-Time Analytics Integration Guide

## ✅ Refactored Implementation (Integrated with Existing Schema)

This system now properly integrates with your existing OJS `metrics` table structure without creating duplicate tables.

---

## 🏗️ Architecture Overview

### Flow Diagram

```
OJS Plugin → POST /api/v1/events/ingest → [GeoIP Resolution] → [Insert to metrics table]
                                              ↓                            ↓
                                         (microseconds)            (async, non-blocking)
                                              ↓                            ↓
                                    [WebSocket Broadcast]    [Update metrics with country_id]
                                              ↓
                                    Frontend receives update
                                              ↓
                            Frontend can query updated geo distribution
```

---

## 📊 Database Integration

### Existing `metrics` Table Structure (No Changes Required!)

Your existing table already has all the fields needed:

```sql
metrics table:
- load_id (VARCHAR) - Unique ID (we use UUID for new events)
- context_id (BIGINT) - Journal context
- submission_id (BIGINT) - Article ID
- assoc_id (BIGINT) - Associated object ID
- assoc_type (BIGINT) - **1048585 = READS, 515 = DOWNLOADS**
- day (VARCHAR) - Format: YYYYMMDD
- month (VARCHAR) - Format: YYYYMM
- country_id (VARCHAR) - ISO 2-letter code (enriched by GeoIP)
- region (VARCHAR) - Region code (enriched by GeoIP)
- city (VARCHAR) - City name (enriched by GeoIP)
- metric_type (VARCHAR) - Usually "ojs::counter"
- metric (INT) - Count (1 per event)
```

---

## 🔌 API Endpoints

### 1. Event Ingestion (POST)

**Endpoint**: `POST /api/v1/events/ingest`

**Headers**:
```
Content-Type: application/json
X-API-Key: your-api-key-here
```

**Request Body**:
```json
{
  "eventType": "READ",  // or "DOWNLOAD"
  "timestamp": "2026-02-14T18:00:00Z",
  "ip": "197.232.45.12",
  "articleId": 1542,
  "articleTitle": "Climate Change Impact",
  "journalPath": "tjpsd",
  "journalTitle": "Tanzania Journal of Population Studies",
  "userAgent": "Mozilla/5.0...",
  "referrer": "https://google.com/...",
  "doi": "10.1234/tjpsd.v1i2.1542",
  "sectionTitle": "Research Articles",
  "authors": ["John Doe", "Jane Smith"],
  "galley": {  // Only for DOWNLOAD events
    "galleyId": 87,
    "galleyLabel": "PDF",
    "mimeType": "application/pdf",
    "fileName": "article-1542.pdf"
  }
}
```

**What Happens**:
1. ✅ IP resolved to country/city using GeoIP (synchronous, fast)
2. ✅ Event saved to `metrics` table with `assoc_type` = 1048585 (reads) or 515 (downloads)
3. ✅ WebSocket broadcast to `/topic/reads` or `/topic/downloads`
4. ✅ WebSocket broadcast updated geo distribution to `/topic/geo/reads/{articleId}`
5. ✅ Returns 202 Accepted immediately

---

### 2. Existing REST Endpoints (Unchanged!)

Your existing endpoints continue to work exactly as before:

**Get Geographical Reads** (uses existing `metrics` table):
```
GET /api/v1/articles/totalreads/geographicalwise/{articleId}
```

**Get Geographical Downloads** (uses existing `metrics` table):
```
GET /api/v1/articles/totaldownloads/geographicwise/{articleId}
```

**Response**:
```json
[
  {
    "country": "Tanzania",
    "region": "Dar es Salaam",
    "city": "Dar es Salaam",
    "totalMetric": 145
  },
  {
    "country": "Kenya",
    "region": "Nairobi",
    "city": "Nairobi",
    "totalMetric": 87
  }
]
```

---

## 🔄 WebSocket Integration

### Topics

1. **`/topic/reads`** - Real-time read events (enriched with geo data)
2. **`/topic/downloads`** - Real-time download events (enriched with geo data)
3. **`/topic/stats`** - Aggregated statistics (broadcast every 5 seconds)
4. **`/topic/geo/reads/{articleId}`** - Updated geo distribution for specific article (reads)
5. **`/topic/geo/downloads/{articleId}`** - Updated geo distribution for specific article (downloads)

### Frontend Implementation

#### 1. Connect to WebSocket

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// Connect
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
  console.log('Connected:', frame);
  
  // Subscribe to topics...
});
```

#### 2. Subscribe to Real-Time Events (All Articles)

```javascript
// Listen for ALL read events across all articles
stompClient.subscribe('/topic/reads', (message) => {
  const event = JSON.parse(message.body);
  console.log('New read event:', {
    articleId: event.articleId,
    country: event.country,
    city: event.city,
    latitude: event.latitude,
    longitude: event.longitude
  });
  
  // Update your heatmap/visualization in real-time
  addPointToHeatmap(event.latitude, event.longitude);
});

// Listen for ALL download events
stompClient.subscribe('/topic/downloads', (message) => {
  const event = JSON.parse(message.body);
  console.log('New download event:', event);
});
```

#### 3. Subscribe to Geo Distribution Updates (Specific Article)

```javascript
// Subscribe to real-time geo updates for article 1542
const articleId = 1542;

stompClient.subscribe(`/topic/geo/reads/${articleId}`, (message) => {
  const geoData = JSON.parse(message.body);
  console.log(`Updated geo distribution for article ${articleId}:`, geoData);
  
  // geoData is an array of GeographicalMetricsDto:
  // [{ country: "Tanzania", region: "...", city: "...", totalMetric: 145 }, ...]
  
  updateMap(geoData); // Update your map visualization
});
```

#### 4. Request Geo Distribution On-Demand

```javascript
// Request current geo distribution for a specific article
stompClient.send('/app/geo/reads', {}, JSON.stringify({
  type: 'article',
  articleId: 1542
}));

// The response will come via the subscribed topic: /topic/geo/reads/1542
```

#### 5. Subscribe to Aggregated Stats

```javascript
stompClient.subscribe('/topic/stats', (message) => {
  const stats = JSON.parse(message.body);
  
  // Update dashboard counters
  document.getElementById('totalReads').textContent = stats.totalReads;
  document.getElementById('totalDownloads').textContent = stats.totalDownloads;
  document.getElementById('uniqueCountries').textContent = stats.uniqueCountries;
});
```

---

## 🎯 Complete Frontend Example

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class RealtimeAnalyticsDashboard {
  constructor() {
    this.socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = Stomp.over(this.socket);
    this.currentArticleId = null;
  }

  connect() {
    this.stompClient.connect({}, (frame) => {
      console.log('Connected to real-time analytics');
      
      // Subscribe to all real-time events
      this.subscribeToRealtimeEvents();
      
      // Subscribe to stats updates
      this.subscribeToStats();
    });
  }

  subscribeToRealtimeEvents() {
    // Listen to ALL read events
    this.stompClient.subscribe('/topic/reads', (message) => {
      const event = JSON.parse(message.body);
      this.handleNewReadEvent(event);
    });

    // Listen to ALL download events
    this.stompClient.subscribe('/topic/downloads', (message) => {
      const event = JSON.parse(message.body);
      this.handleNewDownloadEvent(event);
    });
  }

  subscribeToStats() {
    this.stompClient.subscribe('/topic/stats', (message) => {
      const stats = JSON.parse(message.body);
      this.updateStatsDisplay(stats);
    });
  }

  watchArticle(articleId) {
    this.currentArticleId = articleId;
    
    // Subscribe to geo updates for this specific article
    this.stompClient.subscribe(`/topic/geo/reads/${articleId}`, (message) => {
      const geoData = JSON.parse(message.body);
      this.updateHeatmap(geoData);
    });

    this.stompClient.subscribe(`/topic/geo/downloads/${articleId}`, (message) => {
      const geoData = JSON.parse(message.body);
      this.updateDownloadMap(geoData);
    });

    // Request initial data
    this.stompClient.send('/app/geo/reads', {}, JSON.stringify({
      type: 'article',
      articleId: articleId
    }));
  }

  handleNewReadEvent(event) {
    console.log(`New read from ${event.country} for article ${event.articleId}`);
    
    // Show notification
    this.showNotification(`New read from ${event.city}, ${event.country}`);
    
    // Add to realtime activity feed
    this.addToActivityFeed(event);
    
    // If watching this article, the geo update will come automatically via /topic/geo/reads/{articleId}
  }

  handleNewDownloadEvent(event) {
    console.log(`New download from ${event.country} for article ${event.articleId}`);
    this.showNotification(`PDF downloaded from ${event.city}, ${event.country}`);
  }

  updateHeatmap(geoData) {
    // geoData = [{ country, region, city, totalMetric }, ...]
    const heatmapPoints = geoData.map(item => ({
      lat: item.latitude, // You may need to add lat/lng to GeographicalMetricsDto
      lng: item.longitude,
      count: item.totalMetric
    }));
    
    // Update your Leaflet/Google Maps heatmap layer
    this.heatmapLayer.setData(heatmapPoints);
  }

  updateStatsDisplay(stats) {
    document.getElementById('totalReads').textContent = stats.totalReads.toLocaleString();
    document.getElementById('totalDownloads').textContent = stats.totalDownloads.toLocaleString();
    document.getElementById('uniqueCountries').textContent = stats.uniqueCountries;
  }
}

// Initialize
const dashboard = new RealtimeAnalyticsDashboard();
dashboard.connect();

// When user selects an article
dashboard.watchArticle(1542);
```

---

## 🧪 Testing

### Test Event Ingestion

```bash
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key-here" \
  -d '{
    "eventType": "READ",
    "timestamp": "2026-02-14T18:00:00Z",
    "ip": "154.118.224.1",
    "articleId": 1542,
    "articleTitle": "Test Article"
  }'
```

### Verify in Database

```sql
-- Check if event was inserted into metrics table
SELECT * FROM metrics 
WHERE submission_id = 1542 
AND assoc_type = 1048585 
ORDER BY load_id DESC 
LIMIT 5;

-- Check geo distribution
SELECT country_id, city, SUM(metric) as total
FROM metrics
WHERE submission_id = 1542 AND assoc_type = 1048585
GROUP BY country_id, city
ORDER BY total DESC;
```

---

## 📋 Summary of Changes

### ✅ What Was Removed (Duplicates)
- ❌ `LiveEvent` entity (was duplicate of `Metric`)
- ❌ `LiveEventRepository` (was duplicate of `MetricRepository`)
- ❌ `RealtimeAnalyticsController` (REST endpoints - was duplicate of `ArticleAnalyticsController`)

### ✅ What Was Added
- ✅ `RealtimeWebSocketController` - Handles WebSocket subscriptions for real-time geo updates
- ✅ Updated `Metric` entity to support writes (`@Setter`, `@Builder`, changed ID to `load_id`)
- ✅ `JacksonConfig` - Provides `ObjectMapper` bean

### ✅ What Was Updated
- ✅ `EventIngestionService` - Now uses existing `Metric` entity and `MetricRepository`
- ✅ `StatsScheduler` - Now uses existing `MetricRepository` queries
- ✅ `EventIngestionController` - Works with refactored service

### ✅ What Remains Unchanged
- ✅ All existing REST endpoints in `ArticleAnalyticsController`
- ✅ All existing queries in `MetricRepository`
- ✅ Database schema (no migrations needed!)
- ✅ Existing frontend code using REST endpoints continues to work

---

## 🎉 Benefits

1. **No Schema Changes** - Works with your existing `metrics` table
2. **No Duplicate Code** - Reuses your existing repository and service layer
3. **Real-Time Updates** - WebSocket broadcasts keep frontends in sync
4. **Fast Ingestion** - 202 Accepted response, async persistence
5. **GeoIP Enrichment** - IP addresses automatically resolved to country/city
6. **Backward Compatible** - All existing REST endpoints continue to work

---

## 🚀 Next Steps

1. ✅ Start the application: `./gradlew bootRun`
2. ✅ Test ingestion endpoint with curl
3. ✅ Connect frontend to WebSocket
4. ✅ Subscribe to real-time topics
5. ✅ Build your dashboard with live updates!

---

**Need Help?** Check `GEOIP_SETUP_GUIDE.md` for GeoIP database setup and `QUICK_START.md` for quick reference.
