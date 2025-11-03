// java
package app.controllers.impl;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.dtos.GuideTotalDTO;
import app.entities.Trip;
import app.entities.Guide;
import app.security.controllers.SecurityController;
import app.security.daos.SecurityDAO;
import app.security.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import jakarta.persistence.EntityManagerFactory;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TripControllerTest {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static SecurityController securityController = SecurityController.getInstance();
    private static final SecurityDAO securityDao = new SecurityDAO(emf);
    private static Javalin app;
    private static Trip[] trips;
    private static UserDTO userDTO, adminDTO;
    private static String userToken, adminToken;
    private static final String BASE = "http://localhost:7070/api";

    @BeforeAll
    void setUpAll() {
        HibernateConfig.setTest(true);
        // ensure PackingService returns canned data during tests
        System.setProperty("TEST_ENV", "true");
        app = ApplicationConfig.startServer(7070);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7070;
        RestAssured.basePath = "/api";
    }

    @BeforeEach
    void setup() {
        // seed DB
        trips = Populator.populateTrips(emf);
        UserDTO[] users = Populator.populateUsers(emf);
        userDTO = users[0];
        adminDTO = users[1];

        try {
            var verifiedUser = securityDao.getVerifiedUser(userDTO.getUsername(), userDTO.getPassword());
            var verifiedAdmin = securityDao.getVerifiedUser(adminDTO.getUsername(), adminDTO.getPassword());
            userToken = "Bearer " + securityController.createToken(verifiedUser);
            adminToken = "Bearer " + securityController.createToken(verifiedAdmin);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Trip").executeUpdate();
            em.createQuery("DELETE FROM Guide").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @AfterAll
    void tearDownAll() {
        ApplicationConfig.stopServer(app);
    }

    @Test
    void testReadAllTrips() {
        given()
                .when()
                .get("/trips/")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    void testGetTripById_includesPackingItems() {
        int id = trips[0].getId();
        given()
                .when()
                .get("/trips/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("packing_items", notNullValue())
                .body("packing_items.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void testCreateAndReadGuide() {
        // create guide
        String payload = """
                {
                  "name": "New Guide",
                  "email": "guide@example.com",
                  "phonenumber": "66677788",
                  "years_of_experience": 3
                }
                """;
        int createdId =
                given()
                        .contentType("application/json")
                        .body(payload)
                        .when()
                        .post("/guides/")
                        .then()
                        .statusCode(201)
                        .body("name", equalTo("New Guide"))
                        .extract()
                        .path("id");

        // read guide by id
        given()
                .when()
                .get("/guides/{id}", createdId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdId))
                .body("email", equalTo("guide@example.com"));
    }

    @Test
    void testGetTotalPricePerGuide() {
        given()
                .when()
                .get("/trips/guides/totalprice")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    void testLinkGuide() {
        // create a guide to link
        String guideJson = """
                {
                  "name": "Linker",
                  "email": "linker@example.com",
                  "phonenumber": "11122233",
                  "years_of_experience": 1
                }
                """;
        int guideId = given()
                .contentType("application/json")
                .body(guideJson)
                .when()
                .post("/guides/")
                .then()
                .statusCode(201)
                .extract().path("id");

        int tripId = trips[1].getId();

        // link guide to trip
        given()
                .when()
                .put("/trips/{tripId}/guides/{guideId}", tripId, guideId)
                .then()
                .statusCode(200)
                .body("id", equalTo(tripId))
                .body("guide_id", equalTo(guideId));
    }
}
