package de.kaleidox.galio.preferences.guild;

import jakarta.persistence.Basic;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRoleBinding {
    @Basic String emoji;
    @Basic String name;
    @Basic String description;
    @Basic long   roleId;

    public MessageEmbed.Field toField() {
        return new MessageEmbed.Field(emoji + " - " + name, description, false);
    }
}
