package net.diaowen.dwsurvey.service;

import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.dwsurvey.entity.MailInviteInbox;

import java.util.List;

public interface MailInviteInboxManager {
  public void save(MailInviteInbox entity);

  public PageDto<MailInviteInbox> findPage(PageDto<MailInviteInbox> page2,
                                           String surveyInviteId);

  public List<MailInviteInbox> findList(String mailInviteId);

  public MailInviteInbox getBySendcloudEmailId(String emailId);

  public MailInviteInbox getByEmail(String mailInviteId, String email);

  public void post(String mailInviteId);

}
