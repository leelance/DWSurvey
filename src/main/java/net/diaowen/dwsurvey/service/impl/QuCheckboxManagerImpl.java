package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.QuCheckboxDao;
import net.diaowen.dwsurvey.entity.QuCheckbox;
import net.diaowen.dwsurvey.repository.question.QuCheckBoxRepository;
import net.diaowen.dwsurvey.service.QuCheckboxManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 多选题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@RequiredArgsConstructor
@Service("quCheckboxManager")
public class QuCheckboxManagerImpl extends BaseServiceImpl<QuCheckbox, String> implements QuCheckboxManager {
  private final QuCheckBoxRepository quCheckBoxRepository;
  private final QuCheckboxDao quCheckboxDao;

  @Override
  public void setBaseDao() {
    this.baseDao = quCheckboxDao;
  }

  @Override
  public List<QuCheckbox> findByQuId(String quId) {
    Specification<QuCheckbox> spec = questionSpec(quId);
    Sort sort = Sort.by(Sort.Direction.ASC, "orderById");
    return quCheckBoxRepository.findAll(spec, sort);
  }


  public int getOrderById(String quId) {
    Criterion criterion = Restrictions.eq("quId", quId);
    QuCheckbox quCheckbox = quCheckboxDao.findFirst("orderById", false, criterion);
    if (quCheckbox != null) {
      return quCheckbox.getOrderById();
    }
    return 0;
  }

  /*******************************************************************8
   * 更新操作
   */

  @Override
  @Transactional
  public QuCheckbox upOptionName(String quId, String quItemId, String optionName) {
    if (quItemId != null && !"".equals(quItemId)) {
      QuCheckbox quCheckbox = quCheckboxDao.get(quItemId);
      quCheckbox.setOptionName(optionName);
      quCheckboxDao.save(quCheckbox);
      return quCheckbox;
    } else {
      //取orderById
      int orderById = getOrderById(quId);
      //新加选项
      QuCheckbox quCheckbox = new QuCheckbox();
      quCheckbox.setQuId(quId);
      quCheckbox.setOptionName(optionName);
      //title
      quCheckbox.setOrderById(++orderById);
      quCheckbox.setOptionTitle(orderById + "");
      quCheckboxDao.save(quCheckbox);
      return quCheckbox;
    }
  }

  @Override
  @Transactional
  public List<QuCheckbox> saveManyOptions(String quId, List<QuCheckbox> quCheckboxs) {
    //取orderById
    int orderById = getOrderById(quId);
    for (QuCheckbox quCheckbox : quCheckboxs) {
      //新加选项
      quCheckbox.setOrderById(++orderById);
      quCheckbox.setOptionTitle(orderById + "");
      quCheckboxDao.save(quCheckbox);
    }
    return quCheckboxs;
  }

  @Override
  public void ajaxDelete(String quItemId) {
    quCheckBoxRepository.findById(quItemId).ifPresent(c -> {
      String quId = c.getQuId();
      int orderById = c.getOrderById();

      quCheckBoxRepository.deleteById(quItemId);
      quCheckBoxRepository.subCheckBoxOrderId(quId, orderById);
    });
  }

  @Override
  @Transactional
  public void saveAttr(String quItemId, String isNote) {
    QuCheckbox quCheckbox = get(quItemId);
    if (isNote != null && "1".equals(isNote)) {
      quCheckbox.setIsNote(1);
    } else {
      quCheckbox.setIsNote(0);
    }
    quCheckboxDao.save(quCheckbox);
  }

}
