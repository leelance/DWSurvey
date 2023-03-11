package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.common.SurveyConst;
import net.diaowen.dwsurvey.dao.AnDFillblankDao;
import net.diaowen.dwsurvey.entity.AnDFillblank;
import net.diaowen.dwsurvey.entity.QuMultiFillblank;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AndFillBlankRepository;
import net.diaowen.dwsurvey.service.AnDFillblankManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 多行填空题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnDFillblankManagerImpl extends BaseServiceImpl<AnDFillblank, String> implements AnDFillblankManager {
  private final AnDFillblankDao anDFillblankDao;
  private final AndFillBlankRepository andFillBlankRepository;

  @Override
  public void setBaseDao() {
    this.baseDao = anDFillblankDao;
  }

  @Override
  public List<AnDFillblank> findAnswer(String belongAnswerId, String quId) {
    Specification<AnDFillblank> spec = answerSpec(quId, belongAnswerId);
    return andFillBlankRepository.findAll(spec);
  }

  @Override
  public void findGroupStats(Question question) {
    List<Map<String, Object>> list = andFillBlankRepository.findGroupStats(question.getId());
    List<QuMultiFillblank> fillBlanks = question.getQuMultiFillblanks();

    for (QuMultiFillblank quMultiFillblank : fillBlanks) {
      String fillBlankId = quMultiFillblank.getId();
      for (Map<String, Object> objects : list) {
        if (fillBlankId.equals(objects.get(SurveyConst.FIELD_EMPTY_COUNT))) {
          quMultiFillblank.setAnCount(((BigInteger) objects.get(SurveyConst.FIELD_BLANK_COUNT)).intValue());
        }
      }
    }
  }

  @Override
  public PageDto<AnDFillblank> findPage(PageDto<AnDFillblank> page, String quItemId) {
    Criterion cri1 = Restrictions.eq("quItemId", quItemId);
    Criterion cri2 = Restrictions.eq("visibility", 1);
    return findPage(page, cri1, cri2);
  }
}
