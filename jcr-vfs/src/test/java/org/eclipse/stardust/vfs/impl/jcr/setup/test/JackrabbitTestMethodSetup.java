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

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.eclipse.stardust.vfs.impl.jcr.JcrDocumentRepositoryService;
import org.eclipse.stardust.vfs.jcr.ISessionFactory;
import org.junit.rules.ExternalResource;

public class JackrabbitTestMethodSetup extends ExternalResource
{
   private final IJackrabbitTest testObj;
   private final JackrabbitTestClassSetup testClassSetup;

   public JackrabbitTestMethodSetup(IJackrabbitTest testObj, JackrabbitTestClassSetup testClassSetup)
   {
      this.testObj = testObj;
      this.testClassSetup = testClassSetup;
   }

   @Override
   protected void before()
   {
      JcrDocumentRepositoryService jcrVfs = new JcrDocumentRepositoryService();
      jcrVfs.setSessionFactory(new ISessionFactory()
      {
         public Session getSession() throws RepositoryException
         {
            return testClassSetup.sessionWithAllPrivileges();
         }

         public void releaseSession(Session session)
         {}
      });
      jcrVfs.initializeRepository();

      testObj.setJcrVfsWithAllPrivileges(jcrVfs);

      JcrDocumentRepositoryService jcrVfs1 = new JcrDocumentRepositoryService();
      jcrVfs1.setSessionFactory(new ISessionFactory()
      {
         public Session getSession() throws RepositoryException
         {
            return testClassSetup.sessionWithReadPrivileges();
         }

         public void releaseSession(Session session)
         {}
      });
      jcrVfs1.initializeRepository();

      testObj.setJcrVfsWithReadPrivileges(jcrVfs1);
   }
}
