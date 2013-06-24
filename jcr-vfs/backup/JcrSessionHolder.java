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
 * $Id: JcrSessionHolder.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.jcr.spring;

import javax.jcr.Session;

import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * Holder object for JCR Session.
 * 
 * @author Guillaume Bort <guillaume.bort@zenexity.fr>
 * @author Costin Leau
 * @author rsauer
 * 
 */
public class JcrSessionHolder extends ResourceHolderSupport
{

   private Session session;

   public JcrSessionHolder(Session session)
   {
      setSession(session);
   }

   protected void setSession(Session session)
   {
      this.session = session;
   }

   public Session getSession()
   {
      return session;
   }

   /**
    * @see org.springframework.transaction.support.ResourceHolderSupport#clear()
    */
   public void clear()
   {
      super.clear();

      this.session = null;
   }
}
