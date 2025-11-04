// java
// File: src/main/java/app/dtos/SkillRefDTO.java
package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillRefDTO {
    private int id;
    private String slug;
    private String name;
    private String categoryKey;
    private String description;
    private Integer popularityScore;
    private Integer averageSalary;
    private ZonedDateTime updatedAt;

    public SkillRefDTO(int id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }
}
