package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.QuOrderbyDao;
import net.diaowen.dwsurvey.entity.QuOrderby;
import net.diaowen.dwsurvey.repository.QuOrderByRepository;
import net.diaowen.dwsurvey.service.QuOrderbyManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;


/**
 * 排序题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuOrderbyManagerImpl extends BaseServiceImpl<QuOrderby, String> implements QuOrderbyManager {
  private final QuOrderbyDao quOrderbyDao;
  private final QuOrderByRepository quOrderByRepository;

  @Override
  public void setBaseDao() {
    this.baseDao = quOrderbyDao;
  }

  @Override
  public List<QuOrderby> findByQuId(String quId) {
    List<QuOrderby> list = quOrderByRepository.findByQuIdAndVisibility(quId, 1);

    if (Objects.nonNull(list) && !list.isEmpty()) {
      list.sort(Comparator.comparing(QuOrderby::getOrderById));
    }
    return list;
  }

  public int getOrderById(String quId) {
    Criterion criterion = Restrictions.eq("quId", quId);
    QuOrderby quOrderby = quOrderbyDao.findFirst("orderById", false, criterion);
    if (quOrderby != null) {
      return quOrderby.getOrderById();
    }
    return 0;
  }


  /*******************************************************************8
   * 更新操作
   */

  @Override
  @Transactional
  public QuOrderby upOptionName(String quId, String quItemId, String optionName) {
    if (quItemId != null && !"".equals(quItemId)) {
      QuOrderby quOrderby = quOrderbyDao.get(quItemId);
      quOrderby.setOptionName(optionName);
      quOrderbyDao.save(quOrderby);
      return quOrderby;
    } else {
      //取orderById
      int orderById = getOrderById(quId);
      //新加选项
      QuOrderby quOrderby = new QuOrderby();
      quOrderby.setQuId(quId);
      quOrderby.setOptionName(optionName);
      //title
      quOrderby.setOrderById(++orderById);
      quOrderby.setOptionTitle(orderById + "");
      quOrderbyDao.save(quOrderby);
      return quOrderby;
    }
  }

  @Override
  @Transactional
  public List<QuOrderby> saveManyOptions(String quId, List<QuOrderby> quOrderbys) {
    //取orderById
    int orderById = getOrderById(quId);
    for (QuOrderby quOrderby : quOrderbys) {
      //新加选项
      quOrderby.setOrderById(++orderById);
      quOrderby.setOptionTitle(orderById + "");
      quOrderbyDao.save(quOrderby);
    }
    return quOrderbys;
  }

  @Override
  @Transactional
  public void ajaxDelete(String quItemId) {
    QuOrderby quOrderby = get(quItemId);
    quOrderby.setVisibility(0);
    quOrderbyDao.save(quOrderby);
  }

  @Override
  @Transactional
  public void saveAttr(String quItemId) {
    QuOrderby quOrderby = get(quItemId);
    quOrderbyDao.save(quOrderby);
  }
}
