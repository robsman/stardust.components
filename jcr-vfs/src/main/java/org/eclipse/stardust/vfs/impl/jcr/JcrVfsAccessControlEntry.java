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
import java.util.Set;

import org.eclipse.stardust.vfs.IAccessControlEntry;
import org.eclipse.stardust.vfs.IPrivilege;



/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class JcrVfsAccessControlEntry implements IAccessControlEntry
{

   private final Set<IPrivilege> privileges;
   private final Principal principal;
   private final EntryType type;
   
   public JcrVfsAccessControlEntry(Principal principal, Set<IPrivilege> privileges)
   {
      this(principal, privileges, null);
   }
   
   public JcrVfsAccessControlEntry(Principal principal, Set<IPrivilege> privileges, EntryType type)
   {      
      this.privileges = privileges;
      this.principal = principal;
      this.type = (type != null ? type : EntryType.ALLOW);
   }
   
   public Principal getPrincipal()
   {
      return this.principal;
   }
   
   public EntryType getType()
   {
      return type;
   }
         

   public Set<IPrivilege> getPrivileges()
   {
      return this.privileges;
   }

   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append(this.principal);
      sb.append(": ");
      sb.append(this.privileges);
      sb.append(" (" + type + ")");
      
      return sb.toString();
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((principal == null) ? 0 : principal.hashCode());
      result = prime * result + ((privileges == null) ? 0 : privileges.hashCode());
      result = prime * result + type.hashCode();
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if ( !(obj instanceof IAccessControlEntry))
         return false;
      IAccessControlEntry other = (IAccessControlEntry) obj;
      if (getPrincipal() == null)
      {
         if (other.getPrincipal() != null)
            return false;
      }
      else if ( !getPrincipal().equals(other.getPrincipal()))
         return false;
      if (getPrivileges() == null)
      {
         if (other.getPrivileges() != null)
            return false;
      }
      else if ( !getPrivileges().equals(other.getPrivileges()))
         return false;
      if ( !getType().equals(other.getType()))
         return false;      
      return true;
   }
   
}