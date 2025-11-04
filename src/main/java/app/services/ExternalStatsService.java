// java
package app.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import app.dtos.SkillRefDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class ExternalStatsService {

    // allow override from tests via system property EXTERNAL_STATS_BASE
    private static final String DEFAULT_BASE = "https://apiprovider.cphbusinessapps.dk/api/v1/skills/stats?slugs=";
    private final String base;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper;

    public ExternalStatsService() {
        this.base = System.getProperty("EXTERNAL_STATS_BASE", DEFAULT_BASE);
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Map<String, JsonNode> fetchStatsBySlugs(Set<String> slugs) {
        if (slugs == null || slugs.isEmpty()) return Map.of();
        String param = slugs.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.joining(","));
        if (param.isEmpty()) return Map.of();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base + param))
                .GET()
                .header("Accept", "application/json")
                .build();

        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return Map.of();

            JsonNode root = mapper.readTree(resp.body());
            JsonNode data = root.get("data");
            if (data == null || !data.isArray()) return Map.of();

            Map<String, JsonNode> map = new HashMap<>();
            for (JsonNode item : data) {
                JsonNode slugNode = item.get("slug");
                if (slugNode != null && !slugNode.asText().isBlank()) {
                    map.put(slugNode.asText().toLowerCase(), item);
                }
            }
            return map;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return Map.of();
        }
    }

    public void enrichSkillRefs(Collection<SkillRefDTO> skills, Map<String, JsonNode> statsBySlug) {
        if (skills == null || skills.isEmpty()) return;

        for (SkillRefDTO s : skills) {
            if (s == null || s.getSlug() == null) continue;

            JsonNode stat = statsBySlug.get(s.getSlug().toLowerCase());
            if (stat == null) continue;

            JsonNode pop = stat.get("popularityScore");
            JsonNode avg = stat.get("averageSalary");
            if (pop != null && !pop.isNull()) s.setPopularityScore(pop.asInt());
            if (avg != null && !avg.isNull()) s.setAverageSalary(avg.asInt());

            JsonNode catKey = stat.get("categoryKey");
            if (catKey != null && !catKey.isNull()) s.setCategoryKey(catKey.asText());

            JsonNode desc = stat.get("description");
            if (desc != null && !desc.isNull()) s.setDescription(desc.asText());

            JsonNode updated = stat.get("updatedAt");
            if (updated != null && !updated.isNull()) {
                try {
                    ZonedDateTime zdt = parseUpdatedAt(updated);
                    s.setUpdatedAt(zdt);
                } catch (Exception ignored) {
                    // leave updatedAt null if parsing fails
                }
            }
        }
    }

    private ZonedDateTime parseUpdatedAt(com.fasterxml.jackson.databind.JsonNode updated) {
        if (updated.isNumber() || updated.asText().matches("\\d+(\\.\\d+)?")) {
            long value = updated.isNumber()
                    ? updated.longValue()
                    : (long) Double.parseDouble(updated.asText());
            return (value > 1_000_000_000_000L)
                    ? Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC)
                    : Instant.ofEpochSecond(value).atZone(ZoneOffset.UTC);
        }
        String text = updated.asText().trim();
        try {
            if (text.endsWith("Z") || text.contains("+") || text.contains("-")) {
                return ZonedDateTime.parse(text);
            } else {
                LocalDateTime ldt = LocalDateTime.parse(text);
                return ldt.atZone(ZoneOffset.UTC);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse updatedAt: " + text, e);
        }
    }
}
