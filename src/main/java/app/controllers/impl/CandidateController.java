package app.controllers.impl;

import app.config.HibernateConfig;
import app.daos.impl.CandidateDAO;
import app.dtos.CandidateDTO;
import app.dtos.SkillRefDTO;
import app.controllers.IController;
import app.services.ExternalStatsService;
import io.javalin.http.Context;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CandidateController implements IController<CandidateDTO, Integer> {

    private final CandidateDAO dao;
    private final ExternalStatsService statsService = new ExternalStatsService();

    public CandidateController() {
        var emf = HibernateConfig.getEntityManagerFactory();
        this.dao = CandidateDAO.getInstance(emf);
    }

    @Override
    public void read(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        CandidateDTO dto = dao.read(id);
        if (dto == null) {
            ctx.status(404);
            return;
        }

        // If candidate has skills, fetch enrichment once and merge
        Set<SkillRefDTO> skills = dto.getSkills();
        if (skills != null && !skills.isEmpty()) {
            Set<String> slugs = skills.stream()
                    .map(SkillRefDTO::getSlug)
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            var statsBySlug = statsService.fetchStatsBySlugs(slugs);
            statsService.enrichSkillRefs(skills, statsBySlug);
        } else {
            dto.setSkills(java.util.Set.of()); // ensure empty set per acceptance criteria
        }

        ctx.json(dto);
    }

    @Override
    public void readAll(Context ctx) {
        List<CandidateDTO> results = dao.readAll();

        // Collect all slugs from all candidates (single external call)
        Set<String> slugs = results.stream()
                .flatMap(dto -> dto.getSkills() == null ? java.util.stream.Stream.empty() : dto.getSkills().stream())
                .map(SkillRefDTO::getSlug)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        var statsBySlug = statsService.fetchStatsBySlugs(slugs);

        // Enrich each candidate's skills (or set empty set if none)
        results.forEach(dto -> {
            Set<SkillRefDTO> skills = dto.getSkills();
            if (skills != null && !skills.isEmpty()) {
                statsService.enrichSkillRefs(skills, statsBySlug);
            } else {
                dto.setSkills(java.util.Set.of());
            }
        });

        ctx.json(results);
    }

    // Separate endpoint for filtering by category
    public void readByCategory(Context ctx) {
        String category = ctx.queryParam("category");
        List<CandidateDTO> results = dao.readAllByCategory(category);

        // same enrichment logic as readAll
        Set<String> slugs = results.stream()
                .flatMap(dto -> dto.getSkills() == null ? java.util.stream.Stream.empty() : dto.getSkills().stream())
                .map(SkillRefDTO::getSlug)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        var statsBySlug = statsService.fetchStatsBySlugs(slugs);
        results.forEach(dto -> {
            Set<SkillRefDTO> skills = dto.getSkills();
            if (skills != null && !skills.isEmpty()) {
                statsService.enrichSkillRefs(skills, statsBySlug);
            } else {
                dto.setSkills(java.util.Set.of());
            }
        });

        ctx.json(results);
    }

    @Override
    public void create(Context ctx) {
        CandidateDTO dto = ctx.bodyAsClass(CandidateDTO.class);
        CandidateDTO created = dao.create(dto);
        ctx.status(201).json(created);
    }

    @Override
    public void update(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        CandidateDTO dto = ctx.bodyAsClass(CandidateDTO.class);
        CandidateDTO updated = dao.update(id, dto);
        if (updated == null) {
            ctx.status(404);
            return;
        }
        ctx.json(updated);
    }

    @Override
    public void delete(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        dao.delete(id);
        ctx.status(204);
    }

    @Override
    public boolean validatePrimaryKey(Integer integer) {
        return dao.validatePrimaryKey(integer);
    }

    @Override
    public CandidateDTO validateEntity(Context ctx) {
        return ctx.bodyAsClass(CandidateDTO.class);
    }

    // Link an existing skill to a candidate
    public void linkSkill(Context ctx) {
        int candidateId = Integer.parseInt(ctx.pathParam("candidateId"));
        int skillId = Integer.parseInt(ctx.pathParam("skillId"));
        CandidateDTO result = dao.linkSkill(candidateId, skillId);
        if (result == null) {
            ctx.status(404);
            return;
        }

        // Enrich the returned candidate's skills (same approach as other endpoints)
        Set<SkillRefDTO> skills = result.getSkills();
        if (skills != null && !skills.isEmpty()) {
            Set<String> slugs = skills.stream()
                    .map(SkillRefDTO::getSlug)
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            var statsBySlug = statsService.fetchStatsBySlugs(slugs);
            statsService.enrichSkillRefs(skills, statsBySlug);
        } else {
            result.setSkills(java.util.Set.of());
        }

        ctx.json(result);
    }

    public void populate(Context ctx) {
        String result = app.config.Populator.populateSampleData();
        ctx.status(200);
        ctx.json(java.util.Map.of("msg", result));
    }

}
