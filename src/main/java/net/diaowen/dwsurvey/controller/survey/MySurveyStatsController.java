package net.diaowen.dwsurvey.controller.survey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.base.service.AccountManager;
import net.diaowen.common.plugs.httpclient.HttpResult;
import net.diaowen.dwsurvey.config.security.UserDetailsImpl;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.entity.SurveyDirectory;
import net.diaowen.dwsurvey.entity.SurveyStats;
import net.diaowen.dwsurvey.service.SurveyDirectoryManager;
import net.diaowen.dwsurvey.service.SurveyStatsManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * MySurveyStatsController
 *
 * @author diaowen
 * @since 2023/3/10 10:35
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/survey/app/stats")
public class MySurveyStatsController {
  private final SurveyStatsManager surveyStatsManager;
  private final AccountManager accountManager;
  private final SurveyDirectoryManager surveyDirectoryManager;

  @RequestMapping("/report.do")
  public HttpResult report(String surveyId) {
    // 得到频数分析数据
    UserDetailsImpl user = accountManager.getCurUser();
    if (user != null) {
      SurveyDirectory survey = surveyDirectoryManager.findUniqueBy(surveyId);
      if (survey != null) {
        if (!user.getId().equals(survey.getUserId())) {
          return HttpResult.FAILURE_MSG("没有相应数据权限");
        }
        List<Question> questions = surveyStatsManager.findFrequency(survey);
        SurveyStats surveyStats = new SurveyStats();
        surveyStats.setQuestions(questions);
        return HttpResult.SUCCESS(surveyStats);
      }
    }
    return HttpResult.FAILURE();
  }
}
