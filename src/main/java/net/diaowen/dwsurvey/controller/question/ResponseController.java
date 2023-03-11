package net.diaowen.dwsurvey.controller.question;

import com.octo.captcha.service.image.ImageCaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.QuType;
import net.diaowen.common.base.service.AccountManager;
import net.diaowen.common.plugs.httpclient.HttpResult;
import net.diaowen.common.plugs.ipaddr.IPService;
import net.diaowen.common.plugs.web.Token;
import net.diaowen.common.plugs.zxing.ZxingUtil;
import net.diaowen.common.utils.CookieUtils;
import net.diaowen.common.utils.NumberUtils;
import net.diaowen.common.utils.StringConst;
import net.diaowen.dwsurvey.common.AnswerCheckData;
import net.diaowen.dwsurvey.common.SurveyConst;
import net.diaowen.dwsurvey.config.DWSurveyConfig;
import net.diaowen.dwsurvey.config.security.UserDetailsImpl;
import net.diaowen.dwsurvey.entity.*;
import net.diaowen.dwsurvey.service.SurveyAnswerManager;
import net.diaowen.dwsurvey.service.SurveyDirectoryManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 答卷 action
 *
 * @author KeYuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/survey/anon/response")
public class ResponseController {
  private final SurveyAnswerManager surveyAnswerManager;
  private final SurveyDirectoryManager directoryManager;
  private final IPService ipService;
  private final AccountManager accountManager;
  private final ImageCaptchaService imageCaptchaService;

  @RequestMapping("/save.do")
  public String save(HttpServletRequest request, HttpServletResponse response, String surveyId) throws Exception {
    return saveSurvey(request, response, surveyId);
  }

  @RequestMapping("/saveMobile.do")
  public String saveMobile(HttpServletRequest request, HttpServletResponse response, String surveyId) throws Exception {
    return saveSurvey(request, response, surveyId);
  }

  public String saveSurvey(HttpServletRequest request, HttpServletResponse response, String surveyId) throws Exception {
    SurveyDirectory directory = null;
    try {
      directory = directoryManager.getSurvey(surveyId);
      SurveyAnswer entity = new SurveyAnswer();
      AnswerCheckData answerCheckData = answerCheckData(request, directory, true, entity);
      if (!answerCheckData.isAnswerCheck()) {
        return answerRedirect(directory, answerCheckData.getAnswerCheckCode());
      }
      answerSurvey(request, surveyId, entity);
      answerAfterUpData(request, response, surveyId, entity.getId());
      return answerRedirect(directory, 6, entity.getId());
    } catch (Exception e) {
      log.warn("===>Survey[{}] save fail: ", surveyId, e);
      return answerRedirect(directory, 5);
    }
  }

