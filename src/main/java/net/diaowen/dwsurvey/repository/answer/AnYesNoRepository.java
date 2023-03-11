package net.diaowen.dwsurvey.repository.answer;

import net.diaowen.dwsurvey.entity.AnYesno;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Map;

/**
 * 答案yes/no
 *
 * @author lance
 * @since 2023/3/10 23:46
 */
public interface AnYesNoRepository extends CrudRepository<AnYesno, String>, JpaSpecificationExecutor<AnYesno> {

  /**
   * 根据quId查询答案
   *
   * @param quId 题目id
   * @return List<Object [ ]>
   */
  @Query(value = "select yesno_answer as emptyCount,count(yesno_answer)as blankCount from t_an_yesno where visibility=1 and qu_id=?1 GROUP BY yesno_answer", nativeQuery = true)
  List<Map<String, Object>> findGroupStats(String quId);
}
