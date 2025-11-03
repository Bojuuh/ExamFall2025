// java
package app.controllers.impl;

import app.entities.Guide;
import app.entities.Trip;
import app.security.daos.SecurityDAO;
import app.security.entities.User;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.util.Set;

public class Populator {

    public static Trip[] populateTrips(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Guide g1 = new Guide("Alex", "alex@example.com", "12345678", 10);
            Guide g2 = new Guide("Anna", "anna@example.com", "87654321", 5);
            em.persist(g1);
            em.persist(g2);

            Trip t1 = new Trip();
            t1.setName("Beach Paradise");
            t1.setStartTime(LocalDateTime.of(2026,7,1,9,0));
            t1.setEndTime(LocalDateTime.of(2026,7,15,17,0));
            t1.setLatitude(-8.5568);
            t1.setLongitude(46.5555);
            t1.setPrice(700);
            t1.setCategory("Beach");
            t1.setGuide(g1);

            Trip t2 = new Trip();
            t2.setName("Forest Escape");
            t2.setStartTime(LocalDateTime.of(2026,5,11,9,0));
            t2.setEndTime(LocalDateTime.of(2026,5,20,15,0));
            t2.setLatitude(46.5555);
            t2.setLongitude(8.5568);
            t2.setPrice(1300);
            t2.setCategory("forest");
            t2.setGuide(g2);

            em.persist(t1);
            em.persist(t2);
            em.getTransaction().commit();

            return new Trip[]{ t1, t2 };
        }
    }

    public static UserDTO[] populateUsers(EntityManagerFactory emf) {
        SecurityDAO dao = new SecurityDAO(emf);
        String userName = "user_test";
        String adminName = "admin_test";
        String password = "test123";

        // create via DAO so password is hashed and stored correctly
        dao.createUser(userName, password);
        dao.createUser(adminName, password);
        // add admin role to admin user
        dao.addRole(new UserDTO(adminName, Set.of("USER")), "admin");

        // return UserDTOs holding username + password for tests (used by SecurityDAO.getVerifiedUser)
        return new UserDTO[]{
                new UserDTO(userName, password),
                new UserDTO(adminName, password)
        };
    }
}
