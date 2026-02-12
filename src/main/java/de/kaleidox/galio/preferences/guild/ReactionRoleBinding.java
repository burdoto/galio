package de.kaleidox.galio.preferences.guild;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.hibernate.annotations.Collate;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRoleBinding {
    @Collate("utf8mb4_uca1400_ai_ci") String emoji;
    @Collate("utf8mb4_uca1400_ai_ci") String name;
    @Collate("utf8mb4_uca1400_ai_ci") String description;
    @Collate("utf8mb4_uca1400_ai_ci") long   roleId;

    public MessageEmbed.Field toField() {
        return new MessageEmbed.Field(emoji + " - " + name, description, false);
    }
}
