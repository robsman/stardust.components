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
 * $Id: TestJcrVfsAgainstSpringEmbeddedJackrabbit.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr.test;

import org.eclipse.stardust.vfs.IDocumentRepositoryService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"jcr-vfs-test-context.xml"})
@TransactionConfiguration(transactionManager="xaTransactionManager", defaultRollback=false)
@Transactional
public class TestJcrVfsAgainstSpringEmbeddedJackrabbit extends AbstractJcrVfsTest
{

   @Autowired
   @Override
   public void setJcrVfsWithAllPrivileges(IDocumentRepositoryService jcrVfs)
   {
      super.setJcrVfsWithAllPrivileges(jcrVfs);
   }
   

}
