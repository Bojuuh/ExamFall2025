// File: src/main/java/app/controllers/impl/ReportController.java
package app.controllers.impl;

import app.config.HibernateConfig;
import app.daos.impl.CandidateDAO;
import app.dtos.CandidateDTO;
import app.dtos.SkillRefDTO;
import app.services.ExternalStatsService;
import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.http.Context;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportController {

    private final CandidateDAO dao;
    private final ExternalStatsService statsService = new ExternalStatsService();

    public ReportController() {
        var emf = HibernateConfig.getEntityManagerFactory();
        this.dao = CandidateDAO.getInstance(emf);
    }

    public void topByPopularity(Context ctx) {
        List<CandidateDTO> candidates = dao.readAll();
        if (candidates == null || candidates.isEmpty()) {
            ctx.status(200).json(Map.of()); // no candidates
            return;
        }

        // Collect all slugs (single external call)
        Set<String> slugs = candidates.stream()
                .flatMap(dto -> dto.getSkills() == null ? Stream.empty() : dto.getSkills().stream())
                .map(SkillRefDTO::getSlug)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        var statsBySlug = statsService.fetchStatsBySlugs(slugs);

        // average popularity per candidate
        Optional<AbstractMap.SimpleEntry<Integer, Double>> best = candidates.stream()
                .map(dto -> {
                    int id = dto.getId();
                    int sum = 0;
                    int count = 0;
                    if (dto.getSkills() != null) {
                        for (SkillRefDTO s : dto.getSkills()) {
                            if (s == null || s.getSlug() == null) continue;
                            JsonNode stat = statsBySlug.get(s.getSlug().toLowerCase());
                            if (stat != null) {
                                JsonNode pop = stat.get("popularityScore");
                                if (pop != null && !pop.isNull()) {
                                    sum += pop.asInt();
                                    count++;
                                }
                            }
                        }
                    }
                    double avg = (count == 0) ? Double.NaN : (double) sum / count;
                    return new AbstractMap.SimpleEntry<>(id, avg);
                })
                .filter(entry -> !Double.isNaN(entry.getValue())) // only candidates with at least one popularity score
                .max(Comparator.comparingDouble(Map.Entry::getValue));

        if (best.isEmpty()) {
            ctx.status(200).json(Map.of()); // no candidates with popularity data
            return;
        }

        Map.Entry<Integer, Double> winner = best.get();
        // Round to 2 decimals
        double rounded = Math.round(winner.getValue() * 100.0) / 100.0;
        ctx.json(Map.of(
                "id", winner.getKey(),
                "averagePopularityScore", rounded
        ));
    }
}
