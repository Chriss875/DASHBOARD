# Angular WebSocket Integration Guide - OJS Analytics Platform

## 🎯 Overview

This guide provides complete instructions for integrating real-time WebSocket functionality with the OJS Analytics backend using Angular. The backend uses **STOMP over SockJS** for WebSocket communication.

---

## 📡 Backend WebSocket Configuration

### Connection Details
- **WebSocket Endpoint:** `ws://localhost:8080/ws` (development)
- **Production Endpoint:** `wss://your-domain.com/ws`
- **Protocol:** STOMP over SockJS
- **Application Prefix:** `/app`
- **Topic Prefix:** `/topic`

### CORS Configuration
Backend is configured to accept connections from:
- `http://localhost:4200` (Angular default)
- `http://localhost:3000`
- Your configured origins

---

## 📥 Available WebSocket Topics (Subscribe to Receive Data)

### 1. Real-Time Read Events
```typescript
Topic: /topic/reads
```
**Description:** Receives live notifications when articles are read  
**Payload Example:**
```json
{
  "submissionId": 12345,
  "articleTitle": "Sample Article",
  "country": "Tanzania",
  "city": "Dar es Salaam",
  "timestamp": "2024-02-14T18:30:00"
}
```

### 2. Real-Time Download Events
```typescript
Topic: /topic/downloads
```
**Description:** Receives live notifications when articles are downloaded  
**Payload Example:**
```json
{
  "submissionId": 12345,
  "articleTitle": "Sample Article",
  "country": "Kenya",
  "city": "Nairobi",
  "timestamp": "2024-02-14T18:30:00"
}
```

### 3. Statistics Updates (Every 5 seconds)
```typescript
Topic: /topic/stats
```
**Description:** Receives aggregated statistics updates  
**Payload Example:**
```json
{
  "totalReads": 15234,
  "totalDownloads": 8456,
  "totalArticles": 234,
  "topCountries": [
    {"country": "Tanzania", "count": 523},
    {"country": "Kenya", "count": 412}
  ],
  "recentActivity": [...]
}
```

### 4. Geographical Read Distribution (Per Article)
```typescript
Topic: /topic/geo/reads/{articleId}
```
**Description:** Receives geographical distribution of reads for a specific article  
**Payload Example:**
```json
[
  {
    "country": "Tanzania",
    "city": "Dar es Salaam",
    "latitude": -6.7924,
    "longitude": 39.2083,
    "count": 45
  },
  {
    "country": "Kenya",
    "city": "Nairobi",
    "latitude": -1.2864,
    "longitude": 36.8172,
    "count": 32
  }
]
```

### 5. Geographical Download Distribution (Per Article)
```typescript
Topic: /topic/geo/downloads/{articleId}
```
**Description:** Receives geographical distribution of downloads for a specific article  
**Payload Example:** Same format as geo reads

---

## 📤 Message Destinations (Send Requests to Backend)

### 1. Request Geographical Read Distribution
```typescript
Destination: /app/geo/reads
```
**Request All Articles:**
```json
{
  "type": "all"
}
```

**Request Specific Article:**
```json
{
  "type": "article",
  "articleId": 1542
}
```

**Request with Date Filter:**
```json
{
  "type": "article",
  "articleId": 1542,
  "fromDate": "2024-01-01",
  "toDate": "2024-12-31"
}
```

### 2. Request Geographical Download Distribution
```typescript
Destination: /app/geo/downloads
```
**Request Format:** Same as geo reads above

---

## 🚀 Angular Implementation

### Step 1: Install Required Dependencies

```bash
npm install @stomp/stompjs sockjs-client
npm install --save-dev @types/sockjs-client
```

### Step 2: Create WebSocket Service

