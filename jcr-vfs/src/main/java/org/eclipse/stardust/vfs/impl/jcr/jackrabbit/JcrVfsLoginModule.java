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
import java.util.Set;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.security.auth.login.FailedLoginException;

import org.apache.jackrabbit.core.security.simple.SimpleLoginModule;
import org.eclipse.stardust.vfs.impl.jcr.AuthorizableOrganizationDetails;
import org.eclipse.stardust.vfs.impl.jcr.JcrVfsPrincipal;


/**
 * Extension of the SimpleLoginModule
 */
public class JcrVfsLoginModule extends SimpleLoginModule
{

   @SuppressWarnings("unchecked")
   @Override
   protected boolean authenticate(Principal principal, Credentials credentials)
         throws FailedLoginException, RepositoryException
   {
      boolean authenticated = super.authenticate(principal, credentials);

      if (credentials instanceof SimpleCredentials
            && principal instanceof JcrVfsPrincipal)
      {
         // transfer the organisation information to principal
         Set<AuthorizableOrganizationDetails> directGroups = (Set<AuthorizableOrganizationDetails>) 
            ((SimpleCredentials) credentials).getAttribute(AuthorizableOrganizationDetails.DIRECT_GROUPS_ATT);
         ((JcrVfsPrincipal) principal).setDirectGroups(directGroups);
      }

      return authenticated;
   }

}
