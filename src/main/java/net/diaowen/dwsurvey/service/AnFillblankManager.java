package net.diaowen.dwsurvey.service;

import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.common.service.BaseService;
import net.diaowen.dwsurvey.entity.AnFillblank;
import net.diaowen.dwsurvey.entity.Question;

/**
 * 填空题
 * @author keyuan(keyuan258@gmail.com)
 *
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface AnFillblankManager extends BaseService<AnFillblank, String>{
	public AnFillblank findAnswer(String belongAnswerId, String quId);

	public void findGroupStats(Question question);

  PageDto<AnFillblank> findPage(PageDto<AnFillblank> page, String quId);
}
