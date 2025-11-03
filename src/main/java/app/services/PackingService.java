package app.services;

import app.dtos.PackingItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackingService {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://packingapi.cphbusinessapps.dk/packinglist/";

    // single instance so static methods can use the configured objectMapper
    private static final PackingService INSTANCE = new PackingService();

    private final ObjectMapper objectMapper;

    public PackingService() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static List<PackingItemDTO> fetchPackingItems(String category) {
        if ("true".equalsIgnoreCase(System.getProperty("TEST_ENV"))) {
            List<PackingItemDTO> list = new ArrayList<>();
            list.add(new PackingItemDTO(
                    "TestItem", 100, 1,
                    "Test item for tests", category, ZonedDateTime.now(),
                    ZonedDateTime.now(), null));
            return list;
        }
        if (category == null || category.isBlank()) {
            return Collections.emptyList();
        }
        String cat = category.trim().toLowerCase();
        if (!List.of("beach", "city", "forest", "lake", "sea", "snow").contains(cat)) {
            return Collections.emptyList();
        }
        String url = BASE_URL + cat;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Collections.emptyList();
            }
            PackingApiResponse apiResponse = INSTANCE.objectMapper.readValue(response.body(), PackingApiResponse.class);
            return apiResponse != null && apiResponse.items != null ? apiResponse.items : Collections.emptyList();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // small internal holder matching external API structure
    private static class PackingApiResponse {
        public List<PackingItemDTO> items;
    }
}
