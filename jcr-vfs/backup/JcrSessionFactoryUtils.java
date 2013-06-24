/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: JcrSessionFactoryUtils.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.jcr.spring;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.vfs.impl.utils.SessionUtils;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;


/**
 * FactoryBean for instantiating a Java Content Repository. This abstract class adds
 * custom functionality subclasses handling only the configuration issues.
 *
 *
 * @author Costin Leau
 * @author rsauer
 *
 */
public abstract class JcrSessionFactoryUtils {

   private static final Log logger = LogFactory.getLog(JcrSessionFactoryUtils.class);

   /**
    * Get a JCR Session for the given Repository. Is aware of and will return
    * any existing corresponding Session bound to the current thread, for
    * example when using JcrTransactionManager. Will create a new Session
    * otherwise, if allowCreate is true. This is the getSession method used by
    * typical data access code, in combination with releaseSession called when
    * done with the Session. Note that JcrTemplate allows to write data access
    * code without caring about such resource handling. Supports
    * synchronization with both Spring-managed JTA transactions (i.e.
    * JtaTransactionManager) and non-Spring JTA transactions (i.e. plain JTA or
    * EJB CMT).
    *
    * @param sessionFactory JCR Repository to create session with
    * @param allowCreate
    *            if a non-transactional Session should be created when no
    *            transactional Session can be found for the current thread
    * @param enlistWithTxManager TODO
    * @throws RepositoryException
    * @return
    */
   public static Session getSession(JcrSessionFactory sessionFactory, boolean allowCreate, boolean enlistWithTxManager)
         throws RepositoryException
   {
      Assert.notNull(sessionFactory, "No sessionFactory specified");

      // check if there is any transaction going on
      JcrSessionHolder sessionHolder = (JcrSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
      if ((null != sessionHolder) && (null != sessionHolder.getSession()))
      {
         return sessionHolder.getSession();
      }

      if ( !allowCreate && !TransactionSynchronizationManager.isSynchronizationActive())
      {
         throw new IllegalStateException(
               "No session bound to thread, and configuration does not allow creation of non-transactional one here");
      }

      if (logger.isDebugEnabled())
      {
         logger.debug("Opening JCR Session");
      }

      final Session session = sessionFactory.createSession();

      if (enlistWithTxManager)
      {
         // TODO
      }

      if (TransactionSynchronizationManager.isSynchronizationActive())
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("Registering transaction synchronization for JCR session");
         }

         // Use same session for further JCR actions within the transaction
         // thread object will get removed by synchronization at transaction
         // completion.
         sessionHolder = sessionFactory.getSessionHolder(session);
         sessionHolder.setSynchronizedWithTransaction(true);
         TransactionSynchronizationManager.registerSynchronization(new JcrSessionSynchronization(
               sessionHolder, sessionFactory));
         TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);
      }

      return session;
   }

   /**
    * Return whether the given JCR Session is thread-bound that is, bound to
    * the current thread by Spring's transaction facilities (which is used as a thread-bounding
    * utility class).
    *
    * @param session
    *            the JCR Session to check
    * @param sessionFactory
    *            the JCR SessionFactory that the Session was created with (can
    *            be null)
    * @return whether the Session is transactional
    */
   public static boolean isSessionThreadBound(Session session,
         JcrSessionFactory sessionFactory)
   {
      if (null == sessionFactory)
      {
         return false;
      }

      final JcrSessionHolder sessionHolder = (JcrSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

      return ((null != sessionHolder) && (session == sessionHolder.getSession()));
   }

   /**
    * Close the given Session, created via the given repository, if it is not
    * managed externally (i.e. not bound to the thread).
    *
    * @param session
    *            the Jcr Session to close
    * @param sessionFactory
    *            JcrSessionFactory that the Session was created with (can be
    *            null)
    */
   public static void releaseSession(Session session, JcrSessionFactory sessionFactory)
   {
      if (null == session)
      {
         return;
      }

      // Only close non thread bound Sessions.
      if ( !isSessionThreadBound(session, sessionFactory))
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("Closing JCR Session");
         }
         SessionUtils.logout(session);
      }
   }

   /**
    * Callback for resource cleanup at the end of a non-JCR transaction (e.g.
    * when participating in a JtaTransactionManager transaction).
    *
    * @see org.springframework.transaction.jta.JtaTransactionManager
    */
   private static class JcrSessionSynchronization
         extends TransactionSynchronizationAdapter
   {

      private final JcrSessionHolder sessionHolder;

      private final JcrSessionFactory sessionFactory;

      private boolean holderActive = true;

      public JcrSessionSynchronization(JcrSessionHolder holder,
            JcrSessionFactory sessionFactory)
      {
         this.sessionHolder = holder;
         this.sessionFactory = sessionFactory;
      }

      public void suspend()
      {
         if (this.holderActive)
         {
            TransactionSynchronizationManager.unbindResource(this.sessionFactory);
         }
      }

      public void resume()
      {
         if (this.holderActive)
         {
            TransactionSynchronizationManager.bindResource(this.sessionFactory,
                  this.sessionHolder);
         }
      }

      public void beforeCompletion()
      {
         TransactionSynchronizationManager.unbindResource(this.sessionFactory);
         this.holderActive = false;

         releaseSession(this.sessionHolder.getSession(), this.sessionFactory);
      }
   }

}
