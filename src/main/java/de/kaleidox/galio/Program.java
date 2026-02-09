package de.kaleidox.galio;

import de.kaleidox.galio.components.DiscordProvider;
import lombok.extern.java.Log;
import org.comroid.annotations.Description;
import org.comroid.api.io.FileFlag;
import org.comroid.commands.Command;
import org.comroid.commands.impl.CommandManager;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.File;

@Log
@SpringBootApplication
@EnableJpaRepositories(basePackages = "de.kaleidox.galio.repo")
public class Program {
    public static void main(String[] args) {
        SpringApplication.run(Program.class, args);
    }

    @Command(permission = "8")
    @Description("Shutdown the Bot")
    public static String shutdown(
            @Command.Arg(value = "purgecommands",
                         required = false) @Description("Whether to purge commands on restart") @Nullable Boolean purgeCommands
    ) {
        if (Boolean.TRUE.equals(purgeCommands)) FileFlag.enable(DiscordProvider.COMMAND_PURGE_FILE);
        System.exit(0);
        return "Goodbye";
    }

    @Bean
    public File botDir() {
        return new File("/srv/galio/");
    }

    @Order
    @EventListener
    public void on(ApplicationStartedEvent event) {
        var commandManager = event.getApplicationContext().getBean(CommandManager.class);
        commandManager.register(this);
        commandManager.initialize();

        log.info("Initialized");
    }
}
