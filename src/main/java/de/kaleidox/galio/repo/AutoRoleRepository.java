package de.kaleidox.galio.repo;

import de.kaleidox.galio.feature.autorole.model.AutoRoleMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface AutoRoleRepository extends CrudRepository<AutoRoleMapping, AutoRoleMapping.Key> {
    Collection<AutoRoleMapping> findAllByGuildId(long guildId);
}
