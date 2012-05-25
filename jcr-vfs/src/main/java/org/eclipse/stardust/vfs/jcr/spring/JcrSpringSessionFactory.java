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
package org.eclipse.stardust.vfs.jcr.spring;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.vfs.impl.utils.SessionUtils;
import org.eclipse.stardust.vfs.jcr.ISessionFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * 
 * @author thomas.wolfram
 *
 */
public class JcrSpringSessionFactory implements ISessionFactory {

	private Credentials credentials;
	private String workspace;
	private Repository repo;
	private boolean permitCreation;
	
	public static final Log trace = LogFactory.getLog(JcrSpringSessionFactory.class);
	
	/**
	 * 
	 * @return Session
	 * @throws RepositoryException
	 */
	public Session createSession() throws RepositoryException
	{
		Session session = repo.login(credentials, workspace);
	    return session;	
	}
	
	/**
	 * @param Session
	 */
	public void releaseSession(Session session) {
		
		JcrSpringSessionFactory.unbindSession(session, this);
		
	}
	
	public static void unbindSession (Session session, JcrSpringSessionFactory factory)
	{
	      if (session == null)
	      {
	         return;
	      }

	   
	      if (!JcrSpringSessionFactory.isSessionBoundToThread(session, factory))
	      {
			if (trace.isDebugEnabled())
			{
				trace.debug("JCR Session is released");
			}
	         SessionUtils.logout(session);
	      }
	}

    private static boolean isSessionBoundToThread(Session session, JcrSpringSessionFactory factory)
    {
		if (factory != null) 
		{
			final JcrSpringSessionContainer container = (JcrSpringSessionContainer) TransactionSynchronizationManager
					.getResource(factory);

			if (container != null && session == container.getSession())
			{
				return true;
			}
		}
		return false;
	}
	
	
    /**
     * @return Session
     */
	public Session getSession() throws RepositoryException {
		
	    Assert.notNull(this, "SessionFactory not specified");

		JcrSpringSessionContainer container = (JcrSpringSessionContainer) TransactionSynchronizationManager
				.getResource(this);
	      
		if (container != null && container.getSession() != null) 
		{
			return container.getSession();
		}

	      if (!TransactionSynchronizationManager.isSynchronizationActive() && !permitCreation) 
	      {
			throw new IllegalStateException(
					"Configuration does not allow creation of non-transaction resources and no session bound.");
	      }
	      

		 
	      if (trace.isDebugEnabled())
		 {
			 trace.debug("Getting JCR Session");
		 }

	      final Session session = this.createSession();

	      if (TransactionSynchronizationManager.isSynchronizationActive())
	      {
	    	  if (trace.isDebugEnabled())
	    	  {
	    		  trace.debug("Registering transaction synchronization for JCR session");
	    	  }

	         container = this.getSessionContainer(session);
	         container.setSynchronizedWithTransaction(true);
			 TransactionSynchronizationManager
					.registerSynchronization(new JcrSpringSessionSynchronizer(
							container, this));
			 TransactionSynchronizationManager.bindResource(this, container);
	      }

	      return session;				
	}

	/**
	 * 
	 * @param session
	 * @return
	 */
	public JcrSpringSessionContainer getSessionContainer(Session session) {
		return new JcrSpringSessionContainer(session);
	}
	
	
	// ---------------------- Public Accessors -------------------
	
	/**
	 * 
	 * @return Repository
	 */
	public Repository getRepository() 
	{
		return repo;
	}

	/**
	 * 
	 * @param repository
	 */
	public void setRepository(Repository repository) 
	{
		this.repo = repository;
	}	
	
	/**
	 * 
	 * @return String
	 */
	public String getWorkspaceName() 
	{
		return workspace;
	}

	/**
	 * 
	 * @param workspaceName
	 */
	public void setWorkspaceName(String workspaceName) 
	{
		this.workspace = workspaceName;
	}

	/**
	 * 
	 * @return Credentials
	 */
	public Credentials getCredentials() 
	{
		return credentials;
	}
	
	/**
	 * 
	 * @param credentials
	 */
	public void setCredentials(Credentials credentials) 
	{
		this.credentials = credentials;
	}	
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isAllowCreate() {
		return permitCreation;
	}

	/**
	 * 
	 * @param allowCreate
	 */
	public void setAllowCreate(boolean allowCreate) {
		this.permitCreation = allowCreate;
	}

}