Create `src/app/services/websocket.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';

export interface ReadEvent {
  submissionId: number;
  articleTitle: string;
  country: string;
  city: string;
  timestamp: string;
}

export interface DownloadEvent {
  submissionId: number;
  articleTitle: string;
  country: string;
  city: string;
  timestamp: string;
}

export interface StatsUpdate {
  totalReads: number;
  totalDownloads: number;
  totalArticles: number;
  topCountries: Array<{ country: string; count: number }>;
  recentActivity: any[];
}

export interface GeoMetric {
  country: string;
  city: string;
  latitude: number;
  longitude: number;
  count: number;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private connected$ = new BehaviorSubject<boolean>(false);
  
  // Observables for real-time data
  private readsSubject = new BehaviorSubject<ReadEvent | null>(null);
  private downloadsSubject = new BehaviorSubject<DownloadEvent | null>(null);
  private statsSubject = new BehaviorSubject<StatsUpdate | null>(null);
  
  public reads$ = this.readsSubject.asObservable();
  public downloads$ = this.downloadsSubject.asObservable();
  public stats$ = this.statsSubject.asObservable();
  public connected$ = this.connected$.asObservable();

  constructor() {
    this.client = new Client();
    this.configureClient();
  }

  private configureClient(): void {
    // WebSocket endpoint configuration
    this.client.webSocketFactory = () => {
      return new SockJS('http://localhost:8080/ws');
    };

    // Connection callbacks
    this.client.onConnect = (frame) => {
      console.log('WebSocket Connected:', frame);
      this.connected$.next(true);
      this.subscribeToTopics();
    };

    this.client.onDisconnect = () => {
      console.log('WebSocket Disconnected');
      this.connected$.next(false);
    };

    this.client.onStompError = (frame) => {
      console.error('WebSocket Error:', frame);
      this.connected$.next(false);
    };

    // Reconnection settings
    this.client.reconnectDelay = 5000; // 5 seconds
    this.client.heartbeatIncoming = 4000;
    this.client.heartbeatOutgoing = 4000;
  }

  /**
   * Connect to WebSocket server
   */
  public connect(): void {
    if (!this.client.active) {
      this.client.activate();
    }
  }

  /**
   * Disconnect from WebSocket server
   */
  public disconnect(): void {
    if (this.client.active) {
      this.client.deactivate();
    }
  }

  /**
   * Subscribe to all default topics
   */
  private subscribeToTopics(): void {
    // Subscribe to real-time read events
    this.client.subscribe('/topic/reads', (message) => {
      const data = JSON.parse(message.body);
      this.readsSubject.next(data);
    });

    // Subscribe to real-time download events
    this.client.subscribe('/topic/downloads', (message) => {
      const data = JSON.parse(message.body);
      this.downloadsSubject.next(data);
    });

    // Subscribe to statistics updates
    this.client.subscribe('/topic/stats', (message) => {
      const data = JSON.parse(message.body);
      this.statsSubject.next(data);
    });
  }

  /**
   * Subscribe to geographical reads for a specific article
   */
  public subscribeToArticleGeoReads(
    articleId: number,
    callback: (data: GeoMetric[]) => void
  ): StompSubscription {
    return this.client.subscribe(`/topic/geo/reads/${articleId}`, (message) => {
      const data = JSON.parse(message.body);
      callback(data);
    });
  }

  /**
   * Subscribe to geographical downloads for a specific article
   */
  public subscribeToArticleGeoDownloads(
    articleId: number,
    callback: (data: GeoMetric[]) => void
  ): StompSubscription {
    return this.client.subscribe(`/topic/geo/downloads/${articleId}`, (message) => {
      const data = JSON.parse(message.body);
      callback(data);
    });
  }

  /**
   * Request geographical read distribution for an article
   */
  public requestGeoReads(articleId: number, fromDate?: string, toDate?: string): void {
    const payload: any = {
      type: 'article',
      articleId: articleId
    };

    if (fromDate && toDate) {
      payload.fromDate = fromDate;
      payload.toDate = toDate;
    }

    this.client.publish({
      destination: '/app/geo/reads',
      body: JSON.stringify(payload)
    });
  }

  /**
   * Request geographical download distribution for an article
   */
  public requestGeoDownloads(articleId: number, fromDate?: string, toDate?: string): void {
    const payload: any = {
      type: 'article',
      articleId: articleId
    };

    if (fromDate && toDate) {
      payload.fromDate = fromDate;
      payload.toDate = toDate;
    }

    this.client.publish({
      destination: '/app/geo/downloads',
      body: JSON.stringify(payload)
    });
  }

  /**
   * Request all articles geo distribution
   */
  public requestAllGeoReads(): void {
    this.client.publish({
      destination: '/app/geo/reads',
      body: JSON.stringify({ type: 'all' })
    });
  }

  /**
   * Check if WebSocket is connected
   */
  public isConnected(): boolean {
    return this.client.active && this.connected$.value;
  }
}
```

### Step 3: Create a Component to Use WebSocket

Example: `src/app/components/live-dashboard/live-dashboard.component.ts`

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { WebSocketService, ReadEvent, DownloadEvent, StatsUpdate, GeoMetric } from '../../services/websocket.service';
import { StompSubscription } from '@stomp/stompjs';

@Component({
  selector: 'app-live-dashboard',
  templateUrl: './live-dashboard.component.html',
  styleUrls: ['./live-dashboard.component.css']
})
export class LiveDashboardComponent implements OnInit, OnDestroy {
  
  // Real-time data
  latestRead: ReadEvent | null = null;
  latestDownload: DownloadEvent | null = null;
  currentStats: StatsUpdate | null = null;
  isConnected = false;

  // Geographical data
  geoReadsData: GeoMetric[] = [];
  geoDownloadsData: GeoMetric[] = [];

