package net.diaowen.dwsurvey.repository.answer;

import net.diaowen.dwsurvey.entity.AnAnswer;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * answer
 *
 * @author lance
 * @since 2023/3/11 00:12
 */
public interface AnAnswerRepository extends CrudRepository<AnAnswer, String>, JpaSpecificationExecutor<AnAnswer> {

  /**
   * 根据quId查询答案
   *
   * @param quId 题目id
   * @return List<Object [ ]>
   */
  @Query(value = "select count(case when answer='' then answer end) emptyCount, count(case when answer!='' then answer end) blankCount from t_an_answer where visibility=1 and qu_id=?1", nativeQuery = true)
  Object[] findGroupStats(String quId);
}
