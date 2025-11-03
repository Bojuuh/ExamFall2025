package app.dtos;

import app.entities.Guide;
import app.entities.Trip;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GuideDTO {
    private int id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("phonenumber")
    private String phoneNumber;
    @JsonProperty("years_of_experience")
    private int yearsOfExperience;
    private List<TripDTO> trips = new ArrayList<>();

    // Keep original behavior
    public GuideDTO(Guide g) {
        this(g, true);
    }

    // New constructor to control inclusion of nested TripDTO objects.
    // When includeTrips is false, trips list remains empty preventing recursion.
    public GuideDTO(Guide g, boolean includeTrips) {
        this.id = g.getId();
        this.name = g.getName();
        this.email = g.getEmail();
        this.phoneNumber = g.getPhoneNumber();
        this.yearsOfExperience = g.getYearsOfExperience();
        if (includeTrips && g.getTrips() != null) {
            for (Trip t : g.getTrips()) {
                this.trips.add(new TripDTO(t, false)); // create TripDTO without nested GuideDTO
            }
        }
    }
}
