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

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Synchronization class 
 * @author thomas.wolfram
 *
 */
public class JcrSpringSessionSynchronizer extends
		TransactionSynchronizationAdapter 
{

	private boolean isActive = true;
	
    private final JcrSpringSessionContainer container;
    private final JcrSpringSessionFactory factory;

    /**
     * 
     * @param container
     * @param factory
     */
    public JcrSpringSessionSynchronizer(JcrSpringSessionContainer container,
          JcrSpringSessionFactory factory)
    {
       this.container = container;
       this.factory = factory;
    }

    /**
     * Call before completion
     */
    public void beforeCompletion()
    {
    	TransactionSynchronizationManager.unbindResource(this.factory);
    	this.isActive = false;
    	
    	factory.releaseSession(this.container.getSession());
    }

    
    /**
     * call for resume
     */
    public void resume()
    {
       if (this.isActive)
       {
          TransactionSynchronizationManager.bindResource(this.factory,
                this.container);
       }
    }

    /**
     * call for suspend
     */
    public void suspend()
    {
    	if (this.isActive)
    	{
    		TransactionSynchronizationManager.unbindResource(this.factory);
    	}
    }
 }

	


