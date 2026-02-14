# Angular Real-Time Analytics Map - Implementation Guide

## 🎯 Objective

Implement a **real-time global analytics dashboard** for the OJS (Open Journal Systems) platform that displays live article reads and downloads on an interactive world map using WebSocket technology.

---

## 📡 Backend WebSocket Configuration

**WebSocket Endpoint:** `ws://localhost:8080/ws` (development)  
**Production Endpoint:** `wss://your-production-domain.com/ws`  
**Protocol:** STOMP over SockJS  
**Application Prefix:** `/app`  
**Topic Prefix:** `/topic`

---

## 🔌 WebSocket Topics (4 Total)

Your Angular app must subscribe to these **4 topics**:

### **1. `/topic/reads/live` - Individual Read Events**
- **When:** Real-time, every time someone reads an article
- **Data:** Complete event details with exact location

### **2. `/topic/reads/geo` - Global Read Distribution**
- **When:** After each read (aggregated update)
- **Data:** Total reads per country across ALL articles

### **3. `/topic/downloads/live` - Individual Download Events**
- **When:** Real-time, every time someone downloads an article
- **Data:** Complete event details with file information

### **4. `/topic/downloads/geo` - Global Download Distribution**
- **When:** After each download (aggregated update)
- **Data:** Total downloads per country across ALL articles

---

## 📦 Required NPM Packages

```bash
npm install @stomp/stompjs sockjs-client
npm install --save-dev @types/sockjs-client

# For map visualization (choose one):
npm install leaflet @types/leaflet           # Option 1: Leaflet
# OR
npm install @angular/google-maps             # Option 2: Google Maps
```

---

## 📋 TypeScript Interfaces

Create `src/app/models/analytics.model.ts`:

```typescript
/**
 * Individual event (reads or downloads)
 * Received on: /topic/reads/live or /topic/downloads/live
 */
export interface LiveEvent {
  eventType: 'READ' | 'DOWNLOAD';
  timestamp: string;                    // ISO-8601 format
  articleId: number;
  articleTitle: string;
  doi: string;
  authorsJson: string;                  // JSON string, parse with JSON.parse()
  sectionTitle: string;
  journalTitle: string;
  
  // Geographic data
  country: string;                      // "Tanzania"
  countryCode: string;                  // "TZ"
  city: string;                         // "Dar es Salaam"
  continent: string;                    // "Africa"
  latitude: number;                     // -6.7924
  longitude: number;                    // 39.2083
  
  // Technical data
  ip: string;
  userAgent: string;
  
  // Download-specific fields (only for DOWNLOAD events)
  galleyLabel?: string;                 // "PDF"
  galleyMimeType?: string;              // "application/pdf"
  galleyFileName?: string;              // "1542-article.pdf"
}

/**
 * Country-level metric for aggregated distribution
 */
export interface CountryMetric {
  countryCode: string;                  // "TZ"
  countryName: string;                  // "Tanzania"
  count: number;                        // 2456
  latitude: number;                     // -6.7924 (capital city)
  longitude: number;                    // 39.2083 (capital city)
  percentage: number;                   // 16.12
}

/**
 * Global geographical distribution
 * Received on: /topic/reads/geo or /topic/downloads/geo
 */
export interface GlobalGeoDistribution {
  type: 'reads' | 'downloads';
  total: number;                        // 15234
  countryCount: number;                 // 87
  countries: CountryMetric[];           // Sorted by count (descending)
}

/**
 * Parsed authors from authorsJson
 */
export interface Author {
  name: string;
}
```

---

## 🔧 WebSocket Service Implementation

