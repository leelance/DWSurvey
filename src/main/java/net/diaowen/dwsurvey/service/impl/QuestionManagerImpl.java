package net.diaowen.dwsurvey.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.QuType;
import net.diaowen.common.base.service.AccountManager;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.common.utils.ReflectionUtils;
import net.diaowen.dwsurvey.config.security.UserDetailsImpl;
import net.diaowen.dwsurvey.dao.QuestionDao;
import net.diaowen.dwsurvey.entity.*;
import net.diaowen.dwsurvey.repository.question.*;
import net.diaowen.dwsurvey.repository.survey.SurveyDirectoryRepository;
import net.diaowen.dwsurvey.service.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;


/**
 * 基础题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@RequiredArgsConstructor
@Service("questionManager")
public class QuestionManagerImpl extends BaseServiceImpl<Question, String> implements QuestionManager {
  private final QuestionDao questionDao;
  private final QuCheckboxManager quCheckboxManager;
  private final QuRadioManager quRadioManager;
  private final QuMultiFillblankManager quMultiFillblankManager;
  private final QuScoreManager quScoreManager;
  private final QuOrderbyManager quOrderbyManager;
  private final QuestionLogicManager questionLogicManager;
  private final AccountManager accountManager;
  private final QuestionRepository questionRepository;
  private final SurveyDirectoryRepository surveyDirectoryRepository;
  private final QuRadioRepository quRadioRepository;
  private final QuCheckBoxRepository quCheckBoxRepository;
  private final QuMultiFillBankRepository quMultiFillBankRepository;
  private final QuScoreRepository quScoreRepository;
  private final QuOrderByRepository quOrderByRepository;
  private final QuLogicRepository quLogicRepository;

  @Override
  public void setBaseDao() {
    this.baseDao = questionDao;
  }

  /**
   * 所有修改，新增题的入口 方法
   *
   * @param question Question
   */
  @Override
  public void save(Question question) {
    UserDetailsImpl user = accountManager.getCurUser();
    if (user != null) {
      surveyDirectoryRepository.findById(question.getBelongId()).ifPresent(survey -> {
        if (user.getId().equals(survey.getUserId())) {
          String uuid = question.getId();
          if (uuid == null || "".equals(uuid)) {
            question.setId(null);
          }

          saveQuestion(question);
        }
      });
    }
  }

  /**
   * 依据条件得到符合条件的题列表，不包含选项信息   用于列表显示
   *
   * @param tag 1题库  2问卷
   * @return List<Question>
   */
  @Override
  public List<Question> find(String belongId, int tag) {
    List<Question> list = questionRepository.findByBelongIdAndTagAndQuTagNot(belongId, tag, 3);
    if (Objects.nonNull(list) && !list.isEmpty()) {
      list.sort(Comparator.comparing(Question::getOrderById));
    }
    return list;
  }

  /**
   * 查出指定条件下的所有题，及每一题内容的选项   用于展示试卷,如预览,答卷,查看
   *
   * @param tag      tag
   * @param belongId belongId
   * @return List<Question>
   */
  @Override
  public List<Question> findDetails(String belongId, int tag) {
    List<Question> questions = find(belongId, tag);
    for (Question question : questions) {
      getQuestionOption(question);
    }
    return questions;
  }

  /**
   * 得到某一题下面的选项，包含大题下面的小题
   *
   * @param question
   */
  @Override
  public void getQuestionOption(Question question) {
    String quId = question.getId();
    QuType quType = question.getQuType();
    if (quType == QuType.RADIO || quType == QuType.COMPRADIO) {
      question.setQuRadios(quRadioManager.findByQuId(quId));
    } else if (quType == QuType.CHECKBOX || quType == QuType.COMPCHECKBOX) {
      question.setQuCheckboxs(quCheckboxManager.findByQuId(quId));
    } else if (quType == QuType.MULTIFILLBLANK) {
      question.setQuMultiFillblanks(quMultiFillblankManager.findByQuId(quId));
    } else if (quType == QuType.BIGQU) {
      //根据大题ID，找出所有小题
      String parentQuId = question.getId();
      List<Question> childQuList = findByParentQuId(parentQuId);
      for (Question childQu : childQuList) {
        getQuestionOption(childQu);
      }
      question.setQuestions(childQuList);
      //根据小题的类型，取选项
    } else if (quType == QuType.SCORE) {
      List<QuScore> quScores = quScoreManager.findByQuId(quId);
      question.setQuScores(quScores);
    } else if (quType == QuType.ORDERQU) {
      List<QuOrderby> orderBy = quOrderbyManager.findByQuId(quId);
      question.setQuOrderbys(orderBy);
    }
    List<QuestionLogic> questionLogics = questionLogicManager.findByCkQuId(quId);
    question.setQuestionLogics(questionLogics);
  }

  @Override
  public List<Question> findByParentQuId(String parentQuId) {
    Specification<Question> spec = (root, query, cb) -> cb.and(cb.equal(root.get("parentQuUuId"), parentQuId));
    Sort sort = Sort.by(Sort.Direction.ASC, "orderById");
    return questionRepository.findAll(spec, sort);
  }

  /**
   * 根据ID，得到一批题
   *
   * @param quIds
   * @param b     表示是否提出每一题的详细选项信息
   * @return
   */
  @Override
  public List<Question> findByQuIds(String[] quIds, boolean b) {
    List<Question> questions = new ArrayList<Question>();
    if (quIds == null || quIds.length <= 0) {
      return questions;
    }
    StringBuffer hqlBuf = new StringBuffer("from Question qu where qu.id in(");
    for (String quId : quIds) {
      hqlBuf.append("'" + quId + "'").append(",");
    }
//		hqlBuf.append("0)");
    String hql = hqlBuf.substring(0, hqlBuf.lastIndexOf(",")) + ")";
    questions = questionDao.find(hql);
    if (b) {
      for (Question question : questions) {
        getQuestionOption(question);
      }
    }
    return questions;
  }

  /**
   * 批量删除题，及题包含的选项一同删除-真删除。
   *
   * @param delQuIds
   */
  @Override
  public void deletes(String[] delQuIds) {
    if (delQuIds != null) {
      for (String quId : delQuIds) {

      }
    }
  }

  @Override
  public void delete(String quId) {
    if (quId != null && !"".equals(quId)) {
      Question question = get(quId);
      //同时删除掉相应的选项
      if (question != null) {
        String belongId = question.getBelongId();
        int orderById = question.getOrderById();
        questionRepository.delete(question);
        questionRepository.subQuestionOrderId(belongId, orderById);
      }
    }
  }

  /**
   * 题排序
   *
   * @param prevId
   * @param nextId
   */
  @Override
  public boolean upsort(String prevId, String nextId) {
    if (prevId != null && !"".equals(prevId) && nextId != null && !"".equals(nextId)) {
      Question prevQuestion = get(prevId);
      Question nextQuestion = get(nextId);
      int prevNum = prevQuestion.getOrderById();
      int nextNum = nextQuestion.getOrderById();

      prevQuestion.setOrderById(nextNum);
      nextQuestion.setOrderById(prevNum);

      save(prevQuestion);
      save(nextQuestion);
      return true;
    }
    return false;
  }

  public Question findUnById(String id) {
    return questionDao.findUniqueBy("id", id);
  }

  public List<Question> findByparentQuId(String parentQuUuId) {
		/*List<PropertyFilter> filters=new ArrayList<PropertyFilter>();
		filters.add(new PropertyFilter("EQS_parentQuUuId", parentQuUuId));
		return findList(filters);*/
    CriteriaBuilder criteriaBuilder = questionDao.getSession().getCriteriaBuilder();
    CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(Question.class);
    Root root = criteriaQuery.from(Question.class);
    criteriaQuery.select(root);
    Predicate eqParentQuId = criteriaBuilder.equal(root.get("parentQuUuId"), parentQuUuId);
    criteriaQuery.where(eqParentQuId);
    criteriaQuery.orderBy(criteriaBuilder.asc(root.get("orderById")));
    return questionDao.findAll(criteriaQuery);
  }

  @Override
  public void saveBySurvey(String belongId, int tag, List<Question> questions) {
    for (Question question : questions) {
      copyQu(belongId, tag, question);
    }
  }

  /**
   * 保存选中的题目 即从题库或从其它试卷中的题
   */
  @Override
  public void saveChangeQu(String belongId, int tag, String[] quIds) {
    for (String quId : quIds) {
      Question changeQuestion = findUnById(quId);
      copyQu(belongId, tag, changeQuestion);
    }
  }

  private void copyQu(String belongId, int tag, Question changeQuestion) {
    String quId = changeQuestion.getId();
    if (changeQuestion.getQuType() == QuType.BIGQU) {
      Question question = new Question();
      ReflectionUtils.copyAttr(changeQuestion, question);
      //设置相关要改变的值
      question.setId(null);
      question.setBelongId(belongId);
      question.setCreateDate(new Date());
      question.setTag(tag);
      question.setQuTag(2);
      question.setCopyFromId(quId);

      List<Question> changeChildQuestions = findByparentQuId(quId);
      List<Question> qulits = new ArrayList<Question>();
      for (Question changeQu : changeChildQuestions) {
        Question question2 = new Question();
        ReflectionUtils.copyAttr(changeQu, question2);
        //设置相关要改变的值
        question2.setId(null);
        question2.setBelongId(belongId);
        question2.setCreateDate(new Date());
        question2.setTag(tag);
        question2.setQuTag(3);
        question2.setCopyFromId(changeQu.getId());

        getQuestionOption(changeQu);
        copyItems(belongId, changeQu, question2);

        qulits.add(question2);
      }
      question.setQuestions(qulits);
      save(question);
    } else {
      copyroot(belongId, tag, changeQuestion);
    }
  }

  private void copyroot(String belongId, Integer tag, Question changeQuestion) {
    //拷贝先中的问题属性值到新对象中
    Question question = new Question();
    ReflectionUtils.copyAttr(changeQuestion, question);
    //设置相关要改变的值
    question.setId(null);
    question.setBelongId(belongId);
    question.setCreateDate(new Date());
    question.setTag(tag);
    question.setCopyFromId(changeQuestion.getId());

    getQuestionOption(changeQuestion);
    copyItems(belongId, changeQuestion, question);
    save(question);
  }

  private void copyItems(String quBankUuid, Question changeQuestion, Question question) {
    QuType quType = changeQuestion.getQuType();
    if (quType == QuType.RADIO || quType == QuType.COMPRADIO) {
      List<QuRadio> changeQuRadios = changeQuestion.getQuRadios();
      List<QuRadio> quRadios = new ArrayList<>();
      for (QuRadio changeQuRadio : changeQuRadios) {
        QuRadio quRadio = new QuRadio();
        ReflectionUtils.copyAttr(changeQuRadio, quRadio);
        quRadio.setId(null);
        quRadios.add(quRadio);
      }
      question.setQuRadios(quRadios);
    } else if (quType == QuType.CHECKBOX || quType == QuType.COMPCHECKBOX) {
      List<QuCheckbox> changeQuCheckboxs = changeQuestion.getQuCheckboxs();
      List<QuCheckbox> quCheckboxs = new ArrayList<>();
      for (QuCheckbox changeQuCheckbox : changeQuCheckboxs) {
        QuCheckbox quCheckbox = new QuCheckbox();
        ReflectionUtils.copyAttr(changeQuCheckbox, quCheckbox);
        quCheckbox.setId(null);
        quCheckboxs.add(quCheckbox);
      }
      question.setQuCheckboxs(quCheckboxs);
    } else if (quType == QuType.MULTIFILLBLANK) {
      List<QuMultiFillblank> changeQuDFillbanks = changeQuestion.getQuMultiFillblanks();
      List<QuMultiFillblank> quDFillbanks = new ArrayList<>();
      for (QuMultiFillblank changeQuDFillbank : changeQuDFillbanks) {
        QuMultiFillblank quDFillbank = new QuMultiFillblank();
        ReflectionUtils.copyAttr(changeQuDFillbank, quDFillbank);
        quDFillbank.setId(null);
        quDFillbanks.add(quDFillbank);
      }
      question.setQuMultiFillblanks(quDFillbanks);
    } else if (quType == QuType.SCORE) {
      //评分
      List<QuScore> changeQuScores = changeQuestion.getQuScores();
      List<QuScore> quScores = new ArrayList<>();
      for (QuScore changeQuScore : changeQuScores) {
        QuScore quScore = new QuScore();
        ReflectionUtils.copyAttr(changeQuScore, quScore);
        quScore.setId(null);
        quScores.add(quScore);
      }
      question.setQuScores(quScores);
    } else if (quType == QuType.ORDERQU) {
      //评分
      List<QuOrderby> changeQuOrderbys = changeQuestion.getQuOrderbys();
      List<QuOrderby> quOrderbyList = new ArrayList<>();
      for (QuOrderby changeQuOrder : changeQuOrderbys) {
        QuOrderby quOrderby = new QuOrderby();
        ReflectionUtils.copyAttr(changeQuOrder, quOrderby);
        quOrderby.setId(null);
        quOrderbyList.add(quOrderby);
      }
      question.setQuOrderbys(quOrderbyList);
    }

  }


  @Override
  public List<Question> findStatsRowVarQus(SurveyDirectory survey) {
    Criterion criterion1 = Restrictions.eq("belongId", survey.getId());
    Criterion criterion2 = Restrictions.eq("tag", 2);

//		Criterion criterion31=Restrictions.ne("quType", QuType.FILLBLANK);
//		Criterion criterion32=Restrictions.ne("quType", QuType.MULTIFILLBLANK);
//		Criterion criterion33=Restrictions.ne("quType", QuType.ANSWER);
//
////		Criterion criterion3=Restrictions.or(criterion31, criterion32);
//		//where s=2 and (fds !=1 or fds!=2 )
//		return questionDao.find(criterion1,criterion2,criterion31,criterion32,criterion33);

    Criterion criterion31 = Restrictions.ne("quType", QuType.FILLBLANK);
    Criterion criterion32 = Restrictions.ne("quType", QuType.MULTIFILLBLANK);
    Criterion criterion33 = Restrictions.ne("quType", QuType.ANSWER);
    Criterion criterion34 = Restrictions.ne("quType", QuType.CHENCHECKBOX);
    Criterion criterion35 = Restrictions.ne("quType", QuType.CHENFBK);
    Criterion criterion36 = Restrictions.ne("quType", QuType.CHENRADIO);
    Criterion criterion37 = Restrictions.ne("quType", QuType.ENUMQU);
    Criterion criterion38 = Restrictions.ne("quType", QuType.ORDERQU);
    Criterion criterion39 = Restrictions.ne("quType", QuType.SCORE);

    return questionDao.find(criterion1, criterion2, criterion31, criterion32, criterion33, criterion34, criterion35, criterion36, criterion37, criterion38, criterion39);
//		return null;
  }


  @Override
  public List<Question> findStatsColVarQus(SurveyDirectory survey) {
    Criterion criterion1 = Restrictions.eq("belongId", survey.getId());
    Criterion criterion2 = Restrictions.eq("tag", 2);

//		Criterion criterion31=Restrictions.ne("quType", QuType.FILLBLANK);
//		Criterion criterion32=Restrictions.ne("quType", QuType.MULTIFILLBLANK);
//		Criterion criterion33=Restrictions.ne("quType", QuType.ANSWER);
//
////		Criterion criterion3=Restrictions.or(criterion31, criterion32);
//		//where s=2 and (fds !=1 or fds!=2 )
//		return questionDao.find(criterion1,criterion2,criterion31,criterion32,criterion33);

    Criterion criterion31 = Restrictions.ne("quType", QuType.FILLBLANK);
    Criterion criterion32 = Restrictions.ne("quType", QuType.MULTIFILLBLANK);
    Criterion criterion33 = Restrictions.ne("quType", QuType.ANSWER);
    Criterion criterion34 = Restrictions.ne("quType", QuType.CHENCHECKBOX);
    Criterion criterion35 = Restrictions.ne("quType", QuType.CHENFBK);
    Criterion criterion36 = Restrictions.ne("quType", QuType.CHENRADIO);
    Criterion criterion37 = Restrictions.ne("quType", QuType.ENUMQU);
    Criterion criterion38 = Restrictions.ne("quType", QuType.ORDERQU);
    Criterion criterion39 = Restrictions.ne("quType", QuType.SCORE);

    return questionDao.find(criterion1, criterion2, criterion31, criterion32, criterion33, criterion34, criterion35, criterion36, criterion37, criterion38, criterion39);
  }


  @Override
  public Question getDetail(String quId) {
    Question question = questionRepository.findById(quId).orElse(new Question());
    getQuestionOption(question);
    return question;
  }

  @Override
  public void update(Question entity) {
    questionRepository.save(entity);
  }

  /**
   * 根据quId查询题目记录
   *
   * @param quId quId
   * @return Question
   */
  @Override
  public Question findOne(String quId) {
    if (StringUtils.isBlank(quId)) {
      Question question = new Question();
      return questionRepository.save(question);
    }

    return questionRepository.findById(quId).orElse(new Question());
  }

  private void saveQuestion(Question entity) {
    boolean isNew = false;
    String id = entity.getId();
    String belongId = entity.getBelongId();
    int orderById = entity.getOrderById();
    //如果是新增的题目，则根据已有的题来设置排序号
    if (id == null || "".equals(id)) {
      isNew = true;
    }

    //保存题目的题干部分
    questionRepository.save(entity);

    //判断题目类型
    QuType quType = entity.getQuType();
    if (quType == QuType.RADIO || quType == QuType.COMPRADIO) {
      saveRadio(entity);
    } else if (quType == QuType.CHECKBOX || quType == QuType.COMPCHECKBOX) {
      saveCheckbox(entity);
    } else if (quType == QuType.MULTIFILLBLANK) {
      saveMultiFillBlank(entity);
    } else if (quType == QuType.BIGQU) {
      saveQuBig(entity);
    } else if (quType == QuType.SCORE) {
      saveQuScore(entity);
    } else if (quType == QuType.ORDERQU) {
      saveQuOrderBy(entity);
    }
    //更新排序号--如果是新增
    saveQuLogic(entity);
    if (isNew) {
      updateQuestionOrderId(belongId, orderById);
    }
  }

  /**
   * 题目逻辑保存
   *
   * @param entity Question
   */
  private void saveQuLogic(Question entity) {
    List<QuestionLogic> logics = entity.getQuestionLogics();
    if (logics != null) {
      for (QuestionLogic logic : logics) {
        String logicId = logic.getId();
        if ("".equals(logicId)) {
          logic.setId(null);
        }
        logic.setCkQuId(entity.getId());
      }

      quLogicRepository.saveAll(logics);
    }
  }

  /**
   * 保存评分题
   *
   * @param entity Question
   */
  private void saveQuScore(Question entity) {
    List<QuScore> scores = entity.getQuScores();
    for (QuScore score : scores) {
      score.setQuId(entity.getId());
    }

    quScoreRepository.saveAll(scores);
  }

  /**
   * 保存排序题
   *
   * @param entity Question
   */
  private void saveQuOrderBy(Question entity) {
    List<QuOrderby> orderBys = entity.getQuOrderbys();
    for (QuOrderby orderBy : orderBys) {
      orderBy.setQuId(entity.getId());
    }

    quOrderByRepository.saveAll(orderBys);
  }

  /**
   * 保存大题
   *
   * @param entity Question
   */
  private void saveQuBig(Question entity) {
    List<Question> questions = entity.getQuestions();
    questionRepository.save(entity);

    for (Question question : questions) {
      question.setParentQuId(entity.getId());
      saveQuestion(question);
    }
  }

  /**
   * 保存单选题的单选项
   *
   * @param entity Question
   */
  private void saveRadio(Question entity) {
    List<QuRadio> radios = entity.getQuRadios();
    for (QuRadio radio : radios) {
      String quRadioId = radio.getId();
      if ("0".equals(quRadioId)) {
        radio.setId(null);
      }
      radio.setQuId(entity.getId());
    }

    quRadioRepository.saveAll(radios);
  }

  /**
   * 保存多选题选项
   *
   * @param entity Question
   */
  private void saveCheckbox(Question entity) {
    List<QuCheckbox> checkboxes = entity.getQuCheckboxs();
    for (QuCheckbox checkbox : checkboxes) {
      String checkboxId = checkbox.getId();
      if ("0".equals(checkboxId)) {
        checkbox.setId(null);
      }

      checkbox.setQuId(entity.getId());
    }

    quCheckBoxRepository.saveAll(checkboxes);
  }

  /**
   * 保存多项填空题选项
   *
   * @param entity Question
   */
  private void saveMultiFillBlank(Question entity) {
    List<QuMultiFillblank> multiFillBlanks = entity.getQuMultiFillblanks();

    for (QuMultiFillblank quMultiFillblank : multiFillBlanks) {
      quMultiFillblank.setQuId(entity.getId());
    }
    quMultiFillBankRepository.saveAll(multiFillBlanks);

    // 执行要删除的选项
    String[] removeOptionUuIds = entity.getRemoveOptionUuIds();
    if (removeOptionUuIds != null) {
      quMultiFillBankRepository.deleteAllById(Lists.newArrayList(removeOptionUuIds));
    }
  }

  /**
   * 属性 belongId所有题目，只要大于等于orderById+1
   *
   * @param belongId  belongId
   * @param orderById orderById
   */
  private void updateQuestionOrderId(String belongId, Integer orderById) {
    if (StringUtils.isNotBlank(belongId)) {
      orderById = orderById == null ? 0 : orderById;
      questionRepository.addQuestionOrderId(belongId, orderById);
    }
  }
}
