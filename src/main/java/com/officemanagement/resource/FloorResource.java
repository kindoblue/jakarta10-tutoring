package com.officemanagement.resource;

import com.officemanagement.dto.FloorDTO;
import com.officemanagement.model.Floor;
import com.officemanagement.model.FloorPlanimetry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@Path("/floors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class FloorResource {
    @Inject EntityManager entityManager;

    private static final Logger LOG = Logger.getLogger(FloorResource.class);

    @GET
    public Response getAllFloors() {
        List<Floor> floors =
                entityManager
                        .createQuery(
                                "SELECT DISTINCT f FROM Floor f LEFT JOIN FETCH f.rooms",
                                Floor.class)
                        .getResultList();

        List<FloorDTO> dtos = floors.stream().map(FloorDTO::new).collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/{id}")
    public Response getFloor(@PathParam("id") Long id) {
        Floor floor =
                entityManager
                        .createQuery(
                                "SELECT f FROM Floor f LEFT JOIN FETCH f.rooms WHERE f.id = :id",
                                Floor.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

        if (floor == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        FloorDTO dto = new FloorDTO(floor);
        return Response.ok(dto).build();
    }

    @GET
    @Path("/{id}/svg")
    @Produces("image/svg+xml")
    public Response getFloorPlan(@PathParam("id") Long id) {
        FloorPlanimetry planimetry = entityManager.find(FloorPlanimetry.class, id);
        if (planimetry == null
                || planimetry.getPlanimetry() == null
                || planimetry.getPlanimetry().isEmpty()) {
            LOG.warnf("Planimetry not found or empty for floor %d", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No floor plan found for this floor.")
                    .build();
        }

        LOG.infof("Returning planimetry for floor %d", id);
        return Response.ok(planimetry.getPlanimetry()).build();
    }

    @POST
    @Transactional
    public Response createFloor(Floor floor) {
        if (floor == null
                || floor.getName() == null
                || floor.getName().trim().isEmpty()
                || floor.getFloorNumber() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Name and floor number are required")
                    .build();
        }

        Long count =
                entityManager
                        .createQuery(
                                "SELECT COUNT(f) FROM Floor f WHERE f.floorNumber = :number",
                                Long.class)
                        .setParameter("number", floor.getFloorNumber())
                        .getSingleResult();
        if (count > 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Floor number " + floor.getFloorNumber() + " already exists.")
                    .build();
        }

        floor.setCreatedAt(LocalDateTime.now());
        entityManager.persist(floor);
        entityManager.flush();

        FloorDTO dto = new FloorDTO(floor);
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateFloor(@PathParam("id") Long id, Floor floorData) {
        if (floorData == null
                || floorData.getName() == null
                || floorData.getName().trim().isEmpty()
                || floorData.getFloorNumber() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Name and floor number are required")
                    .build();
        }

        Floor existingFloor = entityManager.find(Floor.class, id);
        if (existingFloor == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!existingFloor.getFloorNumber().equals(floorData.getFloorNumber())) {
            Long count =
                    entityManager
                            .createQuery(
                                    "SELECT COUNT(f) FROM Floor f WHERE f.floorNumber = :number AND f.id != :id",
                                    Long.class)
                            .setParameter("number", floorData.getFloorNumber())
                            .setParameter("id", id)
                            .getSingleResult();
            if (count > 0) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Floor number " + floorData.getFloorNumber() + " already exists.")
                        .build();
            }
            existingFloor.setFloorNumber(floorData.getFloorNumber());
        }

        existingFloor.setName(floorData.getName());

        Floor updatedFloor = entityManager.merge(existingFloor);
        entityManager.flush();

        FloorDTO dto = new FloorDTO(updatedFloor);
        return Response.ok(dto).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteFloor(@PathParam("id") Long id) {
        Floor floor = entityManager.find(Floor.class, id);
        if (floor == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Long roomCount =
                entityManager
                        .createQuery(
                                "SELECT COUNT(r) FROM OfficeRoom r WHERE r.floor.id = :floorId",
                                Long.class)
                        .setParameter("floorId", id)
                        .getSingleResult();
        if (roomCount > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Cannot delete floor with existing rooms.")
                    .build();
        }

        FloorPlanimetry planimetry = entityManager.find(FloorPlanimetry.class, id);
        if (planimetry != null) {
            LOG.infof("Removing planimetry for floor %d", id);
            entityManager.remove(planimetry);
        }

        LOG.infof("Removing floor %d", id);
        entityManager.remove(floor);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/svg")
    @Consumes(MediaType.TEXT_PLAIN)
    @Transactional
    public Response uploadFloorPlan(@PathParam("id") Long id, String svgData) {
        Floor floor = entityManager.find(Floor.class, id);
        if (floor == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (svgData == null || svgData.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("SVG data cannot be empty")
                    .build();
        }

        FloorPlanimetry planimetry = entityManager.find(FloorPlanimetry.class, id);

        if (planimetry == null) {
            LOG.infof("Creating new planimetry for floor %d", id);
            planimetry = new FloorPlanimetry();
            planimetry.setFloor(floor);
            planimetry.setPlanimetry(svgData);
            entityManager.persist(planimetry);
        } else {
            LOG.infof("Updating existing planimetry for floor %d", id);
            planimetry.setPlanimetry(svgData);
            entityManager.merge(planimetry);
        }
        entityManager.flush();

        Floor updatedFloor = entityManager.find(Floor.class, id);
        FloorDTO dto = new FloorDTO(updatedFloor);
        return Response.ok(dto).build();
    }
}
