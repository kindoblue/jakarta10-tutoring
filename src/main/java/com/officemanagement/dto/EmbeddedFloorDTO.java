package com.officemanagement.dto;

import com.officemanagement.model.Floor;
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
public class EmbeddedFloorDTO {
    private Long id;
    private String name;
    private Integer floorNumber;
    private LocalDateTime createdAt;
    private Set<EmbeddedRoomDTO> rooms;
    private boolean hasPlanimetry;

    public EmbeddedFloorDTO(Floor floor) {
        this.id = floor.getId();
        this.name = floor.getName();
        this.floorNumber = floor.getFloorNumber();
        this.createdAt = floor.getCreatedAt();
        if (floor.getRooms() != null) {
            this.rooms =
                    floor.getRooms().stream().map(EmbeddedRoomDTO::new).collect(Collectors.toSet());
        } else {
            this.rooms = java.util.Collections.emptySet();
        }
        this.hasPlanimetry = floor.getPlanimetry() != null;
    }
}