  // Subscriptions
  private geoReadsSubscription?: StompSubscription;
  private geoDownloadsSubscription?: StompSubscription;

  constructor(private wsService: WebSocketService) {}

  ngOnInit(): void {
    // Connect to WebSocket
    this.wsService.connect();

    // Monitor connection status
    this.wsService.connected$.subscribe(connected => {
      this.isConnected = connected;
      console.log('Connection status:', connected);
    });

    // Subscribe to real-time reads
    this.wsService.reads$.subscribe(readEvent => {
      if (readEvent) {
        this.latestRead = readEvent;
        console.log('New Read Event:', readEvent);
        // Update your UI, trigger animations, etc.
      }
    });

    // Subscribe to real-time downloads
    this.wsService.downloads$.subscribe(downloadEvent => {
      if (downloadEvent) {
        this.latestDownload = downloadEvent;
        console.log('New Download Event:', downloadEvent);
      }
    });

    // Subscribe to statistics updates
    this.wsService.stats$.subscribe(stats => {
      if (stats) {
        this.currentStats = stats;
        console.log('Stats Update:', stats);
        // Update charts, counters, etc.
      }
    });
  }

  /**
   * Load geographical data for a specific article
   */
  loadArticleGeoData(articleId: number): void {
    // Subscribe to geographical reads
    this.geoReadsSubscription = this.wsService.subscribeToArticleGeoReads(
      articleId,
      (data) => {
        this.geoReadsData = data;
        console.log('Geo Reads Data:', data);
        // Update your map visualization
      }
    );

    // Subscribe to geographical downloads
    this.geoDownloadsSubscription = this.wsService.subscribeToArticleGeoDownloads(
      articleId,
      (data) => {
        this.geoDownloadsData = data;
        console.log('Geo Downloads Data:', data);
      }
    );

    // Request the data
    this.wsService.requestGeoReads(articleId);
    this.wsService.requestGeoDownloads(articleId);
  }

  /**
   * Load geo data with date filter
   */
  loadArticleGeoDataWithDateFilter(
    articleId: number,
    fromDate: string,
    toDate: string
  ): void {
    this.wsService.requestGeoReads(articleId, fromDate, toDate);
    this.wsService.requestGeoDownloads(articleId, fromDate, toDate);
  }

  ngOnDestroy(): void {
    // Unsubscribe from article-specific topics
    if (this.geoReadsSubscription) {
      this.geoReadsSubscription.unsubscribe();
    }
    if (this.geoDownloadsSubscription) {
      this.geoDownloadsSubscription.unsubscribe();
    }

    // Disconnect WebSocket
    this.wsService.disconnect();
  }
}
```

### Step 4: Template Example

`live-dashboard.component.html`:

```html
<div class="dashboard-container">
  <!-- Connection Status -->
  <div class="connection-status" [class.connected]="isConnected">
    <span *ngIf="isConnected">🟢 Connected</span>
    <span *ngIf="!isConnected">🔴 Disconnected</span>
  </div>

  <!-- Real-Time Statistics -->
  <div class="stats-panel" *ngIf="currentStats">
    <h2>Live Statistics</h2>
    <div class="stats-grid">
      <div class="stat-card">
        <h3>Total Reads</h3>
        <p class="stat-value">{{ currentStats.totalReads | number }}</p>
      </div>
      <div class="stat-card">
        <h3>Total Downloads</h3>
        <p class="stat-value">{{ currentStats.totalDownloads | number }}</p>
      </div>
      <div class="stat-card">
        <h3>Total Articles</h3>
        <p class="stat-value">{{ currentStats.totalArticles | number }}</p>
      </div>
    </div>
  </div>

  <!-- Latest Read Event -->
  <div class="event-card" *ngIf="latestRead">
    <h3>📖 Latest Read</h3>
    <p><strong>Article:</strong> {{ latestRead.articleTitle }}</p>
    <p><strong>Location:</strong> {{ latestRead.city }}, {{ latestRead.country }}</p>
    <p><strong>Time:</strong> {{ latestRead.timestamp | date:'short' }}</p>
  </div>

  <!-- Latest Download Event -->
  <div class="event-card" *ngIf="latestDownload">
    <h3>⬇️ Latest Download</h3>
    <p><strong>Article:</strong> {{ latestDownload.articleTitle }}</p>
    <p><strong>Location:</strong> {{ latestDownload.city }}, {{ latestDownload.country }}</p>
    <p><strong>Time:</strong> {{ latestDownload.timestamp | date:'short' }}</p>
  </div>

  <!-- Geographical Map (integrate with your map library) -->
  <div class="map-container">
    <h3>Geographical Distribution</h3>
    <!-- Use Leaflet, Google Maps, or any map library here -->
    <!-- Pass geoReadsData or geoDownloadsData to your map component -->
  </div>
