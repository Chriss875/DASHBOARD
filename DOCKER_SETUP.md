# Docker Setup Guide - OJS Analytics Platform

This guide explains how to build and run the OJS Analytics Platform using Docker and Docker Compose.

## Prerequisites

- Docker Engine 20.10+ installed
- Docker Compose 2.0+ installed
- At least 4GB of available RAM
- At least 10GB of available disk space

## Quick Start

### 1. Clone the Repository (if pulling from remote)

```bash
git clone <your-repository-url>
cd UDSM_HACKATHON2026
```

### 2. Start All Services

```bash
docker-compose up -d
```

This command will:
- Build the Spring Boot application
- Start MariaDB database
- Start Redis cache
- Start the application with all dependencies

### 3. Check Service Status

```bash
docker-compose ps
```

All services should show as "Up" or "healthy".

### 4. View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f app
docker-compose logs -f mariadb
docker-compose logs -f redis
```

### 5. Access the Application

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/actuator/health
- **WebSocket Endpoint**: ws://localhost:8080/ws

## Architecture

The Docker setup includes three main services:

### 1. MariaDB (Database)
- **Image**: mariadb:10.4.28
- **Port**: 3306
- **Database**: tjpsd32
- **Default Credentials**: 
  - Root Password: rootpassword
  - User: ojsuser
  - Password: ojspassword

### 2. Redis (Cache)
- **Image**: redis:7-alpine
- **Port**: 6379
- **Persistence**: Data is stored in Docker volume

### 3. Spring Boot Application
- **Build**: Multi-stage Docker build using Gradle
- **Port**: 8080
- **Dependencies**: Waits for MariaDB and Redis to be healthy before starting

## Configuration

### Environment Variables

You can customize the application by modifying environment variables in `compose.yaml`:

```yaml
environment:
  # Database
  SPRING_DATASOURCE_URL: jdbc:mariadb://mariadb:3306/tjpsd32
  SPRING_DATASOURCE_USERNAME: root
  SPRING_DATASOURCE_PASSWORD: rootpassword
  
  # Redis
  SPRING_DATA_REDIS_HOST: redis
  SPRING_DATA_REDIS_PORT: 6379
  
  # CORS (add your frontend URL here)
  SPRING_WEB_CORS_ALLOWED_ORIGINS: http://localhost:4200,http://localhost:3000
  
  # API Key
  APP_INGESTION_API_KEY: ur04O0u7OXSkNwjPJ0mNLFOnrMMCPX1CwkpjhyHoXeQ=
```

### Frontend Integration

To connect your frontend (running on port 4200) to the Dockerized backend:

1. **Update CORS origins** in `compose.yaml`:
   ```yaml
   SPRING_WEB_CORS_ALLOWED_ORIGINS: http://localhost:4200,http://frontend-host:4200
   ```

2. **Set API URL** in your frontend:
   ```typescript
   const API_BASE_URL = 'http://localhost:8080';
   ```

3. **WebSocket Connection**:
   ```typescript
   const socket = new SockJS('http://localhost:8080/ws');
   const stompClient = Stomp.over(socket);
   ```

## GeoIP Database Setup

The application requires MaxMind GeoLite2-City database for IP geolocation:

1. Download GeoLite2-City.mmdb from [MaxMind](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data)

2. Place it in:
   ```
   src/main/resources/geoip/GeoLite2-City.mmdb
   ```

3. The Docker volume will mount this file into the container automatically

See `GEOIP_SETUP_GUIDE.md` for detailed instructions.

## Common Commands

### Build and Start Services

```bash
# Build and start in detached mode
docker-compose up -d --build

# Start without rebuilding
docker-compose up -d

# Start with logs visible
docker-compose up
```

### Stop Services

```bash
# Stop all services
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove containers + volumes (WARNING: deletes data)
docker-compose down -v
```

### Rebuild Application

```bash
# Rebuild only the app service
docker-compose build app

# Rebuild and restart
docker-compose up -d --build app
```

### Access Containers

```bash
# Access application container
docker exec -it ojs-analytics-app sh

