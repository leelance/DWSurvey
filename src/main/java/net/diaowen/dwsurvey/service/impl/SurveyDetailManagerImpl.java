package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.common.utils.ReflectionUtils;
import net.diaowen.dwsurvey.dao.SurveyDetailDao;
import net.diaowen.dwsurvey.entity.SurveyDetail;
import net.diaowen.dwsurvey.repository.survey.SurveyDetailRepository;
import net.diaowen.dwsurvey.service.SurveyDetailManager;
import org.springframework.stereotype.Service;


/**
 * 问卷详情
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyDetailManagerImpl extends BaseServiceImpl<SurveyDetail, String> implements SurveyDetailManager {
  private final SurveyDetailRepository surveyDetailRepository;
  private final SurveyDetailDao surveyDetailDao;

  @Override
  public void setBaseDao() {
    this.baseDao = surveyDetailDao;
  }

  @Override
  public void save(SurveyDetail t) {
    //判断有无，有则更新
    SurveyDetail surveyDetail = surveyDetailRepository.findByDirId(t.getDirId());
    if (surveyDetail == null) {
      surveyDetail = new SurveyDetail();
    }
    ReflectionUtils.copyAttr(t, surveyDetail);
    surveyDetailRepository.save(surveyDetail);
  }

  @Override
  public SurveyDetail getBySurveyId(String surveyId) {
    return surveyDetailRepository.findByDirId(surveyId);
  }

  @Override
  public void saveBaseUp(SurveyDetail t) {
    SurveyDetail surveyDetail = surveyDetailRepository.findByDirId(t.getDirId());
    if (surveyDetail != null) {
      surveyDetail.setEffective(t.getEffective());
      surveyDetail.setEffectiveIp(t.getEffectiveIp());
      surveyDetail.setRefresh(t.getRefresh());
      surveyDetail.setRule(t.getRule());
      surveyDetail.setRuleCode(t.getRuleCode());
      surveyDetail.setYnEndTime(t.getYnEndTime());
      surveyDetail.setYnEndNum(t.getYnEndNum());
      surveyDetail.setEndNum(t.getEndNum());
      surveyDetail.setEndTime(t.getEndTime());

      surveyDetailRepository.save(surveyDetail);
    }
  }
}
