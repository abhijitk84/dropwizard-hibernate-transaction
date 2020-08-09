package com.dropwizard.hibernate.transaction;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.List;
import javax.persistence.criteria.CriteriaQuery;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.internal.AbstractProducedQuery;

public abstract class AbstractDAO<E> {

  private final Class<?> entityClass;

  public AbstractDAO() {
    this.entityClass = getClass();
  }


  protected Session currentSession() {
    return TransactionHandler.getSessionFactory().getCurrentSession();
  }


  protected CriteriaQuery<E> criteriaQuery() {
    return this.currentSession().getCriteriaBuilder().createQuery(getEntityClass());
  }


  protected Query namedQuery(String queryName) throws HibernateException {
    return currentSession().getNamedQuery(requireNonNull(queryName));
  }


  protected Query<E> query(String queryString) {
    return currentSession().createQuery(requireNonNull(queryString), getEntityClass());
  }


  public Class<E> getEntityClass() {
    return (Class<E>) entityClass;
  }


  protected E uniqueResult(CriteriaQuery<E> criteriaQuery) throws HibernateException {
    return AbstractProducedQuery.uniqueElement(
        currentSession()
            .createQuery(requireNonNull(criteriaQuery))
            .getResultList()
    );
  }


  @SuppressWarnings("unchecked")
  protected E uniqueResult(Criteria criteria) throws HibernateException {
    return (E) requireNonNull(criteria).uniqueResult();
  }

  protected E uniqueResult(Query<E> query) throws HibernateException {
    return requireNonNull(query).uniqueResult();
  }

  @SuppressWarnings("unchecked")
  protected List<E> list(Criteria criteria) throws HibernateException {
    return requireNonNull(criteria).list();
  }

  protected List<E> list(CriteriaQuery<E> criteria) throws HibernateException {
    return currentSession().createQuery(requireNonNull(criteria)).getResultList();
  }


  protected List<E> list(Query<E> query) throws HibernateException {
    return requireNonNull(query).list();
  }


  @SuppressWarnings("unchecked")
  protected E get(Serializable id) {
    return (E) currentSession().get(entityClass, requireNonNull(id));
  }


  protected E persist(E entity) throws HibernateException {
    currentSession().saveOrUpdate(requireNonNull(entity));
    return entity;
  }
}
