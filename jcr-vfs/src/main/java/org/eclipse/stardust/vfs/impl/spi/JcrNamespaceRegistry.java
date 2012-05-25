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
 * $Id: JcrNamespaceRegistry.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.spi;

import javax.jcr.AccessDeniedException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class JcrNamespaceRegistry
{

   public static String getPrefix(NamespaceRegistry namespaceRegistry, String uri)
         throws NamespaceException, RepositoryException
   {
      return namespaceRegistry.getPrefix(uri);
   }

   public static String[] getURIs(NamespaceRegistry namespaceRegistry)
         throws RepositoryException
   {
      return namespaceRegistry.getURIs();
   }

   public static void registerNamespace(NamespaceRegistry namespaceRegistry,
         String prefix, String uri) throws NamespaceException,
         UnsupportedRepositoryOperationException, AccessDeniedException,
         RepositoryException
   {
      namespaceRegistry.registerNamespace(prefix, uri);
   }

}
