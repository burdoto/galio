package de.kaleidox.galio.components.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kaleidox.galio.components.config.model.Config;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.comroid.api.java.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;

@Log
@Configuration
public class ConfigurationProvider {
    @Bean
    public File configFile(@Autowired File botDir) {
        return new File(botDir, "config.json5");
    }

    @Bean
    @SneakyThrows
    public Config config(@Autowired ObjectMapper objectMapper, @Autowired File configFile) {
        try (
                var is = configFile.exists()
                         ? new FileInputStream(configFile)
                         : ResourceLoader.fromResourceString("/config.json5")
        ) {
            return objectMapper.readValue(is, Config.class);
        }
    }
}
