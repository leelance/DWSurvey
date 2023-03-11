package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.AnEnumquDao;
import net.diaowen.dwsurvey.entity.AnEnumqu;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnEnumQuRepository;
import net.diaowen.dwsurvey.service.AnEnumquManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    Specification<AnEnumqu> spec = answerSpec(quId, belongAnswerId);
    return anEnumQuRepository.findAll(spec);
  }

  @Override
  public void findGroupStats(Question question) {
    List<Map<String, Object>> list = anEnumQuRepository.findGroupStats(question.getId());
    //一共有多少对枚举数
    if (list != null && !list.isEmpty()) {
      question.setAnCount(list.size());
    }
  }
}
