/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.vfs.impl.jcr.setup.test;

import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.jackrabbit.core.jndi.RegistryHelper;
import org.eclipse.stardust.vfs.impl.jcr.AuthorizableOrganizationDetails;
import org.eclipse.stardust.vfs.impl.spi.JcrRepository;
import org.eclipse.stardust.vfs.impl.spi.JcrSession;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;
import org.junit.rules.ExternalResource;
import org.springframework.core.io.ClassPathResource;

public class JackrabbitTestClassSetup extends ExternalResource
{
   private final String repoFile;
   private final String workspace;

   private InitialContext context;

   private Session sessionWithAllPrivileges;

   private Session sessionWithReadPrivileges;

   public JackrabbitTestClassSetup(String repoFile, String workspace)
   {
      this.repoFile = repoFile;
      this.workspace = workspace;
   }

   @Override
   protected void before() throws IOException, RepositoryException
   {
      Repository jcrRepository = connect();

      if (jcrRepository == null)
      {
         throw new NullPointerException();
      }

      // motu is administrator
      SimpleCredentials credentials = new SimpleCredentials("motu", "motu".toCharArray());
      // credentials.setAttribute(AuthorizableOrganizationDetails.DIRECT_GROUPS_ATT,
      // Collections.singleton(new
      // AuthorizableOrganizationDetails("{ipp.role}Administrator")));
      credentials.setAttribute(AuthorizableOrganizationDetails.DIRECT_GROUPS_ATT,
            Collections.singleton(new AuthorizableOrganizationDetails("administrators")));
      sessionWithAllPrivileges = JcrRepository.login(jcrRepository, credentials);

      // user1 is a normal user and can only read by default
      credentials = new SimpleCredentials("user1", "user1".toCharArray());
      AuthorizableOrganizationDetails group1level1 = new AuthorizableOrganizationDetails(
            "{ipp.organisation}Group_1_Level_1");
      AuthorizableOrganizationDetails group2level1 = new AuthorizableOrganizationDetails(
            "{ipp.organisation}Group_2_Level_1");
      AuthorizableOrganizationDetails group1level2 = new AuthorizableOrganizationDetails(
            "{ipp.role}Group_1_Level_2", Collections.singleton(group1level1));
      AuthorizableOrganizationDetails group2level2 = new AuthorizableOrganizationDetails(
            "{ipp.role}Group_2_Level_2", Collections.singleton(group2level1));
      Set<AuthorizableOrganizationDetails> directGroups = CollectionUtils.newSet();
      directGroups.add(group1level2);
      directGroups.add(group2level2);
      credentials.setAttribute(AuthorizableOrganizationDetails.DIRECT_GROUPS_ATT,
            directGroups);
      sessionWithReadPrivileges = JcrRepository.login(jcrRepository, credentials);
   }

   @Override
   protected void after()
   {
      if (sessionWithAllPrivileges != null)
      {
         JcrSession.logout(sessionWithAllPrivileges);
         sessionWithAllPrivileges = null;
      }

      if (sessionWithReadPrivileges != null)
      {
         JcrSession.logout(sessionWithReadPrivileges);
         sessionWithReadPrivileges = null;
      }

      if (context != null)
      {
         try
         {
            RegistryHelper.unregisterRepository(context, "repo");
         }
         catch (NamingException e)
         {
            throw new RuntimeException(e);
         }
         context = null;
      }
   }

   /* package-private */ Session sessionWithAllPrivileges()
   {
      return sessionWithAllPrivileges;
   }

   /* package-private */ Session sessionWithReadPrivileges()
   {
      return sessionWithReadPrivileges;
   }

   private Repository connect() throws IOException
   {
      Repository repository = null;

      final ClassPathResource cpRes = new ClassPathResource(repoFile);
      final String actualWorkspace;
      if (workspace != null)
      {
         actualWorkspace = workspace;
      }
      else
      {
         actualWorkspace = TempFolder.folder().getCanonicalPath();
      }

      Hashtable<Object, Object> env = new Hashtable<Object, Object>();
      env.put(Context.INITIAL_CONTEXT_FACTORY,
            org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory.class
                  .getName());

      env.put(Context.PROVIDER_URL, "localhost");

      try
      {
         context = new InitialContext(env);

         RegistryHelper.registerRepository(context, "repo", cpRes.getFile().getCanonicalPath(), actualWorkspace, true);

         repository = (Repository) context.lookup("repo");
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      catch (RepositoryException e)
      {
         throw new RuntimeException(e);
      }

      return repository;
   }
}
