package net.diaowen.dwsurvey.service;

import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.common.service.BaseService;
import net.diaowen.dwsurvey.entity.SurveyDirectory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.List;

/**
 * 问卷处理
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface SurveyDirectoryManager extends BaseService<SurveyDirectory, String> {

  /**
   * 根据 最底层对象，得到此对象所在的目录结构
   *
   * @param surveyDirectory
   * @return
   */
  List<SurveyDirectory> findPath(SurveyDirectory surveyDirectory);

  SurveyDirectory getSurvey(String id);

  SurveyDirectory findUniqueBy(String id);

  SurveyDirectory getSurveyBySid(String sId);

  SurveyDirectory getSurveyByUser(String id, String userId);

  void getSurveyDetail(String id, SurveyDirectory directory);

  void upSurveyData(SurveyDirectory entity);

  void executeSurvey(SurveyDirectory entity);

  void closeSurvey(SurveyDirectory entity);

  SurveyDirectory findByNameUn(String id, String parentId, String surveyName);

  void backDesign(SurveyDirectory entity);

//	 void save(SurveyDirectory entity, String[] surGroupIds);

//	 void saveUserSurvey(SurveyDirectory entity, String[] surGroupIds);

  void saveUser(SurveyDirectory t);

  void saveUserSurvey(SurveyDirectory entity);

  SurveyDirectory findByNameUserUn(String id, String surveyName);

  PageDto<SurveyDirectory> findPage(PageDto<SurveyDirectory> page, String surveyName, Integer surveyState, Integer isShare);

  List<SurveyDirectory> newSurveyList();

  void upSuveyText(SurveyDirectory entity);

  void checkUp(SurveyDirectory surveyDirectory);

  SurveyDirectory findNext(SurveyDirectory directory);

  void saveAll(SurveyDirectory directory);

  Page<SurveyDirectory> findByUser(PageRequest page, SurveyDirectory surveyDirectory);

  Page<SurveyDirectory> findByUser(PageRequest page, String surveyName, Integer surveyState);

  PageDto<SurveyDirectory> findByGroup(String groupId1, String groupId2, PageDto<SurveyDirectory> page);

  List<SurveyDirectory> findByIndex();

  List<SurveyDirectory> findByT1();

  void saveByAdmin(SurveyDirectory t);

  PageDto<SurveyDirectory> findModel(PageDto<SurveyDirectory> page,
                                     SurveyDirectory entity);

  SurveyDirectory createBySurvey(String fromBankId, String surveyName,
                                 String tag);

  void devSurvey(SurveyDirectory survey) throws IOException;

  String devSurveyJson(String surveyId);

  void delete(String[] id);

  void upSurveyState(String surveyId, Integer surveyState) throws IOException;

}
