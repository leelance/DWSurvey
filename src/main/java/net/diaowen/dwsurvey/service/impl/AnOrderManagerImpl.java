package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.common.SurveyConst;
import net.diaowen.dwsurvey.dao.AnOrderDao;
import net.diaowen.dwsurvey.entity.AnOrder;
import net.diaowen.dwsurvey.entity.QuOrderby;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnOrderRepository;
import net.diaowen.dwsurvey.service.AnOrderManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
    Specification<AnOrder> spec = answerSpec(quId, belongAnswerId);

    List<AnOrder> list = anOrderRepository.findAll(spec);
    list.sort(Comparator.comparing(AnOrder::getOrderyNum));
    return list;
  }

  @Override
  public void findGroupStats(Question question) {
    List<Map<String, Object>> list = anOrderRepository.findGroupStats(question.getId());
    List<QuOrderby> orderBys = question.getQuOrderbys();

    List<QuOrderby> list2 = new ArrayList<>();
    for (Map<String, Object> objects : list) {
      double num = (double) objects.get(SurveyConst.FIELD_BLANK_COUNT);
      String quOrderById = objects.get(SurveyConst.FIELD_EMPTY_COUNT).toString();
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
