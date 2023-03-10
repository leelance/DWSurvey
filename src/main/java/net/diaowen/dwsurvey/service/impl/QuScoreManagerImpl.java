package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.QuScoreDao;
import net.diaowen.dwsurvey.entity.QuScore;
import net.diaowen.dwsurvey.repository.question.QuScoreRepository;
import net.diaowen.dwsurvey.service.QuScoreManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class QuScoreManagerImpl extends BaseServiceImpl<QuScore, String> implements QuScoreManager {
  private final QuScoreRepository quScoreRepository;
  private final QuScoreDao quScoreDao;

  @Override
  public void setBaseDao() {
    this.baseDao = quScoreDao;
  }

  @Override
  public List<QuScore> findByQuId(String quId) {
    Specification<QuScore> spec = questionSpec(quId);
    Sort sort = Sort.by(Sort.Direction.ASC, "orderById");
    return quScoreRepository.findAll(spec, sort);
  }

  public int getOrderById(String quId) {
    Criterion criterion = Restrictions.eq("quId", quId);
    QuScore quRadio = quScoreDao.findFirst("orderById", false, criterion);
    if (quRadio != null) {
      return quRadio.getOrderById();
    }
    return 0;
  }


  /*******************************************************************8
   * 更新操作
   */

  @Override
  @Transactional
  public QuScore upOptionName(String quId, String quItemId, String optionName) {
    if (quItemId != null && !"".equals(quItemId)) {
      QuScore quScore = quScoreDao.get(quItemId);
      quScore.setOptionName(optionName);
      quScoreDao.save(quScore);
      return quScore;
    } else {
      //取orderById
      int orderById = getOrderById(quId);
      //新加选项
      QuScore quScore = new QuScore();
      quScore.setQuId(quId);
      quScore.setOptionName(optionName);
      //title
      quScore.setOrderById(++orderById);
      quScore.setOptionTitle(orderById + "");
      quScoreDao.save(quScore);
      return quScore;
    }
  }

  @Override
  @Transactional
  public List<QuScore> saveManyOptions(String quId, List<QuScore> quScores) {
    //取orderById
    int orderById = getOrderById(quId);
    for (QuScore quScore : quScores) {
      //新加选项
      quScore.setOrderById(++orderById);
      quScore.setOptionTitle(orderById + "");
      quScoreDao.save(quScore);
    }
    return quScores;
  }

  @Override
  @Transactional
  public void ajaxDelete(String quItemId) {
    QuScore quScore = get(quItemId);
    quScore.setVisibility(0);
    quScoreDao.save(quScore);
  }

  @Override
  @Transactional
  public void saveAttr(String quItemId) {
    QuScore quScore = get(quItemId);
    quScoreDao.save(quScore);
  }
}
