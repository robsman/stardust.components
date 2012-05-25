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
 * $Id: IFolder.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs;

import java.util.List;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public interface IFolder extends IResource, IFolderInfo
{

   /**
    * @deprecated Replaced by {@link VfsUtils#REPOSITORY_ROOT}
    */
   String ID_REPOSITORY_ROOT = VfsUtils.REPOSITORY_ROOT;

   int LOD_NO_MEMBERS = 0;

   int LOD_LIST_MEMBERS = 1;

   int LOD_LIST_MEMBERS_OF_MEMBERS = 2;

   int getLevelOfDetail();

   int getFileCount();

   List<? extends IFile> getFiles();

   IFile getFile(int index);

   IFile getFile(String id);

   IFile findFile(String name);

   int getFolderCount();

   List<? extends IFolder> getFolders();

   IFolder getFolder(int index);

   IFolder getFolder(String id);

   IFolder findFolder(String name);

}
