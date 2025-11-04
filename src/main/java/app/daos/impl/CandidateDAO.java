package app.daos.impl;

import app.config.Populator;
import app.daos.IDAO;
import app.dtos.CandidateDTO;
import app.dtos.SkillRefDTO;
import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;
import app.enums.SkillCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CandidateDAO implements IDAO<CandidateDTO, Integer> {

    private static CandidateDAO instance;
    private static EntityManagerFactory emf;

    public static CandidateDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new CandidateDAO();
        }
        return instance;
    }

    @Override
    public CandidateDTO read(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            Candidate c = em.find(Candidate.class, id);
            if (c == null) return null;
            // initialize collection
            if (c.getCandidateSkills() != null) c.getCandidateSkills().size();
            return toDTO(c);
        }
    }

    @Override
    public List<CandidateDTO> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c", Candidate.class);
            List<Candidate> list = q.getResultList();
            list.forEach(c -> { if (c.getCandidateSkills() != null) c.getCandidateSkills().size(); });
            return list.stream().map(this::toDTO).collect(Collectors.toList());
        }
    }

    /**
     * Read all candidates that have at least one skill in the given category.
     * If category is null/blank -> returns all candidates.
     * If category value is invalid -> returns empty list.
     */
    public List<CandidateDTO> readAllByCategory(String category) {
        if (category == null || category.isBlank()) {
            return readAll();
        }

        SkillCategory cat;
        try {
            cat = SkillCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // invalid category string -> no results
            return List.of();
        }

        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Candidate> q = em.createQuery(
                    "SELECT DISTINCT c FROM Candidate c JOIN c.candidateSkills cs JOIN cs.skill s WHERE s.category = :category",
                    Candidate.class);
            q.setParameter("category", cat);
            List<Candidate> list = q.getResultList();
            list.forEach(c -> { if (c.getCandidateSkills() != null) c.getCandidateSkills().size(); });
            return list.stream().map(this::toDTO).collect(Collectors.toList());
        }
    }

    @Override
    public CandidateDTO create(CandidateDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Candidate c = new Candidate();
            c.setName(dto.getName());
            c.setPhone(dto.getPhone());
            c.setEducation(dto.getEducation());
            c.setCandidateSkills(new HashSet<>());

            // attach skills by existing skill ids (SkillRefDTO.id)
            if (dto.getSkills() != null) {
                for (SkillRefDTO ref : dto.getSkills()) {
                    if (ref == null || ref.getId() <= 0) continue;
                    Skill managedSkill = em.find(Skill.class, ref.getId());
                    if (managedSkill == null) continue;
                    CandidateSkill cs = new CandidateSkill();
                    cs.setCandidate(c);
                    cs.setSkill(managedSkill);
                    c.getCandidateSkills().add(cs);
                    // maintain other side if present
                    if (managedSkill.getCandidateSkills() == null) managedSkill.setCandidateSkills(new HashSet<>());
                    managedSkill.getCandidateSkills().add(cs);
                }
            }

            em.persist(c);
            em.getTransaction().commit();

            // reload to return fully initialized DTO
            try (EntityManager em2 = emf.createEntityManager()) {
                Candidate persisted = em2.find(Candidate.class, c.getId());
                if (persisted != null && persisted.getCandidateSkills() != null) persisted.getCandidateSkills().size();
                return persisted == null ? null : toDTO(persisted);
            }
        }
    }

    @Override
    public CandidateDTO update(Integer id, CandidateDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Candidate existing = em.find(Candidate.class, id);
            if (existing == null) {
                em.getTransaction().rollback();
                return null;
            }

            existing.setName(dto.getName());
            existing.setPhone(dto.getPhone());
            existing.setEducation(dto.getEducation());

            // ensure collection exists
            if (existing.getCandidateSkills() == null) existing.setCandidateSkills(new HashSet<>());

            // remove existing links (orphanRemoval + cascade will handle DB rows when CandidateSkill detached)
            existing.getCandidateSkills().forEach(cs -> {
                if (cs.getSkill() != null && cs.getSkill().getCandidateSkills() != null) {
                    cs.getSkill().getCandidateSkills().remove(cs);
                }
                cs.setCandidate(null);
                cs.setSkill(null);
            });
            existing.getCandidateSkills().clear();

            // attach new skills from DTO
            if (dto.getSkills() != null) {
                for (SkillRefDTO ref : dto.getSkills()) {
                    if (ref == null || ref.getId() <= 0) continue;
                    Skill managedSkill = em.find(Skill.class, ref.getId());
                    if (managedSkill == null) continue;
                    CandidateSkill cs = new CandidateSkill();
                    cs.setCandidate(existing);
                    cs.setSkill(managedSkill);
                    existing.getCandidateSkills().add(cs);
                    if (managedSkill.getCandidateSkills() == null) managedSkill.setCandidateSkills(new HashSet<>());
                    managedSkill.getCandidateSkills().add(cs);
                }
            }

            Candidate merged = em.merge(existing);
            em.getTransaction().commit();

            if (merged.getCandidateSkills() != null) merged.getCandidateSkills().size();
            return toDTO(merged);
        }
    }

    @Override
    public void delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Candidate c = em.find(Candidate.class, id);
            if (c != null) em.remove(c);
            em.getTransaction().commit();
        }
    }

    @Override
    public boolean validatePrimaryKey(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Candidate.class, id) != null;
        }
    }

    public CandidateDTO linkSkill(Integer candidateId, Integer skillId) {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                em.getTransaction().begin();
                Candidate candidate = em.find(Candidate.class, candidateId);
                Skill skill = em.find(Skill.class, skillId);
                if (candidate == null || skill == null) {
                    em.getTransaction().rollback();
                    return null;
                }

                // Check database for existing link to avoid duplicate key errors
                TypedQuery<Long> q = em.createQuery(
                        "SELECT COUNT(cs) FROM CandidateSkill cs WHERE cs.candidate.id = :cid AND cs.skill.id = :sid",
                        Long.class);
                q.setParameter("cid", candidateId);
                q.setParameter("sid", skillId);
                Long existing = q.getSingleResult();

                if (existing == 0L) {
                    CandidateSkill cs = new CandidateSkill();
                    cs.setCandidate(candidate);
                    cs.setSkill(skill);
                    em.persist(cs);

                    // maintain in-memory associations
                    if (candidate.getCandidateSkills() == null) candidate.setCandidateSkills(new HashSet<>());
                    candidate.getCandidateSkills().add(cs);
                    if (skill.getCandidateSkills() == null) skill.setCandidateSkills(new HashSet<>());
                    skill.getCandidateSkills().add(cs);
                }

                em.getTransaction().commit();
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
            // reload to return fully initialized DTO
            try (EntityManager em2 = emf.createEntityManager()) {
                Candidate refreshed = em2.find(Candidate.class, candidateId);
                if (refreshed != null && refreshed.getCandidateSkills() != null) refreshed.getCandidateSkills().size();
                return refreshed == null ? null : toDTO(refreshed);
            }
        }
    }


    // ----- mapping helper -----
    private CandidateDTO toDTO(Candidate c) {
        Set<SkillRefDTO> skills = c.getCandidateSkills() == null ? new HashSet<>() :
                c.getCandidateSkills().stream()
                        .map(cs -> cs.getSkill())
                        .filter(java.util.Objects::nonNull)
                        .map(s -> new SkillRefDTO(s.getId(), s.getName(), s.getSlug()))
                        .collect(Collectors.toSet());
        CandidateDTO dto = new CandidateDTO(c.getId(), c.getName(), c.getPhone(), c.getEducation(), skills);
        return dto;
    }

    public void Populate() {
        Populator.populateSampleData();
    }

}
