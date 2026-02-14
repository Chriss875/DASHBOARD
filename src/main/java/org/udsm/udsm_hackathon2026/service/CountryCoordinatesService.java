package org.udsm.udsm_hackathon2026.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to provide country coordinates (capital cities or centroids)
 * Used for displaying markers on global maps
 */
@Service
public class CountryCoordinatesService {
    
    private static final Map<String, CountryCoords> COUNTRY_COORDINATES = new HashMap<>();
    
    static {
        // Major countries (add more as needed)
        COUNTRY_COORDINATES.put("US", new CountryCoords("United States", 38.8951, -77.0364));
        COUNTRY_COORDINATES.put("GB", new CountryCoords("United Kingdom", 51.5074, -0.1278));
        COUNTRY_COORDINATES.put("CA", new CountryCoords("Canada", 45.4215, -75.6972));
        COUNTRY_COORDINATES.put("AU", new CountryCoords("Australia", -35.2809, 149.1300));
        COUNTRY_COORDINATES.put("DE", new CountryCoords("Germany", 52.5200, 13.4050));
        COUNTRY_COORDINATES.put("FR", new CountryCoords("France", 48.8566, 2.3522));
        COUNTRY_COORDINATES.put("JP", new CountryCoords("Japan", 35.6762, 139.6503));
        COUNTRY_COORDINATES.put("CN", new CountryCoords("China", 39.9042, 116.4074));
        COUNTRY_COORDINATES.put("IN", new CountryCoords("India", 28.6139, 77.2090));
        COUNTRY_COORDINATES.put("BR", new CountryCoords("Brazil", -15.8267, -47.9218));
        
        // East African countries
        COUNTRY_COORDINATES.put("TZ", new CountryCoords("Tanzania", -6.7924, 39.2083));
        COUNTRY_COORDINATES.put("KE", new CountryCoords("Kenya", -1.2864, 36.8172));
        COUNTRY_COORDINATES.put("UG", new CountryCoords("Uganda", 0.3476, 32.5825));
        COUNTRY_COORDINATES.put("RW", new CountryCoords("Rwanda", -1.9403, 29.8739));
        COUNTRY_COORDINATES.put("BI", new CountryCoords("Burundi", -3.3731, 29.9189));
        COUNTRY_COORDINATES.put("ET", new CountryCoords("Ethiopia", 9.0320, 38.7469));
        COUNTRY_COORDINATES.put("SO", new CountryCoords("Somalia", 2.0469, 45.3182));
        COUNTRY_COORDINATES.put("SD", new CountryCoords("Sudan", 15.5007, 32.5599));
        COUNTRY_COORDINATES.put("SS", new CountryCoords("South Sudan", 4.8517, 31.5825));
        
        // Southern African countries
        COUNTRY_COORDINATES.put("ZA", new CountryCoords("South Africa", -25.7479, 28.2293));
        COUNTRY_COORDINATES.put("ZW", new CountryCoords("Zimbabwe", -17.8252, 31.0335));
        COUNTRY_COORDINATES.put("ZM", new CountryCoords("Zambia", -15.3875, 28.3228));
        COUNTRY_COORDINATES.put("MW", new CountryCoords("Malawi", -13.9626, 33.7741));
        COUNTRY_COORDINATES.put("MZ", new CountryCoords("Mozambique", -25.9655, 32.5832));
        COUNTRY_COORDINATES.put("BW", new CountryCoords("Botswana", -24.6282, 25.9231));
        COUNTRY_COORDINATES.put("NA", new CountryCoords("Namibia", -22.5597, 17.0832));
        
        // West African countries
        COUNTRY_COORDINATES.put("NG", new CountryCoords("Nigeria", 9.0765, 7.3986));
        COUNTRY_COORDINATES.put("GH", new CountryCoords("Ghana", 5.6037, -0.1870));
        COUNTRY_COORDINATES.put("SN", new CountryCoords("Senegal", 14.6928, -17.4467));
        COUNTRY_COORDINATES.put("CI", new CountryCoords("Côte d'Ivoire", 5.3600, -4.0083));
        
        // North African countries
        COUNTRY_COORDINATES.put("EG", new CountryCoords("Egypt", 30.0444, 31.2357));
        COUNTRY_COORDINATES.put("MA", new CountryCoords("Morocco", 33.9716, -6.8498));
        COUNTRY_COORDINATES.put("DZ", new CountryCoords("Algeria", 36.7538, 3.0588));
        COUNTRY_COORDINATES.put("TN", new CountryCoords("Tunisia", 36.8065, 10.1815));
        COUNTRY_COORDINATES.put("LY", new CountryCoords("Libya", 32.8872, 13.1913));
        
        // European countries
        COUNTRY_COORDINATES.put("IT", new CountryCoords("Italy", 41.9028, 12.4964));
        COUNTRY_COORDINATES.put("ES", new CountryCoords("Spain", 40.4168, -3.7038));
        COUNTRY_COORDINATES.put("NL", new CountryCoords("Netherlands", 52.3702, 4.8952));
        COUNTRY_COORDINATES.put("BE", new CountryCoords("Belgium", 50.8503, 4.3517));
        COUNTRY_COORDINATES.put("CH", new CountryCoords("Switzerland", 46.9480, 7.4474));
        COUNTRY_COORDINATES.put("SE", new CountryCoords("Sweden", 59.3293, 18.0686));
        COUNTRY_COORDINATES.put("NO", new CountryCoords("Norway", 59.9139, 10.7522));
        COUNTRY_COORDINATES.put("DK", new CountryCoords("Denmark", 55.6761, 12.5683));
        COUNTRY_COORDINATES.put("FI", new CountryCoords("Finland", 60.1695, 24.9354));
        
        // Asian countries
        COUNTRY_COORDINATES.put("SG", new CountryCoords("Singapore", 1.3521, 103.8198));
        COUNTRY_COORDINATES.put("MY", new CountryCoords("Malaysia", 3.1390, 101.6869));
        COUNTRY_COORDINATES.put("TH", new CountryCoords("Thailand", 13.7563, 100.5018));
        COUNTRY_COORDINATES.put("VN", new CountryCoords("Vietnam", 21.0285, 105.8542));
        COUNTRY_COORDINATES.put("PH", new CountryCoords("Philippines", 14.5995, 120.9842));
        COUNTRY_COORDINATES.put("ID", new CountryCoords("Indonesia", -6.2088, 106.8456));
        COUNTRY_COORDINATES.put("KR", new CountryCoords("South Korea", 37.5665, 126.9780));
        COUNTRY_COORDINATES.put("PK", new CountryCoords("Pakistan", 33.6844, 73.0479));
        COUNTRY_COORDINATES.put("BD", new CountryCoords("Bangladesh", 23.8103, 90.4125));
        
        // Middle East
        COUNTRY_COORDINATES.put("SA", new CountryCoords("Saudi Arabia", 24.7136, 46.6753));
        COUNTRY_COORDINATES.put("AE", new CountryCoords("United Arab Emirates", 24.4539, 54.3773));
        COUNTRY_COORDINATES.put("IL", new CountryCoords("Israel", 31.7683, 35.2137));
        COUNTRY_COORDINATES.put("TR", new CountryCoords("Turkey", 39.9334, 32.8597));
        
        // Latin America
        COUNTRY_COORDINATES.put("MX", new CountryCoords("Mexico", 19.4326, -99.1332));
        COUNTRY_COORDINATES.put("AR", new CountryCoords("Argentina", -34.6037, -58.3816));
        COUNTRY_COORDINATES.put("CL", new CountryCoords("Chile", -33.4489, -70.6693));
        COUNTRY_COORDINATES.put("CO", new CountryCoords("Colombia", 4.7110, -74.0721));
        COUNTRY_COORDINATES.put("PE", new CountryCoords("Peru", -12.0464, -77.0428));
        
        // Unknown/Default
        COUNTRY_COORDINATES.put("XX", new CountryCoords("Unknown", 0.0, 0.0));
    }
    
    /**
     * Get coordinates for a country code
     * Returns default (0,0) for unknown countries
     */
    public CountryCoords getCoordinates(String countryCode) {
        return COUNTRY_COORDINATES.getOrDefault(
            countryCode, 
            new CountryCoords("Unknown", 0.0, 0.0)
        );
    }
    
    /**
     * Get country name for a code
     */
    public String getCountryName(String countryCode) {
        return getCoordinates(countryCode).name;
    }
    
    /**
     * Inner class to hold country coordinates
     */
    public static class CountryCoords {
        public final String name;
        public final double latitude;
        public final double longitude;
        
        public CountryCoords(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
