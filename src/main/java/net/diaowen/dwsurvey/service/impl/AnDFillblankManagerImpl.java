package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.AnDFillblankDao;
import net.diaowen.dwsurvey.entity.AnDFillblank;
import net.diaowen.dwsurvey.entity.QuMultiFillblank;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AndFillBlankRepository;
import net.diaowen.dwsurvey.service.AnDFillblankManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import java.util.List;

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
    //belongAnswerId quId
    Criterion criterion1 = Restrictions.eq("belongAnswerId", belongAnswerId);
    Criterion criterion2 = Restrictions.eq("quId", quId);
    return anDFillblankDao.find(criterion1, criterion2);
  }

  @Override
  public void findGroupStats(Question question) {
    List<Object[]> list = andFillBlankRepository.findGroupStats(question.getId());
    List<QuMultiFillblank> fillBlanks = question.getQuMultiFillblanks();

    for (QuMultiFillblank quMultiFillblank : fillBlanks) {
      String fillBlankId = quMultiFillblank.getId();
      for (Object[] objects : list) {
        if (fillBlankId.equals(objects[0].toString())) {
          quMultiFillblank.setAnCount(Integer.parseInt(objects[1].toString()));
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
