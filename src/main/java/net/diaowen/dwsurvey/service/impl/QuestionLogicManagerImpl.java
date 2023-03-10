package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.dwsurvey.entity.QuestionLogic;
import net.diaowen.dwsurvey.repository.QuLogicRepository;
import net.diaowen.dwsurvey.service.QuestionLogicManager;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 题逻辑
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionLogicManagerImpl implements QuestionLogicManager {
  private final QuLogicRepository quLogicRepository;

  @Override
  public List<QuestionLogic> findByCkQuId(String ckQuId) {
    return quLogicRepository.findByCkQuIdAndVisibility(ckQuId, 1);
  }
}
