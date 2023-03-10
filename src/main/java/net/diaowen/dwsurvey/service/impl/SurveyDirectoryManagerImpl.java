package net.diaowen.dwsurvey.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.QuType;
import net.diaowen.common.base.service.AccountManager;
import net.diaowen.common.plugs.httpclient.HttpResult;
import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.common.utils.RandomUtils;
import net.diaowen.dwsurvey.config.DWSurveyConfig;
import net.diaowen.dwsurvey.config.security.UserDetailsImpl;
import net.diaowen.dwsurvey.dao.SurveyDirectoryDao;
import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.entity.SurveyDetail;
import net.diaowen.dwsurvey.entity.SurveyDirectory;
import net.diaowen.dwsurvey.entity.SurveyStats;
import net.diaowen.dwsurvey.repository.SurveyDetailRepository;
import net.diaowen.dwsurvey.repository.SurveyDirectoryRepository;
import net.diaowen.dwsurvey.service.QuestionManager;
import net.diaowen.dwsurvey.service.SurveyDetailManager;
import net.diaowen.dwsurvey.service.SurveyDirectoryManager;
import net.diaowen.dwsurvey.service.SurveyStatsManager;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * 问卷
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyDirectoryManagerImpl extends BaseServiceImpl<SurveyDirectory, String> implements SurveyDirectoryManager {
  private final SurveyDirectoryDao surveyDirectoryDao;
  private final SurveyDetailManager surveyDetailManager;
  private final QuestionManager questionManager;
  private final SurveyStatsManager surveyStatsManager;
  private final AccountManager accountManager;
  private final SurveyDirectoryRepository surveyDirectoryRepository;
  private final SurveyDetailRepository surveyDetailRepository;

  @Override
  public void setBaseDao() {
    this.baseDao = surveyDirectoryDao;
  }

  @Override
  public void save(SurveyDirectory t) {
    UserDetailsImpl user = accountManager.getCurUser();
    String userId = t.getUserId();
    String id = t.getId();
    if (id == null) {
      t.setUserId(user.getId());
      userId = t.getUserId();
    }
    if (userId != null && userId.equals(user.getId())) {
      String sId = t.getSid();
      if (sId == null || "".equals(sId)) {
        sId = RandomUtils.randomStr(6, 12);
        t.setSid(sId);
      }
      surveyDirectoryRepository.save(t);
      //保存SurveyDirectory
      if (t.getDirType() == 2) {
        SurveyDetail surveyDetailTemp = t.getSurveyDetail();

        SurveyDetail surveyDetail = surveyDetailRepository.findByDirId(t.getId());
        if (surveyDetail != null) {
          if (surveyDetailTemp != null) {
            surveyDetail.setSurveyNote(surveyDetailTemp.getSurveyNote());
          }
        } else {
          surveyDetail = new SurveyDetail();
          surveyDetail.setSurveyNote("非常感谢您的参与！如有涉及个人信息，我们将严格保密。");
        }
        surveyDetail.setDirId(t.getId());
        surveyDetailRepository.save(surveyDetail);
      }
    }
  }

  @Override
  public void saveByAdmin(SurveyDirectory t) {
    String sId = t.getSid();
    if (sId == null || "".equals(sId)) {
      sId = RandomUtils.randomStr(6, 12);
      t.setSid(sId);
    }

    surveyDirectoryRepository.save(t);
  }

  /**
   * 得到当前目录所在的目录位置
   */
  @Override
  public List<SurveyDirectory> findPath(SurveyDirectory surveyDirectory) {
    List<SurveyDirectory> resultList = new ArrayList<SurveyDirectory>();
    if (surveyDirectory != null) {
      List<SurveyDirectory> dirPathList = new ArrayList<SurveyDirectory>();
      dirPathList.add(surveyDirectory);
      String parentUuid = surveyDirectory.getParentId();
      while (parentUuid != null && !"".equals(parentUuid)) {
        SurveyDirectory surveyDirectory2 = get(parentUuid);
        parentUuid = surveyDirectory2.getParentId();
        dirPathList.add(surveyDirectory2);
      }
      for (int i = dirPathList.size() - 1; i >= 0; i--) {
        resultList.add(dirPathList.get(i));
      }
    }
    return resultList;
  }

  @Override
  public SurveyDirectory getSurveyBySid(String sid) {
    SurveyDirectory surveyDirectory = surveyDirectoryRepository.findBySid(sid);
    getSurveyDetail(surveyDirectory.getId(), surveyDirectory);
    return surveyDirectory;
  }

  @Override
  public SurveyDirectory getSurvey(String id) {
    if (id == null || "".equals(id)) {
      return new SurveyDirectory();
    }
    SurveyDirectory directory = surveyDirectoryRepository.findById(id).orElse(new SurveyDirectory());
    getSurveyDetail(id, directory);
    return directory;
  }

  @Override
  public SurveyDirectory findUniqueBy(String id) {
    if (id == null || "".equals(id)) {
      return new SurveyDirectory();
    }
    SurveyDirectory directory = surveyDirectoryRepository.findById(id).orElse(new SurveyDirectory());
    getSurveyDetail(id, directory);
    return directory;
  }

  @Override
  public SurveyDirectory getSurveyByUser(String id, String userId) {
    SurveyDirectory directory = get(id);
    if (userId.equals(directory.getUserId())) {
      getSurveyDetail(id, directory);
      return directory;
    }
    return null;
  }

  @Override
  public void getSurveyDetail(String id, SurveyDirectory directory) {
    String surveyDetailId = directory.getSurveyDetailId();
    SurveyDetail surveyDetail = null;
    if (StringUtils.isNotBlank(surveyDetailId)) {
      surveyDetail = surveyDetailRepository.findById(surveyDetailId).orElse(new SurveyDetail());
    }
    if (surveyDetail == null) {
      surveyDetail = surveyDetailRepository.findByDirId(id);
    }
    if (surveyDetail == null) {
      surveyDetail = new SurveyDetail();
    }
    directory.setSurveyDetail(surveyDetail);
  }

  @Override
  public void upSurveyData(SurveyDirectory entity) {
    List<Question> questions = questionManager.findDetails(entity.getId(), 2);
    if (questions != null) {
      int anItemLeastNum = 0;
      for (Question question : questions) {
        QuType quType = question.getQuType();
        if (quType == QuType.ENUMQU) {//枚举
          anItemLeastNum += question.getParamInt01();
        } else if (quType == QuType.MULTIFILLBLANK) {//组合填空
          anItemLeastNum += question.getQuMultiFillblanks().size();
        } else if (quType == QuType.SCORE) {//评分
          anItemLeastNum += question.getQuScores().size();
        } else {
          anItemLeastNum++;
        }
      }
      entity.setSurveyQuNum(questions.size());
      entity.setAnItemLeastNum(anItemLeastNum);
    }
  }

  @Override
  public void executeSurvey(SurveyDirectory entity) {
    entity.setSurveyState(1);
    //计算可以回答的题量
    upSurveyData(entity);
    super.save(entity);
    //生成全局统计结果记录表

    SurveyStats surveyStats = new SurveyStats();
    surveyStats.setSurveyId(entity.getId());
    surveyStatsManager.save(surveyStats);
  }

  @Override
  public void closeSurvey(SurveyDirectory entity) {
    entity.setSurveyState(2);
    //计算可以回答的题量
    super.save(entity);
    //生成全局统计结果记录表
  }

  @Override
  public void delete(String id) {
    //设为不可见
    SurveyDirectory parentDirectory = get(id);
    parentDirectory.setVisibility(0);
    surveyDirectoryRepository.save(parentDirectory);

    Criterion criterion = Restrictions.eq("parentId", parentDirectory.getId());
    List<SurveyDirectory> directories = findList(criterion);
    if (directories != null) {
      for (SurveyDirectory surveyDirectory : directories) {
        delete(surveyDirectory);
      }
    }
  }

  @Override
  public void delete(SurveyDirectory parentDirectory) {
    String id = parentDirectory.getId();
    //目录ID，为1的为系统默认注册用户目录不能删除
    if (!"1".equals(id)) {
      //设为不可见
      parentDirectory.setVisibility(0);
      Criterion criterion = Restrictions.eq("parentId", parentDirectory.getId());
      List<SurveyDirectory> directories = findList(criterion);
      if (directories != null) {
        for (SurveyDirectory surveyDirectory : directories) {
          delete(surveyDirectory);
        }
      }
    }
  }

  @Override
  public SurveyDirectory findByNameUn(String id, String parentId, String surveyName) {
    List<Criterion> criterions = new ArrayList<Criterion>();
    Criterion eqName = Restrictions.eq("surveyName", surveyName);
    Criterion eqParentId = Restrictions.eq("parentId", parentId);
    criterions.add(eqName);
    criterions.add(eqParentId);

    if (id != null && !"".equals(id)) {
      Criterion eqId = Restrictions.ne("id", id);
      criterions.add(eqId);
    }
    return surveyDirectoryDao.findFirst(criterions);
  }

  @Override
  public SurveyDirectory findByNameUserUn(String id, String surveyName) {
    UserDetailsImpl user = accountManager.getCurUser();
    if (user != null) {
      List<Criterion> criterions = new ArrayList<Criterion>();
      Criterion eqName = Restrictions.eq("surveyName", surveyName);
      Criterion eqUserId = Restrictions.eq("userId", user.getId());
      criterions.add(eqName);
      criterions.add(eqUserId);

      if (id != null && !"".equals(id)) {
        Criterion eqId = Restrictions.ne("id", id);
        criterions.add(eqId);
      }
      return surveyDirectoryDao.findFirst(criterions);
    }
    return null;
  }

  @Override
  public void backDesign(SurveyDirectory entity) {
    entity.setSurveyState(0);
    //计算可以回答的题量
    super.save(entity);
  }

  @Override
  public void checkUp(SurveyDirectory entity) {
    //计算可以回答的题量
    super.save(entity);
  }


  @Override
  public void upSuveyText(SurveyDirectory t) {
    String id = t.getId();
    if (id != null && id.length() > 0) {
      super.save(t);
      //保存SurveyDirectory
      if (t.getDirType() == 2) {
        SurveyDetail surveyDetail = t.getSurveyDetail();
        surveyDetail.setDirId(t.getId());
        surveyDetailManager.save(surveyDetail);
      }
    }
  }

  @Override
  public void saveUser(SurveyDirectory t) {
    super.save(t);
    //保存SurveyDirectory
    if (t.getDirType() == 2) {
      SurveyDetail surveyDetail = t.getSurveyDetail();
      surveyDetail.setDirId(t.getId());
      surveyDetailManager.save(surveyDetail);
    }
  }

  @Override
  public void saveUserSurvey(SurveyDirectory entity) {
    UserDetailsImpl user = accountManager.getCurUser();
    if (user != null) {
      String enId = entity.getId();
      String userId = user.getId();
      if (enId == null || "".equals(enId)) {
        //根据用户名得到目录
        String loginName = user.getUsername();
        SurveyDirectory surveyDirUser = findByNameUn(null, "1", loginName);
        if (surveyDirUser == null) {
          //没有此目录则创建此目录，你ID=1
          surveyDirUser = new SurveyDirectory();
          surveyDirUser.setSurveyName(loginName);
          surveyDirUser.setDirType(1);
          surveyDirUser.setUserId(user.getId());
          surveyDirUser.setParentId("1");
          saveUser(surveyDirUser);
        }
        entity.setParentId(surveyDirUser.getId());

        entity.setUserId(userId);
        saveUser(entity);
      } else {
        //判断当前人有无权限修改
        String enUserId = entity.getUserId();
        if (userId.equals(enUserId)) {
          entity.setUserId(userId);
          saveUser(entity);
        }
      }
    }
  }

  @Override
  public PageDto<SurveyDirectory> findPage(PageDto<SurveyDirectory> page, String surveyName, Integer surveyState, Integer isShare) {
    page.setOrderBy("createDate");
    page.setOrderDir("desc");

    List<Criterion> criterions = new ArrayList<Criterion>();
    criterions.add(Restrictions.eq("visibility", 1));
    criterions.add(Restrictions.eq("dirType", 2));
    criterions.add(Restrictions.eq("surveyModel", 1));

    if (surveyName != null) {
      criterions.add(Restrictions.like("surveyName", "%" + surveyName + "%"));
    }
    if (surveyState != null) {
      criterions.add(Restrictions.eq("surveyState", surveyState));
    }
    if (isShare != null) {
      criterions.add(Restrictions.eq("isShare", isShare));
    }

    return surveyDirectoryDao.findPageList(page, criterions);
  }

  @Override
  public List<SurveyDirectory> newSurveyList() {
    List<SurveyDirectory> result = new ArrayList<SurveyDirectory>();
    try {
      SurveyDirectory entity = new SurveyDirectory();
      PageDto<SurveyDirectory> page = new PageDto<SurveyDirectory>();
      page.setPageSize(25);
      page = findPage(page, null, 1, null);
      result = page.getResult();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public void saveAll(SurveyDirectory directory) {
    List<Question> questions = directory.getQuestions();
    directory.setDirType(2);
    directory.setParentId("402880e5428a2dca01428a2f1f290000");
    directory.setSurveyTag(0);
    directory.setSurveyQuNum(questions.size());
    surveyDirectoryDao.save(directory);
    String surveyId = directory.getId();
    //详细信息
    SurveyDetail detail = directory.getSurveyDetail();
    detail.setDirId(surveyId);
    surveyDetailManager.save(detail);
//		directory.setSurveyDetailId(detail.getId());
    //题目列表
    for (Question question : questions) {
      if (question != null) {
        question.setBelongId(surveyId);
        question.setCreateDate(directory.getCreateDate());
        question.setTag(2);
        questionManager.save(question);
      }
    }
  }

  @Override
  public SurveyDirectory findNext(SurveyDirectory directory) {
    Date date = directory.getCreateDate();
    Criterion criterion = Restrictions.gt("createDate", date);
    return surveyDirectoryDao.findFirst(criterion);
  }

  @Override
  public Page<SurveyDirectory> findByUser(PageRequest page, SurveyDirectory entity) {
    if (entity != null) {
      return findByUser(page, entity.getSurveyName(), entity.getSurveyState());
    }

    return new PageImpl<>(Collections.emptyList(), page, 0);
  }

  @Override
  public Page<SurveyDirectory> findByUser(PageRequest page, String surveyName, Integer surveyState) {
    UserDetailsImpl user = accountManager.getCurUser();
    if (user != null) {
      Sort sort = Sort.by(Sort.Direction.DESC, "createDate");
      page.withSort(sort);

      Specification<SurveyDirectory> spec = (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("userId"), user.getId()));
        predicates.add(cb.equal(root.get("visibility"), 1));
        predicates.add(cb.equal(root.get("dirType"), 2));
        predicates.add(cb.equal(root.get("surveyModel"), 1));

        if (surveyState != null) {
          predicates.add(cb.equal(root.get("surveyState"), surveyState));
        }

        if (StringUtils.isNotBlank(surveyName)) {
          predicates.add(cb.like(root.get("surveyName"), "%" + surveyName + "%"));
        }
        return cb.and(predicates.toArray(new Predicate[0]));
      };

      //分页查询数据
      return surveyDirectoryRepository.findAll(spec, page);
    }
    return new PageImpl<>(Collections.emptyList(), page, 0);
  }

  @Override
  public PageDto<SurveyDirectory> findByGroup(String groupId1, String groupId2, PageDto<SurveyDirectory> page) {
    List<Criterion> criterions = new ArrayList<>();
    if (groupId1 != null && !"".equals(groupId1)) {
      Criterion cri1 = Restrictions.eq("groupId1", groupId1);
      criterions.add(cri1);
    }
    if (groupId2 != null && !"".equals(groupId2)) {
      Criterion cri1_2 = Restrictions.eq("groupId2", groupId2);
      criterions.add(cri1_2);
    }

    Criterion cri2 = Restrictions.eq("visibility", 1);
    Criterion cri4 = Restrictions.eq("surveyModel", 4);

    criterions.add(cri2);
    criterions.add(cri4);
    page.setOrderBy("createDate");
    page.setOrderDir("desc");

    return surveyDirectoryDao.findPage(page, criterions.toArray(new Criterion[criterions.size()]));
  }


  @Override
  public PageDto<SurveyDirectory> findModel(PageDto<SurveyDirectory> page,
                                            SurveyDirectory entity) {
    Integer surveyState = entity.getSurveyState();
    String surveyName = entity.getSurveyName();
    List<Criterion> criterions = new ArrayList<>();

    if (surveyState != null && surveyState != 100) {
      Criterion cri1 = Restrictions.eq("surveyState", surveyState);
      criterions.add(cri1);
    }
    if (surveyName != null && !"".equals(surveyName)) {
      Criterion cri1 = Restrictions.like("surveyName", "%" + surveyName + "%");
      criterions.add(cri1);
    }
    Criterion cri2 = Restrictions.eq("visibility", 1);
    criterions.add(cri2);
    Criterion cri4 = Restrictions.eq("surveyModel", 4);
    criterions.add(cri4);
    page.setOrderBy("createDate");
    page.setOrderDir("desc");
    return surveyDirectoryDao.findPageList(page, criterions);
  }

  @Override
  public List<SurveyDirectory> findByIndex() {
    Criterion cri1 = Restrictions.eq("visibility", 1);
    Criterion cri2 = Restrictions.eq("parentId", "402880e5428a2dca01428a2f1f290000");
    Criterion cri3 = Restrictions.eq("surveyTag", 1);
    Criterion cri4 = Restrictions.isNull("sid");
    PageDto<SurveyDirectory> page = new PageDto<SurveyDirectory>();
    page.setOrderBy("createDate");
    page.setOrderDir("desc");
    page.setPageSize(10);
    return surveyDirectoryDao.findPage(page, cri1, cri2, cri3, cri4).getResult();
  }

  @Override
  public List<SurveyDirectory> findByT1() {
    Criterion cri1 = Restrictions.eq("visibility", 1);
    Criterion cri2 = Restrictions.eq("parentId", "402880e5428a2dca01428a2f1f290000");
    Criterion cri3 = Restrictions.eq("surveyTag", 1);
    Criterion cri4 = Restrictions.isNull("sid");
    PageDto<SurveyDirectory> page = new PageDto<>();
    page.setOrderBy("createDate");
    page.setOrderDir("desc");
    page.setPageSize(10);
    return surveyDirectoryDao.findPage(page, cri1, cri2, cri3, cri4).getResult();
  }

  @Override
  public SurveyDirectory createBySurvey(String fromBankId, String surveyName, String tag) {
    SurveyDirectory surveyDirectory = buildCopyObj(fromBankId, surveyName, tag);
    saveUserSurvey(surveyDirectory);
    String belongId = surveyDirectory.getId();
    List<Question> questions = questionManager.find(fromBankId, Integer.parseInt(tag));
    questionManager.saveBySurvey(belongId, 2, questions);
    return surveyDirectory;
  }

  private SurveyDirectory buildCopyObj(String fromBankId, String surveyName, String tag) {
    SurveyDirectory surveyDirectory = new SurveyDirectory();
    surveyDirectory.setSurveyName(surveyName);
    surveyDirectory.setSurveyNameText(surveyName);
    surveyDirectory.setDirType(2);
    surveyDirectory.setSurveyDetail(new SurveyDetail());
    SurveyDirectory directory = getSurvey(fromBankId);
    directory.setExcerptNum(directory.getExcerptNum() + 1);
    super.save(directory);
    surveyDirectory.setSurveyQuNum(directory.getSurveyQuNum());
    surveyDirectory.getSurveyDetail().setSurveyNote(surveyDirectory.getSurveyDetail().getSurveyNote());
    return surveyDirectory;
  }


  @Override
  public void delete(String[] id) {
    if (id != null) {
      for (String idValue : id) {
        delete(idValue);
      }
    }
  }

  @Override
  public void upSurveyState(String surveyId, Integer surveyState) throws IOException {
    if (surveyId != null && surveyState != null) {
      SurveyDirectory survey = findUniqueBy(surveyId);
      if (surveyState == 1) {
        devSurvey(survey);
      } else {
        survey.setSurveyState(surveyState);
        super.save(survey);
      }
    }

  }

  @Override
  public void devSurvey(SurveyDirectory survey) throws IOException {
    String surveyId = survey.getId();
    survey.setSurveyState(1);
    super.save(survey);
    String path = devSurveyJson(surveyId);
    survey.setJsonPath(path);
    super.save(survey);
  }

  @Override
  public String devSurveyJson(String surveyId) {
    try {
      SurveyDirectory surveyDirectory = findUniqueBy(surveyId);
      String sid = surveyDirectory.getSid();
      ObjectMapper mapper = new ObjectMapper();

      String infoJsonString = mapper.writeValueAsString(HttpResult.SUCCESS(surveyDirectory));
      String savePath = File.separator + "file" + File.separator + "survey" + File.separator + sid + File.separator;

      writerJson(infoJsonString, savePath, sid + "_info.json");

      List<Question> questions = questionManager.findDetails(surveyDirectory.getId(), 2);
      surveyDirectory.setQuestions(questions);
      surveyDirectory.setSurveyQuNum(questions.size());
      String jsonString = mapper.writeValueAsString(HttpResult.SUCCESS(surveyDirectory));
      String fileName = sid + ".json";
      writerJson(jsonString, savePath, fileName);

      return savePath + fileName;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void writerJson(String jsonString, String savePath, String fileName) throws IOException {
    String rootPath = DWSurveyConfig.DWSURVEY_WEB_FILE_PATH;
    File dirFile = new File(rootPath + savePath);
    if (!dirFile.exists() && !dirFile.isDirectory()) {
      dirFile.mkdirs();
    }
    File localFile = new File(rootPath + savePath + fileName);
    if (!localFile.exists()) {
      localFile.createNewFile();
    }
    FileOutputStream fos = new FileOutputStream(localFile);
    fos.write(jsonString.getBytes());
    fos.close();
  }
}
