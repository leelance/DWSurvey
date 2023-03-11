package net.diaowen.common.utils;

import net.diaowen.dwsurvey.entity.Question;
import net.diaowen.dwsurvey.service.SurveyAnswerManager;
import net.diaowen.dwsurvey.service.impl.SurveyAnswerManagerImpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * run answer
 *
 * @author diaowen
 * @since 2023/3/11 14:44
 */
public class RunAnswerUtil {
  private static final ExecutorService SERVICE = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

  /**
   * 返回新question Map
   *
   * @param questions      questions
   * @param surveyAnswerId surveyAnswerId
   */
  public void getQuestionMap(List<Question> questions, String surveyAnswerId) {
    int quIndex = 0;
    Map<Integer, Question> questionMap = new ConcurrentHashMap<>();
    for (Question question : questions) {
      SERVICE.execute(new AnswerQuestionRun(quIndex++, questionMap, surveyAnswerId, question));
    }
    while (questionMap.size() != questions.size()) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        e.printStackTrace();
      }
    }
  }


  public static class AnswerQuestionRun implements Runnable {
    private final int quIndex;
    private final Map<Integer, Question> questionMap;
    private final String surveyAnswerId;
    private final Question question;

    public AnswerQuestionRun(int quIndex, Map<Integer, Question> questionMap, String surveyAnswerId, Question question) {
      this.quIndex = quIndex;
      this.questionMap = questionMap;
      this.surveyAnswerId = surveyAnswerId;
      this.question = question;
    }

    @Override
    public void run() {
      SurveyAnswerManager surveyManager = SpringContextHolder.getBean(SurveyAnswerManagerImpl.class);
      surveyManager.getQuestionAnswer(surveyAnswerId, question);
      questionMap.put(quIndex, question);
    }
  }
}
