// java
package app.config;


import app.entities.Guide;
import app.entities.Trip;
import jakarta.persistence.EntityManagerFactory;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public class Populate {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // guides
            Guide guide1 = new Guide("Alex", "alex@example.com", "12345678", 10);
            Guide guide2 = new Guide("Anna", "anna@example.com", "87654321", 5);
            Guide guide3 = new Guide("Bob", "bob@example.com", "12563478", 20);

            em.persist(guide1);
            em.persist(guide2);
            em.persist(guide3);

            // trips
            Trip trip1 = new Trip();
            trip1.setName("Hiking in the Alps");
            trip1.setStartTime(LocalDateTime.of(2026, 5, 11, 9, 0));
            trip1.setEndTime(LocalDateTime.of(2026, 5, 20, 15, 0));
            trip1.setLatitude(46.5555);
            trip1.setLongitude(8.5568);
            trip1.setPrice(1300);
            trip1.setCategory("Forest");
            trip1.setGuide(guide1);

            Trip trip2 = new Trip();
            trip2.setName("Beach Paradise");
            trip2.setStartTime(LocalDateTime.of(2026, 7, 1, 9, 0));
            trip2.setEndTime(LocalDateTime.of(2026, 7, 15, 17, 0));
            trip2.setLatitude(-8.5568);
            trip2.setLongitude(46.5555);
            trip2.setPrice(700);
            trip2.setCategory("Beach");
            trip2.setGuide(guide2);

            Trip trip3 = new Trip();
            trip3.setName("New York");
            trip3.setStartTime(LocalDateTime.of(2026, 6, 20, 16, 0));
            trip3.setEndTime(LocalDateTime.of(2026, 7, 10, 20, 0));
            trip3.setLatitude(8.5568);
            trip3.setLongitude(-46.5555);
            trip3.setPrice(900);
            trip3.setCategory("City");
            trip3.setGuide(guide3);

            Trip trip4 = new Trip();
            trip4.setName("Hiking in snow");
            trip4.setStartTime(LocalDateTime.of(2026, 10, 1, 10, 0));
            trip4.setEndTime(LocalDateTime.of(2026, 10, 8, 17, 0));
            trip4.setLatitude(-46.5555);
            trip4.setLongitude(-8.5568);
            trip4.setPrice(1200);
            trip4.setCategory("snow");
            trip4.setGuide(guide1);

            Trip trip5 = new Trip();
            trip5.setName("Sunny Beach");
            trip5.setStartTime(LocalDateTime.of(2026, 7, 1, 9, 0));
            trip5.setEndTime(LocalDateTime.of(2026, 7, 15, 17, 0));
            trip5.setLatitude(-10.5568);
            trip5.setLongitude(46.5555);
            trip5.setPrice(700);
            trip5.setCategory("Beach");
            trip5.setGuide(guide2);

            Trip trip6 = new Trip();
            trip6.setName("Los Angeles");
            trip6.setStartTime(LocalDateTime.of(2026, 6, 20, 16, 0));
            trip6.setEndTime(LocalDateTime.of(2026, 7, 10, 20, 0));
            trip6.setLatitude(10.5568);
            trip6.setLongitude(-46.5555);
            trip6.setPrice(900);
            trip6.setCategory("City");
            trip6.setGuide(guide3);

            em.persist(trip1);
            em.persist(trip2);
            em.persist(trip3);
            em.persist(trip4);
            em.persist(trip5);
            em.persist(trip6);

            em.getTransaction().commit();

            System.out.println("Database has been populated with trips and guides");
        }
    }
}