Create `src/app/services/websocket.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { LiveEvent, GlobalGeoDistribution } from '../models/analytics.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private connected$ = new BehaviorSubject<boolean>(false);
  
  // Observables for real-time data
  private readsLiveSubject = new BehaviorSubject<LiveEvent | null>(null);
  private readsGeoSubject = new BehaviorSubject<GlobalGeoDistribution | null>(null);
  private downloadsLiveSubject = new BehaviorSubject<LiveEvent | null>(null);
  private downloadsGeoSubject = new BehaviorSubject<GlobalGeoDistribution | null>(null);
  
  // Public observables
  public readsLive$ = this.readsLiveSubject.asObservable();
  public readsGeo$ = this.readsGeoSubject.asObservable();
  public downloadsLive$ = this.downloadsLiveSubject.asObservable();
  public downloadsGeo$ = this.downloadsGeoSubject.asObservable();
  public connected$ = this.connected$.asObservable();

  constructor() {
    this.client = new Client();
    this.configureClient();
  }

  private configureClient(): void {
    // WebSocket endpoint configuration
    this.client.webSocketFactory = () => {
      return new SockJS(environment.wsEndpoint);
    };

    // Connection callbacks
    this.client.onConnect = (frame) => {
      console.log('✅ WebSocket Connected:', frame);
      this.connected$.next(true);
      this.subscribeToTopics();
    };

    this.client.onDisconnect = () => {
      console.log('❌ WebSocket Disconnected');
      this.connected$.next(false);
    };

    this.client.onStompError = (frame) => {
      console.error('⚠️ WebSocket Error:', frame);
      this.connected$.next(false);
    };

    // Reconnection settings
    this.client.reconnectDelay = 5000;        // 5 seconds
    this.client.heartbeatIncoming = 4000;
    this.client.heartbeatOutgoing = 4000;
  }

  /**
   * Connect to WebSocket server
   */
  public connect(): void {
    if (!this.client.active) {
      console.log('🔌 Connecting to WebSocket...');
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
   * Subscribe to all 4 topics
   */
  private subscribeToTopics(): void {
    console.log('📡 Subscribing to WebSocket topics...');
    
    // 1. Live read events
    this.client.subscribe('/topic/reads/live', (message) => {
      const data: LiveEvent = JSON.parse(message.body);
      console.log('📖 Live read event:', data);
      this.readsLiveSubject.next(data);
    });

    // 2. Global read distribution
    this.client.subscribe('/topic/reads/geo', (message) => {
      const data: GlobalGeoDistribution = JSON.parse(message.body);
      console.log('🗺️ Global read distribution:', data);
      this.readsGeoSubject.next(data);
    });

    // 3. Live download events
    this.client.subscribe('/topic/downloads/live', (message) => {
      const data: LiveEvent = JSON.parse(message.body);
      console.log('⬇️ Live download event:', data);
      this.downloadsLiveSubject.next(data);
    });

    // 4. Global download distribution
    this.client.subscribe('/topic/downloads/geo', (message) => {
      const data: GlobalGeoDistribution = JSON.parse(message.body);
      console.log('🗺️ Global download distribution:', data);
      this.downloadsGeoSubject.next(data);
    });
    
    console.log('✅ Subscribed to all 4 topics');
  }

  /**
   * Check if WebSocket is connected
   */
  public isConnected(): boolean {
    return this.client.active && this.connected$.value;
  }

  /**
   * Parse authors from authorsJson string
   */
  public parseAuthors(authorsJson: string): string[] {
    try {
      return JSON.parse(authorsJson);
    } catch (e) {
      return [];
    }
  }
}
```

---

## 🌍 Map Component Implementation

Create `src/app/components/analytics-map/analytics-map.component.ts`:

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { WebSocketService } from '../../services/websocket.service';
import { LiveEvent, GlobalGeoDistribution, CountryMetric } from '../../models/analytics.model';
import { Subscription } from 'rxjs';
import * as L from 'leaflet';

@Component({
  selector: 'app-analytics-map',
  templateUrl: './analytics-map.component.html',
  styleUrls: ['./analytics-map.component.css']
})
export class AnalyticsMapComponent implements OnInit, OnDestroy {
  
  // Leaflet map instance
  private map!: L.Map;
  private markersLayer!: L.LayerGroup;
  
  // Connection status
  isConnected = false;
  
  // Statistics
  totalReads = 0;
  totalDownloads = 0;
  countriesCount = 0;
  
  // Recent activity (last 10 events)
  recentActivity: LiveEvent[] = [];
  maxRecentActivity = 10;
  
  // Current view mode
  viewMode: 'reads' | 'downloads' = 'reads';
  
  // Subscriptions
  private subscriptions: Subscription[] = [];

  constructor(private wsService: WebSocketService) {}

