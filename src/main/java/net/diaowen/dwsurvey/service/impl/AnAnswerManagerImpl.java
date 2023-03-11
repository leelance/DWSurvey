package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.common.SurveyConst;
import net.diaowen.dwsurvey.dao.AnAnswerDao;
import net.diaowen.dwsurvey.entity.AnAnswer;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnAnswerRepository;
import net.diaowen.dwsurvey.service.AnAnswerManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Map;

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
    Specification<AnAnswer> spec = answerSpec(quId, belongAnswerId);
    return anAnswerRepository.findOne(spec).orElse(null);
  }

  @Override
  public void findGroupStats(Question question) {
    Map<String, BigInteger> objs = anAnswerRepository.findGroupStats(question.getId());
    //未回答数
    question.setRowContent(objs.get(SurveyConst.FIELD_EMPTY_COUNT).toString());
    //回答的项数
    question.setOptionContent(objs.get(SurveyConst.FIELD_BLANK_COUNT).toString());
    question.setAnCount(objs.get(SurveyConst.FIELD_BLANK_COUNT).intValue());
  }

}
