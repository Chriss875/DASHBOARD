# Angular Frontend: Page Refresh & Initial Data Loading Strategy

## 🎯 Objective

Implement a robust data loading strategy that handles page refreshes gracefully by combining REST API (for initial state) with WebSocket (for real-time updates).

---

## 📋 The Problem

When a user refreshes the page:
- ❌ WebSocket connection is lost
- ❌ All subscriptions are cleared
- ❌ Real-time data is lost
- ❌ Map shows empty/blank until new events arrive

---

## ✅ The Solution: REST + WebSocket Hybrid

### **Pattern:**
```
1. Page loads → Fetch initial data via REST API (immediate)
2. Then connect to WebSocket (for real-time updates)
3. On reconnection → Refresh data via REST API
```

This ensures the map **always shows current data**, even after refresh.

---

## 🔌 Backend Architecture

Your backend provides **BOTH** mechanisms:

### **REST Endpoints** (For Initial Data Load)
- Get current aggregated state
- No WebSocket connection needed
- Returns complete snapshot

### **WebSocket Topics** (For Real-Time Updates)
- Continuous stream of updates
- Requires active connection
- Updates existing data

---

## 📡 REST API Endpoints

### **Base URL**
```
Development: http://localhost:8080
Production:  https://your-domain.com
```

---

### **1. GET /api/v1/global/map/reads**

**Purpose:** Get current global read distribution across ALL articles

**Method:** `GET`

**Headers:** None required

**Response Status:** `200 OK`

**Response Body:**
```json
{
  "type": "reads",
  "timestamp": "2026-02-15T01:00:00.123Z",
  "total": 15234,
  "countryCount": 87,
  "countries": [
    {
      "countryCode": "TZ",
      "countryName": "Tanzania",
      "count": 2456,
      "latitude": -6.7924,
      "longitude": 39.2083,
      "percentage": 16.12
    },
    {
      "countryCode": "US",
      "countryName": "United States",
      "count": 1789,
      "latitude": 38.8951,
      "longitude": -77.0364,
      "percentage": 11.74
    },
    {
      "countryCode": "KE",
      "countryName": "Kenya",
      "count": 1234,
      "latitude": -1.2864,
      "longitude": 36.8172,
      "percentage": 8.10
    }
  ]
}
```

**TypeScript Interface:**
```typescript
interface GlobalGeoDistribution {
  type: 'reads' | 'downloads';
  timestamp: string;              // ISO-8601 format
  total: number;                  // Total across all countries
  countryCount: number;           // Number of unique countries
  countries: CountryMetric[];     // Array sorted by count (desc)
}

interface CountryMetric {
  countryCode: string;            // ISO 2-letter code (e.g., "TZ")
  countryName: string;            // Full name (e.g., "Tanzania")
  count: number;                  // Number of reads/downloads
  latitude: number;               // Capital city latitude
  longitude: number;              // Capital city longitude
  percentage: number;             // Percentage of total (0-100)
}
```

**Usage:**
```typescript
// Fetch global read distribution
this.http.get<GlobalGeoDistribution>('http://localhost:8080/api/v1/global/map/reads')
  .subscribe(data => {
    console.log('Total reads:', data.total);
    console.log('Countries:', data.countryCount);
    this.updateMap(data.countries);
  });
```

---

### **2. GET /api/v1/global/map/downloads**

**Purpose:** Get current global download distribution across ALL articles

**Method:** `GET`

**Headers:** None required

**Response Status:** `200 OK`

**Response Body:** Same structure as `/reads` endpoint, but `type: "downloads"`

```json
{
  "type": "downloads",
  "timestamp": "2026-02-15T01:00:00.456Z",
  "total": 8456,
  "countryCount": 62,
  "countries": [
    {
      "countryCode": "TZ",
      "countryName": "Tanzania",
      "count": 1234,
      "latitude": -6.7924,
      "longitude": 39.2083,
      "percentage": 14.59
    }
  ]
}
```

**TypeScript Interface:** Same as `/reads` endpoint (`GlobalGeoDistribution`)

**Usage:**
```typescript
// Fetch global download distribution
this.http.get<GlobalGeoDistribution>('http://localhost:8080/api/v1/global/map/downloads')
  .subscribe(data => {
    console.log('Total downloads:', data.total);
    this.updateMap(data.countries);
  });
```

