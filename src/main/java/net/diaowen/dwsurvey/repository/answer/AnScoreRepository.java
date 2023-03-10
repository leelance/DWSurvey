package net.diaowen.dwsurvey.repository.answer;

import net.diaowen.dwsurvey.entity.AnScore;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * answer score
 *
 * @author lance
 * @since 2023/3/11 00:24
 */
public interface AnScoreRepository extends CrudRepository<AnScore, String>, JpaSpecificationExecutor<AnScore> {

  /**
   * 根据quId查询答案
   *
   * @param quId 题目id
   * @return List<Object [ ]>
   */
  @Query(value = "select qu_row_id,count(qu_row_id),AVG(answser_score) from t_an_score where visibility=1 and qu_id=?1 GROUP BY qu_row_id", nativeQuery = true)
  List<Object[]> findGroupStats(String quId);
}
