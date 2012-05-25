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

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;


/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class JcrVfsPrincipal implements Principal, Serializable
{

   private static final long serialVersionUID = 43647110820226773L;
   private String name;
   private Set<AuthorizableOrganizationDetails> directGroups;

   public JcrVfsPrincipal(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return this.name;
   }

   public Set<AuthorizableOrganizationDetails> getDirectGroups()
   {
      return directGroups;
   }

   public void setDirectGroups(Set<AuthorizableOrganizationDetails> directGroups)
   {
      this.directGroups = directGroups;
   }

   @Override
   public String toString()
   {
      return this.name;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if ( !(obj instanceof Principal))
         return false;
      Principal other = (Principal) obj;
      if (getName() == null)
      {
         if (other.getName() != null)
            return false;
      }
      else if ( !getName().equals(other.getName()))
         return false;
      return true;
   }

}