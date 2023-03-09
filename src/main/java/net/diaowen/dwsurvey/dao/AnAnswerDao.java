package net.diaowen.dwsurvey.dao;

import net.diaowen.common.dao.BaseDao;
import net.diaowen.dwsurvey.entity.AnAnswer;
import net.diaowen.dwsurvey.entity.Question;

/**
 * 答卷数据 interface
 *
 * @author KeYuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface AnAnswerDao extends BaseDao<AnAnswer, String> {

  void findGroupStats(Question question);

}
