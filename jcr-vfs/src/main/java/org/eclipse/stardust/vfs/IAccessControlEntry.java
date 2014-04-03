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
 * $Id: IFile.java 24736 2008-09-09 16:00:18Z rsauer $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs;

import java.security.Principal;
import java.util.Set;


/**
 * IAccessControlEntry represents a number of privileges assigned to 
 * a specific principal.
 * 
 * @author rsauer
 * @version $Revision: 24736 $
 */
public interface IAccessControlEntry 
{

   /**
    * Returns the principal the privileges are assigned to
    * @return the principal the privileges are assigned to
    */
   Principal getPrincipal();
   
   /**
    * Set of privileges assigned to the principal
    * @return set of privileges assigned to the principal
    */
   Set<IPrivilege> getPrivileges();
   
   EntryType getType();
   
   public static enum EntryType
   {
      ALLOW, DENY;
   }
}
