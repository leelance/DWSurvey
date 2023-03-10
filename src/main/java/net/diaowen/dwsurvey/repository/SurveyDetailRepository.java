package net.diaowen.dwsurvey.repository;

import net.diaowen.dwsurvey.entity.SurveyDetail;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * survey detail
 *
 * @author lance
 * @since 2023/3/10 09:51
 */
public interface SurveyDetailRepository extends CrudRepository<SurveyDetail, String>, JpaSpecificationExecutor<SurveyDetail> {
  /**
   * 根据问卷调查id查询明细
   *
   * @param dirId dirId
   * @return SurveyDetail
   */
  SurveyDetail findByDirId(String dirId);
}
