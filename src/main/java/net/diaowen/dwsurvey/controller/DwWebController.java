package net.diaowen.dwsurvey.controller;

import lombok.RequiredArgsConstructor;
import net.diaowen.common.base.service.AccountManager;
import net.diaowen.common.plugs.httpclient.HttpResult;
import net.diaowen.dwsurvey.common.FooterInfo;
import net.diaowen.dwsurvey.config.DWSurveyConfig;
import net.diaowen.dwsurvey.config.security.UserDetailsImpl;
import net.diaowen.dwsurvey.entity.SurveyDirectory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DwWebController
 *
 * @author diaowen
 * @since 2023/3/9 13:35
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/survey/anon/web")
public class DwWebController {
  private final AccountManager accountManager;

  /**
   * 获取问卷详情
   *
   * @return HttpResult<SurveyDirectory>
   */
  @GetMapping(value = "/footer-info.do")
  public HttpResult<SurveyDirectory> footerInfo() {
    try {
      FooterInfo footerInfo = new FooterInfo();
      footerInfo.setVersionInfo(DWSurveyConfig.DWSURVEY_VERSION_INFO);
      footerInfo.setVersionNumber(DWSurveyConfig.DWSURVEY_VERSION_NUMBER);
      footerInfo.setVersionBuilt(DWSurveyConfig.DWSURVEY_VERSION_BUILT);
      footerInfo.setSiteName(DWSurveyConfig.DWSURVEY_WEB_INFO_SITE_NAME);
      footerInfo.setSiteUrl(DWSurveyConfig.DWSURVEY_WEB_INFO_SITE_URL);
      footerInfo.setSiteIcp(DWSurveyConfig.DWSURVEY_WEB_INFO_SITE_ICP);
      footerInfo.setSiteMail(DWSurveyConfig.DWSURVEY_WEB_INFO_SITE_MAIL);
      footerInfo.setSitePhone(DWSurveyConfig.DWSURVEY_WEB_INFO_SITE_PHONE);
      footerInfo.setYears("2012-" + new SimpleDateFormat("yyyy").format(new Date()));
      UserDetailsImpl user = accountManager.getCurUser();
      if (user != null) {
        //登录用户返回带版本号
        return HttpResult.SUCCESS(footerInfo);
      } else {
        //非登录用户返回不带版本号
        footerInfo.setVersionNumber("");
        return HttpResult.SUCCESS(footerInfo);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return HttpResult.FAILURE();
  }


}
