package net.diaowen.dwsurvey.repository;

import net.diaowen.dwsurvey.entity.QuestionLogic;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * question logic
 *
 * @author lance
 * @since 2023/3/10 12:48
 */
public interface QuLogicRepository extends CrudRepository<QuestionLogic, String>, JpaSpecificationExecutor<QuestionLogic> {

  /**
   * 根据题目id查询题目分数集合
   *
   * @param quId       题目id
   * @param visibility visibility
   * @return List<QuestionLogic>
   */
  List<QuestionLogic> findByCkQuIdAndVisibility(String quId, int visibility);
}
