package com.officemanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "office_rooms")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"floor", "seats"})
public class OfficeRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "office_room_seq")
    @SequenceGenerator(
            name = "office_room_seq",
            sequenceName = "office_room_seq",
            allocationSize = 1)
    @Column(name = "id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "floor_id")
    @JsonIgnoreProperties("rooms")
    private Floor floor;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "name")
    private String name;

    @Column(name = "x")
    private Float x = 0f;

    @Column(name = "y")
    private Float y = 0f;

    @Column(name = "width")
    private Float width = 300f;

    @Column(name = "height")
    private Float height = 200f;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "room", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("room")
    private Set<Seat> seats = new HashSet<>();
}
