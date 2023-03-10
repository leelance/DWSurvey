package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.QuMultiFillblankDao;
import net.diaowen.dwsurvey.entity.QuMultiFillblank;
import net.diaowen.dwsurvey.repository.question.QuMultiFillBankRepository;
import net.diaowen.dwsurvey.service.QuMultiFillblankManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 多项填空题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@RequiredArgsConstructor
@Service("quMultiFillblankManager")
public class QuMultiFillBlankManagerImpl extends BaseServiceImpl<QuMultiFillblank, String> implements QuMultiFillblankManager {
  private final QuMultiFillblankDao quMultiFillblankDao;
  private final QuMultiFillBankRepository quMultiFillBankRepository;

  @Override
  public void setBaseDao() {
    this.baseDao = quMultiFillblankDao;
  }

  @Override
  public List<QuMultiFillblank> findByQuId(String quId) {
    Specification<QuMultiFillblank> spec = questionSpec(quId);
    Sort sort = Sort.by(Sort.Direction.ASC, "orderById");
    return quMultiFillBankRepository.findAll(spec, sort);
  }

  public int getOrderById(String quId) {
    Criterion criterion = Restrictions.eq("quId", quId);
    QuMultiFillblank quMultiFillblank = quMultiFillblankDao.findFirst("orderById", false, criterion);
    if (quMultiFillblank != null) {
      return quMultiFillblank.getOrderById();
    }
    return 0;
  }

  @Override
  @Transactional
  public QuMultiFillblank upOptionName(String quId, String quItemId, String optionName) {
    if (quItemId != null && !"".equals(quItemId)) {
      QuMultiFillblank quMultiFillblank = quMultiFillblankDao.get(quItemId);
      quMultiFillblank.setOptionName(optionName);
      quMultiFillblankDao.save(quMultiFillblank);
      return quMultiFillblank;
    } else {
      //取orderById
      int orderById = getOrderById(quId);
      //新加选项
      QuMultiFillblank quMultiFillblank = new QuMultiFillblank();
      quMultiFillblank.setQuId(quId);
      quMultiFillblank.setOptionName(optionName);
      //title
      quMultiFillblank.setOrderById(++orderById);
      quMultiFillblank.setOptionTitle(orderById + "");
      quMultiFillblankDao.save(quMultiFillblank);
      return quMultiFillblank;
    }
  }

  @Override
  @Transactional
  public List<QuMultiFillblank> saveManyOptions(String quId, List<QuMultiFillblank> quMultiFillblanks) {
    //取orderById
    int orderById = getOrderById(quId);
    for (QuMultiFillblank quMultiFillblank : quMultiFillblanks) {
      //新加选项
      quMultiFillblank.setOrderById(++orderById);
      quMultiFillblank.setOptionTitle(orderById + "");
      quMultiFillblankDao.save(quMultiFillblank);
    }
    return quMultiFillblanks;
  }

  @Override
  @Transactional
  public void ajaxDelete(String quItemId) {
    QuMultiFillblank quMultiFillblank = get(quItemId);
    quMultiFillblank.setVisibility(0);
    quMultiFillblankDao.save(quMultiFillblank);
  }

  @Override
  @Transactional
  public void saveAttr(String quItemId) {
    QuMultiFillblank quMultiFillblank = get(quItemId);
    quMultiFillblankDao.save(quMultiFillblank);
  }
}
