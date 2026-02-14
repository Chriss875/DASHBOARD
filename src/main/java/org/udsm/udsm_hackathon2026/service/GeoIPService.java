package org.udsm.udsm_hackathon2026.service;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

@Service
@Slf4j
public class GeoIPService {

    @Value("${app.geoip.database-path}")
    private String databasePath;

    private DatabaseReader reader;

    @PostConstruct
    public void init() throws IOException {
        File database = new File(databasePath);
        if (!database.exists()) {
            log.warn("GeoIP database not found at: {}. Geo enrichment will use defaults.", databasePath);
            return;
        }
        reader = new DatabaseReader.Builder(database).build();
        log.info("GeoIP database loaded successfully from: {}", databasePath);
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (reader != null) {
            reader.close();
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
        if (reader == null || ipAddress == null || ipAddress.isEmpty()) {
            return createUnknownLocation();
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            
            // Check if IP is private/local
            if (inetAddress.isSiteLocalAddress() || inetAddress.isLoopbackAddress()) {
                log.debug("Private/local IP detected: {}", ipAddress);
                return createUnknownLocation();
            }

            CityResponse response = reader.city(inetAddress);

            return GeoLocation.builder()
                    .country(response.getCountry().getName() != null ? response.getCountry().getName() : "Unknown")
                    .countryCode(response.getCountry().getIsoCode() != null ? response.getCountry().getIsoCode() : "XX")
                    .city(response.getCity().getName() != null ? response.getCity().getName() : "Unknown")
                    .continent(response.getContinent().getName() != null ? response.getContinent().getName() : "Unknown")
                    .latitude(response.getLocation().getLatitude() != null ? response.getLocation().getLatitude() : 0.0)
                    .longitude(response.getLocation().getLongitude() != null ? response.getLocation().getLongitude() : 0.0)
                    .region(response.getMostSpecificSubdivision().getIsoCode())
                    .build();

        } catch (GeoIp2Exception | IOException e) {
            log.debug("Could not resolve IP {}: {}", ipAddress, e.getMessage());
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
