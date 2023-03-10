package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.AnAnswerDao;
import net.diaowen.dwsurvey.entity.AnAnswer;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnAnswerRepository;
import net.diaowen.dwsurvey.service.AnAnswerManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

/**
 * @author keyuan
 * keyuan258@gmail.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnAnswerManagerImpl extends BaseServiceImpl<AnAnswer, String> implements AnAnswerManager {
  private final AnAnswerRepository anAnswerRepository;
  private final AnAnswerDao anAnswerDao;

  @Override
  public void setBaseDao() {
    this.baseDao = anAnswerDao;
  }

  /**
   * 根据exam_user信息查询答案
   */
  @Override
  public AnAnswer findAnswer(String belongAnswerId, String quId) {
    Criterion criterion1 = Restrictions.eq("belongAnswerId", belongAnswerId);
    Criterion criterion2 = Restrictions.eq("quId", quId);
    return anAnswerDao.findUnique(criterion1, criterion2);
  }

  @Override
  public void findGroupStats(Question question) {
    Object[] objs = anAnswerRepository.findGroupStats(question.getId());
    //未回答数
    question.setRowContent(objs[0].toString());
    //回答的项数
    question.setOptionContent(objs[1].toString());
    question.setAnCount(Integer.parseInt(objs[1].toString()));
  }

}
