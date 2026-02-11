package de.kaleidox.galio.util;

import lombok.Value;
import net.dv8tion.jda.api.entities.emoji.Emoji;

@Value
public class Constant {
    public static final Emoji EMOJI_WARNING = Emoji.fromUnicode("⚠️");

    private Constant() {
        throw new UnsupportedOperationException();
    }
}
