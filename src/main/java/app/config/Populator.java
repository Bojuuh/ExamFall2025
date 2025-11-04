package app.config;

import app.config.HibernateConfig;
import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;
import app.enums.SkillCategory;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

public class Populator {


    public static Handler populateHandler() {
        return (Context ctx) -> {
            String result = populateSampleData();
            ctx.json(new java.util.HashMap<>() {{
                put("msg", result);
            }});
        };
    }

    public static String populateSampleData() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // If there are already skills or candidates, avoid duplicating
            Long skillCount = em.createQuery("select count(s) from Skill s", Long.class).getSingleResult();
            Long candidateCount = em.createQuery("select count(c) from Candidate c", Long.class).getSingleResult();
            if (skillCount > 0 || candidateCount > 0) {
                em.getTransaction().commit();
                return "Database already contains data; skipping population.";
            }

            // Create skills
            List<Skill> skills = new ArrayList<>();
            skills.add(persistSkill(em, "Java", SkillCategory.PROG_LANG, "General-purpose programming languages"));
            skills.add(persistSkill(em, "Spring Boot", SkillCategory.FRAMEWORK, "Application frameworks and libraries"));
            skills.add(persistSkill(em, "PostgreSQL", SkillCategory.DB, "Databases and data storage technologies"));
            skills.add(persistSkill(em, "Docker", SkillCategory.DEVOPS, "CTools and practices for deployment"));
            skills.add(persistSkill(em, "React", SkillCategory.FRONTEND, "Application frameworks and libraries"));

            // Create candidates
            Candidate alice = new Candidate();
            alice.setName("Alice");
            alice.setPhone("12345678");
            alice.setEducation("MSc Computer Science");
            em.persist(alice);

            Candidate bob = new Candidate();
            bob.setName("Bob");
            bob.setPhone("87654321");
            bob.setEducation("BSc Software Engineering");
            em.persist(bob);

            // Link candidate skills by persisting CandidateSkill entries
            persistCandidateSkill(em, alice, findSkillByName(em, "Java"));
            persistCandidateSkill(em, alice, findSkillByName(em, "Spring Boot"));
            persistCandidateSkill(em, bob, findSkillByName(em, "React"));
            persistCandidateSkill(em, bob, findSkillByName(em, "Docker"));
            persistCandidateSkill(em, bob, findSkillByName(em, "PostgreSQL"));

            em.getTransaction().commit();
            return "Populated sample candidates and skills.";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return "Failed to populate data: " + e.getMessage();
        } finally {
            em.close();
        }
    }

    private static Skill persistSkill(EntityManager em, String name, SkillCategory category, String description) {
        Skill s = new Skill();
        s.setName(name);
        s.setCategory(category);
        s.setDescription(description);
        // slug is auto-generated in @PrePersist/@PreUpdate if not set
        em.persist(s);
        return s;
    }

    private static void persistCandidateSkill(EntityManager em, Candidate candidate, Skill skill) {
        CandidateSkill cs = new CandidateSkill();
        cs.setCandidate(candidate);
        cs.setSkill(skill);
        // Persist CandidateSkill directly; CandidateSkill table has cascade on other side but direct persist is explicit
        em.persist(cs);

        // Maintain in-memory associations to keep entity graphs consistent
        if (candidate.getCandidateSkills() == null) candidate.setCandidateSkills(new java.util.HashSet<>());
        candidate.getCandidateSkills().add(cs);
        if (skill.getCandidateSkills() == null) skill.setCandidateSkills(new java.util.HashSet<>());
        skill.getCandidateSkills().add(cs);
    }

    private static Skill findSkillByName(EntityManager em, String name) {
        TypedQuery<Skill> q = em.createQuery("select s from Skill s where s.name = :name", Skill.class);
        q.setParameter("name", name);
        return q.getResultList().stream().findFirst().orElse(null);
    }

    public static void main(String[] args) {
        String result = populateSampleData();
        System.out.println(result);
        if (result.startsWith("Failed")) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }
}
