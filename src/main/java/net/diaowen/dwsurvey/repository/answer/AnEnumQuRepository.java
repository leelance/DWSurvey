package net.diaowen.dwsurvey.repository.answer;

import net.diaowen.dwsurvey.entity.AnEnumqu;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * answer enum question
 *
 * @author lance
 * @since 2023/3/11 00:19
 */
public interface AnEnumQuRepository extends CrudRepository<AnEnumqu, String>, JpaSpecificationExecutor<AnEnumqu> {

  /**
   * 根据quId查询答案
   *
   * @param quId 题目id
   * @return List<Object [ ]>
   */
  @Query(value = "select answer,count(answer) from t_an_enumqu where visibility=1 and qu_id=?1 GROUP BY answer", nativeQuery = true)
  List<Object[]> findGroupStats(String quId);
}
