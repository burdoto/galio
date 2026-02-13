package de.kaleidox.galio.components;

import lombok.extern.java.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.comroid.annotations.Description;
import org.comroid.api.io.FileFlag;
import org.comroid.api.java.ResourceLoader;
import org.comroid.commands.Command;
import org.comroid.commands.impl.CommandManager;
import org.comroid.commands.model.CommandError;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

@Log
@Component
@Command("maintenance")
public class MaintenanceProvider {
    private static final long[]  superadmins;
    private static       boolean enabled;

    static {
        long[] ids;
        try (var json = ResourceLoader.fromResourceString("/static/superadmins.json")) {
            var list = new ObjectMapper().readTree(json);
            ids = list.values().stream().mapToLong(JsonNode::asLong).sorted().toArray();
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to load superadmins.json", e);
            ids = new long[]{ 141476933849448448L };
        }
        superadmins = ids;
    }

    public static boolean isMaintenance() {
        return enabled;
    }

    @Command(permission = "8")
    @Description("Toggle maintenance mode")
    public String toggle(User user, @Command.Arg(required = false) @Nullable Boolean state) {
        verifySuperadmin(user);

        if (state == null) state = !enabled;
        if (enabled == state) return "Maintenance mode unchanged";
        enabled = state;

        return "Maintenance mode turned " + (enabled ? "*on*" : "*off*");
    }

    @Command(permission = "8")
    @Description("Shutdown the Bot")
    public String shutdown(
            User user, @Command.Arg(value = "purgecommands",
                                    required = false) @Description("Whether to purge commands on restart") @Nullable Boolean purgeCommands
    ) {
        verifySuperadmin(user);

        if (Boolean.TRUE.equals(purgeCommands)) FileFlag.enable(DiscordProvider.COMMAND_PURGE_FILE);

        System.exit(0);
        return "Goodbye";
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void on(ApplicationStartedEvent event) {
        event.getApplicationContext().getBean(JDA.class).addEventListener(this);
        event.getApplicationContext().getBean(CommandManager.class).register(this);

        log.info("Initialized");
    }

    public boolean isSuperadmin(User user) {
        return Arrays.binarySearch(superadmins, user.getIdLong()) != -1;
    }

    private void verifySuperadmin(User user) {
        if (!isSuperadmin(user)) throw new CommandError("You are not permitted to use this maintenance command");
    }
}
