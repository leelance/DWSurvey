package net.diaowen.dwsurvey.controller.survey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.base.service.AccountManager;
import net.diaowen.common.plugs.httpclient.HttpResult;
import net.diaowen.common.plugs.httpclient.HttpStatus;
import net.diaowen.common.plugs.httpclient.PageResult;
import net.diaowen.dwsurvey.config.security.UserDetailsImpl;
import net.diaowen.dwsurvey.entity.SurveyDetail;
import net.diaowen.dwsurvey.entity.SurveyDirectory;
import net.diaowen.dwsurvey.service.SurveyAnswerManager;
import net.diaowen.dwsurvey.service.SurveyDetailManager;
import net.diaowen.dwsurvey.service.SurveyDirectoryManager;
import net.diaowen.dwsurvey.service.SurveyStatsManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MySurveyController
 *
 * @author diaowen
 * @since 2023/3/9 14:29
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/survey/app/survey")
public class MySurveyController {
  private final SurveyDirectoryManager surveyDirectoryManager;
  private final SurveyDetailManager surveyDetailManager;
  private final SurveyAnswerManager surveyAnswerManager;
  private final SurveyStatsManager surveyStatsManager;
  private final AccountManager accountManager;

  /**
   * 拉取问卷列表
   */
  @GetMapping(value = "/list.do")
  public PageResult<SurveyDirectory> list(PageResult<SurveyDirectory> pageResult, String surveyName, Integer surveyState) {
    if (log.isDebugEnabled()) {
      log.debug("===>app survey list params name: {}, state: {}", surveyName, surveyState);
    }

    PageRequest page = pageResult.to();
    Page<SurveyDirectory> p = surveyDirectoryManager.findByUser(page, surveyName, surveyState);
    return PageResult.convert(p);
  }


  /**
   * 获取问卷详情
   */
  @GetMapping(value = "/info.do")
  public HttpResult<SurveyDirectory> info(String id) {
    try {
      UserDetailsImpl user = accountManager.getCurUser();
      if (user != null) {
        surveyStatsManager.findBySurvey(id);
        SurveyDirectory survey = surveyDirectoryManager.findUniqueBy(id);
        survey = surveyAnswerManager.upAnQuNum(survey);
        return HttpResult.SUCCESS(survey);
      } else {
        return HttpResult.buildResult(HttpStatus.NO_LOGIN);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return HttpResult.FAILURE();
  }

  /**
   * 创建新问卷
   */
  @PostMapping(value = "/add.do")
  public HttpResult add(@RequestBody SurveyDirectory surveyDirectory) {
    try {
      surveyDirectory.setDirType(2);
      surveyDirectory.setSurveyNameText(surveyDirectory.getSurveyName());
      surveyDirectoryManager.save(surveyDirectory);
      return HttpResult.SUCCESS(surveyDirectory);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return HttpResult.FAILURE();
  }


  /**
   * 引用问卷
   */
  @PostMapping(value = "/copy.do")
  public HttpResult copy(String fromSurveyId, String surveyName, String tag) throws Exception {
    tag = "2";
    SurveyDirectory directory = surveyDirectoryManager.createBySurvey(fromSurveyId, surveyName, tag);
    String surveyId = directory.getId();
    return HttpResult.SUCCESS(directory);
  }


  /**
   * 问卷删除
   */
  @DeleteMapping(value = "/delete.do")
  public HttpResult delete(@RequestBody Map<String, String[]> map) throws Exception {
    String result = null;
    try {
      UserDetailsImpl curUser = accountManager.getCurUser();
      if (curUser != null) {
        if (map != null) {
          if (map.containsKey("id")) {
            String[] ids = map.get("id");
            if (ids != null) {
              surveyDirectoryManager.delete(ids);
              return HttpResult.SUCCESS();
            }
          }
        }
      }
    } catch (Exception e) {
      result = e.getMessage();
    }
    return HttpResult.FAILURE(result);
  }


  /**
   * 修改状态
   */
  @PostMapping(value = "/up-survey-status.do")
  public HttpResult<SurveyDirectory> upSurveyState(String surveyId, Integer surveyState) {
    try {
      surveyDirectoryManager.upSurveyState(surveyId, surveyState);
      return HttpResult.SUCCESS();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return HttpResult.FAILURE();
  }


  /**
   * 保存更新基本属性
   */
  @PutMapping(value = "/survey-base-attr.do")
  public HttpResult<SurveyDirectory> saveBaseAttr(@RequestBody SurveyDetail surveyDetail) {
    try {
      surveyDetailManager.saveBaseUp(surveyDetail);
      return HttpResult.SUCCESS();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return HttpResult.FAILURE();
  }
}
