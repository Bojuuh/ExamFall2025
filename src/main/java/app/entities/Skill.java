package app.entities;

import app.dtos.SkillRefDTO;
import app.enums.SkillCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = {"candidateSkills"})
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private String slug;
    @Column(nullable = false)
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private SkillCategory category;

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CandidateSkill> candidateSkills = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void ensureSlug() {
        if ((slug == null || slug.isBlank()) && name != null) {
            this.slug = slugify(name);
        }
    }

    public static String slugify(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase();
        s = s.replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("-{2,}", "-");
        return s.replaceAll("(^-|-$)", "");
    }

}
