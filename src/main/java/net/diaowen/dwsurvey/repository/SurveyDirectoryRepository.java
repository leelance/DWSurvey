package net.diaowen.dwsurvey.repository;

import net.diaowen.dwsurvey.entity.SurveyDirectory;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * SurveyDirectory
 *
 * @author lance
 * @since 2023/3/9 16:00
 */
public interface SurveyDirectoryRepository extends CrudRepository<SurveyDirectory, String>, JpaSpecificationExecutor<SurveyDirectory> {
  /**
   * 根据sid查询记录
   *
   * @param sid sid
   * @return SurveyDirectory
   */
  SurveyDirectory findBySid(String sid);
}
