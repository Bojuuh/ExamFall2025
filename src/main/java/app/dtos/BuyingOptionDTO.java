package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuyingOptionDTO {
    @JsonProperty("shopName")
    private String shopName;
    @JsonProperty("shopUrl")
    private String shopUrl;
    @JsonProperty("price")
    private double price;
}