  ngOnInit(): void {
    // Initialize map
    this.initMap();
    
    // Connect to WebSocket
    this.wsService.connect();
    
    // Monitor connection status
    this.subscriptions.push(
      this.wsService.connected$.subscribe(connected => {
        this.isConnected = connected;
        console.log('Connection status:', connected ? '🟢 Connected' : '🔴 Disconnected');
      })
    );
    
    // Subscribe to live read events
    this.subscriptions.push(
      this.wsService.readsLive$.subscribe(event => {
        if (event) {
          this.handleLiveReadEvent(event);
        }
      })
    );
    
    // Subscribe to global read distribution
    this.subscriptions.push(
      this.wsService.readsGeo$.subscribe(geoData => {
        if (geoData && this.viewMode === 'reads') {
          this.updateGlobalMap(geoData);
        }
      })
    );
    
    // Subscribe to live download events
    this.subscriptions.push(
      this.wsService.downloadsLive$.subscribe(event => {
        if (event) {
          this.handleLiveDownloadEvent(event);
        }
      })
    );
    
    // Subscribe to global download distribution
    this.subscriptions.push(
      this.wsService.downloadsGeo$.subscribe(geoData => {
        if (geoData && this.viewMode === 'downloads') {
          this.updateGlobalMap(geoData);
        }
      })
    );
  }

  ngOnDestroy(): void {
    // Unsubscribe from all observables
    this.subscriptions.forEach(sub => sub.unsubscribe());
    
    // Disconnect WebSocket
    this.wsService.disconnect();
    
    // Destroy map
    if (this.map) {
      this.map.remove();
    }
  }

