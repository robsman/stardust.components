/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.vfs.impl.utils;

import javax.jcr.RepositoryException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.jackrabbit.core.jndi.RegistryHelper;

public class RepositoryHelper
{

   public static final Object DUMMY_INITIAL_CONTEXT_FACTORY_CLASS_NAME = org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory.class.getName();

   public static void unregisterRepository(InitialContext context, String jndiName) throws NamingException
   {

      RegistryHelper.unregisterRepository(context, jndiName);
   }

   public static void registerRepository(InitialContext context, String jndiName,
         String configFilePath, String actualWorkspace, boolean overwrite)
         throws NamingException, RepositoryException
   {

      RegistryHelper.registerRepository(context, jndiName, configFilePath,
            actualWorkspace, overwrite);
   }

}
