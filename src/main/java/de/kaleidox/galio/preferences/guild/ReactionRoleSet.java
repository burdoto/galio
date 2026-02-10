package de.kaleidox.galio.preferences.guild;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.comroid.api.attr.Described;
import org.comroid.api.attr.Named;

import java.awt.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

import static de.kaleidox.galio.util.ApplicationContextProvider.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRoleSet implements Named, Described {
    String                    name;
    String                    description;
    long                      channelId;
    List<ReactionRoleBinding> roles;

    public MessageCreateBuilder createMessage() {
        var embed = new EmbedBuilder().setTitle(name)
                .setDescription(description)
                .setColor(new Color(hashCode()))
                .setFooter("Select your desired roles by reacting below");

        roles.stream().map(ReactionRoleBinding::toField).forEachOrdered(embed::addField);

        return new MessageCreateBuilder().addEmbeds(embed.build());
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, name);
    }

    @Value
    @jakarta.persistence.Converter
    public static class Converter implements AttributeConverter<ReactionRoleSet, String> {
        @Override
        @SneakyThrows
        public String convertToDatabaseColumn(ReactionRoleSet attribute) {
            try (var sw = new StringWriter()) {
                bean(ObjectMapper.class).writeValue(sw, attribute);
                return sw.toString();
            }
        }

        @Override
        @SneakyThrows
        public ReactionRoleSet convertToEntityAttribute(String dbData) {
            try (var sr = new StringReader(dbData)) {
                return bean(ObjectMapper.class).readValue(sr, ReactionRoleSet.class);
            }
        }
    }
}
