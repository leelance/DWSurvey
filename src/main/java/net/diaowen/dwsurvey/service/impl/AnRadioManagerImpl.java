package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.common.SurveyConst;
import net.diaowen.dwsurvey.dao.AnRadioDao;
import net.diaowen.dwsurvey.entity.AnRadio;
import net.diaowen.dwsurvey.entity.DataCross;
import net.diaowen.dwsurvey.entity.QuRadio;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnRadioRepository;
import net.diaowen.dwsurvey.service.AnRadioManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 单选题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnRadioManagerImpl extends BaseServiceImpl<AnRadio, String> implements AnRadioManager {
  private final AnRadioRepository anRadioRepository;
  private final AnRadioDao anRadioDao;

  @Override
  public void setBaseDao() {
    this.baseDao = anRadioDao;
  }

  /**
   * 根据exam_user信息查询答案
   */
  @Override
  public AnRadio findAnswer(String belongAnswerId, String quId) {
    Specification<AnRadio> spec = answerSpec(quId, belongAnswerId);
    return anRadioRepository.findOne(spec).orElse(null);
  }

  @Override
  public void findGroupStats(Question question) {
    List<Map<String, Object>> list = anRadioRepository.findGroupStats(question.getId());
    List<QuRadio> quRadios = question.getQuRadios();

    int count = 0;
    for (QuRadio quRadio : quRadios) {
      String quRadioId = quRadio.getId();
      for (Map<String, Object> objects : list) {
        if (quRadioId.equals(objects.get(SurveyConst.FIELD_EMPTY_COUNT).toString())) {
          int anCount = ((BigInteger) objects.get(SurveyConst.FIELD_BLANK_COUNT)).intValue();
          count += anCount;
          quRadio.setAnCount(anCount);
        }
      }
    }
    question.setAnCount(count);
  }

  @Override
  public List<DataCross> findStatsDataCross(Question rowQuestion,
                                            Question colQuestion) {
    return anRadioDao.findStatsDataCross(rowQuestion, colQuestion);
  }

  @Override
  public List<DataCross> findStatsDataChart(Question question) {
    return anRadioDao.findStatsDataChart(question);
  }
}
