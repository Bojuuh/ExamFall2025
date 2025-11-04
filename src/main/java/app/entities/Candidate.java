package app.entities;
import app.dtos.CandidateDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String phone;
    private String education;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CandidateSkill> candidateSkills;

    public void addSkill(Skill skill) {
        if (skill == null) return;
        boolean exists = candidateSkills.stream()
                .anyMatch(cs -> cs.getSkill().equals(skill));
        if (exists) return;

        CandidateSkill cs = CandidateSkill.builder()
                .candidate(this)
                .skill(skill)
                .build();
        candidateSkills.add(cs);
        skill.getCandidateSkills().add(cs);
    }

    public void removeSkill(Skill skill) {
        if (skill == null) return;
        candidateSkills.removeIf(cs -> {
            if (cs.getSkill().equals(skill)) {
                skill.getCandidateSkills().remove(cs);
                cs.setCandidate(null);
                cs.setSkill(null);
                return true;
            }
            return false;
        });
    }
}
