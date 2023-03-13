package net.diaowen.dwsurvey.entity;

import lombok.Data;
import net.diaowen.common.base.entity.IdEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * 具体的一次调查
 *
 * @author keyuan
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Data
@Entity
@Table(name = "t_survey_detail")
public class SurveyDetail extends IdEntity {
  /**
   * 所对应的surveyDirectory的ID
   */
  private String dirId;
  /**
   * 问卷有效性限制 1.不限制 2.使用Cookie技术 3.使用来源IP检测 4.每台电脑或手机只能答一次
   */
  private Integer effective = 1;
  /**
   * 有效性间隔时间
   */
  private Integer effectiveTime = 5;
  /**
   * 每个IP只能答一次 1是 0否
   */
  private Integer effectiveIp = 0;

  /**
   * 防刷新  1启用 0不启用
   */
  private Integer refresh = 1;
  /**
   * 刷新次数
   */
  private Integer refreshNum = 3;

  /**
   * 调查规则 1.公开 2.私有 3.令牌表示启用访问密码
   */
  private Integer rule = 1;
  /**
   * rule=3, 设置密码
   */
  private String ruleCode = "令牌";

  /**
   * 结束方式 1.手动结束 2.依据结束时间 3.依据收到的份数
   */
  private Integer endType = 1;
  /**
   * 结束时间
   */
  private Date endTime;
  /**
   * 收到的份数
   */
  private Integer endNum = 1000;
  /**
   * 问卷说明
   */
  private String surveyNote;

  /**
   * 是否依据收到的份数结束
   */
  private Integer ynEndNum = 0;
  /**
   * 是否启用收集时间到了结束
   */
  private Integer ynEndTime = 0;

  /**
   * 问卷题目数
   */
  private Integer surveyQuNum = 0;
  /**
   * 可以回答的最少选项数目
   */
  private Integer anItemLeastNum = 0;
  /**
   * 可以回答的最多选项数目
   */
  private Integer anItemMostNum = 0;

  /**
   * 只有邮件邀请唯一链接的受访者可回答 1.启用 0.不启用
   */
  private Integer mailOnly = 0;

  /**
   * 显示分享
   */
  private Integer showShareSurvey = 1;
  /**
   * 是否公开结果 0.否 1.公开
   */
  private Integer showAnswerDa = 0;
}
