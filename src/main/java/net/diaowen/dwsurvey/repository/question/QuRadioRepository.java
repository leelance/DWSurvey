package net.diaowen.dwsurvey.repository.question;

import net.diaowen.dwsurvey.entity.QuRadio;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Question radio
 *
 * @author lance
 * @since 2023/3/10 14:10
 */
public interface QuRadioRepository extends CrudRepository<QuRadio, String>, JpaSpecificationExecutor<QuRadio> {

  /**
   * Radio 删除后重新排序
   *
   * @param quId      题目id
   * @param orderById 排序id
   */
  @Modifying
  @Query(value = "update QuRadio set orderById=orderById-1 where quId=:quId and orderById>=:orderId")
  void subRadioOrderId(@Param("quId") String quId, @Param("orderId") int orderById);
}
