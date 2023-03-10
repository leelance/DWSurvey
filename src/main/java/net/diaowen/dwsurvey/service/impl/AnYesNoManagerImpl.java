package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.AnYesnoDao;
import net.diaowen.dwsurvey.entity.AnYesno;
import net.diaowen.dwsurvey.entity.DataCross;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnYesNoRepository;
import net.diaowen.dwsurvey.service.AnYesnoManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 枚举题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnYesNoManagerImpl extends BaseServiceImpl<AnYesno, String> implements AnYesnoManager {
  private final AnYesNoRepository anYesNoRepository;
  private final AnYesnoDao anYesnoDao;

  @Override
  public void setBaseDao() {
    this.baseDao = anYesnoDao;
  }

  /**
   * 根据exam_user信息查询答案
   */
  @Override
  public AnYesno findAnswer(String belongAnswerId, String quId) {
    //belongAnswerId quId
    Criterion criterion1 = Restrictions.eq("belongAnswerId", belongAnswerId);
    Criterion criterion2 = Restrictions.eq("quId", quId);
    return anYesnoDao.findUnique(criterion1, criterion2);
  }

  @Override
  public void findGroupStats(Question question) {
    List<Object[]> list = anYesNoRepository.findGroupStats(question.getId());

    String tranValue = question.getYesnoOption().getTrueValue();
    String falseValue = question.getYesnoOption().getFalseValue();

    question.setParamInt01(0);
    question.setParamInt02(0);
    int count = 0;
    for (Object[] objects : list) {
      if (tranValue.equals(objects[0].toString())) {
        int anCount = Integer.parseInt(objects[1].toString());
        count += anCount;
        question.setParamInt01(anCount);
      } else if (falseValue.equals(objects[0].toString())) {
        int anCount = Integer.parseInt(objects[1].toString());
        count += anCount;
        question.setParamInt02(anCount);
      }
    }

    question.setAnCount(count);
  }

  @Override
  public List<DataCross> findStatsDataCross(Question rowQuestion, Question colQuestion) {
    return anYesnoDao.findStatsDataCross(rowQuestion, colQuestion);
  }

  @Override
  public List<DataCross> findStatsDataChart(Question question) {
    return anYesnoDao.findStatsDataChart(question);
  }

}
