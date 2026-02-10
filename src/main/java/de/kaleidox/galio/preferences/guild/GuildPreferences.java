package de.kaleidox.galio.preferences.guild;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuildPreferences {
    @Id long guildId;
    @Singular @ElementCollection @Column(length = 8192) @Convert(converter = ReactionRoleSet.Converter.class)
    Set<ReactionRoleSet> roleSets;
}
