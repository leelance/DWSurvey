package net.diaowen.common.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ISimpleHibernateDao<T, ID extends Serializable> {

  /**
   * 取得sessionFactory.
   */
  SessionFactory getSessionFactory();

  /**
   * 取得当前Session.
   */
  Session getSession();

  /**
   * 保存新增或修改的对象.
   */
  void save(final T entity);

  /**
   * 删除对象.
   *
   * @param entity 对象必须是session中的对象或含id属性的transient对象.
   */
  void delete(final T entity);

  /**
   * 按id删除对象.
   */
  void delete(final ID id);

  /**
   * 按id获取对象.
   */
  T get(final ID id);

  /**
   * 按id列表获取对象列表.
   */
  List<T> get(final Collection<ID> ids);

  /**
   * 获取全部对象.
   */
  List<T> getAll();

  /**
   * 获取全部对象, 支持按属性行序.
   */
  List<T> getAll(String orderByProperty, boolean isAsc);

  /**
   * 按属性查找对象列表, 匹配方式为相等.
   */
  List<T> findBy(final String propertyName, final Object value);

  /**
   * 按属性查找唯一对象, 匹配方式为相等.
   */
  T findUniqueBy(final String propertyName, final Object value);

  /**
   * 按HQL查询对象列表.
   *
   * @param values 数量可变的参数,按顺序绑定.
   */
  <X> List<X> find(final String hql, final Object... values);

  /**
   * 按HQL查询对象列表.
   *
   * @param values 命名参数,按名称绑定.
   */
  <X> List<X> find(final String hql,
                   final Map<String, ?> values);

  /**
   * 按HQL查询唯一对象.
   *
   * @param values 数量可变的参数,按顺序绑定.
   */
  <X> X findUnique(final String hql, final Object... values);

  /**
   * 按HQL查询唯一对象.
   *
   * @param values 命名参数,按名称绑定.
   */
  <X> X findUnique(final String hql,
                   final Map<String, ?> values);

  /**
   * 执行HQL进行批量修改/删除操作.
   *
   * @param values 数量可变的参数,按顺序绑定.
   * @return 更新记录数.
   */
  int batchExecute(final String hql, final Object... values);

  /**
   * 执行HQL进行批量修改/删除操作.
   *
   * @param values 命名参数,按名称绑定.
   * @return 更新记录数.
   */
  int batchExecute(final String hql,
                   final Map<String, ?> values);

  /**
   * 根据查询HQL与参数列表创建Query对象.
   * 与find()函数可进行更加灵活的操作.
   *
   * @param values 数量可变的参数,按顺序绑定.
   */
  Query createQuery(final String queryString,
                    final Object... values);

  /**
   * 根据查询HQL与参数列表创建Query对象.
   * 与find()函数可进行更加灵活的操作.
   *
   * @param values 命名参数,按名称绑定.
   */
  Query createQuery(final String queryString,
                    final Map<String, ?> values);

  /**
   * 按Criteria查询对象列表.
   *
   * @param criterions 数量可变的Criterion.
   */
  List<T> find(final Criterion... criterions);

  /**
   * 按Criteria查询唯一对象.
   *
   * @param criterions 数量可变的Criterion.
   */
  T findUnique(final Criterion... criterions);

  /**
   * 根据Criterion条件创建Criteria.
   * 与find()函数可进行更加灵活的操作.
   *
   * @param criterions 数量可变的Criterion.
   */
  Criteria createCriteria(final Criterion... criterions);

  /**
   * @param criterions
   * @return
   */
  public Criteria createCriteria(List<Criterion> criterions);

  /**
   * 初始化对象.
   * 使用load()方法得到的仅是对象Proxy, 在传到View层前需要进行初始化.
   * 如果传入entity, 则只初始化entity的直接属性,但不会初始化延迟加载的关联集合和属性.
   * 如需初始化关联属性,需执行:
   * Hibernate.initialize(user.getRoles())，初始化User的直接属性和关联集合.
   * Hibernate.initialize(user.getDescription())，初始化User的直接属性和延迟加载的Description属性.
   */
  void initProxyObject(Object proxy);

  /**
   * Flush当前Session.
   */
  void flush();

  /**
   * 为Query添加distinct transformer.
   * 预加载关联对象的HQL会引起主对象重复, 需要进行distinct处理.
   */
  Query distinct(Query query);

  /**
   * 为Criteria添加distinct transformer.
   * 预加载关联对象的HQL会引起主对象重复, 需要进行distinct处理.
   */
  Criteria distinct(Criteria criteria);

  /**
   * 取得对象的主键名.
   */
  String getIdName();

  /**
   * 判断对象的属性值在数据库内是否唯一.
   * <p>
   * 在修改对象的情景下,如果属性新修改的值(value)等于属性原来的值(orgValue)则不作比较.
   */
  boolean isPropertyUnique(final String propertyName,
                           final Object newValue, final Object oldValue);

}