  public AnswerCheckData answerCheckData(HttpServletRequest request, SurveyDirectory directory, boolean isSubmit, SurveyAnswer entity) {
    AnswerCheckData answerCheckData = new AnswerCheckData();
    String surveyId = directory.getId();
    SurveyDetail surveyDetail = directory.getSurveyDetail();
    Integer answerNum = directory.getAnswerNum();
    Integer visibility = directory.getVisibility();

    Integer effective = surveyDetail.getEffective();
    Integer rule = surveyDetail.getRule();
    Integer refresh = surveyDetail.getRefresh();
    Integer ynEndNum = surveyDetail.getYnEndNum();
    Integer endNum = surveyDetail.getEndNum();
    Integer ynEndTime = surveyDetail.getYnEndTime();
    Date endTime = surveyDetail.getEndTime();

    if (visibility != 1) {
      answerCheckData.setAnswerCheck(false);
      //问卷已经删除
      answerCheckData.setAnswerCheckCode(10);
      return answerCheckData;
    }

    if (directory.getSurveyQuNum() <= 0 || directory.getSurveyState() != 1) {
      answerCheckData.setAnswerCheck(false);
      //问卷未开启
      answerCheckData.setAnswerCheckCode(1);
      return answerCheckData;
    }

    //1、每台电脑或手机只能答一次, cookie
    Cookie cookie = CookieUtils.getCookie(request, surveyId);
    int cookieAnNum = 0;
    if (effective != null && effective > 1 && cookie != null) {
      //effective cookie
      String cookieValue = cookie.getValue();
      if (cookieValue != null && NumberUtils.isNumeric(cookieValue)) {
        cookieAnNum = Integer.parseInt(cookieValue);
      }
      if (cookieAnNum > 0) {
        answerCheckData.setAnswerCheck(false);
        //超过cookie次数限制
        answerCheckData.setAnswerCheckCode(3);
        //跳转到结束提示
        return answerCheckData;
      }
    }

    String ip = ipService.getIp(request);
    //2、每个IP只能答一次，N次，IP
    Integer effectiveIp = surveyDetail.getEffectiveIp();
    if (effectiveIp != null && effectiveIp == 1) {
      Long ipAnNum = surveyAnswerManager.getCountByIp(surveyId, ip);
      if (ipAnNum != null && ipAnNum > 0) {
        answerCheckData.setAnswerCheck(false);
        //超过单个IP次数限制
        answerCheckData.setAnswerCheckCode(23);
        return answerCheckData;
      }
    }


    String ruleCode = request.getParameter("ruleCode");
    //4、拥有口令密码才可以答题，可用次数，口令码
    if (rule != null && rule == 3) {
      if (StringUtils.isNotEmpty(ruleCode) && !ruleCode.equals(surveyDetail.getRuleCode())) {
        //code不正确
        answerCheckData.setAnswerCheck(false);
        //口令错误
        answerCheckData.setAnswerCheckCode(302);
        return answerCheckData;
      }
      if (StringUtils.isEmpty(ruleCode)) {
        answerCheckData.setAnswerCheck(false);
        //口令为空
        answerCheckData.setAnswerCheckCode(303);
        return answerCheckData;
      }
    }

    //7、何时结束，结束时间
    if (endTime != null && ynEndTime == 1 && (System.currentTimeMillis() - endTime.getTime()) > 0) {
      directory.setSurveyState(2);
      directoryManager.saveByAdmin(directory);
      answerCheckData.setAnswerCheck(false);
      answerCheckData.setAnswerCheckCode(9);
      return answerCheckData;
    }

    //8、收集量
    if (answerNum != null && ynEndNum == 1 && answerNum >= endNum) {
      answerCheckData.setAnswerCheck(false);
      answerCheckData.setAnswerCheckCode(7);
      return answerCheckData;
    }

    //5、有重复回答启用验证码 cookie
    if (refresh != null && refresh == 1) {
      Cookie refCookie = CookieUtils.getCookie(request, "r_" + surveyId);
      if (refCookie != null) {
        //cookie启用验证码
        answerCheckData.setImgCheckCode(true);
      }
    }

    if (isSubmit) {
      String refCookieKey = "r_" + surveyId;
      Cookie refCookie = CookieUtils.getCookie(request, refCookieKey);
      if ((refresh == 1 && refCookie != null)) {
        String code = request.getParameter("jcaptchaInput");
        if (Boolean.FALSE.equals(imageCaptchaService.validateResponseForID(request.getSession().getId(), code))) {
          answerCheckData.setAnswerCheck(false);
          answerCheckData.setAnswerCheckCode(4);
          return answerCheckData;
        }
      }
      //设置entity的数据
      if (answerCheckData.isAnswerCheck()) {
        UserDetailsImpl user = accountManager.getCurUser();
        if (user != null) {
          entity.setUserId(user.getId());
        }

        entity.setIpAddr(ip);
        entity.setSurveyId(surveyId);
      }
    }
    return answerCheckData;
  }

  public void answerSurvey(HttpServletRequest request, String surveyId, SurveyAnswer entity) {
    Map<String, Map<String, Object>> quMaps = buildSaveSurveyMap(request);
    String bgTimeAttrName = "bgTime" + surveyId;
    Date bgAnTime = (Date) request.getSession().getAttribute(bgTimeAttrName);
    entity.setBgAnDate(bgAnTime);
    entity.setEndAnDate(new Date());
    surveyAnswerManager.saveAnswer(entity, quMaps);
  }

