package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.QuRadioDao;
import net.diaowen.dwsurvey.entity.QuRadio;
import net.diaowen.dwsurvey.repository.question.QuRadioRepository;
import net.diaowen.dwsurvey.service.QuRadioManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 单选题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@RequiredArgsConstructor
@Service("quRadioManager")
public class QuRadioManagerImpl extends BaseServiceImpl<QuRadio, String> implements QuRadioManager {
  private final QuRadioRepository quRadioRepository;
  private final QuRadioDao quRadioDao;

  @Override
  public void setBaseDao() {
    this.baseDao = quRadioDao;
  }

  /**
   * 得到某一题的选项
   */
  @Override
  public List<QuRadio> findByQuId(String quId) {
    Specification<QuRadio> spec = questionSpec(quId);
    Sort sort = Sort.by(Sort.Direction.ASC, "orderById");
    return quRadioRepository.findAll(spec, sort);
  }

  public int getOrderById(String quId) {
    Criterion criterion = Restrictions.eq("quId", quId);
    QuRadio quRadio = quRadioDao.findFirst("orderById", false, criterion);
    if (quRadio != null) {
      return quRadio.getOrderById();
    }
    return 0;
  }


  @Override
  @Transactional
  public QuRadio upOptionName(String quId, String quItemId, String optionName) {
    if (quItemId != null && !"".equals(quItemId)) {
      QuRadio quRadio = quRadioDao.get(quItemId);
      quRadio.setOptionName(optionName);
      quRadioDao.save(quRadio);
      return quRadio;
    } else {
      //取orderById
      int orderById = getOrderById(quId);
      //新加选项
      QuRadio quRadio = new QuRadio();
      quRadio.setQuId(quId);
      quRadio.setOptionName(optionName);
      //title
      quRadio.setOrderById(++orderById);
      quRadio.setOptionTitle(orderById + "");
      quRadioDao.save(quRadio);
      return quRadio;
    }
  }

  @Override
  @Transactional
  public List<QuRadio> saveManyOptions(String quId, List<QuRadio> quRadios) {
    //取orderById
    int orderById = getOrderById(quId);
    for (QuRadio quRadio : quRadios) {
      //新加选项
      quRadio.setOrderById(++orderById);
      quRadio.setOptionTitle(orderById + "");
      quRadioDao.save(quRadio);
    }
    return quRadios;
  }

  @Override
  @Transactional
  public void ajaxDelete(String quItemId) {
    QuRadio quRadio = get(quItemId);
    String quId = quRadio.getQuId();
    int orderById = quRadio.getOrderById();
    quRadioDao.delete(quItemId);
    //修改排序号
    quRadioDao.quOrderByIdDel1(quId, orderById);
  }

  @Override
  @Transactional
  public void saveAttr(String quItemId, String isNote) {
    QuRadio quRadio = get(quItemId);
    if ("1".equals(isNote)) {
      quRadio.setIsNote(1);
    } else {
      quRadio.setIsNote(0);
    }
    quRadioDao.save(quRadio);
  }
}
