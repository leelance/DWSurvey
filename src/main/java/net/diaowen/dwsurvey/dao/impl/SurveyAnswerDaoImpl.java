package net.diaowen.dwsurvey.dao.impl;

import net.diaowen.common.dao.BaseDaoImpl;
import net.diaowen.dwsurvey.dao.SurveyAnswerDao;
import net.diaowen.dwsurvey.entity.SurveyAnswer;
import net.diaowen.dwsurvey.entity.SurveyStats;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * 问卷回答 dao
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */

@Repository
public class SurveyAnswerDaoImpl extends BaseDaoImpl<SurveyAnswer, String> implements SurveyAnswerDao {

  @Override
  public SurveyStats surveyStatsData(SurveyStats surveyStats) {
    try {
      String sqlBuf = "select MIN(bg_an_date) firstDate,MAX(bg_an_date) lastDate,count(id) anCount,min(total_time) minTime,avg(total_time) avgTime, " + "count(case when is_complete =1 then is_complete end) complete1, " +
          "count(case when is_effective =1 then is_effective end) effective1, " +
          "count(case when handle_state =0 then handle_state end) handle0, " +
          "count(case when handle_state =1 then handle_state end) handle1, " +
          "count(case when handle_state =2 then handle_state end) handle2, " +
          "count(case when data_source =0 then data_source end) datasource0, " +
          "count(case when data_source =1 then data_source end) datasource1, " +
          "count(case when data_source =2 then data_source end) datasource2, " +
          "count(case when data_source =3 then data_source end) datasource3 " +
          "from t_survey_answer where survey_id=? ";
      Object[] objects = (Object[]) this.getSession().createSQLQuery(sqlBuf).setParameter(1, surveyStats.getSurveyId()).uniqueResult();

      surveyStats.setFirstAnswer((Date) objects[0]);
      surveyStats.setLastAnswer((Date) objects[1]);
      surveyStats.setAnswerNum(Integer.parseInt(objects[2].toString()));
      String minTime = objects[3].toString();

      int minIndex = minTime.indexOf(".");
      if (minIndex > 0) {
        minTime = minTime.substring(0, minIndex);
      }
      //Min Time
      surveyStats.setAnMinTime(Integer.parseInt(minTime));

      String avgTime = objects[4].toString();
      int avgIndex = avgTime.indexOf(".");
      if (avgIndex > 0) {
        avgTime = avgTime.substring(0, avgIndex);
      }
      //Avg Time
      surveyStats.setAnAvgTime(Integer.parseInt(avgTime));

      surveyStats.setCompleteNum(Integer.parseInt(objects[5].toString()));
      surveyStats.setEffectiveNum(Integer.parseInt(objects[6].toString()));
      surveyStats.setUnHandleNum(Integer.parseInt(objects[7].toString()));
      surveyStats.setHandlePassNum(Integer.parseInt(objects[8].toString()));
      surveyStats.setHandleUnPassNum(Integer.parseInt(objects[9].toString()));

      surveyStats.setOnlineNum(Integer.parseInt(objects[10].toString()));
      surveyStats.setInputNum(Integer.parseInt(objects[11].toString()));
      surveyStats.setMobileNum(Integer.parseInt(objects[12].toString()));
      surveyStats.setImportNum(Integer.parseInt(objects[13].toString()));
    } catch (Exception e) {
      e.printStackTrace();
    }
    //0.网调  1.录入数据 2.移动数据 3.导入数据
    return surveyStats;
  }

  @Override
  public Long countResult(String surveyId) {
    Criterion cri2 = Restrictions.lt("handleState", 3);
    Criterion cri3 = Restrictions.eq("isEffective", 1);
    Criteria c;
    if (surveyId != null) {
      Criterion cri1 = Restrictions.eq("surveyId", surveyId);
      c = createCriteria(cri1, cri2, cri3);
    } else {
      c = createCriteria(cri2, cri3);
    }
    return countCriteriaResult(c);
  }

}