  public void answerAfterUpData(HttpServletRequest request, HttpServletResponse response, String surveyId, String answerId) {
    SurveyDirectory directory = directoryManager.getSurvey(surveyId);
    SurveyDetail surveyDetail = directory.getSurveyDetail();
    Integer answerNum = directory.getAnswerNum();
    Integer ynEndNum = surveyDetail.getYnEndNum();
    Integer endNum = surveyDetail.getEndNum();
    Integer ynEndTime = surveyDetail.getYnEndTime();
    Date endTime = surveyDetail.getEndTime();

    Integer effe = surveyDetail.getEffectiveTime();
    if (effe == null) {
      effe = 24;
    }
    String refCookieKey = "r_" + surveyId;
    CookieUtils.addCookie(response, surveyId, (1) + "", effe * 60, "/");
    CookieUtils.addCookie(response, refCookieKey, (1) + "", null, "/");

    //7、何时结束，结束时间
    if (endTime != null && ynEndTime == 1 && (System.currentTimeMillis() - endTime.getTime()) > 0) {
      directory.setSurveyState(2);
      directoryManager.saveByAdmin(directory);
    }

    if (answerNum != null && ynEndNum == 1 && answerNum >= endNum) {
      directory.setSurveyState(2);
      directoryManager.saveByAdmin(directory);
    }
  }

  public Map<String, Map<String, Object>> buildSaveSurveyMap(HttpServletRequest request) {
    Map<String, Map<String, Object>> quMaps = new HashMap<>(16);

    Map<String, Object> yesNoMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.YESNO + "_");
    quMaps.put(SurveyConst.KEY_YES_NO, yesNoMaps);

