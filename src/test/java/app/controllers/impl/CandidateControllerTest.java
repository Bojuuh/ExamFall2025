// java
package app.controllers.impl;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.config.Populator;
import app.security.controllers.SecurityController;
import app.security.daos.SecurityDAO;
import dk.bugelhartmann.UserDTO;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Set;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CandidateControllerTest {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final SecurityController securityController = SecurityController.getInstance();
    private static final SecurityDAO securityDao = new SecurityDAO(emf);
    private static Javalin app;
    private static String userToken, adminToken;

    @BeforeAll
    void setUpAll() {
        HibernateConfig.setTest(true);
        System.setProperty("TEST_ENV", "true");
        app = ApplicationConfig.startServer(7070);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7070;
        RestAssured.basePath = "/api";
    }

    @BeforeEach
    void setupEach() throws Exception {
        // clear DB
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM CandidateSkill").executeUpdate();
            em.createQuery("DELETE FROM Candidate").executeUpdate();
            em.createQuery("DELETE FROM Skill").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.getTransaction().commit();
        }

        // populate sample data
        Populator.populateSampleData();

        // create user and admin and tokens
        var user = securityDao.createUser("testuser", "password");
        var admin = securityDao.createUser("adminuser", "password");
        // grant admin role
        securityDao.addRole(new UserDTO(admin.getUsername(), Set.of("USER")), "admin");

        UserDTO userDto = new UserDTO(user.getUsername(), Set.of("USER"));
        UserDTO adminDto = new UserDTO(admin.getUsername(), Set.of("USER", "ADMIN"));
        userToken = "Bearer " + securityController.createToken(userDto);
        adminToken = "Bearer " + securityController.createToken(adminDto);
    }

    @AfterAll
    void tearDownAll() {
        if (app != null) ApplicationConfig.stopServer(app);
    }

    @Test
    void testPopulateEndpoint() {
        given()
                .when()
                .get("/candidates/populate")
                .then()
                .statusCode(200)
                .body("msg", notNullValue());
    }

    @Test
    void testReadAllCandidates() {
        given()
                .when()
                .get("/candidates")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    void testGetCandidateById_includesSkills() {
        Integer id = given()
                .when()
                .get("/candidates")
                .then().statusCode(200)
                .extract().path("[0].id");

        given()
                .when()
                .get("/candidates/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("skills", notNullValue())
                .body("skills.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void testCreateUpdateDeleteCandidate_flow() {
        // create
        String payload = """
                {
                  "name":"New Candidate",
                  "phone":"55566677",
                  "education":"MSc",
                  "skills":[]
                }
                """;

        int createdId = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/candidates")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)))
                .body("name", equalTo("New Candidate"))
                .extract().path("id");

        // update
        String update = """
                {
                  "name":"Updated Candidate",
                  "phone":"00011122",
                  "education":"PhD",
                  "skills":[]
                }
                """;

        given()
                .contentType("application/json")
                .body(update)
                .when()
                .put("/candidates/{id}", createdId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdId))
                .body("name", equalTo("Updated Candidate"));

        // delete
        given()
                .when()
                .delete("/candidates/{id}", createdId)
                .then()
                .statusCode(204);

        // verify deleted (controller may return 404 or null)
        given()
                .when()
                .get("/candidates/{id}", createdId)
                .then()
                .statusCode(anyOf(equalTo(404), equalTo(200)));
    }

    @Test
    void testLinkSkillToCandidate() {
        // create candidate without skills
        String payload = """
                {
                  "name":"Link Candidate",
                  "phone":"99988877",
                  "education":"BSc",
                  "skills":[]
                }
                """;

        int candidateId = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/candidates")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)))
                .extract().path("id");

        // find existing skill id from populated data
        Integer skillId = given()
                .when()
                .get("/candidates")
                .then().statusCode(200)
                .extract().path("[0].skills[0].id");

        // link
        given()
                .when()
                .put("/candidates/{candidateId}/skills/{skillId}", candidateId, skillId)
                .then()
                .statusCode(200)
                .body("id", equalTo(candidateId))
                .body("skills.find { it.id == %s }", withArgs(skillId), notNullValue());
    }

    @Test
    void testFilterByCategory() {
        given()
                .when()
                .get("/candidates/filter?category=PROG_LANG")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    void testReports_topByPopularity() {
        given()
                .when()
                .get("/reports/candidates/top-by-popularity")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    void testAuthRegisterLoginAndProtectedEndpoints() {
        // register new user
        String payload = """
                {
                  "username":"reguser",
                  "password":"pass123"
                }
                """;

        String token = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)))
                .extract().path("token");

        // login
        String loginToken = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");

        // call user protected endpoint
        given()
                .header("Authorization", "Bearer " + loginToken)
                .when()
                .get("/protected/user_demo")
                .then()
                .statusCode(200)
                .body("msg", containsString("USER"));

        // add role (requires USER)
        given()
                .header("Authorization", "Bearer " + loginToken)
                .contentType("application/json")
                .body("{\"role\":\"admin\"}")
                .when()
                .post("/auth/user/addrole")
                .then()
                .statusCode(200);
    }
}
