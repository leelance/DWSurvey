package net.diaowen.dwsurvey.repository;

import net.diaowen.dwsurvey.entity.QuMultiFillblank;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * question multi fill blank
 *
 * @author lance
 * @since 2023/3/10 14:28
 */
public interface QuMultiFillBankRepository extends CrudRepository<QuMultiFillblank, String>, JpaSpecificationExecutor<QuMultiFillblank> {
}
