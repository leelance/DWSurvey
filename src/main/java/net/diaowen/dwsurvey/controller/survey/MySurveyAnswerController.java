package net.diaowen.dwsurvey.controller.survey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.base.service.AccountManager;
import net.diaowen.common.plugs.httpclient.HttpResult;
import net.diaowen.common.plugs.httpclient.PageResult;
import net.diaowen.common.utils.UserAgentUtils;
import net.diaowen.common.utils.ZipUtil;
import net.diaowen.dwsurvey.config.DWSurveyConfig;
import net.diaowen.dwsurvey.config.security.UserDetailsImpl;
import net.diaowen.dwsurvey.entity.AnUplodFile;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.entity.SurveyAnswer;
import net.diaowen.dwsurvey.entity.SurveyDirectory;
import net.diaowen.dwsurvey.service.AnUploadFileManager;
import net.diaowen.dwsurvey.service.SurveyAnswerManager;
import net.diaowen.dwsurvey.service.SurveyDirectoryManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * MySurveyAnswerController
 *
 * @author diaowen
 * @since 2023/3/11 00:47
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/survey/app/answer")
public class MySurveyAnswerController {
  private final SurveyDirectoryManager surveyDirectoryManager;
  private final SurveyAnswerManager surveyAnswerManager;
  private final AccountManager accountManager;
  private final AnUploadFileManager anUploadFileManager;

  /**
   * 获取答卷列表
   */
  @GetMapping(value = "/list.do")
  public PageResult survey(HttpServletRequest request, PageResult<SurveyAnswer> pageResult, String surveyId) {
    UserAgentUtils.userAgent(request);
    UserDetailsImpl user = accountManager.getCurUser();
    if (user != null) {
      SurveyDirectory survey = surveyDirectoryManager.findOne(surveyId);
      if (survey != null) {
        if (!user.getId().equals(survey.getUserId())) {
          pageResult.setSuccess(false);
          return pageResult;
        }

        PageRequest pageReq = pageResult.to();
        Page<SurveyAnswer> pageRes = surveyAnswerManager.answerPage(pageReq, surveyId);
        return PageResult.convert(pageRes);
      }
    }
    return pageResult;

  }

  @GetMapping(value = "/info.do")
  public HttpResult info(String answerId) throws Exception {
    try {
      SurveyAnswer answer = null;
      if (StringUtils.isNotEmpty(answerId)) {
        answer = surveyAnswerManager.findOne(answerId);
      }
      if (answer != null) {
        SurveyDirectory survey = surveyDirectoryManager.findUniqueBy(answer.getSurveyId());
        UserDetailsImpl user = accountManager.getCurUser();
        if (user != null && survey != null) {
          if (!user.getId().equals(survey.getUserId())) {
            return HttpResult.fail("没有相应数据权限");
          }
          List<Question> questions = surveyAnswerManager.findAnswerDetail(answer);
          survey.setQuestions(questions);
          survey.setSurveyAnswer(answer);
          return HttpResult.SUCCESS(survey);
        }
      }
    } catch (Exception e) {
      log.warn("===>survey answer[{}] info fail: ", answerId, e);
    }
    return HttpResult.FAILURE();
  }


  @DeleteMapping(value = "/delete.do")
  public HttpResult delete(@RequestBody Map<String, String[]> map) throws Exception {
    try {
      if (map != null && map.containsKey("id")) {
        String[] ids = map.get("id");
        if (ids != null) {
          surveyAnswerManager.deleteData(ids);
        }
      }
      return HttpResult.SUCCESS();
    } catch (Exception e) {
      log.warn("===>survey answer[{}] delete fail: ", map, e);
    }
    return HttpResult.FAILURE();
  }

  @RequestMapping("/export-xls.do")
  public String exportXLS(HttpServletRequest request, HttpServletResponse response, String surveyId, String expUpQu) throws Exception {
    try {
      String savePath = DWSurveyConfig.DWSURVEY_WEB_FILE_PATH;
      UserDetailsImpl user = accountManager.getCurUser();
      if (user != null) {
        SurveyDirectory survey = surveyDirectoryManager.get(surveyId);
        if (survey != null) {
          if (!user.getId().equals(survey.getUserId())) {
            return "没有相应数据权限";
          }
          List<AnUplodFile> anUplodFiles = anUploadFileManager.findAnswer(surveyId);
          if (anUplodFiles != null && anUplodFiles.size() > 0 && expUpQu != null && "1".equals(expUpQu)) {
            //直接导出excel，不存在上传文件的问题
            savePath = surveyAnswerManager.exportXLS(surveyId, savePath, true);
            //启用压缩导出
            String fromPath = DWSurveyConfig.DWSURVEY_WEB_FILE_PATH + "/webin/expfile/" + surveyId;
            fromPath = fromPath.replace("/", File.separator);

            String zipPath = DWSurveyConfig.DWSURVEY_WEB_FILE_PATH + "/webin/zip/".replace("/", File.separator);
            File file = new File(zipPath);
            if (!file.exists()) {
              file.mkdirs();
            }

            String toPath = zipPath + surveyId + ".zip";
            toPath = toPath.replace("/", File.separator);
            ZipUtil.createZip(fromPath, toPath, false);
            response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode("dwsurvey_" + survey.getSid() + ".zip", "UTF-8"));
            request.getRequestDispatcher("/webin/zip/" + surveyId + ".zip").forward(request, response);
          } else {
            //直接导出excel，不存在上传文件的问题
            savePath = surveyAnswerManager.exportXLS(surveyId, savePath, false);
            response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode("dwsurvey_" + survey.getSid() + ".xlsx", "UTF-8"));
            request.getRequestDispatcher(savePath).forward(request, response);
          }
        }
      }
    } catch (Exception e) {
      log.warn("===>survey answer[{}, {}] export excel fail: ", surveyId, expUpQu, e);
    }
    return null;
  }
}
