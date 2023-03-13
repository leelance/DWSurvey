package net.diaowen.dwsurvey.service;

import net.diaowen.dwsurvey.entity.SurveyStyle;

/**
 * 问卷样式
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface SurveyStyleManager {

  SurveyStyle get(String id);

  SurveyStyle getBySurveyId(String surveyId);

  void save(SurveyStyle surveyStyle);
}