</div>
```

---

## 🎨 Integration with Map Libraries

### Using Leaflet for Map Visualization

```bash
npm install leaflet
npm install @types/leaflet --save-dev
```

```typescript
import * as L from 'leaflet';

export class MapComponent implements OnInit {
  private map: L.Map;

  ngOnInit(): void {
    this.initMap();
  }

  private initMap(): void {
    this.map = L.map('map').setView([0, 0], 2);
    
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
  }

  updateMapWithGeoData(geoData: GeoMetric[]): void {
    // Clear existing markers
    this.map.eachLayer(layer => {
      if (layer instanceof L.Marker) {
        this.map.removeLayer(layer);
      }
    });

    // Add markers for each location
    geoData.forEach(metric => {
      const marker = L.marker([metric.latitude, metric.longitude])
        .addTo(this.map)
        .bindPopup(`
          <b>${metric.city}, ${metric.country}</b><br>
          Count: ${metric.count}
        `);
    });
  }
}
```

---

## 🔧 Configuration for Production

Update `environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  wsEndpoint: 'wss://your-production-domain.com/ws',
  apiUrl: 'https://your-production-domain.com/api/v1'
};
```

Update WebSocket service to use environment:

```typescript
import { environment } from '../../environments/environment';

this.client.webSocketFactory = () => {
  return new SockJS(environment.wsEndpoint);
};
```

---

## 🧪 Testing the Integration

### Manual Testing Steps:

1. **Start Backend:**
   ```bash
   cd /path/to/backend
   ./gradlew bootRun
   ```

2. **Start Angular App:**
   ```bash
   cd /path/to/angular-app
   ng serve
   ```

3. **Open Browser Console:**
   - Navigate to `http://localhost:4200`
   - Open Developer Tools → Console
   - You should see: "WebSocket Connected"

4. **Trigger Events:**
   - Use Swagger UI or Postman to send events to `/api/v1/events/ingest`
   - Watch real-time updates in your Angular app

### Test Event (POST to `/api/v1/events/ingest`):

```json
{
  "galley": {
    "pubObjectId": "12345",
    "assocType": 1048585,
    "canonicalUrl": "https://example.com/article/12345"
  },
  "ipAddress": "197.186.6.145"
}
```

**Headers:**
```
X-API-Key: your-api-key-here
Content-Type: application/json
```

---

## 🐛 Troubleshooting

### Issue: WebSocket Connection Fails

**Solutions:**
1. Verify backend is running on port 8080
2. Check CORS configuration in backend
3. Verify SockJS endpoint: `http://localhost:8080/ws`
4. Check browser console for errors

### Issue: Not Receiving Messages

**Solutions:**
1. Verify subscription topics are correct (case-sensitive)
2. Check that backend is broadcasting to correct topics
3. Ensure WebSocket is connected before subscribing
4. Check backend logs for errors

### Issue: Reconnection Not Working

**Solutions:**
1. Verify `reconnectDelay` is set in client configuration
2. Check network connectivity
3. Implement exponential backoff for reconnection

---

## 📚 Additional Resources

- **STOMP Documentation:** https://stomp-js.github.io/
- **SockJS Documentation:** https://github.com/sockjs/sockjs-client
- **Angular WebSocket Best Practices:** https://angular.io/guide/observables

---

## 🎯 Quick Start Checklist

- [ ] Install dependencies (@stomp/stompjs, sockjs-client)
- [ ] Create WebSocketService
- [ ] Configure WebSocket endpoint
- [ ] Subscribe to topics in component
- [ ] Handle connection status
- [ ] Implement reconnection logic
- [ ] Add error handling
- [ ] Test with backend running
- [ ] Update production configuration
- [ ] Implement map visualization (optional)

---

## 💡 Best Practices

1. **Always disconnect** WebSocket in `ngOnDestroy()`
2. **Handle reconnection** automatically with retry logic
3. **Show connection status** to users
4. **Buffer messages** if connection is temporarily lost
5. **Validate data** received from WebSocket
6. **Use TypeScript interfaces** for type safety
7. **Implement error boundaries** for graceful degradation
8. **Test thoroughly** with network throttling

---

## 📞 Support

If you encounter issues:
1. Check backend logs: `./gradlew bootRun --console=plain`
2. Check browser console for client-side errors
3. Verify network connectivity with browser DevTools
4. Test WebSocket connection with online tools

**Backend WebSocket Endpoint:** `ws://localhost:8080/ws`  
**Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

---

**Last Updated:** February 14, 2026  
**Backend Version:** 1.0.0  
**Compatible Angular Versions:** 14+, 15+, 16+, 17+
