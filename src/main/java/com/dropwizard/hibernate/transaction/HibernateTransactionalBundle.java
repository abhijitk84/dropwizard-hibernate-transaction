package com.dropwizard.hibernate.transaction;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module.Feature;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import javax.annotation.Nullable;
import org.hibernate.SessionFactory;

public abstract class HibernateTransactionalBundle<T extends Configuration> implements ConfiguredBundle<T>,
    DatabaseConfiguration<T> {
  public static final String DEFAULT_NAME = "hibernate";

  @Nullable
  private SessionFactory sessionFactory;
  private boolean lazyLoadingEnabled = true;

  private final ImmutableList<Class<?>> entities;
  private final SessionFactoryFactory sessionFactoryFactory;

  protected HibernateTransactionalBundle(Class<?> entity, Class<?>... entities) {
    this(ImmutableList.<Class<?>>builder().add(entity).add(entities).build(),
        new SessionFactoryFactory());
  }

  protected HibernateTransactionalBundle(ImmutableList<Class<?>> entities,
      SessionFactoryFactory sessionFactoryFactory) {
    this.entities = entities;
    this.sessionFactoryFactory = sessionFactoryFactory;
  }

  @Override
  public final void initialize(Bootstrap<?> bootstrap) {
    bootstrap.getObjectMapper().registerModule(createHibernate5Module());
  }

  /**
   * Override to configure the {@link Hibernate5Module}.
   */
  protected Hibernate5Module createHibernate5Module() {
    Hibernate5Module module = new Hibernate5Module();
    if (lazyLoadingEnabled) {
      module.enable(Feature.FORCE_LAZY_LOADING);
    }
    return module;
  }

  /**
   * Override to configure the name of the bundle
   * (It's used for the bundle health check and database pool metrics)
   */
  protected String name() {
    return DEFAULT_NAME;
  }

  @Override
  public final void run(T configuration, Environment environment) throws Exception {
    final PooledDataSourceFactory dbConfig = getDataSourceFactory(configuration);
    this.sessionFactory = requireNonNull(sessionFactoryFactory.build(this, environment, dbConfig,
        entities, name()));
    TransactionHandler.initialize(sessionFactory);
    environment.healthChecks().register(name(),
        new SessionFactoryHealthCheck(
            environment.getHealthCheckExecutorService(),
            dbConfig.getValidationQueryTimeout().orElse(
                Duration.seconds(5)),
            sessionFactory,
            dbConfig.getValidationQuery()));
  }


  public boolean isLazyLoadingEnabled() {
    return lazyLoadingEnabled;
  }

  public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
    this.lazyLoadingEnabled = lazyLoadingEnabled;
  }

  public SessionFactory getSessionFactory() {
    return requireNonNull(sessionFactory);
  }

  protected void configure(org.hibernate.cfg.Configuration configuration) {
  }
}
