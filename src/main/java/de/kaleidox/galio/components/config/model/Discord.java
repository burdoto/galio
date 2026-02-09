package de.kaleidox.galio.components.config.model;

import lombok.Data;
import org.comroid.annotations.Ignore;
import org.comroid.api.config.ConfigurationManager;

@Data
public final class Discord {
    @Ignore(ConfigurationManager.Presentation.class) String token;
}