---

### **3. GET /api/v1/global/map/combined**

**Purpose:** Get both reads AND downloads in a single request

**Method:** `GET`

**Headers:** None required

**Response Status:** `200 OK`

**Response Body:**
```json
{
  "reads": {
    "total": 15234,
    "countryCount": 87,
    "distribution": {
      "TZ": 2456,
      "US": 1789,
      "KE": 1234
    }
  },
  "downloads": {
    "total": 8456,
    "countryCount": 62,
    "distribution": {
      "TZ": 1234,
      "US": 987,
      "KE": 654
    }
  }
}
```

**Usage:**
```typescript
// Fetch both reads and downloads together
this.http.get('http://localhost:8080/api/v1/global/map/combined')
  .subscribe(data => {
    console.log('Reads:', data.reads.total);
    console.log('Downloads:', data.downloads.total);
  });
```

---

## 📡 WebSocket Topics

### **WebSocket Endpoint**
```
Development: ws://localhost:8080/ws
Production:  wss://your-domain.com/ws
```

### **Protocol:** STOMP over SockJS

---

### **1. /topic/reads/live** - Individual Read Events

**Frequency:** Real-time (every read event)

**Data Structure:**
```typescript
interface LiveEvent {
  eventType: 'READ';
  timestamp: string;              // ISO-8601: when event occurred
  articleId: number;
  articleTitle: string;
  doi: string;
  authorsJson: string;            // JSON string: ["Name1", "Name2"]
  sectionTitle: string;
  journalTitle: string;
  country: string;
  countryCode: string;
  city: string;
  continent: string;
  latitude: number;               // Exact location
  longitude: number;              // Exact location
  ip: string;
  userAgent: string;
}
```

**Example Message:**
```json
{
  "eventType": "READ",
  "timestamp": "2026-02-15T00:30:45Z",
  "articleId": 1542,
  "articleTitle": "Impact of Climate Change",
  "authorsJson": "[\"John Doe\",\"Jane Smith\"]",
  "country": "Tanzania",
  "countryCode": "TZ",
  "city": "Dar es Salaam",
  "latitude": -6.7924,
  "longitude": 39.2083
}
```

---

### **2. /topic/reads/geo** - Global Read Distribution Update

**Frequency:** After each read event (aggregated update)

**Data Structure:** Same as REST endpoint (`GlobalGeoDistribution`)

**Example Message:**
```json
{
  "type": "reads",
  "timestamp": "2026-02-15T01:00:15Z",
  "total": 15235,
  "countryCount": 87,
  "countries": [...]
}
```

---

### **3. /topic/downloads/live** - Individual Download Events

**Frequency:** Real-time (every download event)

**Data Structure:** Same as `LiveEvent` but with additional fields:

```typescript
interface LiveEvent {
  // ... all fields from reads
  eventType: 'DOWNLOAD';
  galleyLabel: string;            // "PDF", "HTML", etc.
  galleyMimeType: string;         // "application/pdf"
  galleyFileName: string;         // "1542-article.pdf"
}
```

---

### **4. /topic/downloads/geo** - Global Download Distribution Update

**Frequency:** After each download event

**Data Structure:** Same as REST endpoint (`GlobalGeoDistribution`) with `type: "downloads"`

---

## 🎬 Implementation Strategy

### **Step 1: Service Setup**

