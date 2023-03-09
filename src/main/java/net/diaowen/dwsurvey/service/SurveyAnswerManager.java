package net.diaowen.dwsurvey.service;

import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.common.service.BaseService;
import net.diaowen.dwsurvey.entity.*;

import java.util.List;
import java.util.Map;

/**
 * 问卷回答
 * @author keyuan(keyuan258@gmail.com)
 *
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface SurveyAnswerManager extends BaseService<SurveyAnswer, String>{

	public void saveAnswer(SurveyAnswer surveyAnswer, Map<String, Map<String, Object>> quMaps);

	public List<Question> findAnswerDetail(SurveyAnswer answer);

	public List<SurveyAnswer> answersByIp(String surveyId, String ip);

	public SurveyAnswer getTimeInByIp(SurveyDetail surveyDetail, String ip);

	public Long getCountByIp(String surveyId, String ip);

	public String exportXLS(String surveyId, String savePath, boolean isExpUpQu);

	public SurveyStats surveyStatsData(SurveyStats surveyStats);


	/**
	 * 取出某份问卷的答卷数据
	 *
	 * @param page
	 * @param surveyId
	 * @return
	 */
	public PageDto<SurveyAnswer> answerPage(PageDto<SurveyAnswer> page, String surveyId);

	public void deleteData(String[] ids);

	public int getquestionAnswer(String surveyAnswerId, Question question);

	public SurveyDirectory upAnQuNum(String surveyId);

	public SurveyDirectory upAnQuNum(SurveyDirectory surveyDirectory);

	public List<SurveyDirectory> upAnQuNum(List<SurveyDirectory> result);
}
