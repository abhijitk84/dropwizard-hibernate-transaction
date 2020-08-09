package com.dropwizard.hibernate.transaction;

import com.dropwizard.hibernate.transaction.utils.ManagedContext;
import java.lang.reflect.InvocationTargetException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class TransactionalAspect {

  private static final String INIATIATED = "initiated";

  @Pointcut("within(@com.dropwizard.hibernate.transaction.Transactional *)")
  public void transcationClassCalled() {
    //Empty as required
  }

  @Pointcut("@annotation(com.dropwizard.hibernate.transaction.Transactional)")
  public void transcationFunctionCalled() {
    //Empty as required
  }

  @Pointcut("execution(* *(..))")
  public void anyFunctionCalled() {
    //Empty as required
  }

  @Around(value = "(transcationClassCalled() &&  anyFunctionCalled()) || (transcationFunctionCalled() && anyFunctionCalled())")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    final Signature callSignature = joinPoint.getSignature();
    final MethodSignature methodSignature = MethodSignature.class.cast(callSignature);
    Transactional monitoredFunction = methodSignature.getMethod()
        .getAnnotation(Transactional.class);
    Transactional classTransactional = (Transactional) callSignature.getDeclaringType()
        .getAnnotation(Transactional.class);
    Transactional transactional =
        monitoredFunction == null ? classTransactional : monitoredFunction;
    if (ManagedContext.existKey(INIATIATED)) {
      return joinPoint.proceed();
    }
    return handleTransaction(transactional, joinPoint);
  }

  private Object handleTransaction(Transactional transactional, ProceedingJoinPoint joinPoint)
      throws Throwable {
    final TransactionHandler transactionHandler = new TransactionHandler();
    try {
      transactionHandler.beforeStart(transactional);
      ManagedContext.put(INIATIATED, "true");
      Object result = joinPoint.proceed();
      transactionHandler.afterEnd();
      return result;
    } catch (InvocationTargetException e) {
      transactionHandler.onError();
      throw e.getCause();
    } catch (Exception e) {
      transactionHandler.onError();
      throw e;
    } finally {
      ManagedContext.remove(INIATIATED);
      transactionHandler.onFinish();
    }
  }
}