package de.kaleidox.galio.repo;

import de.kaleidox.galio.preferences.guild.GuildPreferences;
import de.kaleidox.galio.preferences.guild.ReactionRoleSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public interface GuildPreferenceRepo extends CrudRepository<GuildPreferences, @NotNull Long> {
    default Optional<ReactionRoleSet> findReactionRoleSet(long guildId, String name) {
        return findById(guildId).flatMap(prefs -> prefs.findReactionRoleSet(name));
    }

    default Optional<ReactionRoleSet> findReactionRoleSet(long guildId, long messageId) {
        return findById(guildId).stream()
                .flatMap(prefs -> prefs.getRoleSets().stream())
                .filter(set -> Objects.equals(set.getMessageId(), messageId))
                .findAny();
    }
}