Create `src/app/services/analytics-data.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface GlobalGeoDistribution {
  type: 'reads' | 'downloads';
  timestamp: string;
  total: number;
  countryCount: number;
  countries: CountryMetric[];
}

export interface CountryMetric {
  countryCode: string;
  countryName: string;
  count: number;
  latitude: number;
  longitude: number;
  percentage: number;
}

@Injectable({
  providedIn: 'root'
})
export class AnalyticsDataService {
  
  // Cached data
  private readsData$ = new BehaviorSubject<GlobalGeoDistribution | null>(null);
  private downloadsData$ = new BehaviorSubject<GlobalGeoDistribution | null>(null);
  
  // Public observables
  public reads$ = this.readsData$.asObservable();
  public downloads$ = this.downloadsData$.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Load initial global read distribution via REST
   * Call this on component init or page refresh
   */
  loadGlobalReads(): Observable<GlobalGeoDistribution> {
    const url = `${environment.apiUrl}/global/map/reads`;
    console.log('📥 Loading global reads from REST:', url);
    
    return this.http.get<GlobalGeoDistribution>(url).pipe(
      tap(data => {
        console.log('✅ Loaded:', data.total, 'reads across', data.countryCount, 'countries');
        this.readsData$.next(data);
      })
    );
  }

  /**
   * Load initial global download distribution via REST
   * Call this on component init or page refresh
   */
  loadGlobalDownloads(): Observable<GlobalGeoDistribution> {
    const url = `${environment.apiUrl}/global/map/downloads`;
    console.log('📥 Loading global downloads from REST:', url);
    
    return this.http.get<GlobalGeoDistribution>(url).pipe(
      tap(data => {
        console.log('✅ Loaded:', data.total, 'downloads across', data.countryCount, 'countries');
        this.downloadsData$.next(data);
      })
    );
  }

  /**
   * Update data from WebSocket
   * Call this when receiving WebSocket messages
   */
  updateFromWebSocket(data: GlobalGeoDistribution): void {
    if (data.type === 'reads') {
      this.readsData$.next(data);
    } else if (data.type === 'downloads') {
      this.downloadsData$.next(data);
    }
  }
}
```

---

### **Step 2: Component Implementation**

Update `src/app/components/analytics-map/analytics-map.component.ts`:

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { AnalyticsDataService, GlobalGeoDistribution } from '../../services/analytics-data.service';
import { WebSocketService } from '../../services/websocket.service';

@Component({
  selector: 'app-analytics-map',
  templateUrl: './analytics-map.component.html',
  styleUrls: ['./analytics-map.component.css']
})
export class AnalyticsMapComponent implements OnInit, OnDestroy {
  
  // Current data
  totalReads = 0;
  totalDownloads = 0;
  countriesCount = 0;
  
  // View mode
  viewMode: 'reads' | 'downloads' = 'reads';
  
  // Subscriptions
  private subscriptions: Subscription[] = [];

  constructor(
    private analyticsData: AnalyticsDataService,
    private wsService: WebSocketService
  ) {}

  ngOnInit(): void {
    console.log('🚀 Component initializing...');
    
    // STEP 1: Load initial data via REST (synchronous)
    this.loadInitialData();
    
    // STEP 2: Connect to WebSocket for real-time updates
    this.connectWebSocket();
    
    // STEP 3: Handle reconnections
    this.handleReconnections();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.wsService.disconnect();
  }

  /**
   * STEP 1: Load initial data from REST API
   * This runs on page load/refresh to populate the map immediately
   */
  private loadInitialData(): void {
    console.log('📥 Loading initial data via REST API...');
    
    // Load reads
    this.analyticsData.loadGlobalReads().subscribe(
      data => {
        console.log('✅ Initial reads loaded:', data.total);
        this.updateMapWithReads(data);
      },
      error => console.error('❌ Failed to load reads:', error)
    );
    
    // Load downloads
    this.analyticsData.loadGlobalDownloads().subscribe(
      data => {
        console.log('✅ Initial downloads loaded:', data.total);
        this.updateMapWithDownloads(data);
      },
      error => console.error('❌ Failed to load downloads:', error)
    );
  }

  /**
   * STEP 2: Connect to WebSocket for real-time updates
   */
  private connectWebSocket(): void {
    console.log('🔌 Connecting to WebSocket...');
    this.wsService.connect();
    
    // Subscribe to connection status
    this.subscriptions.push(
      this.wsService.connected$.subscribe(connected => {
        console.log('WebSocket:', connected ? '🟢 Connected' : '🔴 Disconnected');
      })
    );
    
    // Subscribe to live read events
    this.subscriptions.push(
      this.wsService.readsLive$.subscribe(event => {
        if (event) {
          console.log('📖 Live read:', event.articleTitle, 'from', event.country);
          this.showLiveEvent(event);
        }
      })
    );
    
    // Subscribe to global read distribution updates
    this.subscriptions.push(
      this.wsService.readsGeo$.subscribe(geoData => {
        if (geoData) {
          console.log('📡 Global reads updated:', geoData.total);
          this.analyticsData.updateFromWebSocket(geoData);
          if (this.viewMode === 'reads') {
            this.updateMapWithReads(geoData);
          }
        }
      })
    );
    
    // Subscribe to live download events
    this.subscriptions.push(
      this.wsService.downloadsLive$.subscribe(event => {
        if (event) {
          console.log('⬇️ Live download:', event.galleyLabel, 'from', event.country);
          this.showLiveEvent(event);
        }
      })
    );
    
    // Subscribe to global download distribution updates
    this.subscriptions.push(
      this.wsService.downloadsGeo$.subscribe(geoData => {
        if (geoData) {
          console.log('📡 Global downloads updated:', geoData.total);
          this.analyticsData.updateFromWebSocket(geoData);
          if (this.viewMode === 'downloads') {
            this.updateMapWithDownloads(geoData);
          }
        }
      })
    );
  }

