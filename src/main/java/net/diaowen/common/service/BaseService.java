package net.diaowen.common.service;

import net.diaowen.common.base.entity.IdEntity;
import net.diaowen.common.plugs.page.PageDto;
import org.hibernate.criterion.Criterion;

import java.io.Serializable;
import java.util.List;

/**
 * 业务基类接口
 * @author keyuan(keyuan258@gmail.com)
 *
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface BaseService<T extends IdEntity,ID extends Serializable> {

	public void setBaseDao();

	public void save(T t);

	public void delete(T t);

	public void delete(ID id);

	public T get(ID id);

	public T getModel(ID id);

	public T findById(ID id);

	public List<T> findList(Criterion... criterions);

	public PageDto<T> findPage(PageDto<T> page, Criterion... criterion);
}
