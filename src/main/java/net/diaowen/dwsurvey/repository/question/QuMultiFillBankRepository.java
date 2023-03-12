package net.diaowen.dwsurvey.repository.question;

import net.diaowen.dwsurvey.entity.QuMultiFillblank;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * question multi fill blank
 *
 * @author lance
 * @since 2023/3/10 14:28
 */
public interface QuMultiFillBankRepository extends CrudRepository<QuMultiFillblank, String>, JpaSpecificationExecutor<QuMultiFillblank> {

  /**
   * multi fill blank 删除后重新排序
   *
   * @param quId      题目id
   * @param orderById 排序id
   */
  @Modifying
  @Query(value = "update QuMultiFillblank set orderById=orderById-1 where quId=:quId and orderById>=:orderId")
  void subMultiFillBlankOrderId(@Param("quId") String quId, @Param("orderId") int orderById);
}
