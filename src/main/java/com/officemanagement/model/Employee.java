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
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_seq")
    @SequenceGenerator(name = "employee_seq", sequenceName = "employee_seq", allocationSize = 1)
    @Column(name = "id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "full_name")
    @ToString.Include
    private String fullName;

    @ToString.Include private String occupation;

    @ManyToMany
    @JoinTable(
            name = "employee_seat_assignments",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id"))
    @JsonIgnoreProperties("employees")
    private Set<Seat> seats = new HashSet<>();

    @Column(name = "created_at")
    @ToString.Include
    private LocalDateTime createdAt;

    public void addSeat(Seat seat) {
        seats.add(seat);
        seat.getEmployees().add(this);
    }

    public void removeSeat(Seat seat) {
        seats.remove(seat);
        seat.getEmployees().remove(this);
    }
}
