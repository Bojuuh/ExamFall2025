package app.routes;

import app.controllers.impl.CandidateController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {

    private final CandidateRoutes candidateRoutes = new CandidateRoutes();
    private final ReportsRoutes reportsRoutes = new ReportsRoutes();

    public EndpointGroup getRoutes() {
        return () -> {
            path("/candidates", candidateRoutes.getRoutes());
            path("/reports", reportsRoutes.getRoutes());
        };
    }

}
