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
 * $Id: AbstractJcrDocumentRepositoryService.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr;

import static org.eclipse.stardust.vfs.impl.utils.CollectionUtils.newList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.*;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.core.query.lucene.QueryResultImpl;
import org.eclipse.stardust.vfs.*;
import org.eclipse.stardust.vfs.impl.AbstractDocumentRepositoryServiceImpl;
import org.eclipse.stardust.vfs.impl.spi.JcrItem;
import org.eclipse.stardust.vfs.impl.spi.JcrNode;
import org.eclipse.stardust.vfs.impl.spi.JcrProperty;
import org.eclipse.stardust.vfs.impl.spi.JcrSession;
import org.eclipse.stardust.vfs.jcr.ISessionFactory;


/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public abstract class AbstractJcrDocumentRepositoryService extends AbstractDocumentRepositoryServiceImpl
{
   private String repositoryId;

   private String repositoryName;

   private String repositoryDescription;

   private JcrMigrationManager migrationManager;

   protected abstract ISessionFactory getSessionFactory();

   public String getRepositoryId()
   {
      return repositoryId;
   }

   public void setRepositoryId(String repositoryId)
   {
      this.repositoryId = repositoryId;
   }

   public String getRepositoryName()
   {
      return repositoryName;
   }

   public void setRepositoryName(String repositoryName)
   {
      this.repositoryName = repositoryName;
   }

   public String getRepositoryDescription()
   {
      return repositoryDescription;
   }

   public void setRepositoryDescription(String repositoryDescription)
   {
      this.repositoryDescription = repositoryDescription;
   }

   public IRepositoryDescriptor getRepositoryDescriptor()
   {
      return new IRepositoryDescriptor()
      {
         public String getId()
         {
            return getRepositoryId();
         }

         public String getName()
         {
            return getRepositoryName();
         }

         public String getDescription()
         {
            return getRepositoryDescription();
         }
      };
   }

   public void initializeRepository()
   {
      doWithJcrVfs(new JcrVfsProcedure()
      {
         public void withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            jcrVfs.ensureRepositoryIsInitialized();
         }
      });
   }

   ///////////////////////////////////////////////////////////////////////////////////////
   // File retrieval.
   ///////////////////////////////////////////////////////////////////////////////////////

   public List<? extends IFile> getFiles(final List<String> fileIds)
   {
      return doWithJcrVfs(new JcrVfsFunction<List<? extends IFile>>()
      {
         public List<? extends IFile> withJcrVfs(JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            List<IFile> result = newList();
            for (String fileId : fileIds)
            {
               // TODO handle errors
               Node nFile = jcrVfs.findFile(fileId);
               if (null != nFile)
               {
                  result.add(jcrVfs.getFileSnapshot(nFile));
               }
               else
               {
                  // TODO throw exception
               }
            }

            return result;
         }
      });
   }

   public List<? extends IFile> getFileVersions(final String fileId)
   {
      return doWithJcrVfs(new JcrVfsFunction<List<? extends IFile>>()
      {
         public List<? extends IFile> withJcrVfs(JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            final List<IFile> result = newList();

            List<Node> fileVersions = jcrVfs.findFileVersions(fileId);

            for (Node fileVersion : fileVersions)
            {
               result.add(jcrVfs.getFileSnapshot(fileVersion));
            }

            return result;
         }
      });
   }

   public List<? extends IFile> findFilesByName(final String namePattern)
   {
      return doWithJcrVfs(new JcrVfsFunction<List<JcrRepositoryFile>>()
      {
         public List<JcrRepositoryFile> withJcrVfs(final JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            final List<JcrRepositoryFile> result = newList();

            NodeIterator nodes = jcrVfs.findNodesByName(namePattern);

            jcrVfs.visitMembers(nodes, new FsNodeVisitorAdapter()
            {
               @Override
               public void visitFile(Node nFile) throws RepositoryException
               {
                  result.add(jcrVfs.getFileSnapshot(nFile));
               }
            });

            return result;
         }
      });
   }


   public IQueryResult findFiles(final String xpathQuery, final long limit, final long offset)
   {
      return (IQueryResult) findResources(xpathQuery, limit, offset, true, false, 0);
   }


   public IResourceQueryResult findResources(final String xpathQuery, final long limit,
         final long offset, final boolean includeFiles, final boolean includeFolders, final int folderLevelOfDetail)
   {
      return doWithJcrVfs(new JcrVfsFunction<IResourceQueryResult>()
      {
         public IResourceQueryResult withJcrVfs(final JcrVfsOperations jcrVfs)
               throws RepositoryException
         {

            QueryResult queryResult = jcrVfs.queryByXPath(xpathQuery, limit, offset);

            NodeIterator nodes = queryResult.getNodes();

            final List<IResource> results = newList();
            jcrVfs.visitMembers(nodes, new FsNodeVisitorAdapter()
            {
               @Override
               public void visitFile(Node nFile) throws RepositoryException
               {
                  if (includeFiles)
                  {
                     results.add(jcrVfs.getFileSnapshot(nFile));
                  }
               }

               @Override
               public void visitFolder(Node nFolder) throws RepositoryException
               {
                  if (includeFolders)
                  {
                  results.add(jcrVfs.getFolderSnapshot(nFolder, folderLevelOfDetail));
                  }
               }

            });

            long totalSize = -1;
            if (queryResult instanceof QueryResultImpl)
            {
               totalSize = ((QueryResultImpl) queryResult).getTotalSize();
            }

            if (includeFiles && includeFolders)
            {
               return new JcrRepositoryResourceQueryResult(results, totalSize);
            }
            else if (includeFiles)
            {
               List<IFile> files = new LinkedList<IFile>();
               for (IResource res : results)
               {
                  if (res instanceof IFile)
                  {
                     files.add((IFile) res);
                  }
               }
               return new JcrRepositoryFileQueryResult(files, totalSize);
            }
            else
            {
               // no special case for folders implemented yet
               return new JcrRepositoryResourceQueryResult(results, totalSize);
            }
         }
      });
   }


   public List<? extends IFile> findFiles(final String xpathQuery)
   {
      return doWithJcrVfs(new JcrVfsFunction<List<JcrRepositoryFile>>()
      {
         public List<JcrRepositoryFile> withJcrVfs(final JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            final List<JcrRepositoryFile> result = newList();

            NodeIterator nodes = jcrVfs.findNodesByXPath(xpathQuery);

            jcrVfs.visitMembers(nodes, new FsNodeVisitorAdapter()
            {
               @Override
               public void visitFile(Node nFile) throws RepositoryException
               {
                  result.add(jcrVfs.getFileSnapshot(nFile));
               }
            });

            return result;
         }
      });
   }

   public void retrieveFileContent(final String fileId, final OutputStream target)
   {
      doWithJcrVfs(new JcrVfsProcedure()
      {
         public void withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            Node nFile = jcrVfs.findFile(fileId);

            if (null != nFile)
            {
               final Node nFileContent = jcrVfs.getContentNode(nFile);
               final Property prpContent = (null != nFileContent)
                     ? JcrNode.getProperty(nFileContent, JcrProperties.JCR_DATA)
                     : null;
               if (null != prpContent)
               {
                  InputStream contentStream = JcrProperty.getBinary(prpContent) != null? JcrProperty.getBinary(prpContent).getStream():new ByteArrayInputStream(new byte[0]);
                  try
                  {
                     byte[] buffer = new byte[4096];
                     try
                     {
                        int nBytesCopied = contentStream.read(buffer);
                        while (0 < nBytesCopied)
                        {
                           target.write(buffer, 0, nBytesCopied);

                           nBytesCopied = contentStream.read(buffer);
                        }
                     }
                     catch (IOException ioe)
                     {
                        // TODO
                        throw new RepositoryOperationFailedException(
                              "Failed retrieving file content.", ioe);
                     }
                  }
                  finally
                  {
                     if (null != contentStream)
                     {
                        try
                        {
                           contentStream.close();
                        }
                        catch (IOException e)
                        {
                           // TODO Auto-generated catch block
                           e.printStackTrace();
                        }
                     }
                  }
               }
            }
         }
      });
   }

   // /////////////////////////////////////////////////////////////////////////////////////
   // Folder retrieval.
   // /////////////////////////////////////////////////////////////////////////////////////

   public List<JcrRepositoryFolder> getFolders(final List<String> folderIds,
         final int levelOfDetail)
   {
      return doWithJcrVfs(new JcrVfsFunction<List<JcrRepositoryFolder>>()
      {
         public List<JcrRepositoryFolder> withJcrVfs(JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            List<JcrRepositoryFolder> result = newList();
            for (String folderId : folderIds)
            {
               // TODO handle errors
               Node nFolder = jcrVfs.findFolder(folderId);
               result.add((null != nFolder) //
                     ? jcrVfs.getFolderSnapshot(nFolder, levelOfDetail)
                     : null);
            }

            return result;
         }
      });
   }

   public List<? extends IFolder> findFoldersByName(final String namePattern,
         final int levelOfDetail)
   {
      return doWithJcrVfs(new JcrVfsFunction<List<JcrRepositoryFolder>>()
      {
         public List<JcrRepositoryFolder> withJcrVfs(final JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            final List<JcrRepositoryFolder> result = newList();

            NodeIterator nodes = jcrVfs.findNodesByName(namePattern);

            jcrVfs.visitMembers(nodes, new FsNodeVisitorAdapter()
            {
               @Override
               public void visitFolder(Node nFolder) throws RepositoryException
               {
                  result.add(jcrVfs.getFolderSnapshot(nFolder, levelOfDetail));
               }
            });

            return result;
         }
      });
   }

   public List<JcrRepositoryFolder> findFolders(final String xpathQuery,
         final int levelOfDetail)
   {
      return doWithJcrVfs(new JcrVfsFunction<List<JcrRepositoryFolder>>()
      {
         public List<JcrRepositoryFolder> withJcrVfs(final JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            final List<JcrRepositoryFolder> result = newList();

            NodeIterator nodes = jcrVfs.findNodesByXPath(xpathQuery);

            jcrVfs.visitMembers(nodes, new FsNodeVisitorAdapter()
            {
               @Override
               public void visitFolder(Node nFolder) throws RepositoryException
               {
                  result.add(jcrVfs.getFolderSnapshot(nFolder, levelOfDetail));
               }
            });

            return result;
         }
      });
   }

   ///////////////////////////////////////////////////////////////////////////////////////
   // File manipulation.
   ///////////////////////////////////////////////////////////////////////////////////////

   public IFile createFile(final String folderId, final IFileInfo file,
         final InputStream content, final String encoding)
   {
      return doWithJcrVfs(new JcrVfsFunction<IFile>()
      {
         public IFile withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            Node nFolder = jcrVfs.findFolder(folderId);
            if (null == nFolder)
            {
               throw new RepositoryOperationFailedException("The folder does not exist.");
            }

            Node nFile = jcrVfs.addFile(nFolder, file, content, encoding);

            // TODO
            return jcrVfs.getFileSnapshot(nFile);
         }
      });
   }

   public IFile createFile(final String folderId, final IFileInfo file, final String contentFileId)
   {
      return doWithJcrVfs(new JcrVfsFunction<IFile>()
            {
               public IFile withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
               {
                  Node nFolder = jcrVfs.findFolder(folderId);
                  if (null == nFolder)
                  {
                     throw new RepositoryOperationFailedException("The folder does not exist.");
                  }

                  Node nFile = jcrVfs.addFile(nFolder, file, contentFileId);

                  // TODO
                  return jcrVfs.getFileSnapshot(nFile);
               }
            });

   }

   public IFile createFileVersion(final String fileId, final String versionLabel,
         final boolean moveLabel)
   {
      return doWithJcrVfs(new JcrVfsFunction<IFile>()
      {
         public IFile withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            Node nFile = jcrVfs.findFile(fileId);

            // TODO if target is already a version, just apply label

            jcrVfs.ensureVersioningIsEnabled(nFile);
            JcrVfsOperations.ensureVersionIsModifiable(nFile);

            jcrVfs.createFrozenVersion(nFile, versionLabel, moveLabel);

            // TODO
            return jcrVfs.getFileSnapshot(nFile);
         }
      });
   }

   public IFile lockFile(String fileId)
   {
      // TODO implement

      throw new UnsupportedOperationException("Locking is not yet implemented.");
   }

   public IFile unlockFile(String fileId)
   {
      // TODO implement

      throw new UnsupportedOperationException("Locking is not yet implemented.");
   }

   public IFile updateFile(final IFile file, final InputStream content,
         final String encoding, final boolean version, final boolean keepLocked)
   {
      return updateFile(file, content, encoding, version, null, keepLocked);
   }


   public IFile updateFile(final IFile file, final InputStream content,
         final String encoding, final boolean version, final String versionLabel, final boolean keepLocked)
   {
      // TODO implement fully
      if (keepLocked)
      {
         throw new UnsupportedOperationException("Locking is not yet implemented.");
      }

      return doWithJcrVfs(new JcrVfsFunction<IFile>()
      {
         public IFile withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            Node nFile = jcrVfs.findFile(file.getId());

            if (version)
            {
               jcrVfs.ensureVersioningIsEnabled(nFile);
            }
            JcrVfsOperations.ensureVersionIsModifiable(nFile);
            jcrVfs.updateFile(nFile, file, content, encoding);

            if (version)
            {
               jcrVfs.createFrozenVersion(nFile, versionLabel, false);
            }

            // TODO
            return jcrVfs.getFileSnapshot(nFile);
         }
      });
   }

   public IFile updateFile(final IFile file, final String contentFileId,
         final boolean version, boolean keepLocked)
   {
      return updateFile(file, contentFileId, version, null, keepLocked);
   }

   public IFile updateFile(final IFile file, final String contentFileId, final boolean version, final String versionLabel, boolean keepLocked)
   {
      // TODO implement fully
      if (keepLocked)
      {
         throw new UnsupportedOperationException("Locking is not yet implemented.");
      }

      return doWithJcrVfs(new JcrVfsFunction<IFile>()
      {
         public IFile withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            Node nFile = jcrVfs.findFile(file.getId());

            if (version)
            {
               jcrVfs.ensureVersioningIsEnabled(nFile);
            }
            JcrVfsOperations.ensureVersionIsModifiable(nFile);

            jcrVfs.updateFile(nFile, file, contentFileId);

            if (version)
            {
               jcrVfs.createFrozenVersion(nFile, versionLabel, false);
            }

            // TODO
            return jcrVfs.getFileSnapshot(nFile);
         }
      });
   }

   public void updateResourceInfos(final List<? extends IResource> resources)
   {
      doWithJcrVfs(new JcrVfsProcedure()
      {
         public void withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            for (IResource resource : resources)
            {
               if (resource instanceof IFile)
               {
                  Node nFile = jcrVfs.findFile(resource.getId());
                  jcrVfs.updateFile(nFile, (IFile) resource, null, null);
               }
               else if (resource instanceof IFolder)
               {
                  Node nFolder = jcrVfs.findFolder(resource.getId());
                  jcrVfs.updateFolder(nFolder, (IFolder) resource);
               }
            }
         }
      });
   }

   public IFile moveFile(final String sourceFileId, final String destinationFilePath, final Map<? extends String, ? extends Serializable> properties)
   {
      return doWithJcrVfs(new JcrVfsFunction<IFile>()
            {
               @SuppressWarnings("unchecked")
               public IFile withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
               {
                  Node nSourceFile = jcrVfs.findFile(sourceFileId);

                  JcrSession.move(nSourceFile.getSession(), JcrItem.getPath(nSourceFile), destinationFilePath);

                  Node nDestinationFile = jcrVfs.findFile(destinationFilePath);
                  // when properties==null -> reuse properties of the sourceFile
                  if (properties != null)
                  {
                     // otherwise replace the properties
                     final Node nMetaData = jcrVfs.getMetaDataNode(nDestinationFile);
                     JcrVfsOperations.ensureVersionIsModifiable(nMetaData);
                     final Node nVfsAttributes = jcrVfs.getVfsAttributesNode(nMetaData, VfsUtils.VFS_ATTRIBUTES);
                     jcrVfs.updateAttributesNode(nVfsAttributes, (Map<String, Serializable>) properties);
                  }

                  // TODO
                  return jcrVfs.getFileSnapshot(nDestinationFile);
               }
            });
   }

   public void removeFile(final String fileId)
   {
      doWithJcrVfs(new JcrVfsProcedure()
      {
         public void withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            doRemoveFile(jcrVfs, fileId);
         }
      });
   }

   private void doRemoveFile(JcrVfsOperations jcrVfs, String fileId)
         throws RepositoryException
   {
      Node nFile = jcrVfs.findFile(fileId);

      if (null != nFile)
      {
         JcrVfsOperations.ensureVersionIsModifiable(nFile);

         jcrVfs.removeHierarchyNode(nFile);
      }
   }

   ///////////////////////////////////////////////////////////////////////////////////////
   // Folder manipulation.
   ///////////////////////////////////////////////////////////////////////////////////////

   public IFolder createFolder(final String parentFolderId, final IFolderInfo folder)
   {
      return doWithJcrVfs(new JcrVfsFunction<IFolder>()
      {
         public IFolder withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            Node nParentFolder = jcrVfs.findFolder(parentFolderId);
            if (null == nParentFolder)
            {
               throw new RepositoryOperationFailedException("The parent folder does not exist.");
            }

            Node nFolder = jcrVfs.createFolder(nParentFolder, folder);

            return jcrVfs.getFolderSnapshot(nFolder, IFolder.LOD_LIST_MEMBERS);
         }
      });
   }

   public IFolder updateFolder(final IFolder folder)
   {
      return doWithJcrVfs(new JcrVfsFunction<IFolder>()
      {
         public IFolder withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            Node nFolder = jcrVfs.findFolder(folder.getId());
            if (null == nFolder)
            {
               throw new RepositoryOperationFailedException("The folder does not exist.");
            }

            jcrVfs.updateFolder(nFolder, folder);

            return jcrVfs.getFolderSnapshot(nFolder, IFolder.LOD_NO_MEMBERS);
         }
      });
   }

   public void removeFolder(final String folderId, final boolean recursively)
   {
      doWithJcrVfs(new JcrVfsProcedure()
      {
         public void withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            Node nFolder = jcrVfs.findFolder(folderId);

            if (null != nFolder)
            {
               IFolder folder = jcrVfs.getFolderSnapshot(nFolder, IFolder.LOD_LIST_MEMBERS);

               if ( !recursively)
               {
                  // check folder is empty
                  if ( !folder.getFiles().isEmpty())
                  {
                     throw new RepositoryOperationFailedException(
                           "Failed removing the folder as is still contains files.");
                  }
                  if ( !folder.getFolders().isEmpty())
                  {
                     throw new RepositoryOperationFailedException(
                           "Failed removing the folder as is still contains subfolders.");
                  }
               }

               doRemoveFolder(jcrVfs, folder.getId());
            }
         }
      });
   }

   ///////////////////////////////////////////////////////////////////////////////////////
   // Security.
   ///////////////////////////////////////////////////////////////////////////////////////

   public Set<IPrivilege> getPrivileges(final String resourceId)
   {
      return doWithJcrVfs(new JcrVfsFunction<Set<IPrivilege>>()
      {
         public Set<IPrivilege> withJcrVfs(JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            Node node = jcrVfs.findNode(resourceId);

            if (null != node)
            {
               return JcrVfsOperations.getNodePrivileges(node);
            }
            else
            {
               throw new AccessControlException("Resource '" + resourceId
                     + "' does not exist.");
            }
         }
      });
   }

   public Set<IAccessControlPolicy> getEffectivePolicies(final String resourceId)
   {
      return doWithJcrVfs(new JcrVfsFunction<Set<IAccessControlPolicy>>()
      {
         public Set<IAccessControlPolicy> withJcrVfs(JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            Node node = jcrVfs.findNode(resourceId);

            if (null != node)
            {
               return JcrVfsOperations.getNodeEffectivePolicies(node);
            }
            else
            {
               throw new AccessControlException("Resource '" + resourceId
                     + "' does not exist.");
            }
         }
      });
   }

   public Set<IAccessControlPolicy> getPolicies(final String resourceId)
   {
      return doWithJcrVfs(new JcrVfsFunction<Set<IAccessControlPolicy>>()
      {
         public Set<IAccessControlPolicy> withJcrVfs(JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            try
            {
               Node node = jcrVfs.findNode(resourceId);

               if (null != node)
               {
                  return JcrVfsOperations.getNodePolicies(node);
               }
               else
               {
                  throw new AccessControlException("Resource '" + resourceId
                        + "' does not exist.");
               }
            }
            catch(PathNotFoundException e)
            {
               throw new AccessControlException("Resource '" + resourceId
                     + "' does not exist.");
            }
         }
      });
   }

   public Set<IAccessControlPolicy> getApplicablePolicies(final String resourceId)
   {
      return doWithJcrVfs(new JcrVfsFunction<Set<IAccessControlPolicy>>()
      {
         public Set<IAccessControlPolicy> withJcrVfs(JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            try
            {
               Node node = jcrVfs.findNode(resourceId);

               if (null != node)
               {
                  return JcrVfsOperations.getNodeApplicablePolicies(node);
               }
               else
               {
                  throw new AccessControlException("Resource '" + resourceId
                        + "' does not exist.");
               }
            }
            catch(PathNotFoundException e)
            {
               throw new AccessControlException("Resource '" + resourceId
                     + "' does not exist.");
            }
         }
      });
   }

   public IPrivilege getPrivilegeByName(final String privilegeName)
   {
      return doWithJcrVfs(new JcrVfsFunction<IPrivilege>()
            {
               public IPrivilege withJcrVfs(JcrVfsOperations jcrVfs)
                     throws RepositoryException
               {
                  return jcrVfs.getPrivilegeByName(privilegeName);
               }
            });
   }

   public void setPolicy(final String resourceId, final IAccessControlPolicy policy)
   {
      doWithJcrVfs(new JcrVfsProcedure()
      {
         public void withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            Node node = jcrVfs.findNode(resourceId);

            if (null != node)
            {
               jcrVfs.setNodePolicy(node, policy);
            }
            else
            {
               throw new AccessControlException("Resource '" + resourceId
                     + "' does not exist.");
            }
         }
      });
   }

   public MetaDataLocation getMetaDataLocation()
   {
      return doWithJcrVfs(new JcrVfsFunction<MetaDataLocation>()
      {
         public MetaDataLocation withJcrVfs(JcrVfsOperations jcrVfs)
               throws RepositoryException
         {
            return jcrVfs.getMetaDataLocation();
         }
      });
   }

   public IMigrationReport migrateRepository(final int batchSize, final boolean evaluateTotalCount)
   {
      return doWithJcrVfs(new JcrVfsFunction<IMigrationReport>()
            {
               public IMigrationReport withJcrVfs(JcrVfsOperations jcrVfs)
                     throws RepositoryException
               {
                  return getMigrationManager(jcrVfs).processBatch(batchSize, evaluateTotalCount);
               }
            });
   }

   public void removeFileVersion(final String fileId, final String fileRevisionId)
   {
      doWithJcrVfs(new JcrVfsProcedure()
      {
         public void withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            Node nFile = jcrVfs.findFile(fileId);

            if (null != nFile)
            {
               if (JcrNode.getBaseVersion(nFile)
                     .getIdentifier()
                     .equals(JcrVfsOperations.getRevisionUuidFromId(fileRevisionId)))
               {
                  // If the baseVersion(latest) is being deleted, and there are other
                  // versions,
                  // restore to a previous version.
                  jcrVfs.restoreNodeVersion(nFile);
               }
               else
               {
                  // Else remove the specified version.
                  jcrVfs.removeNodeVersionByRevision(nFile, fileRevisionId);
               }
            }
//            else
//            {
//               throw new ItemNotFoundException("Item not found " + fileId);
//            }
         }
      });
   }

   protected JcrMigrationManager getMigrationManager(JcrVfsOperations jcrVfs)
   {
      if (this.migrationManager == null)
      {
         this.migrationManager = new JcrMigrationManager();
      }
      this.migrationManager.setJcrVfsOperations(jcrVfs);
      return this.migrationManager;
   }

   private void doRemoveFolder(JcrVfsOperations jcrVfs, final String folderId)
         throws RepositoryException
   {
      Node nFolder = jcrVfs.findFolder(folderId);

      if (null != nFolder)
      {
         IFolder folder = jcrVfs.getFolderSnapshot(nFolder, IFolder.LOD_LIST_MEMBERS);

         for (IFile file : folder.getFiles())
         {
            doRemoveFile(jcrVfs, file.getId());
         }
         for (IFolder subfolder : folder.getFolders())
         {
            doRemoveFolder(jcrVfs, subfolder.getId());
         }

         jcrVfs.removeHierarchyNode(nFolder);
      }
   }

   public <R> R doWithJcrVfs(JcrVfsFunction<R> jcrVfsFunction)
   {
      final ISessionFactory sessionFactory = getSessionFactory();

      if (null == sessionFactory)
      {
         throw new RepositoryOperationFailedException("Missing JCR session factory.");
      }

      Session jcrSession = null;
      try
      {
         jcrSession = sessionFactory.getSession();

         final JcrVfsOperations jcrVfs = new JcrVfsOperations(jcrSession);

         jcrVfs.ensureVfsNamespace();

         R result = jcrVfsFunction.withJcrVfs(jcrVfs);

         if (JcrSession.hasPendingChanges(jcrSession))
         {
            JcrSession.save(jcrSession);
         }
         return result;

      }
      catch (RepositoryException re)
      {
         throw new RepositoryOperationFailedException(re);
      }
      finally
      {
         sessionFactory.releaseSession(jcrSession);
      }
   }

   public void doWithJcrVfs(final JcrVfsProcedure jcrVfsProcedure)
   {
      doWithJcrVfs(new JcrVfsFunction<Void>()
      {
         public Void withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException
         {
            jcrVfsProcedure.withJcrVfs(jcrVfs);

            return null;
         }
      });
   }

   public static interface JcrVfsFunction<R>
   {
      R withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException;
   }

   public static interface JcrVfsProcedure
   {
      void withJcrVfs(JcrVfsOperations jcrVfs) throws RepositoryException;
   }

}
