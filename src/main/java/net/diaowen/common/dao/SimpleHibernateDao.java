/**
 * Copyright (c) 2005-2011 springside.org.cn
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * <p>
 * $Id: SimpleHibernateDao.java 1594 2011-05-11 14:22:29Z calvinxiu $
 */
package net.diaowen.common.dao;

import net.diaowen.common.utils.AssertUtils;
import net.diaowen.common.utils.ReflectionUtils;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 封装Hibernate原生API的DAO泛型基类.
 * <p>
 * 参考Spring2.5自带的Petlinc例子, 取消了HibernateTemplate, 直接使用Hibernate原生API.
 *
 * @param <T>  DAO操作的对象类型
 * @param <ID> 主键类型
 */
public class SimpleHibernateDao<T, ID extends Serializable> implements ISimpleHibernateDao<T, ID> {
  protected Logger logger = LoggerFactory.getLogger(getClass());
  @Autowired
  protected EntityManagerFactory entityManagerFactory;
  @Autowired
  protected EntityManager entityManager;
  protected Class<T> entityClass;

  /**
   * 通过子类的泛型定义取得对象类型Class.
   * eg.
   * public class UserDao extends SimpleHibernateDao<User, Long>
   */
  public SimpleHibernateDao() {
    this.entityClass = ReflectionUtils.getSuperClassGenricType(getClass());
  }

  public SimpleHibernateDao(Class<T> entityClass) {
    this.entityClass = entityClass;
  }

  @Override
  public SessionFactory getSessionFactory() {
    return entityManagerFactory.unwrap(SessionFactory.class);
  }

  @Override
  public Session getSession() {
    return getSessionFactory().getCurrentSession();
  }

  @Override
  public void save(final T entity) {
    try {
      AssertUtils.notNull(entity, "entity不能为空");
      entityManager.persist(entity);
    } catch (Exception e) {
      logger.error("===>save fail: ", e);
    }
  }

  @Override
  public void delete(final T entity) {
    AssertUtils.notNull(entity, "entity不能为空");
    entityManager.remove(entity);
    logger.debug("delete entity: {}", entity);
  }

  @Override
  public void delete(final ID id) {
    AssertUtils.notNull(id, "id不能为空");
    delete(get(id));
    logger.debug("delete entity {},id is {}", entityClass.getSimpleName(), id);
  }

  @Override
  public T get(final ID id) {
    AssertUtils.notNull(id, "id不能为空");
    return entityManager.find(entityClass, id);
  }

  @Override
  public List<T> get(final Collection<ID> ids) {
    return find(Restrictions.in(getIdName(), ids));
  }

  @Override
  public List<T> getAll() {
    return find();
  }

  @Override
  public List<T> getAll(String orderByProperty, boolean isAsc) {
    Criteria c = createCriteria();
    if (isAsc) {
      c.addOrder(Order.asc(orderByProperty));
    } else {
      c.addOrder(Order.desc(orderByProperty));
    }
    return c.list();
  }

  @Override
  public List<T> findBy(final String propertyName, final Object value) {
    AssertUtils.hasText(propertyName, "propertyName不能为空");
    Criterion criterion = Restrictions.eq(propertyName, value);
    return find(criterion);
  }

  @Override
  public T findUniqueBy(final String propertyName, final Object value) {
    AssertUtils.hasText(propertyName, "propertyName不能为空");
    Criterion criterion = Restrictions.eq(propertyName, value);
    return (T) createCriteria(criterion).uniqueResult();
  }

  @Override
  public <X> List<X> find(final String hql, final Object... values) {
    return createQuery(hql, values).list();
  }

  @Override
  public <X> List<X> find(final String hql, final Map<String, ?> values) {
    return createQuery(hql, values).list();
  }

  @Override
  public <X> X findUnique(final String hql, final Object... values) {
    return (X) createQuery(hql, values).uniqueResult();
  }

  @Override
  public <X> X findUnique(final String hql, final Map<String, ?> values) {
    return (X) createQuery(hql, values).uniqueResult();
  }

  @Override
  public int batchExecute(final String hql, final Object... values) {
    return createQuery(hql, values).executeUpdate();
  }

  @Override
  public int batchExecute(final String hql, final Map<String, ?> values) {
    return createQuery(hql, values).executeUpdate();
  }

  @Override
  public Query createQuery(final String queryString, final Object... values) {
    AssertUtils.hasText(queryString, "queryString不能为空");
    Query query = getSession().createQuery(queryString);
    if (values != null) {
      for (int i = 1; i <= values.length; i++) {
        query.setParameter(i, values[i - 1]);
      }
    }
    return query;
  }

  @Override
  public Query createQuery(final String queryString, final Map<String, ?> values) {
    AssertUtils.hasText(queryString, "queryString不能为空");
    Query query = getSession().createQuery(queryString);
    if (values != null) {
      query.setProperties(values);
    }
    return query;
  }

  @Override
  public List<T> find(final Criterion... criterions) {
    return createCriteria(criterions).list();
  }

  @Override
  public T findUnique(final Criterion... criterions) {
    return (T) createCriteria(criterions).uniqueResult();
  }


  @Override
  public Criteria createCriteria(final Criterion... criterions) {
    Criteria criteria = getSession().createCriteria(entityClass);
    for (Criterion c : criterions) {
      criteria.add(c);
    }
    return criteria;
  }

  @Override
  public Criteria createCriteria(List<Criterion> criterions) {
    Criteria criteria = getSession().createCriteria(entityClass);
    for (Criterion c : criterions) {
      criteria.add(c);
    }
    return criteria;
  }

  @Override
  public void initProxyObject(Object proxy) {
    Hibernate.initialize(proxy);
  }

  @Override
  public void flush() {
    getSession().flush();
  }

  @Override
  public Query distinct(Query query) {
    query.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    return query;
  }

  @Override
  public Criteria distinct(Criteria criteria) {
    criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    return criteria;
  }

  @Override
  public String getIdName() {
    ClassMetadata meta = getSessionFactory().getClassMetadata(entityClass);
    return meta.getIdentifierPropertyName();
  }

  @Override
  public boolean isPropertyUnique(final String propertyName, final Object newValue, final Object oldValue) {
    if (newValue == null || newValue.equals(oldValue)) {
      return true;
    }
    Object object = findUniqueBy(propertyName, newValue);
    return (object == null);
  }
}
