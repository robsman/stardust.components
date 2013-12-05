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
package org.eclipse.stardust.vfs.impl.jcr;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import org.eclipse.stardust.vfs.IAccessControlEntry;
import org.eclipse.stardust.vfs.IAccessControlEntry.EntryType;
import org.eclipse.stardust.vfs.IAccessControlPolicy;
import org.eclipse.stardust.vfs.IPrivilege;
import org.eclipse.stardust.vfs.RepositoryOperationFailedException;



/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class JcrVfsReadonlyAccessControlPolicy implements IAccessControlPolicy 
{

   private final Set<IAccessControlEntry> aces;
   
   public JcrVfsReadonlyAccessControlPolicy(Set<IAccessControlEntry> aces)
   {
      this.aces = Collections.unmodifiableSet(aces);
   }
   
   public Set<IAccessControlEntry> getAccessControlEntries()
   {
      return this.aces;
   }

   public void addAccessControlEntry(Principal principal, Set<IPrivilege> privileges)
   {
      throw new RepositoryOperationFailedException("Can not modify readonly access control policy.");
   }

   public void removeAccessControlEntry(IAccessControlEntry ace)
   {
      throw new RepositoryOperationFailedException("Can not modify readonly access control policy.");
   }
   
   public void addAccessControlEntry(Principal principal, Set<IPrivilege> privileges,
         EntryType entry)
   {
      throw new RepositoryOperationFailedException("Can not modify readonly access control policy.");
   }
   
   public void removeAllAccessControlEntries()
   {
      throw new RepositoryOperationFailedException("Can not modify readonly access control policy.");
   }

   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append(this.aces);
      return sb.toString();
   }
   
   public boolean isReadonly()
   {
      return true;
   }

   public Set<IAccessControlEntry> getOriginalState()
   {
      return this.aces;
   }

   public boolean isNew()
   {
      return false;
   }
   
}