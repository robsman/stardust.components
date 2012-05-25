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
 * $Id: ContentDownloadFailedException.java 24691 2008-08-08 12:02:05Z rsauer $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs;

import org.eclipse.stardust.vfs.RepositoryOperationFailedException;

/**
 * AccessControlException is thrown for errors during access control modification.
 *   
 * @author sauer
 * @version $Revision: 24691 $
 */
public class AccessControlException extends RepositoryOperationFailedException
{

   static final long serialVersionUID = 1L;

   public AccessControlException()
   {
   }

   public AccessControlException(String message)
   {
      super(message);
   }
   
   public AccessControlException(Throwable cause)
   {
      super(cause);
   }
   
   public AccessControlException(String message, Throwable cause)
   {
      super(message, cause);
   }

}
