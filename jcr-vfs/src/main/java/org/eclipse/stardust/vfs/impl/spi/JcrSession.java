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
 * $Id: JcrSession.java 54136 2012-02-29 12:33:54Z sven.rottstock $
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
public class JcrSession
{

   public static Node getNodeByUUID(Session session, String uuid)
         throws ItemNotFoundException, RepositoryException
   {
      return session.getNodeByIdentifier(uuid);
   }

   public static Node getRootNode(Session session) throws RepositoryException
   {
      return session.getRootNode();
   }

   public static Workspace getWorkspace(Session session)
   {
      return session.getWorkspace();
   }

   public static void save(Session session) throws AccessDeniedException,
         ItemExistsException, ConstraintViolationException, InvalidItemStateException,
         VersionException, LockException, NoSuchNodeTypeException, RepositoryException
   {
      session.save();
   }

   public static void logout(Session session)
   {
      session.logout();
   }

   public static void move(Session session, String srcAbsPath, String destAbsPath)
         throws ItemExistsException, PathNotFoundException, VersionException,
         ConstraintViolationException, LockException, RepositoryException
   {
      session.move(srcAbsPath, destAbsPath);
   }

   public static boolean hasPendingChanges(Session session) throws RepositoryException
   {
      return session.hasPendingChanges();
   }

   public static void refresh(Session session, boolean keepChanges)
         throws RepositoryException
   {
      session.refresh(keepChanges);
   }

}
