package de.kaleidox.galio;

import lombok.extern.java.Log;
import org.comroid.commands.impl.CommandManager;
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

    @Bean
    public File botDir() {
        return new File("/srv/galio/");
    }

    @Order
    @EventListener
    public void on(ApplicationStartedEvent event) {
        event.getApplicationContext().getBean(CommandManager.class).initialize();

        log.info("Initialized");
    }
}
