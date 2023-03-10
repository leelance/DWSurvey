package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.AnOrderDao;
import net.diaowen.dwsurvey.entity.AnOrder;
import net.diaowen.dwsurvey.entity.QuOrderby;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnOrderRepository;
import net.diaowen.dwsurvey.service.AnOrderManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
public class AnOrderManagerImpl extends BaseServiceImpl<AnOrder, String> implements AnOrderManager {
  private final AnOrderRepository anOrderRepository;
  private final AnOrderDao anOrderDao;

  @Override
  public void setBaseDao() {
    this.baseDao = anOrderDao;
  }

  @Override
  public List<AnOrder> findAnswer(String belongAnswerId, String quId) {
    Criterion criterion1 = Restrictions.eq("belongAnswerId", belongAnswerId);
    Criterion criterion2 = Restrictions.eq("quId", quId);
    return anOrderDao.findByOrder("orderyNum", true, criterion1, criterion2);
  }

  @Override
  public void findGroupStats(Question question) {
    List<Object[]> list = anOrderRepository.findGroupStats(question.getId());
    List<QuOrderby> orderBys = question.getQuOrderbys();

    List<QuOrderby> list2 = new ArrayList<>();
    for (Object[] objects : list) {
      float num = Float.parseFloat(objects[1].toString());
      String quOrderById = objects[0].toString();
      for (QuOrderby quOrderby : orderBys) {
        if (quOrderById.equals(quOrderby.getId())) {
          quOrderby.setAnOrderSum((int) num);
          list2.add(quOrderby);
        }
      }
    }
    question.setQuOrderbys(list2);
  }

}
