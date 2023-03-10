package net.diaowen.dwsurvey.service;

import net.diaowen.common.service.BaseService;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.entity.SurveyDirectory;

import java.util.List;

/**
 * 题基础
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface QuestionManager extends BaseService<Question, String> {

  Question getDetail(String quId);

  List<Question> find(String belongId, int tag);

  List<Question> findDetails(String belongId, int tag);

  void getQuestionOption(Question question);

  List<Question> findByParentQuId(String parentQuId);

  List<Question> findByQuIds(String[] quIds, boolean b);

  void deletes(String[] delQuIds);

  /**
   * 交接排序位置--前台交换
   *
   * @param prevId prevId
   * @param nextId nextId
   * @return boolean
   */
  boolean upsort(String prevId, String nextId);

  void saveBySurvey(String belongId, int tag, List<Question> questions);

  void saveChangeQu(String belongId, int tag, String[] quIds);

  List<Question> findStatsRowVarQus(SurveyDirectory survey);

  List<Question> findStatsColVarQus(SurveyDirectory survey);

  void update(Question entity);

  /**
   * 根据quId查询题目记录
   *
   * @param quId quId
   * @return Question
   */
  Question findOne(String quId);
}
