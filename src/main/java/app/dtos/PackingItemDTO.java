package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackingItemDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("weightInGrams")
    private int weightInGrams;
    @JsonProperty("quantity")
    private int quantity;
    @JsonProperty("description")
    private String description;
    @JsonProperty("category")
    private String category;
    @JsonProperty("createdAt")
    private ZonedDateTime createdAt;
    @JsonProperty("updatedAt")
    private ZonedDateTime updatedAt;
    @JsonProperty("buyingOptions")
    private List<BuyingOptionDTO> buyingOptions;
}
