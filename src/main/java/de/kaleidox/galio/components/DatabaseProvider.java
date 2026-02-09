package de.kaleidox.galio.components;

import de.kaleidox.galio.components.config.model.Config;
import org.mariadb.jdbc.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseProvider {
    @Bean
    public DataSource database(@Autowired Config config) {
        var database = config.getDatabase();
        return DataSourceBuilder.create()
                .driverClassName(Driver.class.getCanonicalName())
                .url(database.getUri())
                .username(database.getUsername())
                .password(database.getPassword())
                .build();
    }
}
