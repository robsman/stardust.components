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
package org.eclipse.stardust.vfs.impl.jcr.test.migration;

import java.net.MalformedURLException;
import java.net.URL;
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
import org.apache.jackrabbit.rmi.repository.URLRemoteRepository;
import org.eclipse.stardust.vfs.impl.jcr.AuthorizableOrganizationDetails;
import org.eclipse.stardust.vfs.impl.jcr.JcrDocumentRepositoryService;
import org.eclipse.stardust.vfs.impl.spi.JcrRepository;
import org.eclipse.stardust.vfs.impl.spi.JcrSession;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;
import org.eclipse.stardust.vfs.jcr.ISessionFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;


/**
 * @author rsauer, roland.stamm
 * @version $Revision: 33391 $
 */
public class TestMigrationAgainstJackrabbit extends AbstractTestMigrationMetaData
{
   private static InitialContext context;

   private static Session sessionWithAllPrivileges;

   private static Session sessionWithReadPrivileges;

   @BeforeClass
   public static void setUpOnce() throws Exception
   {
      try
      {
         Repository jcrRepository = connect(new URL("http://localhost:9190/jackrabbit-webapp/rmi"));

         if (null != jcrRepository)
         {
            try
            {
               // motu is administrator
               SimpleCredentials credentials = new SimpleCredentials("motu", "motu".toCharArray());
               //credentials.setAttribute(AuthorizableOrganizationDetails.DIRECT_GROUPS_ATT, Collections.singleton(new AuthorizableOrganizationDetails("{ipp.role}Administrator")));
               credentials.setAttribute(AuthorizableOrganizationDetails.DIRECT_GROUPS_ATT, Collections.singleton(new AuthorizableOrganizationDetails("administrators")));
               sessionWithAllPrivileges = JcrRepository.login(jcrRepository, credentials);

               // user1 is a normal user and can only read by default
               credentials = new SimpleCredentials("user1", "user1".toCharArray());
               AuthorizableOrganizationDetails group1level1 = new AuthorizableOrganizationDetails("{ipp.organisation}Group_1_Level_1");
               AuthorizableOrganizationDetails group2level1 = new AuthorizableOrganizationDetails("{ipp.organisation}Group_2_Level_1");
               AuthorizableOrganizationDetails group1level2 = new AuthorizableOrganizationDetails("{ipp.role}Group_1_Level_2", Collections.singleton(group1level1));
               AuthorizableOrganizationDetails group2level2 = new AuthorizableOrganizationDetails("{ipp.role}Group_2_Level_2", Collections.singleton(group2level1));
               Set<AuthorizableOrganizationDetails> directGroups = CollectionUtils.newSet();
               directGroups.add(group1level2);
               directGroups.add(group2level2);
               credentials.setAttribute(AuthorizableOrganizationDetails.DIRECT_GROUPS_ATT, directGroups);
               sessionWithReadPrivileges = JcrRepository.login(jcrRepository, credentials);
            }
            catch (RepositoryException re)
            {
               throw new RuntimeException("Failed connecting to repository.", re);
            }
         }
      }
      catch (MalformedURLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   @AfterClass
   public static void tearDownOnce() throws Exception
   {
      if (null != sessionWithAllPrivileges)
      {
         JcrSession.logout(sessionWithAllPrivileges);
         sessionWithAllPrivileges = null;
      }

      if (null != sessionWithReadPrivileges)
      {
         JcrSession.logout(sessionWithReadPrivileges);
         sessionWithReadPrivileges = null;
      }

      if (null != context)
      {
         RegistryHelper.unregisterRepository(context, "repo");
         context = null;
      }
   }

   @Before
   public  void setUp() throws Exception
   {
      JcrDocumentRepositoryService jcrVfs = new JcrDocumentRepositoryService();
      jcrVfs.setSessionFactory(new ISessionFactory()
      {
         public Session getSession() throws RepositoryException
         {
            return sessionWithAllPrivileges;
         }

         public void releaseSession(Session session)
         {
         }
      });
      jcrVfs.initializeRepository();

      setJcrVfsWithAllPrivileges(jcrVfs);

      JcrDocumentRepositoryService jcrVfs1 = new JcrDocumentRepositoryService();
      jcrVfs1.setSessionFactory(new ISessionFactory()
      {
         public Session getSession() throws RepositoryException
         {
            return sessionWithReadPrivileges;
         }

         public void releaseSession(Session session)
         {
         }
      });
      jcrVfs1.initializeRepository();

      setJcrVfs1(jcrVfs1);
   }

   private static Repository connect(URL url)
   {
      boolean local = true;

      Repository repository = null;

      // TODO support alternative JCR implementations
      if (local)
      {
         String configFile = "src/test/config/repository-no-db.xml";
         String repHomeDir = "c:/tmp/test/migration/";

         Hashtable<Object, Object> env = new Hashtable<Object, Object>();
         env.put(
               Context.INITIAL_CONTEXT_FACTORY,
               org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory.class.getName());

         env.put(Context.PROVIDER_URL, "localhost");

         try
         {
            context = new InitialContext(env);

            RegistryHelper.registerRepository(context, "repo", configFile, repHomeDir, true);

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
      }
      else
      {
         repository = new URLRemoteRepository(url);
      }

      return repository;
   }

}
