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
 * $Id: TestJcrVfsAgainstJackrabbit.java 24654 2008-06-25 12:24:55Z rsauer $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr.jackrabbit;

import java.security.Principal;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.Query;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.SessionListener;
import org.apache.jackrabbit.core.security.SecurityConstants;
import org.apache.jackrabbit.core.security.principal.PrincipalImpl;


/**
 * @author rsauer
 * @version $Revision: 24654 $
 */
public class JcrVfsUserManager implements UserManager, SessionListener {

   private final Session securitySession;
   private final String adminId;

   public JcrVfsUserManager(Session securitySession, String adminId)
   {
      this.securitySession = securitySession;
      this.adminId = adminId;
   }

   public String getAdminId()
   {
      return adminId;
   }

   public Group createGroup(Principal arg0) throws AuthorizableExistsException,
         RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Group createGroup(Principal arg0, String arg1)
         throws AuthorizableExistsException, RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public User createUser(String userId, String password) throws AuthorizableExistsException,
         RepositoryException
   {
      return new JcrVfsUser(userId, new PrincipalImpl(userId), this.adminId.equals(userId));
   }

   public User createUser(String arg0, String arg1, Principal arg2, String arg3)
         throws AuthorizableExistsException, RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Iterator findAuthorizables(String arg0, String arg1) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Iterator findAuthorizables(String arg0, String arg1, int arg2)
         throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Authorizable getAuthorizable(String id) throws RepositoryException
   {
      // TODO query UserService and return IPP user here?
      // TODO what to pass as principal?
      return new JcrVfsUser(id, new PrincipalImpl(id), this.adminId.equals(id));
   }

   public Authorizable getAuthorizable(Principal principal) throws RepositoryException
   {
      // TODO query UserService and return IPP user here?

      // TODO is it correct?
      if (SecurityConstants.ADMINISTRATORS_NAME.equals(principal.getName()))
      {
         return new JcrVfsGroup(principal.getName(), principal);
      }
      else
      {
         return new JcrVfsUser(principal.getName(), principal, this.adminId.equals(principal.getName()));
      }
   }

   public void loggedOut(SessionImpl session)
   {
      // TODO react to loggedOut event (remove user from cache?)
   }

   public void loggingOut(SessionImpl session)
   {
      // TODO react to loggedOut event (remove user from cache?)
   }

   public Iterator<Authorizable> findAuthorizables(Query query)
         throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Group createGroup(String groupID) throws AuthorizableExistsException,
         RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Group createGroup(String groupID, Principal principal, String intermediatePath)
         throws AuthorizableExistsException, RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public boolean isAutoSave()
   {
      throw new RuntimeException("Not implemented yet");
   }

   public void autoSave(boolean enable) throws UnsupportedRepositoryOperationException,
         RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }
   
   public Authorizable getAuthorizableByPath(String path)
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

}
