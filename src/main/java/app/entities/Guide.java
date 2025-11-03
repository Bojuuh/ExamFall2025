package app.entities;
import app.dtos.GuideDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guide")
public class Guide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guide_id", nullable = false, unique = true)
    private int id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;
    @Column(name = "years_of_experience", nullable = false)
    private int yearsOfExperience;

    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL)
    private List<Trip> trips = new ArrayList<>();

    public Guide(String name, String email, String phoneNumber, int yearsOfExperience){
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.yearsOfExperience = yearsOfExperience;
    }

    public Guide(GuideDTO guideDTO) {
        this.id = guideDTO.getId();
        this.name = guideDTO.getName();
        this.email = guideDTO.getEmail();
        this.phoneNumber = guideDTO.getPhoneNumber();
        if (guideDTO.getTrips() != null ) {
            guideDTO.getTrips().forEach(tripDTO -> trips.add(new Trip(tripDTO)));
        }
    }
}
