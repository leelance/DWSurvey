package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.common.SurveyConst;
import net.diaowen.dwsurvey.dao.AnFillblankDao;
import net.diaowen.dwsurvey.entity.AnFillblank;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnFillBlankRepository;
import net.diaowen.dwsurvey.service.AnFillblankManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Map;

/**
 * 填空题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnFillblankManagerImpl extends BaseServiceImpl<AnFillblank, String> implements AnFillblankManager {
  private final AnFillBlankRepository anFillBlankRepository;
  private final AnFillblankDao anFillblankDao;

  @Override
  public void setBaseDao() {
    this.baseDao = anFillblankDao;
  }

  @Override
  public AnFillblank findAnswer(String belongAnswerId, String quId) {
    Specification<AnFillblank> spec = answerSpec(quId, belongAnswerId);
    return anFillBlankRepository.findOne(spec).orElse(null);
  }

  @Override
  public void findGroupStats(Question question) {
    Map<String, BigInteger> objs = anFillBlankRepository.findGroupStats(question.getId());
    if (log.isDebugEnabled()) {
      log.debug("===>answer fill blank: {}", objs);
    }
    //未回答数
    question.setRowContent(objs.get(SurveyConst.FIELD_EMPTY_COUNT).toString());
    //回答的项数
    question.setOptionContent(objs.get(SurveyConst.FIELD_BLANK_COUNT).toString());
    question.setAnCount(objs.get(SurveyConst.FIELD_BLANK_COUNT).intValue());
  }

  @Override
  public PageDto<AnFillblank> findPage(PageDto<AnFillblank> page, String quId) {
    Criterion cri1 = Restrictions.eq("quId", quId);
    Criterion cri2 = Restrictions.eq("visibility", 1);
    return findPage(page, cri1, cri2);
  }

}
