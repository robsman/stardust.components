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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.vfs.IAccessControlEntry;
import org.eclipse.stardust.vfs.IAccessControlEntry.EntryType;
import org.eclipse.stardust.vfs.IAccessControlPolicy;
import org.eclipse.stardust.vfs.IPrivilege;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;


/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class JcrVfsAccessControlPolicy implements IAccessControlPolicy 
{

   private final Set<IAccessControlEntry> aces;
   private final Set<IAccessControlEntry> originalState;
   private final boolean isNew;
   
   public JcrVfsAccessControlPolicy(Set<IAccessControlEntry> aces, boolean isNew)
   {
      this.aces = aces;
      this.originalState = saveOriginalState();
      this.isNew = isNew;
   }
   
   private Set<IAccessControlEntry> saveOriginalState()
   {
      Set<IAccessControlEntry> result = CollectionUtils.newSet();
      for (IAccessControlEntry ace : this.aces)
      {
         result.add(new JcrVfsAccessControlEntry(ace.getPrincipal(), new HashSet<IPrivilege>(ace.getPrivileges()), ace.getType()));
      }
      return Collections.unmodifiableSet(result);
   }

   public Set<IAccessControlEntry> getOriginalState()
   {
      return originalState;
   }

   public boolean isNew()
   {
      return isNew;
   }

   public Set<IAccessControlEntry> getAccessControlEntries()
   {
      return this.aces;
   }

   public void addAccessControlEntry(Principal principal, Set<IPrivilege> privileges)
   {
      this.aces.add(new JcrVfsAccessControlEntry(principal, privileges, EntryType.ALLOW));
   }

   public void addAccessControlEntry(Principal principal, Set<IPrivilege> privileges,
         EntryType type)
   {
      this.aces.add(new JcrVfsAccessControlEntry(principal, privileges, type));
   }   
   
   public void removeAccessControlEntry(IAccessControlEntry ace)
   {
      this.aces.remove(ace);
   }

   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append(this.aces);
      if (isNew)
      {
         sb.append(" (new)");
      }
      return sb.toString();
   }

   public void removeAllAccessControlEntries()
   {
      this.aces.clear();
   }
   
   public boolean isReadonly()
   {
      return false;
   }
   
}