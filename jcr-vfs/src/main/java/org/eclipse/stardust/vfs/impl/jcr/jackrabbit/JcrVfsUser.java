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
package org.eclipse.stardust.vfs.impl.jcr.jackrabbit;

import java.security.Principal;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.Impersonation;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.core.security.SecurityConstants;
import org.eclipse.stardust.vfs.impl.jcr.AuthorizableOrganizationDetails;
import org.eclipse.stardust.vfs.impl.jcr.JcrVfsPrincipal;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;


public class JcrVfsUser implements User
{

   private final String id;
   private final Principal principal;
   private final boolean isAdmin;

   public JcrVfsUser(String id, Principal principal, boolean isAdmin)
   {
      this.id = id;
      this.principal = principal;
      this.isAdmin = isAdmin;
   }

   public void changePassword(String arg0) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Credentials getCredentials() throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public Impersonation getImpersonation() throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public boolean isAdmin()
   {
      return this.isAdmin;
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
      return false;
   }

   public Iterator memberOf() throws RepositoryException
   {
      // TODO query UserService for group membership?
      if (this.isAdmin)
      {
         return Collections.singletonList(
               new JcrVfsGroup(SecurityConstants.ADMINISTRATORS_NAME, new JcrVfsPrincipal(
                     SecurityConstants.ADMINISTRATORS_NAME))).iterator();
      }
      else
      {
         if (this.principal instanceof JcrVfsPrincipal)
         {
            Set<Group> groups = CollectionUtils.newSet();
            Set<AuthorizableOrganizationDetails> directGroups = ((JcrVfsPrincipal)this.principal).getDirectGroups();
            if (directGroups != null)
            {
               for (AuthorizableOrganizationDetails o : directGroups)
               {
                  for (String groupId : o.getMemberOfGroupIds())
                  {
                     groups.add(new JcrVfsGroup(groupId, new JcrVfsPrincipal(groupId)));
                  }
               }
            }
            return groups.iterator();
         }
         else
         {
            return Collections.EMPTY_LIST.iterator();
         }
      }
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

   public void disable(String reason) throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public boolean isDisabled() throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

   public String getDisabledReason() throws RepositoryException
   {
      throw new RuntimeException("Not implemented yet");
   }

}
