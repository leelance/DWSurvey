package net.diaowen.dwsurvey.service;

import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.common.service.BaseService;
import net.diaowen.dwsurvey.entity.AnDFillblank;
import net.diaowen.dwsurvey.entity.Question;

import java.util.List;

/**
 * 多项填空题
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface AnDFillblankManager extends BaseService<AnDFillblank, String> {
	public List<AnDFillblank> findAnswer(String belongAnswerId, String quId);

	public void findGroupStats(Question question);

	PageDto<AnDFillblank> findPage(PageDto<AnDFillblank> anPage, String quItemId);
}
