package de.kaleidox.galio.preferences.guild;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.Optional;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuildPreferences {
    @Id                                         long                 guildId;
    @Singular @OneToMany @Column(length = 8192) Set<ReactionRoleSet> roleSets;

    public Optional<ReactionRoleSet> findReactionRoleSet(String name) {
        return roleSets.stream().filter(set -> set.getName().equals(name)).findAny();
    }
}
