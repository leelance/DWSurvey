package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.AnFillblankDao;
import net.diaowen.dwsurvey.entity.AnFillblank;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnFillBlankRepository;
import net.diaowen.dwsurvey.service.AnFillblankManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

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
    //belongAnswerId quId
    Criterion criterion1 = Restrictions.eq("belongAnswerId", belongAnswerId);
    Criterion criterion2 = Restrictions.eq("quId", quId);
    return anFillblankDao.findUnique(criterion1, criterion2);
  }

  @Override
  public void findGroupStats(Question question) {
    Object[] objs = anFillBlankRepository.findGroupStats(question.getId());
    //未回答数
    question.setRowContent(objs[0].toString());
    //回答的项数
    question.setOptionContent(objs[1].toString());
    question.setAnCount(Integer.parseInt(objs[1].toString()));
  }

  @Override
  public PageDto<AnFillblank> findPage(PageDto<AnFillblank> page, String quId) {
    Criterion cri1 = Restrictions.eq("quId", quId);
    Criterion cri2 = Restrictions.eq("visibility", 1);
    return findPage(page, cri1, cri2);
  }

}