  /**
   * STEP 3: Handle WebSocket reconnections
   * When WebSocket reconnects, refresh data to ensure we have current state
   */
  private handleReconnections(): void {
    this.subscriptions.push(
      this.wsService.connected$.subscribe((connected, previousValue) => {
        // If we just reconnected (was false, now true)
        if (connected && previousValue === false) {
          console.log('🔄 WebSocket reconnected! Refreshing data...');
          this.loadInitialData();
        }
      })
    );
  }

  /**
   * Update map with read distribution
   */
  private updateMapWithReads(data: GlobalGeoDistribution): void {
    this.totalReads = data.total;
    this.countriesCount = data.countryCount;
    
    // Clear existing markers
    this.clearMapMarkers();
    
    // Add markers for each country
    data.countries.forEach(country => {
      this.addCountryMarker(country, 'reads');
    });
    
    console.log('🗺️ Map updated with', data.countries.length, 'read markers');
  }

  /**
   * Update map with download distribution
   */
  private updateMapWithDownloads(data: GlobalGeoDistribution): void {
    this.totalDownloads = data.total;
    this.countriesCount = data.countryCount;
    
    // Clear existing markers
    this.clearMapMarkers();
    
    // Add markers for each country
    data.countries.forEach(country => {
      this.addCountryMarker(country, 'downloads');
    });
    
    console.log('🗺️ Map updated with', data.countries.length, 'download markers');
  }

  /**
   * Show live event on map (temporary marker)
   */
  private showLiveEvent(event: any): void {
    // Add temporary marker that fades after 5 seconds
    // Implementation depends on your map library (Leaflet, Google Maps, etc.)
  }

  /**
   * Add country marker to map
   */
  private addCountryMarker(country: any, type: 'reads' | 'downloads'): void {
    // Implementation depends on your map library
  }

  /**
   * Clear all markers from map
   */
  private clearMapMarkers(): void {
    // Implementation depends on your map library
  }

  /**
   * Switch between reads and downloads view
   */
  switchView(mode: 'reads' | 'downloads'): void {
    this.viewMode = mode;
    
    if (mode === 'reads') {
      // Get current reads data
      this.subscriptions.push(
        this.analyticsData.reads$.subscribe(data => {
          if (data) this.updateMapWithReads(data);
        })
      );
    } else {
      // Get current downloads data
      this.subscriptions.push(
        this.analyticsData.downloads$.subscribe(data => {
          if (data) this.updateMapWithDownloads(data);
        })
      );
    }
  }
}
```

---

### **Step 3: Environment Configuration**

Update `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  wsEndpoint: 'http://localhost:8080/ws',
  apiUrl: 'http://localhost:8080/api/v1'
};
```

Update `src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  wsEndpoint: 'wss://your-production-domain.com/ws',
  apiUrl: 'https://your-production-domain.com/api/v1'
};
```

---

## 🧪 Testing the Implementation

### **Test 1: Initial Page Load**

1. Open browser to `http://localhost:4200`
2. Open browser console (F12)
3. Should see:
   ```
   🚀 Component initializing...
   📥 Loading initial data via REST API...
   ✅ Initial reads loaded: 15234
   ✅ Initial downloads loaded: 8456
   🗺️ Map updated with 87 read markers
   🔌 Connecting to WebSocket...
   WebSocket: 🟢 Connected
   ```
4. Map should show data immediately (no blank screen)

