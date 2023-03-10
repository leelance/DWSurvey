package net.diaowen.dwsurvey.repository;

import net.diaowen.dwsurvey.entity.QuRadio;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * Question radio
 *
 * @author lance
 * @since 2023/3/10 14:10
 */
public interface QuRadioRepository extends CrudRepository<QuRadio, String>, JpaSpecificationExecutor<QuRadio> {
}
