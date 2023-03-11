package net.diaowen.dwsurvey.service;

import net.diaowen.common.service.BaseService;
import net.diaowen.dwsurvey.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

/**
 * 问卷回答
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface SurveyAnswerManager extends BaseService<SurveyAnswer, String> {

  void saveAnswer(SurveyAnswer surveyAnswer, Map<String, Map<String, Object>> quMaps);

  List<Question> findAnswerDetail(SurveyAnswer answer);

  List<SurveyAnswer> answersByIp(String surveyId, String ip);

  SurveyAnswer getTimeInByIp(SurveyDetail surveyDetail, String ip);

  Long getCountByIp(String surveyId, String ip);

  String exportXLS(String surveyId, String savePath, boolean isExpUpQu);

  SurveyStats surveyStatsData(SurveyStats surveyStats);


  /**
   * 取出某份问卷的答卷数据
   *
   * @param pageReq  pageReq
   * @param surveyId surveyId
   * @return page
   */
  Page<SurveyAnswer> answerPage(PageRequest pageReq, String surveyId);

  void deleteData(String[] ids);

  int getQuestionAnswer(String surveyAnswerId, Question question);

  SurveyDirectory upAnQuNum(String surveyId);

  SurveyDirectory upAnQuNum(SurveyDirectory surveyDirectory);

  List<SurveyDirectory> upAnQuNum(List<SurveyDirectory> result);

  /**
   * 查询问卷调查答案
   *
   * @param answerId answerId
   * @return SurveyAnswer
   */
  SurveyAnswer findOne(String answerId);
}
