package de.kaleidox.galio.repo;

import de.kaleidox.galio.preferences.guild.GuildPreferences;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuildPreferenceRepo extends CrudRepository<GuildPreferences, @NotNull Long> {
}
