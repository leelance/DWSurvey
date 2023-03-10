package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.AnScoreDao;
import net.diaowen.dwsurvey.entity.AnScore;
import net.diaowen.dwsurvey.entity.QuScore;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnScoreRepository;
import net.diaowen.dwsurvey.service.AnScoreManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import java.util.List;

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
    Criterion criterion1 = Restrictions.eq("belongAnswerId", belongAnswerId);
    Criterion criterion2 = Restrictions.eq("quId", quId);
    return anScoreDao.find(criterion1, criterion2);
  }

  @Override
  public void findGroupStats(Question question) {
    List<Object[]> list = anScoreRepository.findGroupStats(question.getId());
    List<QuScore> quScores = question.getQuScores();

    int count = 0;
    for (QuScore quScore : quScores) {
      String quScoreId = quScore.getId();
      for (Object[] objects : list) {
        if (quScoreId.equals(objects[0].toString())) {
          int anCount = Integer.parseInt(objects[1].toString());
          count += anCount;
          quScore.setAnCount(anCount);
          quScore.setAvgScore(Float.parseFloat(objects[2].toString()));
        }
      }
    }
    question.setAnCount(count);
  }
}