# Access MariaDB
docker exec -it ojs-analytics-db mysql -u root -prootpassword tjpsd32

# Access Redis
docker exec -it ojs-analytics-redis redis-cli
```

### Database Management

```bash
# Create database backup
docker exec ojs-analytics-db mysqldump -u root -prootpassword tjpsd32 > backup.sql

# Restore database
docker exec -i ojs-analytics-db mysql -u root -prootpassword tjpsd32 < backup.sql

# Import existing database
docker cp your-database.sql ojs-analytics-db:/tmp/
docker exec ojs-analytics-db mysql -u root -prootpassword tjpsd32 < /tmp/your-database.sql
```

## Networking

All services are connected via the `ojs-network` bridge network:
- Services can communicate using service names (e.g., `mariadb`, `redis`, `app`)
- Ports are exposed to the host machine for external access

## Persistent Data

Data is stored in Docker volumes:
- `mariadb_data`: Database files
- `redis_data`: Redis cache data

These volumes persist even when containers are stopped or removed.

## Troubleshooting

### Service Won't Start

```bash
# Check logs
docker-compose logs app

# Check service health
docker-compose ps

# Restart specific service
docker-compose restart app
```

### Database Connection Issues

```bash
# Verify MariaDB is running
docker-compose ps mariadb

# Check MariaDB logs
docker-compose logs mariadb

# Test database connection
docker exec -it ojs-analytics-db mysql -u root -prootpassword -e "SELECT 1"
```

### Application Build Fails

```bash
# Clean build
docker-compose build --no-cache app

# Check Gradle logs
docker-compose logs app | grep -i error
```

### CORS Issues

1. Check CORS configuration in `compose.yaml`
2. Ensure your frontend URL is in `SPRING_WEB_CORS_ALLOWED_ORIGINS`
3. Restart the application after changes:
   ```bash
   docker-compose restart app
   ```

### Port Conflicts

If ports 8080, 3306, or 6379 are already in use:

1. Edit `compose.yaml` to change port mappings:
   ```yaml
   ports:
     - "8081:8080"  # Map host port 8081 to container port 8080
   ```

2. Restart services:
   ```bash
   docker-compose down
   docker-compose up -d
   ```

## Production Deployment

For production deployment:

1. **Change default passwords** in `compose.yaml`
2. **Update CORS origins** to include production domains
3. **Use environment variables** instead of hardcoded values
4. **Enable HTTPS** using a reverse proxy (nginx, traefik)
5. **Configure backup strategy** for database volumes
6. **Set up monitoring** and logging aggregation
7. **Use secrets management** for sensitive data

### Example with Docker Secrets

```yaml
services:
  app:
    environment:
      SPRING_DATASOURCE_PASSWORD_FILE: /run/secrets/db_password
    secrets:
      - db_password

secrets:
  db_password:
    external: true
```

## API Endpoints

Once running, the following endpoints are available:

### Event Ingestion
- `POST /api/v1/events/ingest` - Ingest analytics events
  - Headers: `X-API-Key: ur04O0u7OXSkNwjPJ0mNLFOnrMMCPX1CwkpjhyHoXeQ=`

### Analytics
- `GET /api/v1/analytics/reads?articleId={id}&from={date}&to={date}`
- `GET /api/v1/analytics/downloads?articleId={id}&from={date}&to={date}`
- `GET /api/v1/analytics/stats`

### WebSocket Topics
- `/topic/reads` - Real-time read events
- `/topic/downloads` - Real-time download events
- `/topic/stats` - Real-time statistics updates

## Support

For more information, see:
- `QUICK_START.md` - Application quick start guide
- `REALTIME_INTEGRATION_GUIDE.md` - WebSocket integration
- `GEOIP_SETUP_GUIDE.md` - GeoIP database setup

## Cleanup

To completely remove all containers, volumes, and images:

```bash
# Stop and remove everything
docker-compose down -v

# Remove the application image
docker rmi udsm_hackathon2026-app

# Prune unused Docker resources
docker system prune -a --volumes
```

---

**Note**: Remember to restart the services after making configuration changes:
```bash
docker-compose restart app
```
