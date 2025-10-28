package app.entities;
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
    private int id;

    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double latitude;
    private double longitude;
    private double price;
    private String category;

    @ManyToOne
    private Guide guide;
}
