package net.diaowen.dwsurvey.repository.survey;

import net.diaowen.dwsurvey.entity.SurveyDirectory;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

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

  /**
   * 根据当前问卷id查询下级集合
   *
   * @param parentId 当前问卷id
   * @return 下级集合
   */
  List<SurveyDirectory> findByParentId(String parentId);
}
