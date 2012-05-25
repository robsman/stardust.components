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
 * $Id: JcrRepositoryFolder.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr;

import java.util.List;

import org.eclipse.stardust.vfs.IFile;
import org.eclipse.stardust.vfs.IFolder;
import org.eclipse.stardust.vfs.impl.FolderContent;


/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class JcrRepositoryFolder extends JcrRepositoryResource implements IFolder
{

   private final int levelOfDetail;

   private FolderContent<JcrRepositoryFile> files = new FolderContent<JcrRepositoryFile>();

   private FolderContent<JcrRepositoryFolder> subFolders = new FolderContent<JcrRepositoryFolder>();

   public JcrRepositoryFolder(String id, String name, String path, int levelOfDetail)
   {
      super(id, name, path);

      this.levelOfDetail = levelOfDetail;
   }

   public int getLevelOfDetail()
   {
      return levelOfDetail;
   }

   public IFile getFile(int index)
   {
      return files.get(index);
   }

   public IFile getFile(String id)
   {
      return files.findById(id);
   }

   public IFile findFile(String name)
   {
      return files.findByName(name);
   }

   public int getFileCount()
   {
      return files.getSize();
   }

   public List<JcrRepositoryFile> getFiles()
   {
      return files.getAll();
   }

   void addFile(JcrRepositoryFile file)
   {
      files.add(file);
   }

   void removeFile(int index)
   {
      files.remove(index);
   }

   public IFolder getFolder(int index)
   {
      return subFolders.get(index);
   }

   public IFolder getFolder(String id)
   {
      return subFolders.findById(id);
   }

   public IFolder findFolder(String name)
   {
      return subFolders.findByName(name);
   }

   public int getFolderCount()
   {
      return subFolders.getSize();
   }

   public List<JcrRepositoryFolder> getFolders()
   {
      return subFolders.getAll();
   }

   void addFolder(JcrRepositoryFolder folder)
   {
      subFolders.add(folder);
   }

   void removeFolder(int index)
   {
      subFolders.remove(index);
   }

}
