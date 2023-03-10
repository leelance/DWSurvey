package net.diaowen.dwsurvey.repository;

import net.diaowen.dwsurvey.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * SurveyAnswer
 *
 * @author lance
 * @since 2023/3/10 18:10
 */
public interface SurveyAnswerRepository extends CrudRepository<SurveyAnswer, String>, JpaSpecificationExecutor<SurveyAnswer> {
}
