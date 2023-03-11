package net.diaowen.dwsurvey.repository.answer;

import net.diaowen.dwsurvey.entity.AnOrder;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Map;

/**
 * answer order
 *
 * @author lance
 * @since 2023/3/11 00:28
 */
public interface AnOrderRepository extends CrudRepository<AnOrder, String>, JpaSpecificationExecutor<AnOrder> {

  /**
   * 根据quId查询答案
   *
   * @param quId 题目id
   * @return List<Object [ ]>
   */
  @Query(value = "select qu_row_id as emptyCount,sum(ordery_num) as blankCount from t_an_order where visibility=1 and qu_id=?1 group by qu_row_id order by blankCount", nativeQuery = true)
  List<Map<String, Object>> findGroupStats(String quId);
}
