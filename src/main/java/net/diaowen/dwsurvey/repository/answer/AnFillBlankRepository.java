package net.diaowen.dwsurvey.repository.answer;

import net.diaowen.dwsurvey.entity.AnFillblank;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * answer fill blank
 *
 * @author lance
 * @since 2023/3/11 00:06
 */
public interface AnFillBlankRepository extends CrudRepository<AnFillblank, String>, JpaSpecificationExecutor<AnFillblank> {

  /**
   * 根据quId查询答案
   *
   * @param quId 题目id
   * @return List<Object [ ]>
   */
  @Query(value = "select count(case when answer='' then answer end) emptyCount, count(case when answer!='' then answer end) blankCount from t_an_fillblank where  visibility=1 and qu_id=?1", nativeQuery = true)
  Object[] findGroupStats(String quId);
}
