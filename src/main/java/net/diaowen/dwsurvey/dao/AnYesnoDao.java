package net.diaowen.dwsurvey.dao;

import net.diaowen.common.dao.BaseDao;
import net.diaowen.dwsurvey.entity.AnYesno;
import net.diaowen.dwsurvey.entity.DataCross;
import net.diaowen.dwsurvey.entity.Question;

import java.util.List;

/**
 * 是非题 interface
 *
 * @author KeYuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface AnYesnoDao extends BaseDao<AnYesno, String> {

  List<DataCross> findStatsDataCross(Question rowQuestion, Question colQuestion);

  List<DataCross> findStatsDataChart(Question question);

}
