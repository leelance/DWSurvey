package net.diaowen.common.service;

import net.diaowen.common.base.entity.IdEntity;
import net.diaowen.common.dao.BaseDao;
import net.diaowen.common.plugs.page.PageDto;
import org.hibernate.criterion.Criterion;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 业务基类
 *
 * @author keyuan(keyuan258 @ gmail.com)
 * <p>
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Transactional
public abstract class BaseServiceImpl<T extends IdEntity, ID extends Serializable> implements BaseService<T, ID> {

  protected BaseDao<T, ID> baseDao;

  protected BaseDao<T, ID> getBaseDao() {
    if (baseDao == null) {
      setBaseDao();
    }
    return baseDao;
  }

  @Override
  public void save(T t) {
    String id = t.getId();
    if (id != null && "".equals(id)) {
      t.setId(null);
    }
    getBaseDao().save(t);
  }

  @Override
  public void delete(T t) {
    getBaseDao().delete(t);
  }

  public void delete(ID id) {
    getBaseDao().delete(get(id));
  }

  ;

  public T get(ID id) {
    return getBaseDao().get(id);
  }

  public T getModel(ID id) {
    return getBaseDao().getModel(id);
  }

  ;

  @Override
  public T findById(ID id) {
    return getBaseDao().findUniqueBy("id", id);
  }

  @Override
  public List<T> findList(Criterion... criterions) {
    return getBaseDao().find(criterions);
  }

  @Override
  public PageDto<T> findPage(PageDto<T> page, Criterion... criterions) {
    return getBaseDao().findPage(page, criterions);
  }

  public PageDto<T> findPageByCri(PageDto<T> page, List<Criterion> criterions) {
    return getBaseDao().findPageByCri(page, criterions);
  }

  /**
   * 创建题目id查询  Specification
   *
   * @param quId questionId
   * @return Specification<T>
   */
  protected Specification<T> questionSpec(final String quId) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("quId"), quId));
      predicates.add(cb.equal(root.get("visibility"), 1));
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
