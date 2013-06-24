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
 * $Id: JcrSessionFactory.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.jcr.spring;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.eclipse.stardust.vfs.jcr.ISessionFactory;


/**
 * @author sauer
 * @version $Revision: 54136 $
 */
public class JcrSessionFactory implements ISessionFactory
{

   private Repository repository;

   private String workspaceName;

   private Credentials credentials;

   private boolean allowCreate;

   private boolean enlistWithTxManager;

   public Session getSession() throws RepositoryException
   {
      return JcrSessionFactoryUtils.getSession(this, allowCreate, enlistWithTxManager);
   }

   public Session createSession() throws RepositoryException
   {
      Session session = repository.login(credentials, workspaceName);

      //session = addListeners(session);
      
      return session;
   }

   public void releaseSession(Session session)
   {
      JcrSessionFactoryUtils.releaseSession(session, this);
   }

   public Repository getRepository()
   {
      return repository;
   }

   public void setRepository(Repository repository)
   {
      this.repository = repository;
   }

   public String getWorkspaceName()
   {
      return workspaceName;
   }

   public void setWorkspaceName(String workspaceName)
   {
      this.workspaceName = workspaceName;
   }

   public Credentials getCredentials()
   {
      return credentials;
   }

   public void setCredentials(Credentials credentials)
   {
      this.credentials = credentials;
   }

   public boolean isAllowCreate()
   {
      return allowCreate;
   }

   public void setAllowCreate(boolean allowCreate)
   {
      this.allowCreate = allowCreate;
   }

   public boolean isEnlistWithTransactionManager()
   {
      return enlistWithTxManager;
   }

   public void setEnlistWithTransactionManager(boolean enlistWithTransactionManager)
   {
      this.enlistWithTxManager = enlistWithTransactionManager;
   }

   /**
    * Returns a specific SessionHolder for the given Session. The holder provider is used
    * internally by the framework in components such as transaction managers to provide
    * implementation specific information such as transactional support (if it is
    * available).
    * 
    * @return specific sessionHolder.
    */
   public JcrSessionHolder getSessionHolder(Session session)
   {
      // TODO spring-modules originally used a sessionHolderProvider
      return new JcrSessionHolder(session);
   }
   
   /**
    * Hook for adding listeners to the newly returned session. We have to treat
    * exceptions manually and can't reply on the template.
    * 
    * @param session JCR session
    * @return the listened session
    */
/*
   protected Session addListeners(Session session) throws RepositoryException {
      if (eventListeners != null && eventListeners.length > 0) {
         Workspace ws = session.getWorkspace();
         ObservationManager manager = ws.getObservationManager();
         if (log.isDebugEnabled())
            log.debug("adding listeners " + Arrays.asList(eventListeners).toString() + " for session " + session);

         for (int i = 0; i < eventListeners.length; i++) {
            manager.addEventListener(eventListeners[i].getListener(), eventListeners[i].getEventTypes(),
                  eventListeners[i].getAbsPath(), eventListeners[i].isDeep(), eventListeners[i].getUuid(),
                  eventListeners[i].getNodeTypeName(), eventListeners[i].isNoLocal());
         }
      }
      return session;
   }
*/
}
