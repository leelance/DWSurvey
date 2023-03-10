package net.diaowen.dwsurvey.repository.answer;

import net.diaowen.dwsurvey.entity.AnRadio;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * answer radio
 *
 * @author lance
 * @since 2023/3/10 23:55
 */
public interface AnRadioRepository extends CrudRepository<AnRadio, String>, JpaSpecificationExecutor<AnRadio> {
  /**
   * 根据quId查询答案
   *
   * @param quId 题目id
   * @return List<Object [ ]>
   */
  @Query(value = "select qu_item_id,count(qu_item_id) as count from t_an_radio where visibility=1 and qu_id=?1 GROUP BY qu_item_id", nativeQuery = true)
  List<Object[]> findGroupStats(String quId);
}
