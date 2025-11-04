package app.routes;

import app.controllers.impl.ReportController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ReportsRoutes {

    private final ReportController reportController = new ReportController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/candidates/top-by-popularity", reportController::topByPopularity, Role.ANYONE);
        };
    }
}
