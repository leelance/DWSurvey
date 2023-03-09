package net.diaowen.dwsurvey.service;

import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.common.service.BaseService;
import net.diaowen.dwsurvey.entity.QuestionBank;

import java.util.List;

/**
 * 题库
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface QuestionBankManager extends BaseService<QuestionBank, String> {
	/**
	 * 根据 最底层对象，得到此对象所在的目录结构
	 * @param surveyDirectory
	 * @return
	 */
	public List<QuestionBank> findPath(QuestionBank questionBank);

	public QuestionBank getBank(String parentId);

	public QuestionBank findByNameUn(String id, String parentId, String bankName);

	public PageDto<QuestionBank> findPage(PageDto<QuestionBank> page, QuestionBank entity);

	public void executeBank(String id);
	
	public void closeBank(String id);
	
	public List<QuestionBank> newQuestionBank() ;
}
