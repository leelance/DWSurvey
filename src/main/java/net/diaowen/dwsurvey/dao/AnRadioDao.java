package net.diaowen.dwsurvey.dao;

import net.diaowen.common.dao.BaseDao;
import net.diaowen.dwsurvey.entity.AnRadio;
import net.diaowen.dwsurvey.entity.DataCross;
import net.diaowen.dwsurvey.entity.Question;

import java.util.List;

/**
 * 单选题 interface
 *
 * @author KeYuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface AnRadioDao extends BaseDao<AnRadio, String> {


  List<DataCross> findStatsDataCross(Question rowQuestion, Question colQuestion);

  List<DataCross> findStatsDataChart(Question question);

}
