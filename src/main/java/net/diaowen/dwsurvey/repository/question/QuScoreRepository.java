package net.diaowen.dwsurvey.repository.question;

import net.diaowen.dwsurvey.entity.QuScore;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 题目分数
 *
 * @author lance
 * @since 2023/3/10 12:34
 */
public interface QuScoreRepository extends CrudRepository<QuScore, String>, JpaSpecificationExecutor<QuScore> {
  /**
   * 根据题目id查询题目分数集合
   *
   * @param quId       题目id
   * @param visibility visibility
   * @return List<QuScore>
   */
  List<QuScore> findByQuIdAndVisibility(String quId, int visibility);

  /**
   * score 删除后重新排序
   *
   * @param quId      题目id
   * @param orderById 排序id
   */
  @Modifying
  @Query(value = "update QuScore set orderById=orderById-1 where quId=:quId and orderById>=:orderId")
  void subScoreOrderId(@Param("quId") String quId, @Param("orderId") int orderById);
}