    Map<String, Object> radioMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.RADIO + "_");
    Map<String, Object> checkboxMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.CHECKBOX + "_");

    Map<String, Object> fillBlankMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.FILLBLANK + "_");
    quMaps.put(SurveyConst.KEY_BLANK, fillBlankMaps);

    Map<String, Object> dFillBlankMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.MULTIFILLBLANK + "_");
    dFillBlankMaps.forEach((k, v) -> {
      Map<String, Object> map = WebUtils.getParametersStartingWith(request, v.toString());
      dFillBlankMaps.put(k, map);
    });
    quMaps.put(SurveyConst.KEY_MULTI_BLANK, dFillBlankMaps);

    Map<String, Object> answerMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.ANSWER + "_");
    quMaps.put(SurveyConst.KEY_ANSWER, answerMaps);

    Map<String, Object> compRadioMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.COMPRADIO + "_");
    compRadioMaps.forEach((k, v) -> {
      String otherText = request.getParameter("text_qu_" + QuType.COMPRADIO + "_" + k + "_" + v.toString());
      AnRadio anRadio = new AnRadio();
      anRadio.setQuId(k);
      anRadio.setQuItemId(v.toString());
      anRadio.setOtherText(otherText);
      compRadioMaps.put(k, anRadio);
    });

    quMaps.put(SurveyConst.KEY_COMP_RADIO, compRadioMaps);
    Map<String, Object> compCheckboxMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.COMPCHECKBOX + "_");
    for (String key : compCheckboxMaps.keySet()) {
      String dFillValue = compCheckboxMaps.get(key).toString();
      Map<String, Object> map = WebUtils.getParametersStartingWith(request, "tag_" + dFillValue);
      for (String key2 : map.keySet()) {
        String quItemId = map.get(key2).toString();
        String otherText = request.getParameter("text_" + dFillValue + quItemId);
        AnCheckbox anCheckbox = new AnCheckbox();
        anCheckbox.setQuItemId(quItemId);
        anCheckbox.setOtherText(otherText);
        map.put(key2, anCheckbox);
      }
      compCheckboxMaps.put(key, map);
    }
    quMaps.put(SurveyConst.KEY_COMP_CHECK_BOX, compCheckboxMaps);
    Map<String, Object> enumMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.ENUMQU + "_");
    quMaps.put(SurveyConst.KEY_ENUM, enumMaps);
    Map<String, Object> quOrderMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.ORDERQU + "_");
    for (String key : quOrderMaps.keySet()) {
      String tag = quOrderMaps.get(key).toString();
      Map<String, Object> map = WebUtils.getParametersStartingWith(request, tag);
      quOrderMaps.put(key, map);
    }
    quMaps.put(SurveyConst.KEY_ORDER, quOrderMaps);
    for (String key : radioMaps.keySet()) {
      String quItemId = radioMaps.get(key).toString();
      String otherText = request.getParameter("text_qu_" + QuType.RADIO + "_" + key + "_" + quItemId);
      AnRadio anRadio = new AnRadio();
      anRadio.setQuId(key);
      anRadio.setQuItemId(quItemId);
      anRadio.setOtherText(otherText);
      radioMaps.put(key, anRadio);
    }
    // 评分题
    Map<String, Object> scoreMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.SCORE + "_");
    for (String key : scoreMaps.keySet()) {
      String tag = scoreMaps.get(key).toString();
      Map<String, Object> map = WebUtils.getParametersStartingWith(request, tag);
      scoreMaps.put(key, map);
    }
    quMaps.put(SurveyConst.KEY_SCORE, scoreMaps);
    quMaps.put(SurveyConst.KEY_COMP_RADIO, radioMaps);
    for (String key : checkboxMaps.keySet()) {
      String dfillValue = checkboxMaps.get(key).toString();
      Map<String, Object> map = WebUtils.getParametersStartingWith(request, dfillValue);
      for (String key2 : map.keySet()) {
        String quItemId = map.get(key2).toString();
        String otherText = request.getParameter("text_" + dfillValue + quItemId);
        AnCheckbox anCheckbox = new AnCheckbox();
        anCheckbox.setQuItemId(quItemId);
        anCheckbox.setOtherText(otherText);
        map.put(key2, anCheckbox);
      }
      checkboxMaps.put(key, map);
    }
    quMaps.put(SurveyConst.KEY_COMP_CHECK_BOX, checkboxMaps);

    Map<String, Object> uploadFileMaps = WebUtils.getParametersStartingWith(request, "qu_" + QuType.UPLOADFILE + "_");
    quMaps.put(SurveyConst.KEY_UPLOAD_FILE, uploadFileMaps);
    return quMaps;
  }

  public String answerRedirect(SurveyDirectory directory, int redType) {
    return answerRedirect(directory, redType, null);
  }

  public String answerRedirect(SurveyDirectory directory, int redType, String answerId) {
    if (directory != null) {
      return "redirect:" + DWSurveyConfig.DWSURVEY_WEB_SITE_URL + "/static/diaowen/message.html?sid=" + directory.getSid() + "&respType=" + redType + "&answerId=" + answerId;
    }
    return "redirect:" + DWSurveyConfig.DWSURVEY_WEB_SITE_URL + "/static/diaowen/message.html";
  }


  @RequestMapping("/answer-dwsurvey.do")
  public ModelAndView answerRedirect(HttpServletRequest request, String surveyId, int redType) {
    ModelAndView modelAndView = new ModelAndView();
    SurveyDirectory directory = directoryManager.getSurvey(surveyId);
    modelAndView.addObject("survey", directory);
    modelAndView.addObject("redType", redType);
    modelAndView.setViewName("/diaowen-answer/response-msg-1");
    return modelAndView;
  }

  /**
   * 答卷异步有效性验
   *
   * @return
   */
  @RequestMapping("/check-answer-survey.do")
  @ResponseBody
  public HttpResult checkAnswerSurvey(HttpServletRequest request, HttpServletResponse response, String surveyId) {
    //0、token 访止重复提交
    String token = Token.getToken(request);
    AnswerCheckData answerCheckData = answerCheckData(request, surveyId);
    answerCheckData.setToken(token);
    return HttpResult.SUCCESS(answerCheckData);
  }

  public AnswerCheckData answerCheckData(HttpServletRequest request, String surveyId) {
    SurveyDirectory directory = directoryManager.getSurvey(surveyId);
    return answerCheckData(request, directory, false, null);
  }


  /**
   * 获取问卷详情
   */
  @RequestMapping(value = "/survey.do")
  public String survey(HttpServletRequest request, HttpServletResponse response, String sid, String surveyId) {
    try {
      if (StringUtils.isEmpty(sid) && StringUtils.isNotEmpty(surveyId)) {
        SurveyDirectory surveyDirectory = directoryManager.get(surveyId);
        sid = surveyDirectory.getSid();
      }
      String jsonPath = "/file/survey/" + sid + StringConst.SLASH + sid + ".json";
      surveyJsonExists(sid, jsonPath);
      request.getRequestDispatcher(jsonPath).forward(request, response);
    } catch (ServletException | IOException e) {
      e.printStackTrace();
      log.warn("===>survey[{}, {}] response fail: ", sid, surveyId, e);
    }
    return null;
  }

  @RequestMapping(value = "/survey_info.do")
  public String surveyInfo(HttpServletRequest request, HttpServletResponse response, String sid) {
    String jsonPath = "/file/survey/" + sid + StringConst.SLASH + sid + "_info.json";
    try {
      surveyJsonExists(sid, jsonPath);
      request.getRequestDispatcher(jsonPath).forward(request, response);
    } catch (ServletException | IOException e) {
      log.warn("===>survey[{}] response info fail: ", sid, e);
    }
    return null;
  }

  private void surveyJsonExists(String sid, String jsonPath) {
    //判断有无没有则生成一个
    String filePath = DWSurveyConfig.DWSURVEY_WEB_FILE_PATH + jsonPath;
    filePath = filePath.replace("/", File.separator);
    filePath = filePath.replace("\\", File.separator);
    File file = new File(filePath);
    if (!file.exists()) {
      //不存在则生成一个
      SurveyDirectory directory = directoryManager.getSurveyBySid(sid);
      directoryManager.devSurveyJson(directory.getId());
    }
  }

  //回答问卷的二维码
  @RequestMapping("/answerTD.do")
  public String answerTD(HttpServletRequest request, HttpServletResponse response, String surveyId, String sid) throws Exception {
    String WEB_SITE_URL = DWSurveyConfig.DWSURVEY_WEB_SITE_URL;
    String down = request.getParameter("down");
    String ruleCode = request.getParameter("ruleCode");
    String baseUrl = "";
    baseUrl = request.getScheme() + "://" + request.getServerName()
        + (request.getServerPort() == 80 ? "" : ":" + request.getServerPort())
        + request.getContextPath();
    baseUrl = WEB_SITE_URL;
//		String encoderContent= baseUrl+"/response/answerMobile.do?surveyId="+surveyId;
    String encoderContent = null;
    if (StringUtils.isNotEmpty(surveyId)) {
      encoderContent = baseUrl + "/static/diaowen/answer-m.html?surveyId=" + surveyId;
    }
    if (StringUtils.isNotEmpty(sid)) {
      encoderContent = baseUrl + "/static/diaowen/answer-m.html?sid=" + sid;
    }
    if (StringUtils.isNotEmpty(ruleCode)) {
      encoderContent += "&ruleCode=" + ruleCode;
    }
    ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
    BufferedImage twoDimensionImg = ZxingUtil.qRCodeCommon(encoderContent, "jpg", 16);
    ImageIO.write(twoDimensionImg, "jpg", jpegOutputStream);
    if (down == null) {
      response.setHeader("Cache-Control", "no-store");
      response.setHeader("Pragma", "no-cache");
      response.setDateHeader("Expires", 0);
      response.setContentType("image/jpeg");
      ServletOutputStream responseOutputStream = response.getOutputStream();
      responseOutputStream.write(jpegOutputStream.toByteArray());
      responseOutputStream.flush();
      responseOutputStream.close();
    } else {
      response.addHeader("Content-Disposition", "attachment;filename=" + new String(("diaowen_" + surveyId + ".jpg").getBytes()));
      byte[] bys = jpegOutputStream.toByteArray();
      response.addHeader("Content-Length", "" + bys.length);
      ServletOutputStream responseOutputStream = response.getOutputStream();
      response.setContentType("application/octet-stream");
      responseOutputStream.write(bys);
      responseOutputStream.flush();
      responseOutputStream.close();
    }
    return null;
  }
}
