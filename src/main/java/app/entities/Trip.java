package app.entities;
import app.dtos.TripDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trip")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id", nullable = false, unique = true)
    private int id;

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    @Column(name = "latitude", nullable = false)
    private double latitude;
    @Column(name = "longitude", nullable = false)
    private double longitude;
    @Column(name = "price", nullable = false)
    private double price;
    @Column(name = "category", nullable = false)
    private String category;

    @ManyToOne
    @JoinColumn(name = "guide_id")
    private Guide guide;

    public Trip(TripDTO tripDTO) {
        this.id = tripDTO.getId();
        this.name = tripDTO.getName();
        this.startTime = tripDTO.getStartTime();
        this.endTime = tripDTO.getEndTime();
        this.latitude = tripDTO.getLatitude();
        this.longitude = tripDTO.getLongitude();
        this.price = tripDTO.getPrice();
        this.category = tripDTO.getCategory();
    }
}
