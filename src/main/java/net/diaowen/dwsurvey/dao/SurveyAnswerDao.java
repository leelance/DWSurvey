package net.diaowen.dwsurvey.dao;

import net.diaowen.common.dao.BaseDao;
import net.diaowen.dwsurvey.entity.SurveyAnswer;
import net.diaowen.dwsurvey.entity.SurveyStats;

public interface SurveyAnswerDao extends BaseDao<SurveyAnswer, String> {

  SurveyStats surveyStatsData(SurveyStats surveyStats);

  Long countResult(String id);
}
