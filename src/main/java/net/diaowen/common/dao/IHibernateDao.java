package net.diaowen.common.dao;

import net.diaowen.common.plugs.page.PageDto;
import net.diaowen.common.plugs.page.PageRequest;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;

import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface IHibernateDao<T, ID extends Serializable> extends ISimpleHibernateDao<T, ID> {

  String DEFAULT_ALIAS = "x";

  /**
   * 分页获取全部对象.
   */
  PageDto<T> getAll(final PageRequest pageRequest);

  /**
   * 按HQL分页查询.
   *
   * @param pageRequest 分页参数.
   * @param hql         hql语句.
   * @param values      数量可变的查询参数,按顺序绑定.
   * @return 分页查询结果, 附带结果列表及所有查询输入参数.
   */
  PageDto<T> findPage(final PageRequest pageRequest, String hql,
                      final Object... values);

  /**
   * 按HQL分页查询.
   *
   * @param pageRequest 分页参数.
   * @param hql         hql语句.
   * @return 分页查询结果, 附带结果列表及所有查询输入参数.
   */
  PageDto<T> findPage(final PageRequest pageRequest, String hql);

  /**
   * 按HQL分页查询.
   *
   * @param page   分页参数.
   * @param hql    hql语句.
   * @param values 命名参数,按名称绑定.
   * @return 分页查询结果, 附带结果列表及所有查询输入参数.
   */
  PageDto<T> findPage(final PageRequest pageRequest, String hql,
                      final Map<String, ?> values);

  /**
   * 按Criteria分页查询.
   *
   * @param page       分页参数.
   * @param criterions 数量可变的Criterion.
   * @return 分页查询结果.附带结果列表及所有查询输入参数.
   */
  PageDto<T> findPage(final PageRequest pageRequest,
                      final Criterion... criterions);

  PageDto<T> findPageList(final PageRequest pageRequest,
                          final List<Criterion> criterions);

  PageDto<T> findPageCriteria(PageRequest pageRequest, Criteria c);

  /**
   * action转入id得到模型
   *
   * @param id
   * @return
   */
  T getModel(ID id);

  /**
   * 排序--取出一列数据
   */
  List<T> findByOrder(String orderByProperty, boolean isAsc, Criterion... criterions);

  /**
   * 取出第一条
   */
  T findFirst(Criterion... criterions);

  T findFirst(List<Criterion> criterions);

  T findFirst(String orderByProperty, boolean isAsc, Criterion... criterions);

  T findFirst(String orderByProperty, boolean isAsc, List<Criterion> criterions);

  /**
   * 执行一条hql返回 一个object[]
   */
  Object findUniObjs(String hql, Object... values);

  /**
   * 返回list<Object[]>
   *
   * @param hql
   * @param values
   * @return
   */
  List<Object[]> findList(String hql, Object... values);

  PageDto<T> findPageByCri(PageDto<T> page, List<Criterion> criterions);

  List<T> findAll(CriteriaQuery criteriaQuery);

  PageDto<T> findPageOderBy(PageDto<T> pageRequest, String orderByProperty, boolean isAsc, List<Criterion> criterions);

  PageDto<T> findPageOderBy(PageDto<T> pageRequest, String orderByProperty, boolean isAsc, Criterion... criterions);


}
