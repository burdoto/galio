package de.kaleidox.galio.components;

import de.kaleidox.galio.components.config.model.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.comroid.api.io.FileFlag;
import org.comroid.commands.impl.CommandManager;
import org.comroid.commands.impl.discord.JdaCommandAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DiscordProvider {
    public static final File COMMAND_PURGE_FILE = new File("./.purge_commands");

    @Bean
    public JDA jda(@Autowired Config config) throws InterruptedException {
        return JDABuilder.create(config.getDiscord().getToken(), GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .addEventListeners(this)
                .build()
                .awaitReady();
    }

    @Bean
    @ConditionalOnBean({ CommandManager.class, JDA.class })
    public JdaCommandAdapter cmdrJdaAdapter(@Autowired CommandManager cmdr, @Autowired JDA jda) {
        var adp = new JdaCommandAdapter(cmdr, jda);
        adp.setPurgeCommands(FileFlag.consume(COMMAND_PURGE_FILE));
        cmdr.addChild(adp);
        return adp;
    }
}
