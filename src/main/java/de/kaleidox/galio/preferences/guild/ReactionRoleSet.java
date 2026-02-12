package de.kaleidox.galio.preferences.guild;

import de.kaleidox.galio.repo.ReactionSetRepo;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.comroid.annotations.Instance;
import org.comroid.commands.autofill.IAutoFillProvider;
import org.comroid.commands.impl.CommandUsage;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static de.kaleidox.galio.util.ApplicationContextProvider.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ReactionRoleSet.Key.class)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "message_id") })
public class ReactionRoleSet {
    @Id long   guildId;
    @Id String name;
    String description;
    long   channelId;
    @ElementCollection(fetch = FetchType.EAGER) List<ReactionRoleBinding> roles;
    @Nullable                                   Long                      messageId;

    public MessageCreateBuilder createMessage() {
        return new MessageCreateBuilder().addEmbeds(toEmbed().build());
    }

    public EmbedBuilder toEmbed() {
        var embed = new EmbedBuilder().setTitle(name)
                .setDescription(description)
                .setColor(new Color(hashCode()))
                .setFooter("Select your desired roles by reacting below");
        roles.stream().map(ReactionRoleBinding::toField).forEachOrdered(embed::addField);
        return embed;
    }

    public Optional<ReactionRoleBinding> findBinding(String name) {
        return roles.stream().filter(role -> role.getName().equals(name)).findAny();
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, name);
    }

    public enum AutoFillSetNames implements IAutoFillProvider {
        @Instance INSTANCE;

        @Override
        public Stream<? extends CharSequence> autoFill(CommandUsage usage, String argName, String currentValue) {
            return usage.fromContext(Guild.class)
                    .flatMap(guild -> bean(ReactionSetRepo.class).findAllByGuildId(guild.getIdLong()).stream())
                    .map(ReactionRoleSet::getName);
        }
    }

    public enum AutoFillRoleNames implements IAutoFillProvider {
        @Instance INSTANCE;

        @Override
        public Stream<? extends CharSequence> autoFill(CommandUsage usage, String argName, String currentValue) {
            return usage.fromContext(Guild.class)
                    .flatMap(guild -> bean(ReactionSetRepo.class).findAllByGuildId(guild.getIdLong()).stream())
                    .flatMap(set -> set.getRoles().stream())
                    .map(ReactionRoleBinding::getName);
        }
    }

    public record Key(long guildId, String name) {}
}
