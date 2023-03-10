package net.diaowen.dwsurvey.repository;

import net.diaowen.dwsurvey.entity.Question;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * question
 *
 * @author lance
 * @since 2023/3/10 11:54
 */
public interface QuestionRepository extends CrudRepository<Question, String>, JpaSpecificationExecutor<Question> {
  /**
   * 根据问卷id查询问题集合
   *
   * @param belongId 问卷id
   * @param tag      tag
   * @param quTag    quTag
   * @return List<Question>
   */
  List<Question> findByBelongIdAndTagAndQuTagNot(String belongId, int tag, int quTag);
}
