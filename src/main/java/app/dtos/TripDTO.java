package app.dtos;

import app.entities.Trip;
import app.entities.Guide;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripDTO {
    private int id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    @JsonProperty("end_time")
    private LocalDateTime endTime;
    @JsonProperty("latitude")
    private double latitude;
    @JsonProperty("longitude")
    private double longitude;
    @JsonProperty("price")
    private double price;
    @JsonProperty("category")
    private String category;
    @JsonProperty("guide_id")
    private int guideId;
    @JsonProperty("guide")
    private GuideDTO guide;
    @JsonProperty("packing_items")
    private List<PackingItemDTO> packingItems = new ArrayList<>();

    // Default behavior: include guide DTO (existing behavior)
    public TripDTO(Trip t) {
        this(t, true);
    }

    // New constructor to control inclusion of nested GuideDTO to prevent recursion
    public TripDTO(Trip t, boolean includeGuide) {
        this.id = t.getId();
        this.name = t.getName();
        this.startTime = t.getStartTime();
        this.endTime = t.getEndTime();
        this.latitude = t.getLatitude();
        this.longitude = t.getLongitude();
        this.price = t.getPrice();
        this.category = t.getCategory();
        this.guideId = t.getGuide() != null ? t.getGuide().getId() : 0;
        if (includeGuide) {
            this.guide = t.getGuide() != null ? new GuideDTO(t.getGuide(), false) : null; // avoid cascading population
        } else {
            this.guide = null;
        }
    }
}
