package net.diaowen.dwsurvey.repository.answer;

import net.diaowen.dwsurvey.entity.AnDFillblank;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * answer d fill blank
 *
 * @author lance
 * @since 2023/3/11 00:15
 */
public interface AndFillBlankRepository extends CrudRepository<AnDFillblank, String>, JpaSpecificationExecutor<AnDFillblank> {

  /**
   * 根据quId查询答案
   *
   * @param quId 题目id
   * @return List<Object [ ]>
   */
  @Query(value = "select qu_item_id,count(*) from t_an_dfillblank where visibility=1 and qu_id=?1 group by qu_item_id", nativeQuery = true)
  List<Object[]> findGroupStats(String quId);
}
