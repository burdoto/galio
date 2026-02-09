package de.kaleidox.galio.components;

import org.comroid.commands.impl.CommandManager;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CommandProvider {
    @Bean
    public CommandManager cmdr(
    ) {
        var cmdr = new CommandManager();
        cmdr.addChild(this);
        cmdr.register(this);
        return cmdr;
    }
}
