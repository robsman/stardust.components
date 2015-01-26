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

import javax.jcr.Session;

import org.springframework.transaction.support.ResourceHolderSupport;


/**
 * Container class to hold a JcrSession
 * @author thomas.wolfram
 *
 */
public class JcrSpringSessionContainer extends ResourceHolderSupport 
{
	private Session session;
	
	/**
	 * 
	 * @param session
	 */
	public JcrSpringSessionContainer(Session session)
	{
		this.session = session;
	}

	/**
	 * 
	 */
	public void clear()
	{
		super.clear();
		this.session = null;
	}
	
	/**
	 * 
	 * @return Session
	 */
	public Session getSession() 
	{
		return session;
	}

	/**
	 * 
	 * @param session
	 */
	public void setSession(Session session) 
	{
		this.session = session;
	}
	
	
}