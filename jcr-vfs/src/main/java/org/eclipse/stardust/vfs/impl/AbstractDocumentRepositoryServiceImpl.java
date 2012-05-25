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
 * $Id: AbstractDocumentRepositoryServiceImpl.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.vfs.*;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;


/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public abstract class AbstractDocumentRepositoryServiceImpl implements IDocumentRepositoryService
{

   ///////////////////////////////////////////////////////////////////////////////////////
   // File retrieval.
   ///////////////////////////////////////////////////////////////////////////////////////

   public IFile getFile(String fileId)
   {
      List<? extends IFile> result = getFiles(Collections.singletonList(fileId));

      return CollectionUtils.unwrapSingleton(result);
   }

   public byte[] retrieveFileContent(String fileId)
   {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try
      {
         retrieveFileContent(fileId, baos);
      }
      finally
      {
         try
         {
            baos.close();
         }
         catch (IOException e)
         {
            // safe to be ignored
         }
      }

      return baos.toByteArray();
   }

   public byte[] retrieveFileContent(IFile file)
   {
      return retrieveFileContent(file.getId());
   }

   public void retrieveFileContent(IFile file, OutputStream target)
   {
      retrieveFileContent(file.getId(), target);
   }

   /**
    * @deprecated Replaced by {@link #getFile(String)}
    */
   @Deprecated
   public final IFile getFileById(String fileId)
   {
      return getFile(fileId);
   }

   /**
    * @deprecated Replaced by {@link #getFile(String)}
    */
   @Deprecated
   public final IFile getFileByPath(String filePath)
   {
      return getFile(filePath);
   }

   /**
    * @deprecated Replaced by {@link #getFiles(List)}
    */
   @Deprecated
   public final List<? extends IFile> getFilesById(List<String> fileIds)
   {
      return getFiles(fileIds);
   }

   /**
    * @deprecated Replaced by {@link #getFiles(List)}
    */
   @Deprecated
   public final List<? extends IFile> getFilesByPath(List<String> filePaths)
   {
      return getFiles(filePaths);
   }

   ///////////////////////////////////////////////////////////////////////////////////////
   // Folder retrieval.
   ///////////////////////////////////////////////////////////////////////////////////////

   public IFolder getFolder(String folderId)
   {
      return getFolder(folderId, IFolder.LOD_LIST_MEMBERS);
   }

   public IFolder getFolder(String folderId, int levelOfDetail)
   {
      List<? extends IFolder> result = getFolders(Collections.singletonList(folderId),
            levelOfDetail);

      return CollectionUtils.unwrapSingleton(result);
   }

   /**
    * @deprecated Replaced by {@link #getFolder(String)}
    */
   @Deprecated
   public final IFolder getFolderById(String folderId)
   {
      return getFolder(folderId);
   }

   /**
    * @deprecated Replaced by {@link #getFolder(String, int)}
    */
   @Deprecated
   public final IFolder getFolderById(String folderId, int levelOfDetail)
   {
      return getFolder(folderId, levelOfDetail);
   }

   /**
    * @deprecated Replaced by {@link #getFolder(String)}
    */
   @Deprecated
   public final IFolder getFolderByPath(String folderPath)
   {
      return getFolder(folderPath);
   }

   /**
    * @deprecated Replaced by {@link #getFolder(String, int)}
    */
   @Deprecated
   public final IFolder getFolderByPath(String folderPath, int levelOfDetail)
   {
      return getFolder(folderPath, levelOfDetail);
   }

   /**
    * @deprecated Replaced by {@link #getFolders(String, int)}
    */
   @Deprecated
   public final List<? extends IFolder> getFoldersById(List<String> folderIds,
         int levelOfDetail)
   {
      return getFolders(folderIds, levelOfDetail);
   }

   /**
    * @deprecated Replaced by {@link #getFolders(String, int)}
    */
   @Deprecated
   public final List<? extends IFolder> getFoldersByPath(List<String> folderPaths,
         int levelOfDetail)
   {
      return getFolders(folderPaths, levelOfDetail);
   }

   // /////////////////////////////////////////////////////////////////////////////////////
   // File manipulation.
   ///////////////////////////////////////////////////////////////////////////////////////

   public IFile createFile(IFolder folder, IFileInfo file, byte[] content, String encoding)
   {
      return createFile(folder.getId(), file, content, encoding);
   }

   public IFile createFile(String folderId, IFileInfo file, byte[] content, String encoding)
   {
      final ByteArrayInputStream bais = (null != content)
            ? new ByteArrayInputStream(content)
            : null;

      try
      {
         return createFile(folderId, file, bais, encoding);
      }
      finally
      {
         try
         {
            if (null != bais)
            {
               bais.close();
            }
         }
         catch (IOException e)
         {
            // safe to be ignored
         }
      }
   }

   public IFile createFile(IFolder folder, IFileInfo file, String contentFileId)
   {
      return createFile(folder.getId(), file, contentFileId);
   }

   public IFile createFile(IFolder folder, IFileInfo file, InputStream content,
         String encoding)
   {
      return createFile(folder.getId(), file, content, encoding);
   }

   public IFile versionizeFile(String fileId)
   {
      return createFileVersion(fileId, null, false);
   }

   public IFile versionizeFile(IFile file)
   {
      return createFileVersion(file, null, false);
   }

   public IFile createFileVersion(IFile file, String versionLabel, boolean moveLabel)
   {
      return createFileVersion(file.getId(), versionLabel, moveLabel);
   }

   public IFile lockFile(IFile file)
   {
      return lockFile(file.getId());
   }

   public IFile unlockFile(IFile file)
   {
      return unlockFile(file.getId());
   }

   public IFile updateFile(IFile file, boolean version, boolean keepLocked)
   {
      return updateFile(file, version, null, keepLocked);
   }

   public IFile updateFile(IFile file, boolean version, String versionLabel, boolean keepLocked)
   {
      return updateFile(file, (InputStream) null, null, version, versionLabel, keepLocked);
   }

   public IFile updateFile(IFile file, byte[] content, String encoding,
         boolean version, boolean keepLocked)
   {
      return updateFile(file, content, encoding, version, null, keepLocked);
   }

   public IFile updateFile(IFile file, byte[] content, String encoding,
         boolean version, String versionLabel, boolean keepLocked)
   {
      final ByteArrayInputStream bais = (null != content)
            ? new ByteArrayInputStream(content)
            : null;

      try
      {
         return updateFile(file, bais, encoding, version, versionLabel, keepLocked);
      }
      finally
      {
         try
         {
            if (null != bais)
            {
               bais.close();
            }
         }
         catch (IOException e)
         {
            // safe to be ignored
         }
      }
   }

   public void removeFile(IFile file)
   {
      removeFile(file.getId());
   }

   ///////////////////////////////////////////////////////////////////////////////////////
   // Folder manipulation.
   ///////////////////////////////////////////////////////////////////////////////////////

   public IFolder createFolder(IFolder parentFolder, IFolderInfo folder)
   {
      return createFolder(parentFolder.getId(), folder);
   }

   public void removeFolder(IFolder folder, boolean recursively)
   {
      removeFolder(folder.getId(), recursively);
   }

   ///////////////////////////////////////////////////////////////////////////////////////
   // Configuration information.
   ///////////////////////////////////////////////////////////////////////////////////////

   public abstract MetaDataLocation getMetaDataLocation();
}
