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
 * $Id: IDocumentRepositoryService.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public interface IDocumentRepositoryService
{

   /**
    * Retrieves the descriptor of the underlying repository.
    *
    * @return The repository descriptor.
    */
   IRepositoryDescriptor getRepositoryDescriptor();

   void initializeRepository();

   ///////////////////////////////////////////////////////////////////////////////////////
   // File retrieval.
   ///////////////////////////////////////////////////////////////////////////////////////

   IFile getFile(String fileId);

   List<? extends IFile> getFileVersions(String fileId);

   List<? extends IFile> getFiles(List<String> fileIds);

   List<? extends IFile> findFilesByName(String namePattern);

   List<? extends IFile> findFiles(String xpathQuery);

   IQueryResult findFiles(String xpathQuery, long limit, long offset);

   byte[] retrieveFileContent(String fileId);

   byte[] retrieveFileContent(IFile file);

   void retrieveFileContent(String fileId, OutputStream target);

   void retrieveFileContent(IFile file, OutputStream target);

   /**
    * @deprecated Replaced by {@link #getFile(String)}
    */
   @Deprecated
   IFile getFileById(String fileId);

   /**
    * @deprecated Replaced by {@link #getFiles(List)}
    */
   @Deprecated
   List<? extends IFile> getFilesById(List<String> fileIds);

   /**
    * @deprecated Replaced by {@link #getFile(String)}
    */
   @Deprecated
   IFile getFileByPath(String filePath);

   /**
    * @deprecated Replaced by {@link #getFiles(List)}
    */
   @Deprecated
   List<? extends IFile> getFilesByPath(List<String> filePaths);

   ///////////////////////////////////////////////////////////////////////////////////////
   // Folder retrieval.
   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Retrieves a folder and lists its members.
    *
    * @param folderId The ID or path expression identifying the folder to be retrieved.
    * @return The resolved folder.
    *
    * @see IFolder#LOD_LIST_MEMBERS
    */
   IFolder getFolder(String folderId);

   IFolder getFolder(String folderId, int levelOfDetail);

   List<? extends IFolder> getFolders(List<String> folderIds, int levelOfDetail);

   List<? extends IFolder> findFoldersByName(String namePattern, int levelOfDetail);

   List<? extends IFolder> findFolders(String xpathQuery, int levelOfDetail);

   /**
    * @deprecated Replaced by {@link #getFolder(String)}
    */
   @Deprecated
   IFolder getFolderById(String folderId);

   /**
    * @deprecated Replaced by {@link #getFolder(String, int)}
    */
   @Deprecated
   IFolder getFolderById(String folderId, int levelOfDetail);

   /**
    * @deprecated Replaced by {@link #getFolder(String)}
    */
   @Deprecated
   IFolder getFolderByPath(String folderPath);

   /**
    * @deprecated Replaced by {@link #getFolder(String, int)}
    */
   @Deprecated
   IFolder getFolderByPath(String folderPath, int levelOfDetail);

   /**
    * @deprecated Replaced by {@link #getFolder(String)}
    */
   @Deprecated
   List<? extends IFolder> getFoldersById(List<String> folderIds, int levelOfDetail);

   /**
    * @deprecated Replaced by {@link #getFolders(List, int)}
    */
   @Deprecated
   List<? extends IFolder> getFoldersByPath(List<String> folderPaths, int levelOfDetail);

   ///////////////////////////////////////////////////////////////////////////////////////
   // File manipulation.
   ///////////////////////////////////////////////////////////////////////////////////////

   IFile createFile(String folderId, IFileInfo file, byte[] content, String encoding);

   IFile createFile(IFolder folder, IFileInfo file, byte[] content, String encoding);

   IFile createFile(String folderId, IFileInfo file, InputStream content, String encoding);

   IFile createFile(IFolder folder, IFileInfo file, InputStream content, String encoding);

   IFile createFile(String folderId, IFileInfo file, String contentFileId);

   IFile createFile(IFolder folder, IFileInfo file, String contentFileId);

   /**
    * @deprecated Replaced by {@link #createFileVersion(String, String, String, boolean)}
    */
   IFile createFileVersion(String fileId, String versionLabel, boolean moveLabel);

   IFile createFileVersion(String fileId, String versionComment, String versionLabel, boolean moveLabel);

   /**
    * @deprecated Replaced by {@link #createFileVersion(IFile, String, String, boolean)}
    */
   IFile createFileVersion(IFile file, String versionLabel, boolean moveLabel);

   IFile createFileVersion(IFile file, String versionComment, String versionLabel, boolean moveLabel);

   void removeFileVersion(String fileId, String fileRevisionId);

   IFile lockFile(String fileId);

   IFile lockFile(IFile file);

   IFile unlockFile(String fileId);

   IFile unlockFile(IFile file);

   /**
    * @deprecated Replaced by {@link #updateFile(IFile, boolean, String, String, boolean)}
    */
   IFile updateFile(IFile file, boolean version, boolean keepLocked);

   /**
    * @deprecated Replaced by {@link #updateFile(IFile, boolean, String, String, boolean)}
    */
   IFile updateFile(IFile file, boolean version, String versionLabel, boolean keepLocked);

   IFile updateFile(IFile file, boolean version, String versionComment, String versionLabel, boolean keepLocked);

   /**
    * @deprecated Replaced by {@link #updateFile(IFile, byte[], String, boolean, String, String, boolean)}
    */
   IFile updateFile(IFile file, byte[] content, String encoding, boolean version,
         boolean keepLocked);
   /**
    * @deprecated Replaced by {@link #updateFile(IFile, byte[], String, boolean, String, String, boolean)}
    */
   IFile updateFile(IFile file, byte[] content, String encoding, boolean version, String versionLabel,
         boolean keepLocked);

   IFile updateFile(IFile file, byte[] content, String encoding, boolean version,
         String versionComment, String versionLabel, boolean keepLocked);

   /**
    * @deprecated Replaced by {@link #updateFile(IFile, InputStream, String, boolean, String, String, boolean)}
    */
   IFile updateFile(IFile file, InputStream content, String encoding, boolean version,
         boolean keepLocked);
   /**
    * @deprecated Replaced by {@link #updateFile(IFile, InputStream, String, boolean, String, String, boolean)}
    */
   IFile updateFile(IFile file, InputStream content, String encoding, boolean version, String versionLabel,
         boolean keepLocked);

   IFile updateFile(IFile file, InputStream content, String encoding, boolean version, String versionComment, String versionLabel,
         boolean keepLocked);

   /**
    * @deprecated Replaced by {@link #updateFile(IFile, String, boolean, String, String, boolean)}
    */
   IFile updateFile(IFile file, String contentFileId, boolean version,
         boolean keepLocked);
   /**
    * @deprecated Replaced by {@link #updateFile(IFile, String, boolean, String, String, boolean)}
    */
   IFile updateFile(IFile file, String contentFileId, boolean version, String versionLabel,
         boolean keepLocked);

   IFile updateFile(IFile file, String contentFileId, boolean version, String versionComment, String versionLabel,
         boolean keepLocked);

   IFile moveFile(String sourceFileId, String destinationFilePath, Map<? extends String, ? extends Serializable> properties);

   void removeFile(String fileId);

   void removeFile(IFile file);

   ///////////////////////////////////////////////////////////////////////////////////////
   // Folder manipulation.
   ///////////////////////////////////////////////////////////////////////////////////////

   IFolder createFolder(String parentFolderId, IFolderInfo folder);

   IFolder createFolder(IFolder parentFolder, IFolderInfo folder);

   IFolder updateFolder(IFolder folder);

   void removeFolder(String folderId, boolean recursive);

   void removeFolder(IFolder folder, boolean recursive);

   ///////////////////////////////////////////////////////////////////////////////////////
   // Security.
   ///////////////////////////////////////////////////////////////////////////////////////

   /**
    * Returns the privileges the session has for the resource denoted by
    * resourceId, which must exist.
    *
    * @param resourceId absolute path or ID of a file or folder
    * @return
    */
   Set<IPrivilege> getPrivileges(String resourceId);

   /**
    * Returns the IAccessControlPolicy objects that currently are in effect on
    * the resource denoted by resourceId (cumulated).
    *
    * Returned objects can not be modified, they represent a read-only view of
    * effective policies.
    *
    * @param resourceId absolute path or ID of a file or folder
    * @return
    */
   Set<IAccessControlPolicy> getEffectivePolicies(String resourceId);

   /**
    * Returns the IAccessControlPolicy objects that are currently set for
    * the resource denoted by resourceId.
    *
    * Returned objects can be changed, changes take effect after calling
    * setPolicy()
    *
    * @param resourceId absolute path or ID of a file or folder
    * @return
    */
   Set<IAccessControlPolicy> getPolicies(String resourceId);

   /**
    * Returns the IAccessControlPolicy objects that can be set for
    * the resource denoted by resourceId.
    *
    * Returned objects can be changed, and used as arguments to
    * setPolicy() in order to add a new policy.
    *
    * @param resourceId absolute path or ID of a file or folder
    * @return
    */
   Set<IAccessControlPolicy> getApplicablePolicies(String resourceId);

   /**
    * Returns the privilege object to be used in IAccessControlList.
    *
    * @param privilegeName one of the IPrivilege.*_PRIVILEGE constants
    * @return
    */
   IPrivilege getPrivilegeByName(String privilegeName);

   /**
    * Binds the policy to the resource denoted by resourceId (overwrites the old
    * version of the policy)
    *
    * If the policy does not contain any IAccessControlEntry then this policy is
    * removed from the resource.
    *
    * If the policy was obtained using getApplicablePolicies(), the policy will
    * be added, if it was obtained using getPolicies(), the policy will replace
    * its old version.
    *
    * @param resourceId absolute path or ID of a file or folder
    * @param policy
    */
   void setPolicy(String resourceId, IAccessControlPolicy policy);


   /**
    * Returns type of strategy that is used to store MetaData.<br>
    * Depending on the MetaData location different XPath queries have to be used. <p>
    * <code>MetaDataLocation.GLOBAL</code> is used in 1.6.2 or older versions of jcr-vfs<br>
    * <code>MetaDataLocation.LOCAL</code> is used in 1.6.3 or newer versions of jcr-vfs<br>
    * @return
    * @deprecated
    */
   MetaDataLocation getMetaDataLocation();

   /**
    * Migrates resources in the repository. This migration process works sequential from
    * the current version to the next higher version. The count of resources migrated in
    * one execution is limited by the parameter batchSize.<br>
    * Subsequent calls will migrate further resources if there are resources for migration
    * available. After all resources for the migration from one version to the next are
    * processed subsequent calls will start the migration to the next higher repository
    * structure version.<br>
    * The migration is complete if the current version of the repository reaches the
    * target version defined by the repository.
    * <p>
    * The MigrationReport returned by each call contains information about: Total
    * resources that need migration to the next version, resources already migrated,
    * current version, next version and target version of the repository structure.
    *
    * @param batchSize
    *           count of resources to be migrated in this call. A value of 0 will return a
    *           MigrationReport without migrating.
    * @param evaluateTotalCount
    *           if set to <code>true</code> the total count of resources that need processing in this
    *           migration step is evaluated. Setting this parameter to <code>false</code> saves
    *           performance.
    * @return a report containing information about the migration batch execution.
    */
   IMigrationReport migrateRepository(int batchSize, boolean evaluateTotalCount);

}