  /**
   * Initialize Leaflet map
   */
  private initMap(): void {
    // Create map centered on Africa (good default for global view)
    this.map = L.map('map').setView([0, 20], 2);
    
    // Add OpenStreetMap tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 18
    }).addTo(this.map);
    
    // Create layer group for markers
    this.markersLayer = L.layerGroup().addTo(this.map);
  }

  /**
   * Handle live read event
   */
  private handleLiveReadEvent(event: LiveEvent): void {
    console.log('📖 New read:', event.articleTitle, 'from', event.city, event.country);
    
    // Add to recent activity
    this.recentActivity.unshift(event);
    this.recentActivity = this.recentActivity.slice(0, this.maxRecentActivity);
    
    // Add temporary marker for live event (fades after 5 seconds)
    this.addLiveEventMarker(event.latitude, event.longitude, 'read', event);
    
    // Show notification
    this.showNotification(`📖 ${event.articleTitle} read from ${event.city}, ${event.country}`);
  }

  /**
   * Handle live download event
   */
  private handleLiveDownloadEvent(event: LiveEvent): void {
    console.log('⬇️ New download:', event.galleyLabel, 'from', event.city, event.country);
    
    // Add to recent activity
    this.recentActivity.unshift(event);
    this.recentActivity = this.recentActivity.slice(0, this.maxRecentActivity);
    
    // Add temporary marker
    this.addLiveEventMarker(event.latitude, event.longitude, 'download', event);
    
    // Show notification
    this.showNotification(`⬇️ ${event.galleyLabel} downloaded from ${event.city}, ${event.country}`);
  }

  /**
   * Update global map with country-level data
   */
  private updateGlobalMap(geoData: GlobalGeoDistribution): void {
    // Update statistics
    if (geoData.type === 'reads') {
      this.totalReads = geoData.total;
    } else {
      this.totalDownloads = geoData.total;
    }
    this.countriesCount = geoData.countryCount;
    
    console.log(`🗺️ Updating global ${geoData.type} map:`, geoData.total, 'across', geoData.countryCount, 'countries');
    
    // Clear existing markers
    this.markersLayer.clearLayers();
    
    // Add marker for each country
    geoData.countries.forEach(country => {
      this.addCountryMarker(country, geoData.type);
    });
  }

  /**
   * Add country marker to map
   */
  private addCountryMarker(country: CountryMetric, type: 'reads' | 'downloads'): void {
    // Calculate marker size based on count (logarithmic scale)
    const baseSize = 10;
    const size = baseSize + Math.log(country.count + 1) * 5;
    
    // Color based on type
    const color = type === 'reads' ? '#3b82f6' : '#10b981'; // Blue for reads, Green for downloads
    
    // Create circle marker
    const marker = L.circleMarker([country.latitude, country.longitude], {
      radius: size,
      fillColor: color,
      color: '#fff',
      weight: 2,
      opacity: 1,
      fillOpacity: 0.6
    });
    
    // Add popup
    const popupContent = `
      <div style="text-align: center;">
        <h3 style="margin: 0 0 8px 0;">${country.countryName}</h3>
        <p style="margin: 4px 0;"><strong>${country.count.toLocaleString()}</strong> ${type}</p>
        <p style="margin: 4px 0; color: #666;">${country.percentage}% of total</p>
      </div>
    `;
    
    marker.bindPopup(popupContent);
    
    // Add to markers layer
    marker.addTo(this.markersLayer);
  }

  /**
   * Add temporary marker for live events (fades after 5 seconds)
   */
  private addLiveEventMarker(lat: number, lng: number, type: 'read' | 'download', event: LiveEvent): void {
    const color = type === 'read' ? '#ef4444' : '#f59e0b'; // Red for reads, Orange for downloads
    
    const marker = L.circleMarker([lat, lng], {
      radius: 8,
      fillColor: color,
      color: '#fff',
      weight: 2,
      opacity: 1,
      fillOpacity: 0.8
    });
    
    // Parse authors
    const authors = this.wsService.parseAuthors(event.authorsJson || '[]');
    
    const popupContent = `
      <div style="min-width: 200px;">
        <h4 style="margin: 0 0 8px 0;">${event.articleTitle}</h4>
        <p style="margin: 4px 0;"><strong>Authors:</strong> ${authors.join(', ')}</p>
        <p style="margin: 4px 0;"><strong>Location:</strong> ${event.city}, ${event.country}</p>
        <p style="margin: 4px 0;"><strong>Time:</strong> ${new Date(event.timestamp).toLocaleString()}</p>
        ${event.galleyLabel ? `<p style="margin: 4px 0;"><strong>File:</strong> ${event.galleyLabel}</p>` : ''}
      </div>
    `;
    
    marker.bindPopup(popupContent);
    marker.addTo(this.markersLayer);
    
    // Animate marker (pulse effect)
    marker.setStyle({ fillOpacity: 1 });
    
    // Remove after 5 seconds
    setTimeout(() => {
      this.markersLayer.removeLayer(marker);
    }, 5000);
  }

  /**
   * Show browser notification
   */
  private showNotification(message: string): void {
    // You can implement this with Angular Material Snackbar or custom toast
    console.log('🔔', message);
  }

  /**
   * Switch between reads and downloads view
   */
  public switchView(mode: 'reads' | 'downloads'): void {
    this.viewMode = mode;
    console.log('Switched to', mode, 'view');
    // The map will automatically update via the subscription
  }

  /**
   * Get authors from event
   */
  public getAuthors(event: LiveEvent): string[] {
    return this.wsService.parseAuthors(event.authorsJson || '[]');
  }

  /**
   * Format time ago
   */
  public timeAgo(timestamp: string): string {
    const seconds = Math.floor((new Date().getTime() - new Date(timestamp).getTime()) / 1000);
    
    if (seconds < 60) return `${seconds} seconds ago`;
    if (seconds < 3600) return `${Math.floor(seconds / 60)} minutes ago`;
    if (seconds < 86400) return `${Math.floor(seconds / 3600)} hours ago`;
    return `${Math.floor(seconds / 86400)} days ago`;
  }
}
```

---

## 🎨 Component Template

Create `src/app/components/analytics-map/analytics-map.component.html`:

```html
<div class="analytics-container">
  
  <!-- Header -->
  <div class="header">
    <h1>🌍 Global Analytics Dashboard</h1>
    <div class="connection-status" [class.connected]="isConnected">
      <span *ngIf="isConnected">🟢 Live</span>
      <span *ngIf="!isConnected">🔴 Disconnected</span>
    </div>
  </div>

  <!-- Statistics Panel -->
  <div class="stats-panel">
    <div class="stat-card reads-card" [class.active]="viewMode === 'reads'" (click)="switchView('reads')">
      <div class="stat-icon">📖</div>
      <div class="stat-content">
        <h3>Total Reads</h3>
        <p class="stat-value">{{ totalReads | number }}</p>
      </div>
    </div>
    
    <div class="stat-card downloads-card" [class.active]="viewMode === 'downloads'" (click)="switchView('downloads')">
      <div class="stat-icon">⬇️</div>
      <div class="stat-content">
        <h3>Total Downloads</h3>
        <p class="stat-value">{{ totalDownloads | number }}</p>
      </div>
    </div>
    
    <div class="stat-card">
      <div class="stat-icon">🌍</div>
      <div class="stat-content">
        <h3>Countries</h3>
        <p class="stat-value">{{ countriesCount }}</p>
      </div>
    </div>
  </div>

  <!-- Main Content Area -->
  <div class="main-content">
    
    <!-- Map Container -->
    <div class="map-container">
      <div id="map" style="height: 600px; width: 100%;"></div>
      
      <div class="map-legend">
        <h4>Legend</h4>
        <div class="legend-item">
          <span class="legend-dot" style="background: #3b82f6;"></span>
          <span>Reads</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background: #10b981;"></span>
          <span>Downloads</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background: #ef4444;"></span>
          <span>Live Read (5s)</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background: #f59e0b;"></span>
          <span>Live Download (5s)</span>
        </div>
      </div>
    </div>

    <!-- Activity Feed -->
    <div class="activity-feed">
      <h3>🔴 Live Activity</h3>
      
      <div *ngIf="recentActivity.length === 0" class="no-activity">
        <p>Waiting for events...</p>
      </div>
      
      <div class="activity-list">
        <div *ngFor="let event of recentActivity" class="activity-item" [class.read-event]="event.eventType === 'READ'" [class.download-event]="event.eventType === 'DOWNLOAD'">
          <div class="activity-icon">
            {{ event.eventType === 'READ' ? '📖' : '⬇️' }}
          </div>
          <div class="activity-content">
            <h4>{{ event.articleTitle }}</h4>
            <p class="activity-authors">{{ getAuthors(event).join(', ') }}</p>
            <p class="activity-location">
              📍 {{ event.city }}, {{ event.country }}
            </p>
            <p class="activity-time">{{ timeAgo(event.timestamp) }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
```

---

## 🎨 Component Styles

Create `src/app/components/analytics-map/analytics-map.component.css`:

```css
.analytics-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
  font-size: 28px;
}

