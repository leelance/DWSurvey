package net.diaowen.dwsurvey.repository.question;

import net.diaowen.dwsurvey.entity.QuOrderby;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * question order by
 *
 * @author lance
 * @since 2023/3/10 12:43
 */
public interface QuOrderByRepository extends CrudRepository<QuOrderby, String>, JpaSpecificationExecutor<QuOrderby> {
  /**
   * 根据题目id查询题目顺序
   *
   * @param quId       题目id
   * @param visibility visibility
   * @return List<QuScore>
   */
  List<QuOrderby> findByQuIdAndVisibility(String quId, int visibility);
}
