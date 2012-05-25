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
package org.eclipse.stardust.vfs.impl.jcr.jackrabbit;

import java.security.Principal;
import java.util.Collections;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.Group;

/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class JcrVfsGroup implements Group
{

   private final String id;
   private final Principal principal;

   public JcrVfsGroup(String id, Principal principal)
   {
      this.id = id;
      this.principal = principal;
   }

   public boolean addMember(Authorizable arg0) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Iterator getDeclaredMembers() throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Iterator getMembers() throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public boolean isMember(Authorizable arg0) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public boolean removeMember(Authorizable arg0) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public boolean addReferee(Principal arg0) throws AuthorizableExistsException,
         RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Iterator declaredMemberOf() throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public String getID() throws RepositoryException
   {
      return this.id;
   }

   public Principal getPrincipal() throws RepositoryException
   {
      return this.principal;
   }

   public PrincipalIterator getPrincipals() throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Value[] getProperty(String arg0) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Iterator getPropertyNames() throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public boolean hasProperty(String arg0) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public boolean isGroup()
   {
      return true;
   }

   public Iterator memberOf() throws RepositoryException
   {
      // TODO query UserService for group membership?
      return Collections.EMPTY_LIST.iterator();
   }

   public void remove() throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public boolean removeProperty(String arg0) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public boolean removeReferee(Principal arg0) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public void setProperty(String arg0, Value arg1) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public void setProperty(String arg0, Value[] arg1) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Iterator<String> getPropertyNames(String relPath) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public boolean isDeclaredMember(Authorizable authorizable) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

}
