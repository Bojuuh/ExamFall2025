// java
package app.daos.impl;

import app.daos.IDAO;
import app.dtos.GuideTotalDTO;
import app.dtos.TripDTO;
import app.entities.Trip;
import app.entities.Guide;
import app.dtos.GuideDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TripDAO implements IDAO<TripDTO, Integer> {

    private static TripDAO instance;
    private static EntityManagerFactory emf;

    public static TripDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new TripDAO();
        }
        return instance;
    }

    @Override
    public TripDTO read(Integer integer) {
        try (EntityManager em = emf.createEntityManager()) {
            Trip trip = em.find(Trip.class, integer);
            if (trip == null) return null;
            TripDTO dto = new TripDTO(trip);
            // packing items will be fetched by controller when needed
            return dto;
        }
    }

    @Override
    public List<TripDTO> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Trip> query = em.createQuery("SELECT t FROM Trip t", Trip.class);
            List<Trip> trips = query.getResultList();
            return trips.stream()
                    .map(t -> new TripDTO(t)) // do not populate packingItems here
                    .toList();
        }
    }

    public List<TripDTO> realAllByCategory(String category) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Trip> query = em.createQuery("SELECT t FROM Trip t WHERE lower(t.category) = :cat", Trip.class);
            query.setParameter("cat", category.toLowerCase());
            List<Trip> trips = query.getResultList();
            return trips.stream()
                    .map(t -> new TripDTO(t)) // do not populate packingItems here
                    .toList();
        }
    }

    @Override
    public TripDTO create(TripDTO tripDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Trip trip = new Trip(tripDTO);
            // attach guide if provided
            if (tripDTO.getGuideId() > 0) {
                Guide guide = em.find(Guide.class, tripDTO.getGuideId());
                trip.setGuide(guide);
            }
            em.persist(trip);
            em.getTransaction().commit();
            return new TripDTO(trip);
        }
    }

    @Override
    public TripDTO update(Integer integer, TripDTO tripDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Trip t = em.find(Trip.class, integer);
            if (t == null) {
                em.getTransaction().commit();
                return null;
            }
            t.setName(tripDTO.getName());
            t.setStartTime(tripDTO.getStartTime());
            t.setEndTime(tripDTO.getEndTime());
            t.setLatitude(tripDTO.getLatitude());
            t.setLongitude(tripDTO.getLongitude());
            t.setPrice(tripDTO.getPrice());
            t.setCategory(tripDTO.getCategory());
            // set guide if provided (0 or negative => remove)
            if (tripDTO.getGuideId() > 0) {
                Guide guide = em.find(Guide.class, tripDTO.getGuideId());
                t.setGuide(guide);
            } else {
                t.setGuide(null);
            }
            Trip mergedTrip = em.merge(t);
            em.getTransaction().commit();
            return mergedTrip != null ? new TripDTO(mergedTrip) : null;
        }
    }

    @Override
    public void delete(Integer integer) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Trip trip = em.find(Trip.class, integer);
            if (trip != null) {
                em.remove(trip);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public boolean validatePrimaryKey(Integer integer) {
        try (EntityManager em = emf.createEntityManager()) {
            Trip trip = em.find(Trip.class, integer);
            return trip != null;
        }
    }

    public List<GuideTotalDTO> getTotalPricePerGuide() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<GuideTotalDTO> query = em.createQuery(
                    "SELECT new app.dtos.GuideTotalDTO(t.guide.id, SUM(t.price)) FROM Trip t WHERE t.guide IS NOT NULL GROUP BY t.guide.id",
                    GuideTotalDTO.class);
            return query.getResultList();
        }
    }

    public TripDTO linkGuide(int tripId, int guideId) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Trip trip = em.find(Trip.class, tripId);
            Guide guide = em.find(Guide.class, guideId);
            if (trip == null) {
                em.getTransaction().commit();
                return null;
            }
            trip.setGuide(guide);
            Trip merged = em.merge(trip);
            em.getTransaction().commit();
            return merged != null ? new TripDTO(merged) : null;
        }
    }

    public void populate() {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Guide g1 = new Guide("Alex", "alex@example.com", "12345678", 10);
            Guide g2 = new Guide("Anna", "anna@example.com", "87654321", 5);
            Guide g3 = new Guide("Bob", "bob@example.com", "12563478", 20);

            em.persist(g1);
            em.persist(g2);
            em.persist(g3);

            Trip trip1 = new Trip();
            trip1.setName("Hiking in the Alps");
            trip1.setStartTime(LocalDateTime.of(2026, 5, 11, 9, 0));
            trip1.setEndTime(LocalDateTime.of(2026, 5, 20, 15, 0));
            trip1.setLatitude(46.5555);
            trip1.setLongitude(8.5568);
            trip1.setPrice(1300);
            trip1.setCategory("forest");
            trip1.setGuide(g1);

            Trip trip2 = new Trip();
            trip2.setName("Beach Paradise");
            trip2.setStartTime(LocalDateTime.of(2026, 7, 1, 9, 0));
            trip2.setEndTime(LocalDateTime.of(2026, 7, 15, 17, 0));
            trip2.setLatitude(-8.5568);
            trip2.setLongitude(46.5555);
            trip2.setPrice(700);
            trip2.setCategory("Beach");
            trip2.setGuide(g2);

            Trip trip3 = new Trip();
            trip3.setName("New York");
            trip3.setStartTime(LocalDateTime.of(2026, 6, 20, 16, 0));
            trip3.setEndTime(LocalDateTime.of(2026, 7, 10, 20, 0));
            trip3.setLatitude(8.5568);
            trip3.setLongitude(-46.5555);
            trip3.setPrice(900);
            trip3.setCategory("City");
            trip3.setGuide(g3);

            Trip trip4 = new Trip();
            trip4.setName("Hiking in Snow");
            trip4.setStartTime(LocalDateTime.of(2026, 10, 1, 10, 0));
            trip4.setEndTime(LocalDateTime.of(2026, 10, 8, 17, 0));
            trip4.setLatitude(-46.5555);
            trip4.setLongitude(-8.5568);
            trip4.setPrice(1200);
            trip4.setCategory("snow");
            trip4.setGuide(g1);

            Trip trip5 = new Trip();
            trip5.setName("Sunny Beach");
            trip5.setStartTime(LocalDateTime.of(2026, 7, 1, 9, 0));
            trip5.setEndTime(LocalDateTime.of(2026, 7, 15, 17, 0));
            trip5.setLatitude(-10.5568);
            trip5.setLongitude(46.5555);
            trip5.setPrice(700);
            trip5.setCategory("Beach");
            trip5.setGuide(g2);

            Trip trip6 = new Trip();
            trip6.setName("Los Angeles");
            trip6.setStartTime(LocalDateTime.of(2026, 6, 20, 16, 0));
            trip6.setEndTime(LocalDateTime.of(2026, 7, 10, 20, 0));
            trip6.setLatitude(10.5568);
            trip6.setLongitude(-46.5555);
            trip6.setPrice(900);
            trip6.setCategory("City");
            trip6.setGuide(g3);

            em.persist(trip1);
            em.persist(trip2);
            em.persist(trip3);
            em.persist(trip4);
            em.persist(trip5);
            em.persist(trip6);
            em.getTransaction().commit();
        }
    }

}
