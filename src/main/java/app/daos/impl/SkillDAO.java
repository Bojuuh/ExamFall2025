package app.daos.impl;

import app.daos.IDAO;
import app.dtos.CandidateRefDTO;
import app.dtos.SkillDTO;
import app.entities.CandidateSkill;
import app.entities.Skill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SkillDAO implements IDAO<SkillDTO, Integer> {

    private static SkillDAO instance;
    private static EntityManagerFactory emf;

    public static SkillDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new SkillDAO();
        }
        return instance;
    }

    @Override
    public SkillDTO read(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            Skill s = em.find(Skill.class, id);
            if (s == null) return null;
            if (s.getCandidateSkills() != null) s.getCandidateSkills().size();
            return toDTO(s);
        }
    }

    @Override
    public List<SkillDTO> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Skill> q = em.createQuery("SELECT s FROM Skill s", Skill.class);
            List<Skill> list = q.getResultList();
            list.forEach(s -> { if (s.getCandidateSkills() != null) s.getCandidateSkills().size(); });
            return list.stream().map(this::toDTO).collect(Collectors.toList());
        }
    }

    @Override
    public SkillDTO create(SkillDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Skill s = new Skill();
            s.setName(dto.getName());
            s.setSlug(dto.getSlug());
            s.setDescription(dto.getDescription());
            s.setCategory(dto.getCategory());
            s.setCandidateSkills(new HashSet<>());

            em.persist(s);
            em.getTransaction().commit();

            try (EntityManager em2 = emf.createEntityManager()) {
                Skill persisted = em2.find(Skill.class, s.getId());
                if (persisted != null && persisted.getCandidateSkills() != null) persisted.getCandidateSkills().size();
                return persisted == null ? null : toDTO(persisted);
            }
        }
    }

    @Override
    public SkillDTO update(Integer id, SkillDTO dto) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Skill existing = em.find(Skill.class, id);
            if (existing == null) {
                em.getTransaction().rollback();
                return null;
            }
            existing.setName(dto.getName());
            existing.setDescription(dto.getDescription());
            existing.setCategory(dto.getCategory());
            if (dto.getSlug() != null && !dto.getSlug().isBlank()) existing.setSlug(dto.getSlug());

            Skill merged = em.merge(existing);
            em.getTransaction().commit();

            if (merged.getCandidateSkills() != null) merged.getCandidateSkills().size();
            return toDTO(merged);
        }
    }

    @Override
    public void delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Skill s = em.find(Skill.class, id);
            if (s != null) em.remove(s);
            em.getTransaction().commit();
        }
    }

    @Override
    public boolean validatePrimaryKey(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Skill.class, id) != null;
        }
    }

    // ---- mapping helper ----
    private SkillDTO toDTO(Skill s) {
        Set<CandidateRefDTO> candidates = s.getCandidateSkills() == null ? new HashSet<>() :
                s.getCandidateSkills().stream()
                        .map(CandidateSkill::getCandidate)
                        .filter(java.util.Objects::nonNull)
                        .map(c -> new CandidateRefDTO(c.getId(), c.getName()))
                        .collect(Collectors.toSet());
        return new SkillDTO(s.getId(), s.getSlug(), s.getName(), s.getDescription(), s.getCategory(), candidates);
    }
}
