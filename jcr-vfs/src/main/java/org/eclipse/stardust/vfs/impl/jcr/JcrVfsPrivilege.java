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
 * $Id: JcrRepositoryFile.java 24736 2008-09-09 16:00:18Z rsauer $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr;

import org.eclipse.stardust.vfs.IPrivilege;

/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class JcrVfsPrivilege implements IPrivilege
{
   /**
    * Alias for the modify privilege. Reading permissions from Jackrabbit somehow produces
    * the rep: NS prefix.
    * 
    * @see IPrivilege#MODIFY_PRIVILEGE
    */
   public static final String JR_MODIFY_PRIVILEGE_ALIAS = "rep:write";

   private final String name;
   
   public JcrVfsPrivilege(String name)
   {
      this.name = JR_MODIFY_PRIVILEGE_ALIAS.equals(name) //
            ? MODIFY_PRIVILEGE
            : name;
   }

   public String getName()
   {
      return this.name;
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
      if ( !(obj instanceof IPrivilege))
         return false;
      IPrivilege other = (IPrivilege) obj;
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
