package net.diaowen.dwsurvey.repository;

import net.diaowen.dwsurvey.entity.QuCheckbox;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * question checkbox
 *
 * @author lance
 * @since 2023/3/10 14:20
 */
public interface QuCheckBoxRepository extends CrudRepository<QuCheckbox, String>, JpaSpecificationExecutor<QuCheckbox> {
}
