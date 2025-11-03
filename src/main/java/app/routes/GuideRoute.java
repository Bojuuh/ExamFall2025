package app.routes;

import app.controllers.impl.GuideController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class GuideRoute {

    private final GuideController guideController = new GuideController();

    protected EndpointGroup getRoutes() {

        return () -> {
            post("/", guideController::create, Role.ADMIN);
            get("/", guideController::readAll, Role.ANYONE);
            get("/{id}", guideController::read, Role.ANYONE);
            put("/{id}", guideController::update, Role.ADMIN);
            delete("/{id}", guideController::delete, Role.ADMIN);
        };
    }
}
