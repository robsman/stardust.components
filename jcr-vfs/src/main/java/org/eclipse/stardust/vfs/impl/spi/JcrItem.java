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
 * $Id: JcrItem.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.spi;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class JcrItem
{

   public static String getName(Item item) throws RepositoryException
   {
      return item.getName();
   }

   public static String getPath(Node node) throws RepositoryException
   {
      return node.getPath();
   }

   public static void remove(Item item) throws VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      item.remove();
   }

   public static void save(Node node) throws AccessDeniedException, ItemExistsException,
         ConstraintViolationException, InvalidItemStateException,
         ReferentialIntegrityException, VersionException, LockException,
         NoSuchNodeTypeException, RepositoryException
   {
      node.getSession().save();
   }

   public static boolean isModified(Node node)
   {
      return node.isModified();
   }

   public static boolean isNew(Node node)
   {
      return node.isNew();
   }

   public static Node getParent(Node node) throws ItemNotFoundException,
         AccessDeniedException, RepositoryException
   {
      return node.getParent();
   }

}
