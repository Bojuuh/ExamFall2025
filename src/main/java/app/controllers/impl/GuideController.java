package app.controllers.impl;

import app.controllers.IController;
import app.dtos.GuideDTO;
import app.config.HibernateConfig;
import app.entities.Guide;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import app.daos.impl.GuideDAO;

import java.util.List;

public class GuideController implements IController<GuideDTO, Integer> {

    private final GuideDAO dao;

    public GuideController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dao = GuideDAO.getInstance(emf);
    }

    @Override
    public void read(Context ctx) {
        // request
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validatePrimaryKey, "Not a valid id").get();
        // DTO
        GuideDTO guideDTO = dao.read(id);
        // response
        ctx.res().setStatus(200);
        ctx.json(guideDTO, GuideDTO.class);
    }

    @Override
    public void readAll(Context ctx) {
        // List of DTOS
        List<GuideDTO> guideDTOS = dao.readAll();
        // response
        ctx.res().setStatus(200);
        ctx.json(guideDTOS, GuideDTO.class);
    }

    @Override
    public void create(Context ctx) {
        // request
        GuideDTO jsonRequest = ctx.bodyAsClass(GuideDTO.class);
        // DTO
        GuideDTO guideDTO = dao.create(jsonRequest);
        // response
        ctx.res().setStatus(201);
        ctx.json(guideDTO, GuideDTO.class);
    }

    @Override
    public void update(Context ctx) {
        // request
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validatePrimaryKey, "Not a valid id").get();
        // dto
        GuideDTO guideDTO = dao.update(id, validateEntity(ctx));
        // response
        ctx.res().setStatus(200);
        ctx.json(guideDTO, Guide.class);
    }

    @Override
    public void delete(Context ctx) {
        // request
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validatePrimaryKey, "Not a valid id").get();
        dao.delete(id);
        // response
        ctx.res().setStatus(204);
    }

    @Override
    public boolean validatePrimaryKey(Integer integer) {
        return dao.validatePrimaryKey(integer);
    }

    @Override
    public GuideDTO validateEntity(Context ctx) {
        return ctx.bodyValidator(GuideDTO.class)
                .check(g -> g.getName() != null && !g.getName().isEmpty(), "Guide name must be set")
                .check(g -> g.getEmail() != null && !g.getEmail().isEmpty(), "Guide email must be set")
                .check(g -> g.getPhoneNumber() != null, "Guide phoneNumber must be set")
                .get();
    }

}

