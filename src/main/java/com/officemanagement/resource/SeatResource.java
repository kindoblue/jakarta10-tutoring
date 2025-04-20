package com.officemanagement.resource;

import com.officemanagement.dto.SeatDTO;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import org.hibernate.Hibernate;

@Path("/seats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Tag(name = "Seat", description = "Operations related to seats")
public class SeatResource {
    @Inject EntityManager entityManager;

    @GET
    @Path("/{id}")
    @Operation(summary = "Get seat by ID", description = "Returns a seat by its ID.")
    public Response getSeat(@PathParam("id") Long id) {
        Seat seat =
                entityManager
                        .createQuery(
                                "SELECT s FROM Seat s LEFT JOIN FETCH s.employees WHERE s.id = :id",
                                Seat.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

        if (seat == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Hibernate.initialize(seat.getEmployees());
        Hibernate.initialize(seat.getRoom());
        if (seat.getRoom() != null) {
            Hibernate.initialize(seat.getRoom().getFloor());
        }

        SeatDTO dto = new SeatDTO(seat);
        return Response.ok(dto).build();
    }

    @POST
    @Transactional
    @Operation(
            summary = "Create a new seat",
            description = "Creates a new seat with the provided details.")
    public Response createSeat(Seat seat) {
        if (seat == null || seat.getSeatNumber() == null || seat.getSeatNumber().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Seat number is required")
                    .build();
        }
        if (seat.getRoom() == null || seat.getRoom().getId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room reference is required")
                    .build();
        }

        OfficeRoom room = entityManager.find(OfficeRoom.class, seat.getRoom().getId());
        if (room == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Referenced room does not exist")
                    .build();
        }

        Long count =
                entityManager
                        .createQuery(
                                "SELECT COUNT(s) FROM Seat s WHERE s.room.id = :roomId AND s.seatNumber = :seatNumber",
                                Long.class)
                        .setParameter("roomId", room.getId())
                        .setParameter("seatNumber", seat.getSeatNumber())
                        .getSingleResult();

        if (count > 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(
                            "A seat with number "
                                    + seat.getSeatNumber()
                                    + " already exists in this room")
                    .build();
        }

        seat.setRoom(room);
        seat.setCreatedAt(LocalDateTime.now());
        entityManager.persist(seat);
        entityManager.flush();

        Hibernate.initialize(seat.getEmployees());

        SeatDTO dto = new SeatDTO(seat);
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update a seat", description = "Updates the details of an existing seat.")
    public Response updateSeat(@PathParam("id") Long id, Seat seatData) {
        if (seatData == null
                || seatData.getSeatNumber() == null
                || seatData.getSeatNumber().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Seat number is required")
                    .build();
        }
        if (seatData.getRoom() == null || seatData.getRoom().getId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room reference is required")
                    .build();
        }

        Seat existingSeat = entityManager.find(Seat.class, id);
        if (existingSeat == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        OfficeRoom room = entityManager.find(OfficeRoom.class, seatData.getRoom().getId());
        if (room == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Referenced room does not exist")
                    .build();
        }

        if (!existingSeat.getSeatNumber().equals(seatData.getSeatNumber())
                || !existingSeat.getRoom().getId().equals(room.getId())) {
            Long count =
                    entityManager
                            .createQuery(
                                    "SELECT COUNT(s) FROM Seat s WHERE s.room.id = :roomId AND s.seatNumber = :seatNumber AND s.id != :seatId",
                                    Long.class)
                            .setParameter("roomId", room.getId())
                            .setParameter("seatNumber", seatData.getSeatNumber())
                            .setParameter("seatId", id)
                            .getSingleResult();

            if (count > 0) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(
                                "A seat with number "
                                        + seatData.getSeatNumber()
                                        + " already exists in this room")
                        .build();
            }
        }

        existingSeat.setSeatNumber(seatData.getSeatNumber());
        existingSeat.setRoom(room);
        if (seatData.getX() != null) existingSeat.setX(seatData.getX());
        if (seatData.getY() != null) existingSeat.setY(seatData.getY());
        if (seatData.getWidth() != null) existingSeat.setWidth(seatData.getWidth());
        if (seatData.getHeight() != null) existingSeat.setHeight(seatData.getHeight());
        if (seatData.getRotation() != null) existingSeat.setRotation(seatData.getRotation());

        Seat updatedSeat = entityManager.merge(existingSeat);
        entityManager.flush();

        Hibernate.initialize(updatedSeat.getEmployees());

        SeatDTO dto = new SeatDTO(updatedSeat);
        return Response.ok(dto).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete a seat", description = "Deletes a seat by its ID.")
    public Response deleteSeat(@PathParam("id") Long id) {
        Seat seat = entityManager.find(Seat.class, id);
        if (seat == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (seat.getEmployees() != null && !seat.getEmployees().isEmpty()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Cannot delete seat with assigned employees. Unassign employees first.")
                    .build();
        }

        entityManager.remove(seat);
        return Response.noContent().build();
    }
}
