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
 * IAccessControlPolicy represents an access control list containing
 * IAccessControlEntry objects.
 * 
 * @author rsauer
 * @version $Revision: 24736 $
 */
public interface IAccessControlPolicy 
{

   /**
    * Creates a new access control entry and fills it with the principal and privileges 
    * passed.
    * @param principal
    * @param privileges
    */
   void addAccessControlEntry(Principal principal, Set<IPrivilege> privileges);
   
   /**
    * Removes a access control entry from this policy.
    * @param ace access control entry that is contained in this policy
    */
   void removeAccessControlEntry(IAccessControlEntry ace);

   /**
    * Returns all access control entries contained in this policy.
    * @return
    */
   Set<IAccessControlEntry> getAccessControlEntries(); 
   
   /**
    * Empties the policy. Empty policies may be removed by the underlying implementation.
    */
   void removeAllAccessControlEntries();
   
   /**
    * Returns the access control entries that existed before policy modifications.
    * @return
    */
   Set<IAccessControlEntry> getOriginalState();

   /**
    * Returns true for policies returned by getApplicablePolicies, false otherwise.
    * @return
    */
   boolean isNew();
   
   /**
    * Policies returned by getEffectivePolicies are readonly.
    * @return
    */
   boolean isReadonly();
   
}
