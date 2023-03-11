package net.diaowen.dwsurvey.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.QuType;
import net.diaowen.common.service.BaseServiceImpl;
import net.diaowen.common.utils.RandomUtils;
import net.diaowen.common.utils.RunAnswerUtil;
import net.diaowen.common.utils.StringConst;
import net.diaowen.common.utils.excel.XLSXExportUtil;
import net.diaowen.common.utils.parsehtml.HtmlUtil;
import net.diaowen.dwsurvey.common.SurveyConst;
import net.diaowen.dwsurvey.config.DWSurveyConfig;
import net.diaowen.dwsurvey.dao.SurveyAnswerDao;
import net.diaowen.dwsurvey.entity.*;
import net.diaowen.dwsurvey.repository.answer.*;
import net.diaowen.dwsurvey.repository.survey.SurveyAnswerRepository;
import net.diaowen.dwsurvey.repository.survey.SurveyDirectoryRepository;
import net.diaowen.dwsurvey.service.*;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 问卷回答记录
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyAnswerManagerImpl extends BaseServiceImpl<SurveyAnswer, String> implements SurveyAnswerManager {
  private final SurveyAnswerDao surveyAnswerDao;
  private final QuestionManager questionManager;
  private final AnYesnoManager anYesnoManager;
  private final AnRadioManager anRadioManager;
  private final AnFillblankManager anFillblankManager;
  private final AnEnumquManager anEnumquManager;
  private final AnDFillblankManager anDFillblankManager;
  private final AnCheckboxManager anCheckboxManager;
  private final AnAnswerManager anAnswerManager;
  private final AnScoreManager anScoreManager;
  private final AnOrderManager anOrderManager;
  private final AnUploadFileManager anUploadFileManager;
  private final SurveyDirectoryRepository surveyDirectoryRepository;
  private final SurveyAnswerRepository surveyAnswerRepository;
  private final AnYesNoRepository anYesNoRepository;
  private final AnRadioRepository anRadioRepository;
  private final AnCheckBoxRepository anCheckBoxRepository;
  private final AnFillBlankRepository anFillBlankRepository;
  private final AndFillBlankRepository andFillBlankRepository;
  private final AnAnswerRepository anAnswerRepository;
  private final AnEnumQuRepository anEnumQuRepository;
  private final AnScoreRepository anScoreRepository;
  private final AnOrderRepository anOrderRepository;
  private final AnUploadFileRepository anUploadFileRepository;

  @Override
  public void setBaseDao() {
    this.baseDao = surveyAnswerDao;
  }

  @Override
  public void saveAnswer(SurveyAnswer surveyAnswer, Map<String, Map<String, Object>> quMaps) {
    String surveyId = surveyAnswer.getSurveyId();
    SurveyDirectory survey = surveyDirectoryRepository.findById(surveyId).orElse(null);
    if (Objects.isNull(survey)) {
      return;
    }

    //可以回答的最少项目数
    int surveyQuAnItemNum = survey.getAnItemLeastNum();
    surveyAnswer.setEndAnDate(new Date());

    //计算答卷用时
    Date endAnDate = surveyAnswer.getEndAnDate();
    Date bgAnDate = surveyAnswer.getBgAnDate();
    surveyAnswer.setTotalTime(0f);
    if (endAnDate != null && bgAnDate != null) {
      long time = endAnDate.getTime() - bgAnDate.getTime();
      surveyAnswer.setTotalTime(Float.parseFloat(time / 1000 + ""));
    }
    surveyAnswerRepository.save(surveyAnswer);

    int anCount = 0;
    Map<String, Object> yesNoMaps = quMaps.get(SurveyConst.KEY_YES_NO);
    anCount += saveAnYesNo(surveyAnswer, yesNoMaps);
    //单选题
    Map<String, Object> radioMaps = quMaps.get("radioMaps");
    anCount += saveCompAnRadio(surveyAnswer, radioMaps);
    //多选题
    Map<String, Object> checkboxMaps = quMaps.get("checkboxMaps");
    anCount += saveCompAnCheckbox(surveyAnswer, checkboxMaps);
    //填空题
    Map<String, Object> fillBlank = quMaps.get(SurveyConst.KEY_BLANK);
    anCount += saveAnFill(surveyAnswer, fillBlank);
    //多项填空题
    Map<String, Object> multiFillBlank = quMaps.get(SurveyConst.KEY_MULTI_BLANK);
    anCount += saveAnMultiFill(surveyAnswer, multiFillBlank);
    //问答题
    Map<String, Object> answerMaps = quMaps.get(SurveyConst.KEY_ANSWER);
    anCount += saveAnAnswer(surveyAnswer, answerMaps);
    //复合单选题
    Map<String, Object> compRadioMaps = quMaps.get(SurveyConst.KEY_COMP_RADIO);
    anCount += saveCompAnRadio(surveyAnswer, compRadioMaps);
    //复合多选题
    Map<String, Object> compCheckboxMaps = quMaps.get(SurveyConst.KEY_COMP_CHECK_BOX);
    anCount += saveCompAnCheckbox(surveyAnswer, compCheckboxMaps);
    //枚举题
    Map<String, Object> enumMaps = quMaps.get(SurveyConst.KEY_ENUM);
    anCount += saveEnum(surveyAnswer, enumMaps);
    //评分题
    Map<String, Object> scoreMaps = quMaps.get(SurveyConst.KEY_SCORE);
    anCount += saveScore(surveyAnswer, scoreMaps);

    //排序题 quOrderMaps
    Map<String, Object> quOrderMaps = quMaps.get(SurveyConst.KEY_ORDER);
    anCount += saveQuOrder(surveyAnswer, quOrderMaps);

    Map<String, Object> uploadFileMaps = quMaps.get(SurveyConst.KEY_UPLOAD_FILE);
    anCount += saveUploadFile(surveyAnswer, uploadFileMaps);

    //保存anCount, 1:表示回完
    surveyAnswer.setCompleteItemNum(anCount);
    int isComplete = 0;
    if (anCount >= surveyQuAnItemNum) {
      isComplete = 1;
    }
    surveyAnswer.setIsComplete(isComplete);
    int isEffective = 0;
    if (anCount > 0) {
      isEffective = 1;
    }
    //1:暂时只要答过一题就表示回答有效
    surveyAnswer.setIsEffective(isEffective);
    surveyAnswerRepository.save(surveyAnswer);
  }

  @Override
  public List<Question> findAnswerDetail(SurveyAnswer answer) {
    String surveyId = answer.getSurveyId();
    String surveyAnswerId = answer.getId();
    List<Question> questions = questionManager.findDetails(surveyId, 2);
    for (Question question : questions) {
      getQuestionAnswer(surveyAnswerId, question);
    }
    return questions;
  }

  /**
   * 取问卷值方式
   */
  @Override
  public int getQuestionAnswer(String surveyAnswerId, Question question) {
    int score = 0;
    String quId = question.getId();
    // 查询每一题的答案,如果是主观题，则判断是否答对
    QuType quType = question.getQuType();

    //重置导出
    question.setAnAnswer(new AnAnswer());
    question.setAnCheckboxs(new ArrayList<>());
    question.setAnDFillblanks(new ArrayList<>());
    question.setAnEnumqus(new ArrayList<>());
    question.setAnFillblank(new AnFillblank());
    question.setAnRadio(new AnRadio());
    question.setAnYesno(new AnYesno());
    question.setAnScores(new ArrayList<>());
    question.setAnOrders(new ArrayList<>());

    if (quType == QuType.YESNO) {
      AnYesno anYesno = anYesnoManager.findAnswer(surveyAnswerId, quId);
      if (anYesno != null) {
        question.setAnYesno(anYesno);
      }
    } else if (quType == QuType.RADIO || quType == QuType.COMPRADIO) {
      AnRadio anRadio = anRadioManager.findAnswer(surveyAnswerId, quId);
      if (anRadio != null) {
        question.setAnRadio(anRadio);
      }
    } else if (quType == QuType.CHECKBOX || quType == QuType.COMPCHECKBOX) {
      List<AnCheckbox> checkboxes = anCheckboxManager.findAnswer(surveyAnswerId, quId);
      if (checkboxes != null) {
        question.setAnCheckboxs(checkboxes);
      }
    } else if (quType == QuType.FILLBLANK) {
      AnFillblank anFillblank = anFillblankManager.findAnswer(surveyAnswerId, quId);
      if (anFillblank != null) {
        question.setAnFillblank(anFillblank);
      }
    } else if (quType == QuType.MULTIFILLBLANK) {
      List<AnDFillblank> anMultiFillBlanks = anDFillblankManager.findAnswer(surveyAnswerId, quId);
      if (anMultiFillBlanks != null) {
        question.setAnDFillblanks(anMultiFillBlanks);
      }
    } else if (quType == QuType.ANSWER) {
      AnAnswer anAnswer = anAnswerManager.findAnswer(surveyAnswerId, quId);
      if (anAnswer != null) {
        question.setAnAnswer(anAnswer);
      }
    } else if (quType == QuType.BIGQU) {
      // List<Question> childQuestions=question.getQuestions();
      // for (Question childQuestion : childQuestions) {
      // score=getquestionAnswer(surveyAnswerId, childQuestion);
      // }
    } else if (quType == QuType.ENUMQU) {
      List<AnEnumqu> enumQus = anEnumquManager.findAnswer(surveyAnswerId, quId);
      if (enumQus != null) {
        question.setAnEnumqus(enumQus);
      }
    } else if (quType == QuType.SCORE) {
      List<AnScore> anScores = anScoreManager.findAnswer(surveyAnswerId, quId);
      if (anScores != null) {
        question.setAnScores(anScores);
      }
    } else if (quType == QuType.ORDERQU) {
      List<AnOrder> anOrders = anOrderManager.findAnswer(surveyAnswerId, quId);
      if (anOrders != null) {
        question.setAnOrders(anOrders);
      }
    } else if (quType == QuType.UPLOADFILE) {
      List<AnUplodFile> uploadFiles = anUploadFileManager.findAnswer(surveyAnswerId, quId);
      question.setAnUplodFiles(uploadFiles);
    }
    return score;
  }

  @Override
  public SurveyAnswer getTimeInByIp(SurveyDetail surveyDetail, String ip) {
    String surveyId = surveyDetail.getDirId();
    Criterion eqSurveyId = Restrictions.eq("surveyId", surveyId);
    Criterion eqIp = Restrictions.eq("ipAddr", ip);

    int minute = surveyDetail.getEffectiveTime();
    Date curdate = new Date();
    Calendar calendarDate = Calendar.getInstance();
    calendarDate.setTime(curdate);
    calendarDate.set(Calendar.MINUTE, calendarDate.get(Calendar.MINUTE)
        - minute);
    Date date = calendarDate.getTime();

    Criterion gtEndDate = Restrictions.gt("endAnDate", date);
    return surveyAnswerDao.findFirst("endAnDate", true, eqSurveyId, eqIp,
        gtEndDate);

  }

  @Override
  public Long getCountByIp(String surveyId, String ip) {
    String hql = "select count(*) from SurveyAnswer x where x.surveyId=?1 and x.ipAddr=?2";
    Long count = (Long) surveyAnswerDao.findUniObjs(hql, surveyId, ip);
    return count;
  }

  @Override
  public List<SurveyAnswer> answersByIp(String surveyId, String ip) {
    Criterion criterionSurveyId = Restrictions.eq("surveyId", surveyId);
    Criterion criterionIp = Restrictions.eq("ipAddr", ip);
    List<SurveyAnswer> answers = surveyAnswerDao.find(criterionSurveyId,
        criterionIp);
    return answers;
  }


  @Override
  public String exportXLS(String surveyId, String savePath, boolean isExpUpQu) {
    String basepath = surveyId + "";
    String urlPath = "/webin/expfile/" + basepath + "/";// 下载所用的地址
    String path = urlPath.replace("/", File.separator);// 文件系统路径
    // File.separator +
    // "file" +
    // File.separator+basepath
    // + File.separator;
    savePath = savePath + path;
    File file = new File(savePath);
    if (!file.exists()) {
      file.mkdirs();
    }

    //SurveyDirectory surveyDirectory = surveyDirectoryRepository.findById(surveyId);
    String fileName = "DWSurvey-" + surveyId + ".xlsx";

    XLSXExportUtil exportUtil = new XLSXExportUtil(fileName, savePath);
//		Criterion cri1 = Restrictions.eq("surveyId",surveyId);
//		PageDto<SurveyAnswer> page = new PageDto<SurveyAnswer>();
//		page.setPageSize(5000);
    try {
//			page = findPage(page,cri1);
//			int totalPage = page.getTotalPage();
      List<SurveyAnswer> answers = answerList(surveyId, 1);
      List<Question> questions = questionManager.findDetails(surveyId, 2);
      exportXLSTitle(exportUtil, questions);
      int answerListSize = answers.size();
      for (int j = 0; j < answerListSize; j++) {
        SurveyAnswer surveyAnswer = answers.get(j);
        String surveyAnswerId = surveyAnswer.getId();
        exportUtil.createRow(j + 1);
        exportXLSRow(exportUtil, surveyAnswerId, questions, surveyAnswer, (j + 1), savePath, isExpUpQu);
      }
      exportUtil.exportXLS();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return urlPath + fileName;
  }

  private void exportXLSRow(XLSXExportUtil exportUtil, String surveyAnswerId, List<Question> questions, SurveyAnswer surveyAnswer, int orderNum, String savePath, boolean isExpUpQu) {
    new RunAnswerUtil().getQuestionMap(questions, surveyAnswerId);
    int cellIndex = 0;
    int quNum = 0;
    for (Question question : questions) {
      QuType quType = question.getQuType();
      if (quType == QuType.PAGETAG || quType == QuType.PARAGRAPH) {
        continue;
      }
      quNum++;
      String quName = question.getQuName();
      String titleName = quType.getCnName();
      if (quType == QuType.YESNO) {// 是非题
        String yesnoAnswer = question.getAnYesno().getYesnoAnswer();
        if ("1".equals(yesnoAnswer)) {
          yesnoAnswer = question.getYesnoOption().getTrueValue();
        } else if ("0".equals(yesnoAnswer)) {
          yesnoAnswer = question.getYesnoOption().getFalseValue();
        } else {
          yesnoAnswer = "";
        }
        exportUtil.setCell(cellIndex++, yesnoAnswer);
      } else if (quType == QuType.RADIO) {// 单选题
        String quItemId = question.getAnRadio().getQuItemId();
        List<QuRadio> quRadios = question.getQuRadios();
        String answerOptionName = "";
        String answerOtherText = "";
        boolean isNote = false;
        for (QuRadio quRadio : quRadios) {
          if (quRadio.getIsNote() == 1) {
            isNote = true;
            break;
          }
        }

        for (QuRadio quRadio : quRadios) {
          String quRadioId = quRadio.getId();
          if (quRadioId.equals(quItemId)) {
            answerOptionName = quRadio.getOptionName();
            if (quRadio.getIsNote() == 1) {
              answerOtherText = question.getAnRadio().getOtherText();
            }
            break;
          }
        }
        answerOptionName = HtmlUtil.removeTagFromText(answerOptionName);
        answerOptionName = answerOptionName.replace("&nbsp;", " ");
        exportUtil.setCell(cellIndex++, answerOptionName);
        if (isNote) {
          exportUtil.setCell(cellIndex++, answerOtherText);
        }
      } else if (quType == QuType.CHECKBOX) {// 多选题
        List<AnCheckbox> anCheckboxs = question.getAnCheckboxs();
        List<QuCheckbox> checkboxs = question.getQuCheckboxs();
        for (QuCheckbox quCheckbox : checkboxs) {
          String quCkId = quCheckbox.getId();
          String answerOptionName = "0";
          String answerOtherText = "";
          for (AnCheckbox anCheckbox : anCheckboxs) {
            String anQuItemId = anCheckbox.getQuItemId();
            if (quCkId.equals(anQuItemId)) {
              answerOptionName = quCheckbox.getOptionName();
              answerOptionName = "1";
              if (quCheckbox.getIsNote() == 1) {
                answerOtherText = anCheckbox.getOtherText();
              }
              break;
            }
          }
          answerOptionName = HtmlUtil.removeTagFromText(answerOptionName);
          answerOptionName = answerOptionName.replace("&nbsp;", " ");
          exportUtil.setCell(cellIndex++, answerOptionName);
          if (quCheckbox.getIsNote() == 1) {
            exportUtil.setCell(cellIndex++, answerOtherText);
          }
        }
      } else if (quType == QuType.FILLBLANK) {// 填空题
        AnFillblank anFillblank = question.getAnFillblank();
        exportUtil.setCell(cellIndex++, anFillblank.getAnswer());
      } else if (quType == QuType.ANSWER) {// 多行填空题
        AnAnswer anAnswer = question.getAnAnswer();
        exportUtil.setCell(cellIndex++, anAnswer.getAnswer());
      } else if (quType == QuType.COMPRADIO) {// 复合单选题
        AnRadio anRadio = question.getAnRadio();
        String quItemId = anRadio.getQuItemId();
        List<QuRadio> quRadios = question.getQuRadios();
        String answerOptionName = "";
        String answerOtherText = "";
        for (QuRadio quRadio : quRadios) {
          String quRadioId = quRadio.getId();
          if (quRadioId.equals(quItemId)) {
            answerOptionName = quRadio.getOptionName();
            answerOtherText = anRadio.getOtherText();
            break;
          }
        }
        answerOptionName = HtmlUtil.removeTagFromText(answerOptionName);
        answerOtherText = HtmlUtil.removeTagFromText(answerOtherText);
        answerOptionName = answerOptionName.replace("&nbsp;", " ");
        exportUtil.setCell(cellIndex++, answerOptionName);
        exportUtil.setCell(cellIndex++, answerOtherText);
      } else if (quType == QuType.COMPCHECKBOX) {// 复合多选题
        List<AnCheckbox> anCheckboxs = question.getAnCheckboxs();
        List<QuCheckbox> checkboxs = question.getQuCheckboxs();
        for (QuCheckbox quCheckbox : checkboxs) {
          String quCkId = quCheckbox.getId();
          String answerOptionName = "0";
          String answerOtherText = "0";
          for (AnCheckbox anCheckbox : anCheckboxs) {
            String anQuItemId = anCheckbox.getQuItemId();
            if (quCkId.equals(anQuItemId)) {
              answerOptionName = quCheckbox.getOptionName();
              answerOptionName = "1";
              answerOtherText = anCheckbox.getOtherText();
              break;
            }
          }
          answerOptionName = HtmlUtil.removeTagFromText(answerOptionName);
          answerOptionName = answerOptionName.replace("&nbsp;", " ");
          exportUtil.setCell(cellIndex++, answerOptionName);
          if (1 == quCheckbox.getIsNote()) {
            answerOtherText = HtmlUtil.removeTagFromText(answerOtherText);
            exportUtil.setCell(cellIndex++, answerOtherText);
          }
        }
      } else if (quType == QuType.ENUMQU) {// 枚举题
        List<AnEnumqu> anEnumqus = question.getAnEnumqus();
        int enumNum = question.getParamInt01();
        for (int i = 0; i < enumNum; i++) {
          String answerEnum = "";
          for (AnEnumqu anEnumqu : anEnumqus) {
            if (i == anEnumqu.getEnumItem()) {
              answerEnum = anEnumqu.getAnswer();
              break;
            }
          }
          exportUtil.setCell(cellIndex++, answerEnum);
        }
      } else if (quType == QuType.ORDERQU) {// 评分题
        List<QuOrderby> quOrderbys = question.getQuOrderbys();
        List<AnOrder> anOrders = question.getAnOrders();
        for (QuOrderby quOrderby : quOrderbys) {
          String quOrderbyId = quOrderby.getId();
          String answerOptionName = "";
          for (AnOrder anOrder : anOrders) {
            if (quOrderbyId.equals(anOrder.getQuRowId())) {
              answerOptionName = anOrder.getOrderyNum();
              break;
            }
          }
          answerOptionName = answerOptionName.replace("&nbsp;", " ");
          exportUtil.setCell(cellIndex++, answerOptionName);
        }
      } else if (quType == QuType.MULTIFILLBLANK) {// 组合填空题
        List<QuMultiFillblank> quMultiFillblanks = question.getQuMultiFillblanks();
        List<AnDFillblank> anDFillblanks = question.getAnDFillblanks();
        for (QuMultiFillblank quMultiFillblank : quMultiFillblanks) {
          String quMultiFillbankId = quMultiFillblank.getId();
          String answerOptionName = "";
          for (AnDFillblank anDFillblank : anDFillblanks) {
            if (quMultiFillbankId.equals(anDFillblank.getQuItemId())) {
              answerOptionName = anDFillblank.getAnswer();
              break;
            }
          }
          answerOptionName = answerOptionName.replace("&nbsp;", " ");
          exportUtil.setCell(cellIndex++, answerOptionName);
        }
      } else if (quType == QuType.SCORE) {// 评分题
        List<QuScore> quScores = question.getQuScores();
        List<AnScore> anScores = question.getAnScores();
        for (QuScore quScore : quScores) {
          String quScoreId = quScore.getId();
          String answerScore = "";
          for (AnScore anScore : anScores) {
            if (quScoreId.equals(anScore.getQuRowId())) {
              answerScore = anScore.getAnswserScore();
              break;
            }
          }
          exportUtil.setCell(cellIndex++, answerScore);
        }
      } else if (quType == QuType.UPLOADFILE) {
        //为导出文件
        String upFilePath = File.separator + "webin" + File.separator + "upload" + File.separator;
        List<AnUplodFile> anUplodFiles = question.getAnUplodFiles();
        String answerBuf = "";

        String toFilePath = "";
        if (isExpUpQu) {
//					String toFilePath = savePath+File.separator+orderNum+File.separator+HtmlUtil.removeTagFromText(titleName);
//					String toFilePath = savePath + File.separator + orderNum + File.separator + quNum + "_" + HtmlUtil.removeTagFromText(titleName);
          toFilePath = savePath + File.separator + orderNum + File.separator + "Q_" + quNum;
          File file = new File(toFilePath);
          if (!file.exists()) file.mkdirs();
        }
        for (AnUplodFile anUplodFile : anUplodFiles) {
          answerBuf += anUplodFile.getFileName() + "      ";
          if (isExpUpQu) {
            File fromFile = new File(DWSurveyConfig.DWSURVEY_WEB_FILE_PATH + anUplodFile.getFilePath());
            if (fromFile.exists()) {
              File toFile = new File(toFilePath + File.separator + anUplodFile.getFileName());
              try {
                FileUtil.copyFile(fromFile, toFile);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        }
//				answerBuf=answerBuf.substring(0,answerBuf.lastIndexOf("      "));
        exportUtil.setCell(cellIndex++, answerBuf);
      }
    }

    exportUtil.setCell(cellIndex++, surveyAnswer.getIpAddr());
    exportUtil.setCell(cellIndex++, surveyAnswer.getCity());
    exportUtil.setCell(cellIndex++, new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒").format(surveyAnswer.getEndAnDate()));
  }

  private void exportXLSTitle(XLSXExportUtil exportUtil, List<Question> questions) {
    exportUtil.createRow(0);
    int cellIndex = 0;
    int quNum = 0;
    for (Question question : questions) {
      QuType quType = question.getQuType();
      if (quType == QuType.PAGETAG || quType == QuType.PARAGRAPH) {
        continue;
      }
      quNum++;

//			String quName = question.getQuName();
      String quName = question.getQuTitle();
      quName = HtmlUtil.removeTagFromText(quName);
      String titleName = quNum + "、" + quName + "[" + quType.getCnName() + "]";
      if (quType == QuType.YESNO) {// 是非题
        exportUtil.setCell(cellIndex++, titleName);
      } else if (quType == QuType.RADIO) {// 单选题
        List<QuRadio> quRadios = question.getQuRadios();
        boolean isNote = false;
        for (QuRadio quRadio : quRadios) {
          if (quRadio.getIsNote() == 1) {
            isNote = true;
            break;
          }
        }
        exportUtil.setCell(cellIndex++, titleName);
        if (isNote) {
          exportUtil.setCell(cellIndex++, titleName + "选项说明");
        }
        // 多选题
      } else if (quType == QuType.CHECKBOX) {
        List<QuCheckbox> checkboxs = question.getQuCheckboxs();
        for (QuCheckbox quCheckbox : checkboxs) {
          String optionName = quCheckbox.getOptionName();
          optionName = HtmlUtil.removeTagFromText(optionName);
          exportUtil.setCell(cellIndex++, titleName + "－" + optionName);
          if (quCheckbox.getIsNote() == 1) {
            exportUtil.setCell(cellIndex++, titleName + "－" + optionName + "－选项说明");
          }
        }
      } else if (quType == QuType.FILLBLANK) {// 填空题
        exportUtil.setCell(cellIndex++, titleName);
      } else if (quType == QuType.ANSWER) {// 多行填空题
        exportUtil.setCell(cellIndex++, titleName);
      } else if (quType == QuType.COMPRADIO) {// 复合单选题
        exportUtil.setCell(cellIndex++, titleName);
        exportUtil.setCell(cellIndex++, titleName + "-说明");

      } else if (quType == QuType.COMPCHECKBOX) {// 复合多选题
        List<QuCheckbox> checkboxs = question.getQuCheckboxs();
        for (QuCheckbox quCheckbox : checkboxs) {
          String optionName = quCheckbox.getOptionName();
          exportUtil.setCell(cellIndex++, titleName + "－"
              + optionName);
          int isNote = quCheckbox.getIsNote();
          if (isNote == 1) {
            optionName = HtmlUtil.removeTagFromText(optionName);
            exportUtil.setCell(cellIndex++, titleName + "－" + optionName
                + "－" + "说明");
          }
        }
      } else if (quType == QuType.ENUMQU) {// 枚举题
        int enumNum = question.getParamInt01();
        for (int i = 0; i < enumNum; i++) {
          exportUtil.setCell(cellIndex++, titleName + i + "－枚举");
        }
      } else if (quType == QuType.MULTIFILLBLANK) {// 组合填空题
        List<QuMultiFillblank> quMultiFillblanks = question
            .getQuMultiFillblanks();
        for (QuMultiFillblank quMultiFillblank : quMultiFillblanks) {
          String optionName = quMultiFillblank.getOptionName();

          optionName = HtmlUtil.removeTagFromText(optionName);
          exportUtil.setCell(cellIndex++, titleName + "－"
              + optionName);
        }
      } else if (quType == QuType.ORDERQU) {// 评分题
        List<QuOrderby> quOrderbys = question.getQuOrderbys();
        for (QuOrderby quOrderby : quOrderbys) {
          String optionName = quOrderby.getOptionName();
          optionName = HtmlUtil.removeTagFromText(optionName);
          exportUtil.setCell(cellIndex++, titleName + "_" + optionName);
        }
      } else if (quType == QuType.SCORE) {// 评分题
        List<QuScore> quScores = question.getQuScores();
        for (QuScore quScore : quScores) {
          String optionName = quScore.getOptionName();
          optionName = HtmlUtil.removeTagFromText(optionName);
          exportUtil.setCell(cellIndex++, titleName + "－" + optionName);
        }
      } else if (quType == QuType.UPLOADFILE) {
        exportUtil.setCell(cellIndex++, titleName);
      }
    }

    exportUtil.setCell(cellIndex++, "回答者IP");
    exportUtil.setCell(cellIndex++, "IP所在地");
    exportUtil.setCell(cellIndex++, "回答时间");

  }

  public void writeToXLS() {

  }

  @Override
  public SurveyStats surveyStatsData(SurveyStats surveyStats) {
    return surveyAnswerDao.surveyStatsData(surveyStats);
  }


  /**
   * 取一份卷子回答的数据
   */
  @Override
  public Page<SurveyAnswer> answerPage(PageRequest page, String surveyId) {
    Specification<SurveyAnswer> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("surveyId"), surveyId));
      predicates.add(cb.lt(root.get(SurveyConst.FIELD_HANDLE_STATE), 2));
      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Sort sort = Sort.by(Sort.Direction.DESC, "endAnDate");
    page.withSort(sort);
    return surveyAnswerRepository.findAll(spec, page);
  }

  public List<SurveyAnswer> answerList(String surveyId, Integer isEff) {
    Criterion cri1 = Restrictions.eq("surveyId", surveyId);
    Criterion cri2 = Restrictions.lt(SurveyConst.FIELD_HANDLE_STATE, 2);
    Criterion cri3 = Restrictions.eq("isEffective", 1);
    if (isEff != null) {
      cri3 = Restrictions.eq("isEffective", isEff);
    }
    return surveyAnswerDao.findByOrder("endAnDate", false, cri1, cri2);
  }

  @Override
  public SurveyDirectory upAnQuNum(String surveyId) {
    Optional<SurveyDirectory> optional = surveyDirectoryRepository.findById(surveyId);
    if (optional.isPresent()) {
      SurveyDirectory directory = optional.get();
      upAnQuNum(directory);
      return directory;
    }
    return null;
  }

  @Override
  public SurveyDirectory upAnQuNum(SurveyDirectory survey) {
    Specification<SurveyAnswer> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.lt(root.get(SurveyConst.FIELD_HANDLE_STATE), 2));
      predicates.add(cb.equal(root.get("isEffective"), 1));

      if (StringUtils.isNotBlank(survey.getId())) {
        predicates.add(cb.equal(root.get("surveyId"), survey.getId()));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    };

    long answerCount = surveyAnswerRepository.count(spec);
    survey.setAnswerNum((int) answerCount);
    surveyDirectoryRepository.save(survey);
    return survey;
  }

  @Override
  public List<SurveyDirectory> upAnQuNum(List<SurveyDirectory> result) {
    if (result != null) {
      for (SurveyDirectory survey : result) {
        upAnQuNum(survey);
      }
    }
    return result;
  }

  /**
   * 查询问卷调查答案
   *
   * @param answerId answerId
   * @return SurveyAnswer
   */
  @Override
  public SurveyAnswer findOne(String answerId) {
    return surveyAnswerRepository.findById(answerId).orElse(null);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteData(String[] ids) {
    String surveyId = null;
    for (String id : ids) {
      SurveyAnswer surveyAnswer = get(id);
      surveyId = surveyAnswer.getSurveyId();
      delete(surveyAnswer);
    }
    if (surveyId != null) {
      upAnQuNum(surveyId);
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(SurveyAnswer t) {
    if (t != null) {
      String belongAnswerId = t.getId();
      t.setHandleState(2);
      surveyAnswerDao.save(t);
      //更新当前答卷的回答记录值
      // 得到题列表
      List<Question> questions = questionManager.findDetails(t.getSurveyId(), 2);
      for (Question question : questions) {
        String quId = question.getId();
        QuType quType = question.getQuType();

        if (quType == QuType.YESNO) {// 是非题

        } else if (quType == QuType.RADIO) {// 单选题
          AnRadio anRadio = anRadioManager.findAnswer(belongAnswerId, quId);
          if (anRadio != null) {
            anRadio.setVisibility(0);
            //是否显示  1显示 0不显示
            anRadioManager.save(anRadio);
          }
        } else if (quType == QuType.CHECKBOX) {// 多选题
          List<AnCheckbox> anCheckboxs = anCheckboxManager.findAnswer(belongAnswerId, quId);
          if (anCheckboxs != null) {
            for (AnCheckbox anCheckbox : anCheckboxs) {
              anCheckbox.setVisibility(0);
              //是否显示  1显示 0不显示
              anCheckboxManager.save(anCheckbox);
            }
          }
        } else if (quType == QuType.FILLBLANK) {// 填空题
          AnFillblank anFillblank = anFillblankManager.findAnswer(belongAnswerId, quId);
          if (anFillblank != null) {
            anFillblank.setVisibility(0);
            //是否显示  1显示 0不显示
            anFillblankManager.save(anFillblank);
          }
        } else if (quType == QuType.ANSWER) {// 多行填空题

          AnAnswer anAnswer = anAnswerManager.findAnswer(belongAnswerId, quId);
          if (anAnswer != null) {
            anAnswer.setVisibility(0);
            //是否显示  1显示 0不显示
            anAnswerManager.save(anAnswer);
          }

        } else if (quType == QuType.COMPRADIO) {// 复合单选题


        } else if (quType == QuType.COMPCHECKBOX) {// 复合多选题

        } else if (quType == QuType.ENUMQU) {// 枚举题

        } else if (quType == QuType.MULTIFILLBLANK) {// 组合填空题
          List<AnDFillblank> anDFillblanks = anDFillblankManager.findAnswer(belongAnswerId, quId);
          if (anDFillblanks != null) {
            for (AnDFillblank anDFillblank : anDFillblanks) {
              anDFillblank.setVisibility(0);
              //是否显示  1显示 0不显示
              anDFillblankManager.save(anDFillblank);
            }
          }
        } else if (quType == QuType.SCORE) {// 评分题

          List<AnScore> anScores = anScoreManager.findAnswer(belongAnswerId, quId);
          if (anScores != null) {
            for (AnScore anScore : anScores) {
              anScore.setVisibility(0);
              //是否显示  1显示 0不显示
              anScoreManager.save(anScore);
            }

          }

        }

      }
    }
    super.delete(t);
  }

  /**
   * 保存是非题答案
   *
   * @param answer surveyAnswer
   * @param map    map
   * @return count
   */
  public int saveAnYesNo(SurveyAnswer answer, Map<String, Object> map) {
    if (Objects.isNull(map) || map.isEmpty()) {
      return 0;
    }
    String surveyId = answer.getSurveyId();
    String surveyAnswerId = answer.getId();

    List<AnYesno> list = new ArrayList<>();
    map.forEach((k, v) -> {
      AnYesno anYesno = new AnYesno(surveyId, surveyAnswerId, k, v.toString());
      list.add(anYesno);
    });

    anYesNoRepository.saveAll(list);
    return list.size();
  }

  /**
   * 复合单选题
   *
   * @param answer SurveyAnswer
   * @param map    map
   * @return count
   */
  private int saveCompAnRadio(SurveyAnswer answer, Map<String, Object> map) {
    if (Objects.isNull(map) || map.isEmpty()) {
      return 0;
    }

    String surveyId = answer.getSurveyId();
    String surveyAnswerId = answer.getId();
    List<AnRadio> list = new ArrayList<>();
    map.forEach((k, v) -> {
      AnRadio tempAnRadio = (AnRadio) v;
      String quItemId = tempAnRadio.getQuItemId();
      String text = tempAnRadio.getOtherText();
      AnRadio anRadio = new AnRadio(surveyId, surveyAnswerId, k, quItemId);
      anRadio.setOtherText(text);
      list.add(anRadio);
    });

    anRadioRepository.saveAll(list);
    return list.size();
  }

  /**
   * 保存复合多选题答案
   *
   * @param answer SurveyAnswer
   * @param map    map
   * @return count
   */
  @SuppressWarnings("unchecked")
  private int saveCompAnCheckbox(SurveyAnswer answer, Map<String, Object> map) {
    if (Objects.isNull(map) || map.isEmpty()) {
      return 0;
    }
    String surveyId = answer.getSurveyId();
    String surveyAnswerId = answer.getId();
    List<AnCheckbox> list = new ArrayList<>();
    map.forEach((k, v) -> {
      Map<String, Object> m = (Map<String, Object>) v;
      for (Map.Entry<String, Object> entry : m.entrySet()) {
        AnCheckbox tempAnCheckbox = (AnCheckbox) entry.getValue();
        String quItemId = tempAnCheckbox.getQuItemId();
        String otherText = tempAnCheckbox.getOtherText();
        if (StringUtils.isNotBlank(quItemId)) {
          AnCheckbox anCheckbox = new AnCheckbox(surveyId, surveyAnswerId, k, quItemId);
          anCheckbox.setOtherText(otherText);
          list.add(anCheckbox);
        }
      }
    });

    anCheckBoxRepository.saveAll(list);
    return list.size();
  }

  /**
   * 保存单项填空题答案
   *
   * @param answer SurveyAnswer
   * @param map    map
   * @return count
   */
  private int saveAnFill(SurveyAnswer answer, Map<String, Object> map) {
    if (Objects.isNull(map) || map.isEmpty()) {
      return 0;
    }

    String surveyId = answer.getSurveyId();
    String surveyAnswerId = answer.getId();
    List<AnFillblank> list = new ArrayList<>();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String quId = entry.getKey();
      String answerValue = entry.getValue().toString();
      if (answerValue != null && !"".equals(answerValue)) {
        list.add(new AnFillblank(surveyId, surveyAnswerId, quId, answerValue));
      }
    }
    anFillBlankRepository.saveAll(list);
    return list.size();
  }

  /**
   * 保存多项填空题答案
   *
   * @param answer SurveyAnswer
   * @param map    map
   * @return count
   */
  @SuppressWarnings("unchecked")
  private int saveAnMultiFill(SurveyAnswer answer, Map<String, Object> map) {
    if (Objects.isNull(map) || map.isEmpty()) {
      return 0;
    }
    String surveyId = answer.getSurveyId();
    String surveyAnswerId = answer.getId();
    List<AnDFillblank> list = new ArrayList<>();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String quId = entry.getKey();

      Map<String, Object> m = (Map<String, Object>) entry.getValue();
      if (m != null && m.size() > 0) {
        for (Map.Entry<String, Object> mEntity : m.entrySet()) {
          String quItemId = mEntity.getKey();
          String answerValue = mEntity.getValue().toString();
          if (answerValue != null && !"".equals(answerValue)) {
            list.add(new AnDFillblank(surveyId, surveyAnswerId, quId, quItemId, answerValue));
          }
        }
      }
    }
    andFillBlankRepository.saveAll(list);
    return list.size();
  }

  /**
   * 保存判断题答案
   *
   * @param answer SurveyAnswer
   * @param map    map
   * @return count
   */
  private int saveAnAnswer(SurveyAnswer answer, Map<String, Object> map) {
    if (Objects.isNull(map) || map.isEmpty()) {
      return 0;
    }
    String surveyId = answer.getSurveyId();
    String surveyAnswerId = answer.getId();
    List<AnAnswer> list = new ArrayList<>();

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String quId = entry.getKey();
      String answerValue = entry.getValue().toString();
      if (answerValue != null && !"".equals(answerValue)) {
        list.add(new AnAnswer(surveyId, surveyAnswerId, quId, answerValue));
      }
    }

    anAnswerRepository.saveAll(list);
    return list.size();
  }

  /**
   * 保存枚举题
   *
   * @param answer SurveyAnswer
   * @param map    map
   * @return count
   */
  private int saveEnum(SurveyAnswer answer, Map<String, Object> map) {
    if (Objects.isNull(map) || map.isEmpty()) {
      return 0;
    }
    String surveyId = answer.getSurveyId();
    String surveyAnswerId = answer.getId();
    List<AnEnumqu> list = new ArrayList<>();

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String[] splitKey = entry.getKey().split(StringConst.UNDERSCORE);
      String quId = splitKey[0];
      Integer quItemNum = Integer.parseInt(splitKey[1]);
      String answerValue = entry.getValue().toString();
      list.add(new AnEnumqu(surveyId, surveyAnswerId, quId, quItemNum, answerValue));
    }

    anEnumQuRepository.saveAll(list);
    return list.size();
  }

  /**
   * 保存评分题
   *
   * @param answer SurveyAnswer
   * @param map    map
   * @return count
   */
  @SuppressWarnings("unchecked")
  private int saveScore(SurveyAnswer answer, Map<String, Object> map) {
    if (Objects.isNull(map) || map.isEmpty()) {
      return 0;
    }
    String surveyId = answer.getSurveyId();
    String surveyAnswerId = answer.getId();
    List<AnScore> list = new ArrayList<>();

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String quId = entry.getKey();
      Map<String, Object> mapRows = (Map<String, Object>) entry.getValue();
      for (Map.Entry<String, Object> row : mapRows.entrySet()) {
        String rowId = row.getKey();
        String scoreValue = row.getValue().toString();
        if (scoreValue != null && !"".equals(scoreValue)) {
          list.add(new AnScore(surveyId, surveyAnswerId, quId, rowId, scoreValue));
        }
      }
    }

    anScoreRepository.saveAll(list);
    return list.size();
  }

  /**
   * 保存评分题
   *
   * @param answer SurveyAnswer
   * @param map    map
   * @return count
   */
  @SuppressWarnings("unchecked")
  private int saveQuOrder(SurveyAnswer answer, Map<String, Object> map) {
    if (Objects.isNull(map) || map.isEmpty()) {
      return 0;
    }
    String surveyId = answer.getSurveyId();
    String surveyAnswerId = answer.getId();
    List<AnOrder> list = new ArrayList<>();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String quId = entry.getKey();
      Map<String, Object> mapRows = (Map<String, Object>) entry.getValue();
      for (Map.Entry<String, Object> row : mapRows.entrySet()) {
        String rowId = row.getKey();
        String orderNumValue = row.getValue().toString();
        if (orderNumValue != null && !"".equals(orderNumValue)) {
          list.add(new AnOrder(surveyId, surveyAnswerId, quId, rowId, orderNumValue));
        }
      }
    }

    anOrderRepository.saveAll(list);
    return list.size();
  }

  /**
   * 上传文件
   *
   * @param answer SurveyAnswer
   * @param map    map
   * @return count
   */
  private int saveUploadFile(SurveyAnswer answer, Map<String, Object> map) {
    if (Objects.isNull(map) || map.isEmpty()) {
      return 0;
    }
    String surveyId = answer.getSurveyId();
    String surveyAnswerId = answer.getId();
    List<AnUplodFile> list = new ArrayList<>();

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String quId = entry.getKey().split(StringConst.UNDERSCORE)[0];
      String answerValue = entry.getValue().toString();
      String[] answerValues = answerValue.split("___");
      String randomCode = RandomUtils.randomWordNum(6);
      list.add(new AnUplodFile(surveyId, surveyAnswerId, quId, answerValues[0], answerValues[1], randomCode));
    }

    anUploadFileRepository.saveAll(list);
    return list.size();
  }
}
