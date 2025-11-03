package app.routes;

import app.controllers.impl.TripController;
//import app.security.enums.Role;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class TripRoute {

    private final TripController tripController = new TripController();

    protected EndpointGroup getRoutes() {
        return () -> {
            get("/populate", tripController::populate, Role.ADMIN);
            post("/", tripController::create, Role.USER);
            get("/", tripController::readAll, Role.ANYONE);
            get("/guides/totalprice", tripController::getTotalPricePerGuide, Role.ANYONE);
            get("/{id}/packing/weight", tripController::getPackingWeight, Role.ANYONE);
            get("/{id}", tripController::read, Role.ANYONE);
            put("/{id}", tripController::update, Role.USER);
            delete("/{id}", tripController::delete, Role.ADMIN);
            put("/{tripId}/guides/{guideId}", tripController::linkGuide, Role.USER);
        };
    }
}
