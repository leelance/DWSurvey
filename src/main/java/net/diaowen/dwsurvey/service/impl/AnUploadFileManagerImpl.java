package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.dwsurvey.dao.AnUploadFileDao;
import net.diaowen.dwsurvey.entity.AnUplodFile;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.repository.answer.AnUploadFileRepository;
import net.diaowen.dwsurvey.service.AnUploadFileManager;
import org.springframework.stereotype.Service;

import java.util.List;

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
    Object[] objs = anUploadFileRepository.findGroupStats(question.getId());
    //未回答数
    question.setRowContent(objs[0].toString());
    //回答的项数
    question.setOptionContent(objs[1].toString());
    question.setAnCount(Integer.parseInt(objs[1].toString()));
  }

  @Override
  public List<AnUplodFile> findAnswer(String surveyId) {
    return anUploadFileRepository.findByBelongId(surveyId);
  }
}
