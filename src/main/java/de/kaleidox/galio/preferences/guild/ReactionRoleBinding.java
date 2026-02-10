package de.kaleidox.galio.preferences.guild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRoleBinding {
    String emoji;
    String name;
    String description;
    long   roleId;

    public MessageEmbed.Field toField() {
        return new MessageEmbed.Field(emoji + " - " + name, description, false);
    }
}
