package net.diaowen.dwsurvey.controller.question;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.base.service.AccountManager;
import net.diaowen.common.plugs.httpclient.HttpResult;
import net.diaowen.dwsurvey.config.security.UserDetailsImpl;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.entity.SurveyDetail;
import net.diaowen.dwsurvey.entity.SurveyDirectory;
import net.diaowen.dwsurvey.service.QuestionManager;
import net.diaowen.dwsurvey.service.SurveyDirectoryManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * 设计问卷
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/survey/app/design/survey-design")
public class SurveyDesignController {
  private final QuestionManager questionManager;
  private final SurveyDirectoryManager surveyDirectoryManager;
  private final AccountManager accountManager;

  @ResponseBody
  @RequestMapping("/surveyAll.do")
  public HttpResult surveyAll(String surveyId, String sid) {
    try {
      if (log.isDebugEnabled()) {
        log.debug("===>app design params surveyId: {}, sid: {}", surveyId, sid);
      }
      return buildSurvey(surveyId, sid);
    } catch (Exception e) {
      log.error("===>design survey fail: ", e);
    }
    return HttpResult.FAILURE();
  }

  @RequestMapping("/devSurvey.do")
  @ResponseBody
  public HttpResult devSurvey(HttpServletRequest request, String surveyId) throws Exception {

    UserDetailsImpl curUser = accountManager.getCurUser();
    if (curUser != null) {
      SurveyDirectory survey = surveyDirectoryManager.get(surveyId);
      try {
        surveyDirectoryManager.devSurvey(survey);
//				sysLogManager.saveNew("发布问卷",survey.getId(),"DEV-SURVEY",curUser.getId(),1);
        return HttpResult.SUCCESS();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      HttpResult.NOLOGIN();
    }
    return HttpResult.FAILURE();
  }

  @RequestMapping("/ajaxSave.do")
  public String ajaxSave(HttpServletRequest request, HttpServletResponse response, String surveyId) throws Exception {
    String svyName = request.getParameter("svyName");
    String svyNameText = request.getParameter("svyNameText");
    String svyNote = request.getParameter("svyNote");
    //属性
    String effective = request.getParameter("effective");
    String effectiveIp = request.getParameter("effectiveIp");
    String rule = request.getParameter("rule");
    String ruleCode = request.getParameter("ruleCode");
    String refresh = request.getParameter("refresh");
    String mailOnly = request.getParameter("mailOnly");
    String ynEndNum = request.getParameter("ynEndNum");
    String endNum = request.getParameter("endNum");
    String ynEndTime = request.getParameter("ynEndTime");
    String endTime = request.getParameter("endTime");
    String showShareSurvey = request.getParameter("showShareSurvey");
    String showAnswerDa = request.getParameter("showAnswerDa");

    SurveyDirectory survey = surveyDirectoryManager.getSurvey(surveyId);
    SurveyDetail surveyDetail = survey.getSurveyDetail();
    UserDetailsImpl user = accountManager.getCurUser();
    if (user != null) {
      String userId = user.getId();
      if (userId.equals(survey.getUserId())) {

        if (svyNote != null) {
          svyNote = URLDecoder.decode(svyNote, StandardCharsets.UTF_8.name());
          surveyDetail.setSurveyNote(svyNote);
        }
        if (svyName != null && !"".equals(svyName)) {
          svyName = URLDecoder.decode(svyName, StandardCharsets.UTF_8.name());
          survey.setSurveyName(svyName);
        }
        if (StringUtils.isNotEmpty(svyNameText)) {
          svyNameText = URLDecoder.decode(svyNameText, StandardCharsets.UTF_8.name());
          survey.setSurveyNameText(svyNameText);
        }

        //保存属性
        if (effective != null && !"".equals(effective)) {
          surveyDetail.setEffective(Integer.parseInt(effective));
        }
        if (effectiveIp != null && !"".equals(effectiveIp)) {
          surveyDetail.setEffectiveIp(Integer.parseInt(effectiveIp));
        }
        if (rule != null && !"".equals(rule)) {
          surveyDetail.setRule(Integer.parseInt(rule));
          surveyDetail.setRuleCode(ruleCode);
        }
        if (refresh != null && !"".equals(refresh)) {
          surveyDetail.setRefresh(Integer.parseInt(refresh));
        }
        if (mailOnly != null && !"".equals(mailOnly)) {
          surveyDetail.setMailOnly(Integer.parseInt(mailOnly));
        }
        if (ynEndNum != null && !"".equals(ynEndNum)) {
          surveyDetail.setYnEndNum(Integer.parseInt(ynEndNum));
          //surveyDetail.setEndNum(Integer.parseInt(endNum));
          if (endNum != null && endNum.matches("\\d*")) {
            surveyDetail.setEndNum(Integer.parseInt(endNum));
          }
        }
        if (ynEndTime != null && !"".equals(ynEndTime)) {
          surveyDetail.setYnEndTime(Integer.parseInt(ynEndTime));
          if (org.apache.commons.lang3.StringUtils.isNotEmpty(endTime)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            surveyDetail.setEndTime(simpleDateFormat.parse(endTime));
          }
        }
        if (showShareSurvey != null && !"".equals(showShareSurvey)) {
          surveyDetail.setShowShareSurvey(Integer.parseInt(showShareSurvey));
          survey.setIsShare(Integer.parseInt(showShareSurvey));
        }
        if (showAnswerDa != null && !"".equals(showAnswerDa)) {
          surveyDetail.setShowAnswerDa(Integer.parseInt(showAnswerDa));
          survey.setViewAnswer(Integer.parseInt(showAnswerDa));
        }

        surveyDirectoryManager.save(survey);

        response.getWriter().write("true");
      }
    }
    return null;
  }

  private HttpResult buildSurvey(String surveyId, String sid) {
    //判断是否拥有权限
    SurveyDirectory surveyDirectory;
    if (StringUtils.isEmpty(surveyId) && StringUtils.isNotEmpty(sid)) {
      surveyDirectory = surveyDirectoryManager.getSurveyBySid(sid);
    } else {
      surveyDirectory = surveyDirectoryManager.getSurvey(surveyId);
    }

    UserDetailsImpl user = accountManager.getCurUser();
    String userId = user.getId();
    if (user != null) {
      if (!userId.equals(surveyDirectory.getUserId())) {
        return HttpResult.fail("未登录或没有相应数据权限");
      }
    } else {
      return HttpResult.fail("未登录或没有相应数据权限");
    }

    if (surveyDirectory != null) {
      List<Question> questions = questionManager.findDetails(surveyDirectory.getId(), 2);
      surveyDirectory.setQuestions(questions);
      surveyDirectory.setSurveyQuNum(questions.size());
      surveyDirectoryManager.save(surveyDirectory);
      return HttpResult.SUCCESS(surveyDirectory);
    } else {
      return HttpResult.NOLOGIN();
    }
  }

}