.connection-status {
  padding: 8px 16px;
  border-radius: 20px;
  background: #fee2e2;
  color: #991b1b;
  font-weight: 600;
  transition: all 0.3s;
}

.connection-status.connected {
  background: #dcfce7;
  color: #166534;
}

/* Statistics Panel */
.stats-panel {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.stat-card.active {
  border-color: #3b82f6;
  background: #eff6ff;
}

.stat-icon {
  font-size: 36px;
}

.stat-content h3 {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.stat-value {
  margin: 4px 0 0 0;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

/* Main Content */
.main-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 20px;
}

/* Map Container */
.map-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: relative;
}

.map-legend {
  position: absolute;
  bottom: 20px;
  right: 20px;
  background: white;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  z-index: 1000;
}

.map-legend h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 4px 0;
  font-size: 12px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
}

/* Activity Feed */
.activity-feed {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  max-height: 600px;
  overflow-y: auto;
}

.activity-feed h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
}

.no-activity {
  text-align: center;
  color: #9ca3af;
  padding: 40px 0;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f9fafb;
  border-left: 4px solid #3b82f6;
  animation: slideIn 0.3s ease-out;
}

.activity-item.download-event {
  border-left-color: #10b981;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.activity-icon {
  font-size: 24px;
}

.activity-content h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
}

.activity-authors {
  margin: 0 0 4px 0;
  font-size: 12px;
  color: #6b7280;
}

.activity-location {
  margin: 0 0 4px 0;
  font-size: 12px;
  color: #6b7280;
}

.activity-time {
  margin: 0;
  font-size: 11px;
  color: #9ca3af;
}

/* Responsive */
@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}
```

---

## ⚙️ Environment Configuration

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

### **Step 1: Start Backend**
```bash
cd /path/to/backend
./gradlew bootRun
```

### **Step 2: Start Angular App**
```bash
ng serve
```

### **Step 3: Open Browser**
Navigate to `http://localhost:4200`

### **Step 4: Check Connection**
You should see in browser console:
```
🔌 Connecting to WebSocket...
✅ WebSocket Connected
📡 Subscribing to WebSocket topics...
✅ Subscribed to all 4 topics
```

### **Step 5: Send Test Events**

Use cURL or Postman to send test events:

```bash
# Test READ event from USA
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "galley": {"pubObjectId": "1542", "assocType": 1048585},
    "ipAddress": "8.8.8.8"
  }'

# Test READ event from Tanzania
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "galley": {"pubObjectId": "1543", "assocType": 1048585},
    "ipAddress": "196.216.128.1"
  }'

# Test DOWNLOAD event
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "galley": {"pubObjectId": "1544", "assocType": 515},
    "ipAddress": "105.112.45.89"
  }'
```