### **Test 2: Page Refresh**

1. With page open, press F5 or Ctrl+R
2. Watch console - should repeat Test 1 steps
3. Map should reload with current data immediately
4. Real-time updates should resume after WebSocket connects

### **Test 3: Real-Time Updates**

1. Send test event via cURL:
   ```bash
   curl -X POST http://localhost:8080/api/v1/events/ingest \
     -H "Content-Type: application/json" \
     -d '{"timestamp":"2026-02-15T01:00:00Z","galley":{"pubObjectId":"1542","assocType":1048585},"ipAddress":"8.8.8.8"}'
   ```
2. Console should show:
   ```
   📖 Live read: ... from United States
   📡 Global reads updated: 15235
   🗺️ Map updated with 87 read markers
   ```
3. Map should update in real-time

### **Test 4: Network Interruption**

1. Open DevTools → Network tab
2. Check "Offline" checkbox
3. Wait 5 seconds
4. Uncheck "Offline" checkbox
5. Console should show:
   ```
   WebSocket: 🔴 Disconnected
   WebSocket: 🟢 Connected
   🔄 WebSocket reconnected! Refreshing data...
   📥 Loading initial data via REST API...
   ```
6. Map should refresh with current data

---

## ✅ Success Criteria

- [ ] Map loads immediately on page load (via REST)
- [ ] Map updates in real-time (via WebSocket)
- [ ] Page refresh works correctly (REST reloads data)
- [ ] WebSocket reconnection triggers data refresh
- [ ] No blank screen at any time
- [ ] Console shows clear logging of data flow
- [ ] Statistics update correctly
- [ ] View switching (reads/downloads) works
- [ ] Network interruption handled gracefully

---

## 📊 Data Flow Summary

```
┌─────────────────────────────────────────┐
│  USER OPENS PAGE / REFRESHES           │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  Component ngOnInit() Executes          │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  STEP 1: REST API Calls                 │
│  GET /api/v1/global/map/reads           │
│  GET /api/v1/global/map/downloads       │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  Map Shows Current Data (Immediate!)    │
│  Total: 15,234 reads across 87 countries│
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  STEP 2: WebSocket Connection           │
│  ws://localhost:8080/ws                 │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  Subscribe to 4 Topics:                 │
│  - /topic/reads/live                    │
│  - /topic/reads/geo                     │
│  - /topic/downloads/live                │
│  - /topic/downloads/geo                 │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  Map Updates in Real-Time               │
│  New events arrive → Map refreshes      │
└─────────────────────────────────────────┘
```

---

## 🎯 Key Points

1. **Always use REST first** - Ensures immediate data on page load
2. **Then connect WebSocket** - For real-time updates
3. **On reconnect, refresh via REST** - Ensures current state
4. **Cache data in service** - Allows quick view switching
5. **Clear console logging** - For debugging and monitoring
6. **Handle errors gracefully** - Show user-friendly messages

---

## 📞 API Endpoints Quick Reference

| Endpoint | Method | Purpose | Returns |
|----------|--------|---------|---------|
| `/api/v1/global/map/reads` | GET | Current read distribution | `GlobalGeoDistribution` |
| `/api/v1/global/map/downloads` | GET | Current download distribution | `GlobalGeoDistribution` |
| `/api/v1/global/map/combined` | GET | Both reads & downloads | Combined object |

## WebSocket Topics Quick Reference

| Topic | Frequency | Purpose | Data Type |
|-------|-----------|---------|-----------|
| `/topic/reads/live` | Per event | Individual reads | `LiveEvent` |
| `/topic/reads/geo` | Per event | Global read distribution | `GlobalGeoDistribution` |
| `/topic/downloads/live` | Per event | Individual downloads | `LiveEvent` |
| `/topic/downloads/geo` | Per event | Global download distribution | `GlobalGeoDistribution` |

---

## 🚀 Implementation Priority

1. **HIGH**: REST API data loading (Step 1)
2. **HIGH**: WebSocket connection (Step 2)
3. **MEDIUM**: Reconnection handling (Step 3)
4. **MEDIUM**: View switching
5. **LOW**: Advanced features (caching, offline mode)

---

**Good luck implementing! This pattern ensures a robust, user-friendly real-time dashboard.** 🎉
