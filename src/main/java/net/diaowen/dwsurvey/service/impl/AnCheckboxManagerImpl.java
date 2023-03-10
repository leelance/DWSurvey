package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.AnCheckboxDao;
import net.diaowen.dwsurvey.entity.AnCheckbox;
import net.diaowen.dwsurvey.entity.DataCross;
import net.diaowen.dwsurvey.entity.QuCheckbox;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnCheckBoxRepository;
import net.diaowen.dwsurvey.service.AnCheckboxManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

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
@Service
@RequiredArgsConstructor
public class AnCheckboxManagerImpl extends BaseServiceImpl<AnCheckbox, String> implements AnCheckboxManager {
  private final AnCheckBoxRepository anCheckBoxRepository;
  private final AnCheckboxDao anCheckboxDao;

  @Override
  public void setBaseDao() {
    this.baseDao = anCheckboxDao;
  }

  @Override
  public List<AnCheckbox> findAnswer(String belongAnswerId, String quId) {
    //belongAnswerId quId
    Criterion criterion1 = Restrictions.eq("belongAnswerId", belongAnswerId);
    Criterion criterion2 = Restrictions.eq("quId", quId);
    return anCheckboxDao.find(criterion1, criterion2);
  }

  @Override
  public void findGroupStats(Question question) {
    List<Object[]> list = anCheckBoxRepository.findGroupStats(question.getId());
    List<QuCheckbox> checkboxes = question.getQuCheckboxs();

    int count = 0;
    for (QuCheckbox quCheckbox : checkboxes) {
      String quCheckboxId = quCheckbox.getId();
      for (Object[] objects : list) {
        if (quCheckboxId.equals(objects[0].toString())) {
          int anCount = Integer.parseInt(objects[1].toString());
          count += anCount;
          quCheckbox.setAnCount(anCount);
        }
      }
    }
    question.setAnCount(count);
  }

  @Override
  public List<DataCross> findStatsDataCross(Question rowQuestion,
                                            Question colQuestion) {
    return anCheckboxDao.findStatsDataCross(rowQuestion, colQuestion);
  }

  @Override
  public List<DataCross> findStatsDataChart(Question question) {
    return anCheckboxDao.findStatsDataChart(question);
  }


}
