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
 * $Id: JcrNode.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.spi;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class JcrNode
{

   public static PropertyIterator getProperties(Node node, String namePattern)
         throws RepositoryException
   {
      return node.getProperties(namePattern);
   }

   public static Node addNode(Node node, String relPath, String primaryNodeTypeName)
         throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException,
         LockException, VersionException, ConstraintViolationException,
         RepositoryException
   {
      return node.addNode(relPath, primaryNodeTypeName);
   }

   public static Property setProperty(Node node, String name, boolean value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      return node.setProperty(name, value);
   }

   public static Property setProperty(Node node, String name, Value[] value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      return node.setProperty(name, value);
   }

   public static Property setProperty(Node node, String name, long value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      return node.setProperty(name, value);
   }

   public static Property setProperty(Node node, String name, double value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      return node.setProperty(name, value);
   }

   public static Property setProperty(Node node, String name, BigDecimal value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      return node.setProperty(name, value);
   }

   public static Property setProperty(Node node, String name, Node value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      return node.setProperty(name, value);
   }

   public static Property setProperty(Node node, String name, String value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      return node.setProperty(name, value);
   }

   public static boolean isNodeType(Node node, String nodeTypeName)
         throws RepositoryException
   {
      return node.isNodeType(nodeTypeName);
   }

   public static void addMixin(Node node, String mixinName)
         throws NoSuchNodeTypeException, VersionException, ConstraintViolationException,
         LockException, RepositoryException
   {
      node.addMixin(mixinName);
   }

   public static Item getPrimaryItem(Node node) throws ItemNotFoundException,
         RepositoryException
   {
      return node.getPrimaryItem();
   }

   public static NodeType getPrimaryNodeType(Node node) throws RepositoryException
   {
      return node.getPrimaryNodeType();
   }

   public static boolean hasProperty(Node node, String relPath)
         throws RepositoryException
   {
      return node.hasProperty(relPath);
   }

   public static Property getProperty(Node node, String relPath)
         throws PathNotFoundException, RepositoryException
   {
      return node.getProperty(relPath);
   }

   public static Property setProperty(Node node, String name, Calendar value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      return node.setProperty(name, value);
   }

   public static boolean hasNode(Node node, String relPath) throws RepositoryException
   {
      return node.hasNode(relPath);
   }

   public static Node getNode(Node node, String relPath) throws PathNotFoundException,
         RepositoryException
   {
      return node.getNode(relPath);
   }

   public static NodeIterator getNodes(Node node, String relPath)
         throws PathNotFoundException, RepositoryException
   {
      return node.getNodes(relPath);
   }

   public static NodeIterator getNodes(Node node) throws RepositoryException
   {
      return node.getNodes();
   }

   @Deprecated
   public static String getUUID(Node node)
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
      return node.getUUID();
   }

   public static String getIdentifier(Node node)
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
      return node.getIdentifier();
   }

   public static Property setProperty(Node node, String name, Binary value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      return node.setProperty(name, value);
   }

   public static Version checkin(Node node) throws VersionException,
         UnsupportedRepositoryOperationException, InvalidItemStateException,
         LockException, RepositoryException
   {
     return node.getSession().getWorkspace().getVersionManager().checkin(node.getPath());
   }

   public static void checkout(Node node) throws UnsupportedRepositoryOperationException,
         LockException, RepositoryException
   {
      node.getSession().getWorkspace().getVersionManager().checkout(node.getPath());
   }

   public static boolean isCheckedOut(Node node) throws RepositoryException
   {
      return node.isCheckedOut();
   }

   public static VersionHistory getVersionHistory(Node node)
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
      return node.getSession().getWorkspace().getVersionManager().getVersionHistory(node.getPath());
   }

   public static Version getBaseVersion(Node node)
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
      return node.getSession().getWorkspace().getVersionManager().getBaseVersion(node.getPath());
   }

   public static void restore(Node node, Version version, boolean removeExisting)
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
      node.getSession()
            .getWorkspace()
            .getVersionManager()
            .restore(version, removeExisting);
   }

}
