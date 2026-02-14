package org.udsm.udsm_hackathon2026.service;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

@Service
@Slf4j
public class GeoIPService {

    @Value("${app.geoip.database-path}")
    private String databasePath;
    
    private final ResourceLoader resourceLoader;

    private DatabaseReader reader;
    
    public GeoIPService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            // Try to load from classpath first
            Resource resource = resourceLoader.getResource("classpath:geoip/GeoLite2-City.mmdb");
            
            if (resource.exists()) {
                log.info("Loading GeoIP database from classpath: geoip/GeoLite2-City.mmdb");
                InputStream inputStream = resource.getInputStream();
                reader = new DatabaseReader.Builder(inputStream).build();
                log.info(" GeoIP database loaded successfully from classpath!");
            } else {
                // Fallback to file path
                File database = new File(databasePath);
                if (database.exists()) {
                    log.info("Loading GeoIP database from file: {}", databasePath);
                    reader = new DatabaseReader.Builder(database).build();
                    log.info(" GeoIP database loaded successfully from file: {}", databasePath);
                } else {
                    log.error(" GeoIP database not found!");
                    log.error("   - Classpath resource: geoip/GeoLite2-City.mmdb (not found)");
                    log.error("   - File path: {} (not found)", databasePath);
                    log.warn("  Geo enrichment will return 'Unknown' for all locations.");
                    log.warn("   Download GeoLite2-City.mmdb from MaxMind and place in src/main/resources/geoip/");
                }
            }
        } catch (IOException e) {
            log.error(" Failed to load GeoIP database: {}", e.getMessage());
            log.warn("  Geo enrichment will return 'Unknown' for all locations.");
        }
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (reader != null) {
            reader.close();
            log.info("GeoIP database reader closed");
        }
    }

    /**
     * Resolve IP address to geographical information.
     * Handles private/unknown IPs gracefully without throwing exceptions.
     *
     * @param ipAddress IP address to resolve
     * @return GeoLocation object with country, city, lat/lng, etc.
     */
    public GeoLocation resolveIP(String ipAddress) {
        if (reader == null) {
            log.debug("GeoIP reader not available, returning unknown location for IP: {}", ipAddress);
            return createUnknownLocation();
        }
        
        if (ipAddress == null || ipAddress.isEmpty()) {
            log.debug("Empty IP address provided");
            return createUnknownLocation();
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            
            // Check if IP is private/local
            if (inetAddress.isSiteLocalAddress() || inetAddress.isLoopbackAddress()) {
                log.debug("Private/local IP detected: {} - returning unknown location", ipAddress);
                return createUnknownLocation();
            }

            CityResponse response = reader.city(inetAddress);
            
            String country = response.getCountry().getName();
            String countryCode = response.getCountry().getIsoCode();
            String city = response.getCity().getName();
            String continent = response.getContinent().getName();
            Double latitude = response.getLocation().getLatitude();
            Double longitude = response.getLocation().getLongitude();
            String region = response.getMostSpecificSubdivision().getIsoCode();
            
            log.debug(" Resolved IP {} -> {}, {} ({}, {})", 
                     ipAddress, city, country, latitude, longitude);

            return GeoLocation.builder()
                    .country(country != null ? country : "Unknown")
                    .countryCode(countryCode != null ? countryCode : "XX")
                    .city(city != null ? city : "Unknown")
                    .continent(continent != null ? continent : "Unknown")
                    .latitude(latitude != null ? latitude : 0.0)
                    .longitude(longitude != null ? longitude : 0.0)
                    .region(region)
                    .build();

        } catch (GeoIp2Exception e) {
            log.debug("IP {} not found in GeoIP database: {}", ipAddress, e.getMessage());
            return createUnknownLocation();
        } catch (IOException e) {
            log.error("Error reading GeoIP database for IP {}: {}", ipAddress, e.getMessage());
            return createUnknownLocation();
        } catch (Exception e) {
            log.error("Unexpected error resolving IP {}: {}", ipAddress, e.getMessage(), e);
            return createUnknownLocation();
        }
    }

    private GeoLocation createUnknownLocation() {
        return GeoLocation.builder()
                .country("Unknown")
                .countryCode("XX")
                .city(null)
                .continent("Unknown")
                .latitude(0.0)
                .longitude(0.0)
                .region(null)
                .build();
    }

    /**
     * Inner class to hold geo location data
     */
    @lombok.Getter
    @lombok.Setter
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GeoLocation {
        private String country;
        private String countryCode;
        private String city;
        private String continent;
        private Double latitude;
        private Double longitude;
        private String region;
    }
}
