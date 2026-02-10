package de.kaleidox.galio.preferences.guild;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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
    @Id                                                   long                 guildId;
    @Singular @Column(length = 8192) @ElementCollection(fetch = FetchType.EAGER)
    @Convert(converter = ReactionRoleSet.Converter.class) Set<ReactionRoleSet> roleSets;

    public Optional<ReactionRoleSet> findReactionRoleSet(String name) {
        return roleSets.stream().filter(set -> set.getName().equals(name)).findAny();
    }
}
