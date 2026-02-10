package de.kaleidox.galio.feature.roles;

import de.kaleidox.galio.preferences.guild.ReactionRoleBinding;
import de.kaleidox.galio.repo.GuildPreferenceRepo;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.comroid.annotations.Description;
import org.comroid.commands.Command;
import org.comroid.commands.impl.CommandManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Log
@Component
@Command("roles")
@Description("Configure reaction roles")
public class ReactionRoleService extends ListenerAdapter {
    @Autowired GuildPreferenceRepo guilds;
    @Autowired JDA                 jda;

    @Command
    @Description("Refresh all reaction role messages in this server")
    @SuppressWarnings("UnusedReturnValue")
    public String refresh(Guild guild) {
        var prefs = guilds.findById(guild.getIdLong()).orElse(null);
        if (prefs == null || prefs.getRoleSets().isEmpty()) return "There are no configured reaction roles";

        for (var roleSet : prefs.getRoleSets()) {
            var channelId = roleSet.getChannelId();
            var channel   = jda.getTextChannelById(channelId);

            if (channel == null) {
                log.warning("Could not find channel with ID %d for role set %s".formatted(channelId, roleSet));
                continue;
            }

            // wipe previous message
            List<Message> ls = List.of();
            do {
                if (ls.size() >= 2) channel.deleteMessages(ls).complete();
                else ls.stream().map(Message::delete).forEach(RestAction::queue);

                ls = channel.getHistory().retrievePast(100).complete().stream().filter(msg -> {
                    if (!(msg.getAuthor() instanceof SelfUser)) return false;
                    var embeds = msg.getEmbeds();
                    if (embeds.isEmpty()) return false;
                    var color = embeds.getFirst().getColor();
                    return color != null && color.getRGB() == roleSet.hashCode();
                }).toList();
            } while (!ls.isEmpty());

            // send new message
            var msg = roleSet.createMessage();
            channel.sendMessage(msg.build())
                    .flatMap(it -> RestAction.allOf(roleSet.getRoles()
                            .stream()
                            .map(ReactionRoleBinding::getEmoji)
                            .map(Emoji::fromFormatted)
                            .map(it::addReaction)
                            .toList()))
                    .queue();
        }

        return "Reaction messages were resent";
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void on(ApplicationStartedEvent event) {
        event.getApplicationContext().getBean(JDA.class).addEventListener(this);
        event.getApplicationContext().getBean(CommandManager.class).register(this);

        for (var guild : jda.getGuilds())
            refresh(guild);

        log.info("Initialized");
    }
}
