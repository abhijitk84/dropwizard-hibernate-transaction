package com.dropwizard.hibernate.transaction;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

public class TransactionHandler {

  private static SessionFactory sessionFactory;
  @Nullable
  private Transactional transactional;
  @Nullable
  private Session session;


  public static void initialize(SessionFactory initSessionFactory) {
    sessionFactory = initSessionFactory;
  }

  public static SessionFactory getSessionFactory() {
    return requireNonNull(sessionFactory);
  }

  public void beforeStart(@Nullable Transactional transactional) {
    if (transactional == null) {
      return;
    }
    this.transactional = transactional;

    if (sessionFactory == null) {
      throw new IllegalArgumentException(
          "Unregistered Hibernate bundle: ");
    }
    session = sessionFactory.openSession();
    try {
      configureSession();
      ManagedSessionContext.bind(session);
      beginTransaction(transactional, session);
    } catch (Throwable th) {
      session.close();
      session = null;
      ManagedSessionContext.unbind(sessionFactory);
      throw th;
    }
  }

  public void afterEnd() {
    if (transactional == null || session == null) {
      return;
    }

    try {
      commitTransaction(transactional, session);
    } catch (Exception e) {
      rollbackTransaction(transactional, session);
      throw e;
    }
  }

  public void onError() {
    if (transactional == null || session == null) {
      return;
    }

    try {
      rollbackTransaction(transactional, session);
    } finally {
      onFinish();
    }
  }

  public void onFinish() {
    try {
      if (session != null) {
        session.close();
      }
    } finally {
      session = null;
      ManagedSessionContext.unbind(sessionFactory);
    }
  }

  protected void configureSession() {
    checkNotNull(transactional);
    checkNotNull(session);
    session.setDefaultReadOnly(transactional.readOnly());
    session.setCacheMode(transactional.cacheMode());
    session.setHibernateFlushMode(transactional.flushMode());
  }

  private void beginTransaction(Transactional transactional, Session session) {
    if (!transactional.transactional()) {
      return;
    }
    session.beginTransaction();
  }

  private void rollbackTransaction(Transactional transactional, Session session) {
    if (!transactional.transactional()) {
      return;
    }
    final Transaction txn = session.getTransaction();
    if (txn != null && txn.getStatus().canRollback()) {
      txn.rollback();
    }
  }

  private void commitTransaction(Transactional transactional, Session session) {
    if (!transactional.transactional()) {
      return;
    }
    final Transaction txn = session.getTransaction();
    if (txn != null && txn.getStatus().canRollback()) {
      txn.commit();
    }
  }

  protected Session getSession() {
    return requireNonNull(session);
  }

}