package de.kaleidox.galio.preferences.guild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.comroid.api.attr.Described;
import org.comroid.api.attr.Named;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRoleBinding implements Named, Described {
    String emoji;
    String name;
    String description;
    long   roleId;

    public MessageEmbed.Field toField() {
        return new MessageEmbed.Field(emoji + " - " + name, description, false);
    }
}
