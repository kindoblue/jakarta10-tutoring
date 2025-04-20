package com.officemanagement.dto;

import com.officemanagement.model.OfficeRoom;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class EmbeddedRoomDTO {
    private Long id;
    private String name;
    private String roomNumber;
    private LocalDateTime createdAt;
    private Float x;
    private Float y;
    private Float width;
    private Float height;
    private Set<SeatDTO> seats;

    public EmbeddedRoomDTO(OfficeRoom room) {
        this.id = room.getId();
        this.name = room.getName();
        this.roomNumber = room.getRoomNumber();
        this.createdAt = room.getCreatedAt();
        this.x = room.getX();
        this.y = room.getY();
        this.width = room.getWidth();
        this.height = room.getHeight();
        if (room.getSeats() != null) {
            this.seats = room.getSeats().stream().map(SeatDTO::new).collect(Collectors.toSet());
        } else {
            this.seats = java.util.Collections.emptySet();
        }
    }
}
