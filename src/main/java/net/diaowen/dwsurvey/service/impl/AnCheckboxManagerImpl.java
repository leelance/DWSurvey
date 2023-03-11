package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.common.SurveyConst;
import net.diaowen.dwsurvey.dao.AnCheckboxDao;
import net.diaowen.dwsurvey.entity.AnCheckbox;
import net.diaowen.dwsurvey.entity.DataCross;
import net.diaowen.dwsurvey.entity.QuCheckbox;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnCheckBoxRepository;
import net.diaowen.dwsurvey.service.AnCheckboxManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

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
    Specification<AnCheckbox> spec = answerSpec(quId, belongAnswerId);
    return anCheckBoxRepository.findAll(spec);
  }

  @Override
  public void findGroupStats(Question question) {
    List<Map<String, Object>> list = anCheckBoxRepository.findGroupStats(question.getId());
    List<QuCheckbox> checkboxes = question.getQuCheckboxs();

    int count = 0;
    for (QuCheckbox quCheckbox : checkboxes) {
      String quCheckboxId = quCheckbox.getId();
      for (Map<String, Object> objects : list) {
        if (quCheckboxId.equals(objects.get(SurveyConst.FIELD_EMPTY_COUNT))) {
          int anCount = ((BigInteger) objects.get(SurveyConst.FIELD_BLANK_COUNT)).intValue();
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
