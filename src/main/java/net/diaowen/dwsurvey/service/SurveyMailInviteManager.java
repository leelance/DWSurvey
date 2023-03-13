package net.diaowen.dwsurvey.service;

import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.dwsurvey.entity.SurveyMailInvite;

public interface SurveyMailInviteManager {

  void saveNew(String inboxs, SurveyMailInvite surveyMailInvite);

  PageDto<SurveyMailInvite> findPage(PageDto<SurveyMailInvite> page, String surveyId);

  void sendMailInvite(String string);

  SurveyMailInvite getById(String mailInviteId);

  void save(SurveyMailInvite surveyMailInvite);
}
