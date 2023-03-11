package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.common.SurveyConst;
import net.diaowen.dwsurvey.dao.AnUploadFileDao;
import net.diaowen.dwsurvey.entity.AnUplodFile;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnUploadFileRepository;
import net.diaowen.dwsurvey.service.AnUploadFileManager;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
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
public class AnUploadFileManagerImpl extends BaseServiceImpl<AnUplodFile, String> implements AnUploadFileManager {
  private final AnUploadFileRepository anUploadFileRepository;
  private final AnUploadFileDao anUploadFileDao;

  @Override
  public void setBaseDao() {
    this.baseDao = anUploadFileDao;
  }

  @Override
  public List<AnUplodFile> findAnswer(String belongAnswerId, String quId) {
    return anUploadFileRepository.findByBelongAnswerIdAndQuId(belongAnswerId, quId);
  }

  @Override
  public void findGroupStats(Question question) {
    Map<String, BigInteger> objs = anUploadFileRepository.findGroupStats(question.getId());
    //未回答数
    question.setRowContent(objs.get(SurveyConst.FIELD_EMPTY_COUNT).toString());
    //回答的项数
    question.setOptionContent(objs.get(SurveyConst.FIELD_BLANK_COUNT).toString());
    question.setAnCount(objs.get(SurveyConst.FIELD_BLANK_COUNT).intValue());
  }

  @Override
  public List<AnUplodFile> findAnswer(String surveyId) {
    return anUploadFileRepository.findByBelongId(surveyId);
  }
}
