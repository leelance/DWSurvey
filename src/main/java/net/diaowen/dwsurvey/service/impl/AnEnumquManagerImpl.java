package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.AnEnumquDao;
import net.diaowen.dwsurvey.entity.AnEnumqu;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnEnumQuRepository;
import net.diaowen.dwsurvey.service.AnEnumquManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 枚举题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnEnumquManagerImpl extends BaseServiceImpl<AnEnumqu, String> implements AnEnumquManager {
  private final AnEnumquDao anEnumquDao;
  private final AnEnumQuRepository anEnumQuRepository;

  @Override
  public void setBaseDao() {
    this.baseDao = anEnumquDao;
  }

  @Override
  public List<AnEnumqu> findAnswer(String belongAnswerId, String quId) {
    //belongAnswerId quId
    Criterion criterion1 = Restrictions.eq("belongAnswerId", belongAnswerId);
    Criterion criterion2 = Restrictions.eq("quId", quId);
    return anEnumquDao.find(criterion1, criterion2);
  }

  @Override
  public void findGroupStats(Question question) {
    List<Object[]> list = anEnumQuRepository.findGroupStats(question.getId());
    //一共有多少对枚举数
    if (list != null && !list.isEmpty()) {
      question.setAnCount(list.size());
    }
  }
}