### **Step 6: Verify Frontend Updates**

You should see:
1. ✅ Console logs showing received events
2. ✅ Map markers appearing for countries
3. ✅ Live events appearing (red/orange dots)
4. ✅ Activity feed updating
5. ✅ Statistics incrementing
6. ✅ Markers fading after 5 seconds

---

## 🎯 Success Criteria

- [ ] WebSocket connects successfully to backend
- [ ] All 4 topics receive data correctly
- [ ] Map displays country markers with correct size/color
- [ ] Live events show temporary markers (fade after 5 seconds)
- [ ] Activity feed displays recent events
- [ ] Statistics update in real-time
- [ ] Switching between reads/downloads updates map
- [ ] Authors are parsed and displayed correctly
- [ ] Connection status indicator works
- [ ] No console errors during operation

---

## 🐛 Troubleshooting

### **Issue: WebSocket Connection Fails**

**Solution:**
1. Verify backend is running: `curl http://localhost:8080/api/v1/global/health`
2. Check CORS configuration in backend
3. Verify WebSocket endpoint URL in `environment.ts`
4. Check browser console for detailed error messages

### **Issue: Not Receiving Messages**

**Solution:**
1. Verify subscriptions in browser console (should see "✅ Subscribed to all 4 topics")
2. Send test event and check backend logs
3. Verify topic names match exactly (case-sensitive)
4. Check if WebSocket is connected: `isConnected` should be `true`

### **Issue: Map Not Displaying**

**Solution:**
1. Ensure Leaflet CSS is imported in `angular.json`:
   ```json
   "styles": [
     "node_modules/leaflet/dist/leaflet.css",
     "src/styles.css"
   ]
   ```
2. Check div `id="map"` exists in template
3. Verify `initMap()` is called in `ngOnInit()`

### **Issue: Markers Not Showing**

**Solution:**
1. Check lat/lng values in console logs
2. Verify `markersLayer` is added to map
3. Check if coordinates are within map bounds
4. Try zooming out to see if markers are outside viewport

---

## 📚 Additional Features (Optional)

### **1. Clustering for High Density**

Install marker clustering:
```bash
npm install leaflet.markercluster @types/leaflet.markercluster
```

### **2. Heat Map Visualization**

Install heat map plugin:
```bash
npm install leaflet.heat
```

### **3. Country Highlighting**

Use GeoJSON to fill countries:
```bash
npm install @types/geojson
```

### **4. Export/Download Data**

Add buttons to export data as CSV/JSON.

### **5. Date Range Filtering**

Add date pickers to filter historical data (using REST endpoints).

---

## 🚀 Performance Optimization

1. **Debounce map updates** if receiving high-frequency events
2. **Limit activity feed** to 10-20 items (already implemented)
3. **Use OnPush change detection** for components
4. **Lazy load map library** for faster initial load
5. **Implement virtual scrolling** for large activity feeds

---

## 📦 Deliverables

Please provide:
1. ✅ Complete WebSocket service implementation
2. ✅ Analytics map component (TS, HTML, CSS)
3. ✅ TypeScript interfaces for all data types
4. ✅ Environment configuration
5. ✅ Screenshots of working map with live data
6. ✅ Brief README with setup instructions

---

## ⏱️ Estimated Implementation Time

- **WebSocket Service:** 1-2 hours
- **Map Component:** 2-3 hours
- **Styling & Polish:** 1-2 hours
- **Testing & Debugging:** 1-2 hours

**Total: 5-9 hours**

---

## 🎨 Design Notes

- Use **vibrant colors** for different event types
- Implement **smooth animations** for live markers
- Add **tooltips/popovers** with article details
- Use **responsive design** for mobile/tablet
- Consider **dark mode** support
- Add **loading states** while connecting

---

## 📞 Support

If you encounter issues:
1. Check backend logs: `./gradlew bootRun --console=plain`
2. Check browser console for frontend errors
3. Verify network tab shows WebSocket connection (Status 101)
4. Test WebSocket with online tool: https://websocketking.com/

**Backend WebSocket Endpoint:** `ws://localhost:8080/ws`  
**Backend REST API:** `http://localhost:8080/api/v1/global/map/reads`  
**Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

---

**Good luck! Build an amazing real-time analytics dashboard! 🚀🌍**
