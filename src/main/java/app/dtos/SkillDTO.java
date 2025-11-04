package app.dtos;

import app.enums.SkillCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillDTO {
    private int id;
    private String slug;
    private String name;
    private String description;
    private SkillCategory category;
    private Set<CandidateRefDTO> candidates;
}
