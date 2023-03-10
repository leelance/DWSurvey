package net.diaowen.dwsurvey.repository.question;

import net.diaowen.dwsurvey.entity.Question;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

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

  /**
   * 属性belongId所有题目，只要大于等于orderById+1
   *
   * @param belongId  问卷id
   * @param orderById 排序值
   */
  @Modifying
  @Query(value = "update Question set orderById=orderById+1 where belongId=:belongId AND orderById>=:orderById")
  void addQuestionOrderId(@Param("belongId") String belongId, @Param("orderById") int orderById);

  /**
   * 属性belongId所有题目，只要大于等于orderById-1
   *
   * @param belongId  问卷id
   * @param orderById 排序值
   */
  @Modifying
  @Query(value = "update Question set orderById=orderById-1 where belongId=:belongId AND orderById>=:orderById")
  void subQuestionOrderId(@Param("belongId") String belongId, @Param("orderById") int orderById);
}
