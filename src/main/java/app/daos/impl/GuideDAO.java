package app.daos.impl;

import app.dtos.GuideDTO;
import app.daos.IDAO;
import app.dtos.GuideDTO;
import app.entities.Guide;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class GuideDAO implements IDAO<GuideDTO, Integer> {

    private static GuideDAO instance;
    private static EntityManagerFactory emf;

    public static GuideDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new GuideDAO();
        }
        return instance;
    }

    @Override
    public GuideDTO read(Integer integer) {
        try (EntityManager em = emf.createEntityManager()) {
            Guide guide = em.find(Guide.class, integer);
            return new GuideDTO(guide);
        }
    }

    @Override
    public List<GuideDTO> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<GuideDTO> query = em.createQuery("SELECT new app.dtos.GuideDTO(g) FROM Guide g", GuideDTO.class);
            return query.getResultList();
        }
    }

    @Override
    public GuideDTO create(GuideDTO guideDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Guide guide = new Guide(guideDTO);
            em.persist(guide);
            em.getTransaction().commit();
            return new GuideDTO(guide);
        }
    }

    @Override
    public GuideDTO update(Integer integer, GuideDTO GuideDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Guide g = em.find(Guide.class, integer);
            g.setName(GuideDTO.getName());
            g.setEmail(GuideDTO.getEmail());
            g.setPhoneNumber(GuideDTO.getPhoneNumber());
            g.setYearsOfExperience(GuideDTO.getYearsOfExperience());
            Guide mergedGuide = em.merge(g);
            em.getTransaction().commit();
            return mergedGuide != null ? new GuideDTO(mergedGuide) : null;
        }
    }

    @Override
    public void delete(Integer integer) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Guide guide = em.find(Guide.class, integer);
            if (guide != null) {
                em.remove(guide);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public boolean validatePrimaryKey(Integer integer) {
        try (EntityManager em = emf.createEntityManager()) {
            Guide guide = em.find(Guide.class, integer);
            return guide != null;
        }
    }
}
