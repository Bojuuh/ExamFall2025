package app.routes;

import app.controllers.impl.CandidateController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CandidateRoutes {

    private final CandidateController candidateController = new CandidateController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/populate", candidateController::populate, Role.ADMIN);
            post("/", candidateController::create, Role.USER);
            get("/", candidateController::readAll, Role.ANYONE);
            get("/filter", candidateController::readByCategory, Role.ANYONE);
            get("/{id}", candidateController::read, Role.ANYONE);
            put("/{id}", candidateController::update, Role.USER);
            delete("/{id}", candidateController::delete, Role.ADMIN);
            put("/{candidateId}/skills/{skillId}", candidateController::linkSkill, Role.USER);
        };
    }
}
