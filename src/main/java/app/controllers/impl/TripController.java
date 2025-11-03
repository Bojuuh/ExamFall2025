package app.controllers.impl;

import app.config.HibernateConfig;
import app.controllers.IController;
import app.daos.impl.TripDAO;
import app.daos.impl.GuideDAO;
import app.dtos.GuideTotalDTO;
import app.dtos.TripDTO;
import app.entities.Trip;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Map;

public class TripController implements IController<TripDTO, Integer> {

    private final TripDAO dao;
    private final GuideDAO guideDAO;

    public TripController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dao = TripDAO.getInstance(emf);
        this.guideDAO = GuideDAO.getInstance(emf);
    }

    @Override
    public void read(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(this::validatePrimaryKey, "Not a valid id")
                .get();

        TripDTO tripDTO = dao.read(id);
        if (tripDTO == null) {
            ctx.status(404);
            ctx.json(new app.exceptions.Message(404, "Trip not found"));
            return;
        }

        // Ensure packing items are included (fetch from external API if needed)
        if (tripDTO.getPackingItems() == null || tripDTO.getPackingItems().isEmpty()) {
            tripDTO.setPackingItems(app.services.PackingService.fetchPackingItems(tripDTO.getCategory()));
        }

        ctx.res().setStatus(200);
        ctx.json(tripDTO, TripDTO.class);
    }


    @Override
    public void readAll(Context ctx) {
        String category = ctx.queryParam("category");
        List<TripDTO> tripDTOS;
        if (category != null && !category.isBlank()) {
            tripDTOS = dao.realAllByCategory(category);
        } else {
            tripDTOS = dao.readAll();
        }
        ctx.res().setStatus(200);
        ctx.json(tripDTOS, TripDTO.class);
    }

    @Override
    public void create(Context ctx) {
        TripDTO jsonRequest = ctx.bodyAsClass(TripDTO.class);
        TripDTO tripDTO = dao.create(jsonRequest);
        ctx.res().setStatus(201);
        ctx.json(tripDTO, TripDTO.class);
    }

    @Override
    public void update(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validatePrimaryKey, "Not a valid id").get();
        TripDTO tripDTO = dao.update(id, validateEntity(ctx));
        ctx.res().setStatus(200);
        ctx.json(tripDTO, Trip.class);
    }

    @Override
    public void delete(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validatePrimaryKey, "Not a valid id").get();
        dao.delete(id);
        ctx.res().setStatus(204);
    }

    @Override
    public boolean validatePrimaryKey(Integer integer) {
        return dao.validatePrimaryKey(integer);
    }

    @Override
    public TripDTO validateEntity(Context ctx) {
        return ctx.bodyValidator(TripDTO.class)
                .check(t -> t.getName() != null && !t.getName().isEmpty(), "Trip name must be set")
                .check(t -> t.getStartTime() != null, "Trip start time must be set")
                .check(t -> t.getEndTime() != null, "Trip end time must be set")
                .check(t -> t.getCategory() != null, "Trip category must be set")
                .get();
    }

    public void populate(Context ctx) {
        dao.populate();
        ctx.res().setStatus(200);
        ctx.json("{ \"message\": \"Database has been populated\" }");
    }

    public void getTotalPricePerGuide(Context ctx) {
        List<GuideTotalDTO> totals = dao.getTotalPricePerGuide();
        ctx.status(200);
        ctx.json(totals);
    }

    public void getPackingWeight(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validatePrimaryKey, "Not a valid id").get();
        TripDTO tripDTO = dao.read(id);
        if (tripDTO == null) {
            ctx.status(404).json(Map.of("msg", "Trip not found"));
            return;
        }
        // Ensure packing items are populated (fetch from external API if needed)
        if (tripDTO.getPackingItems() == null || tripDTO.getPackingItems().isEmpty()) {
            tripDTO.setPackingItems(app.services.PackingService.fetchPackingItems(tripDTO.getCategory()));
        }
        double totalGrams = tripDTO.getPackingItems().stream()
                .mapToDouble(item -> item.getWeightInGrams() * Math.max(1, item.getQuantity()))
                .sum();
        ctx.status(200);
        ctx.json(Map.of("trip_id", id, "total_weight_in_grams", totalGrams));
    }


    public void linkGuide(Context ctx) {
        int tripId = ctx.pathParamAsClass("tripId", Integer.class).check(this::validatePrimaryKey, "Not a valid trip id").get();
        int guideId = ctx.pathParamAsClass("guideId", Integer.class).get();
        if (!guideDAO.validatePrimaryKey(guideId)) {
            ctx.status(404).json(Map.of("msg", "Guide not found"));
            return;
        }
        TripDTO updated = dao.linkGuide(tripId, guideId);
        if (updated == null) {
            ctx.status(404).json(Map.of("msg", "Trip not found"));
            return;
        }
        ctx.status(200).json(updated);
    }
}
