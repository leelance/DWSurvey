package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.common.SurveyConst;
import net.diaowen.dwsurvey.dao.AnScoreDao;
import net.diaowen.dwsurvey.entity.AnScore;
import net.diaowen.dwsurvey.entity.QuScore;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnScoreRepository;
import net.diaowen.dwsurvey.service.AnScoreManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 评分题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnScoreManagerImpl extends BaseServiceImpl<AnScore, String> implements AnScoreManager {
  private final AnScoreRepository anScoreRepository;
  private final AnScoreDao anScoreDao;

  @Override
  public void setBaseDao() {
    this.baseDao = anScoreDao;
  }

  @Override
  public List<AnScore> findAnswer(String belongAnswerId, String quId) {
    Specification<AnScore> spec = answerSpec(quId, belongAnswerId);
    return anScoreRepository.findAll(spec);
  }

  @Override
  public void findGroupStats(Question question) {
    List<Map<String, Object>> list = anScoreRepository.findGroupStats(question.getId());
    List<QuScore> quScores = question.getQuScores();

    int count = 0;
    for (QuScore quScore : quScores) {
      String quScoreId = quScore.getId();
      for (Map<String, Object> objects : list) {
        if (quScoreId.equals(objects.get(SurveyConst.FIELD_EMPTY_COUNT))) {
          int anCount = ((BigInteger) objects.get(SurveyConst.FIELD_BLANK_COUNT)).intValue();
          count += anCount;
          quScore.setAnCount(anCount);
          quScore.setAvgScore(((Double) objects.get(SurveyConst.FIELD_THREE)).floatValue());
        }
      }
    }
    question.setAnCount(count);
  }
}
