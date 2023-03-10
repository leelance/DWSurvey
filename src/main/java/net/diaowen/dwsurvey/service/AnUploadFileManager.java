package net.diaowen.dwsurvey.service;

import net.diaowen.common.service.BaseService;
import net.diaowen.dwsurvey.entity.AnUplodFile;
import net.diaowen.dwsurvey.entity.Question;

import java.util.List;

/**
 * 填空题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface AnUploadFileManager extends BaseService<AnUplodFile, String> {

  List<AnUplodFile> findAnswer(String belongAnswerId, String quId);

  void findGroupStats(Question question);

  List<AnUplodFile> findAnswer(String belongAnswerId);

}
