package app.dtos;

import app.entities.Candidate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CandidateDTO {
    private int id;
    private String name;
    private String phone;
    private String education;
    private Set<SkillRefDTO> skills;

    public CandidateDTO(Candidate c) {
        this.id = c.getId();
        this.name = c.getName();
        this.phone = c.getPhone();
        this.education = c.getEducation();

    }
}
