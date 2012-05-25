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
package org.eclipse.stardust.vfs.impl.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.stardust.vfs.IDocumentRepositoryService;
import org.eclipse.stardust.vfs.IFile;
import org.eclipse.stardust.vfs.IFolder;
import org.eclipse.stardust.vfs.IResource;
import org.eclipse.stardust.vfs.IResourceQueryResult;
import org.eclipse.stardust.vfs.impl.jcr.AbstractJcrDocumentRepositoryService;


/**
 * The MetaDataMigrationJob can be used to migrate file and folder global meta data to
 * local meta data. Global meta data storage structure was used in jcr-vfs-1.6.2 and
 * earlier. Since the introduction of the much better performing local meta data structure
 * in jcr-vfs-1.6.3 global meta data is deprecated.
 *
 * @author roland.stamm
 * @deprecated Use IDocumentRepository#migrateRepository(int) instead.
 * @see IDocumentRepository#migrateRepository(int)
 */
public class MetaDataMigrationJob
{
   /**
    * This interface is used to provide information for logging and progress calculation
    * on the migration job.
    *
    * @author roland.stamm
    *
    */
   public interface IMigrationInformationCallback
   {
      /**
       * This is called after the commit of a processed the folder.
       *
       * @param path
       *           path of the migrated folder.
       */
      void logMigratedFolderPath(String path);

      /**
       * This is called after the commit of a processed file.
       *
       * @param path
       *           path of the migrated file.
       */
      void logMigratedFilePath(String path);

      /**
       * This is called when the migration job starts.
       *
       * @param totalResources
       *           count of the total files and folders in need of processing.
       */
      void logMigrationStart(long totalResources);

      /**
       * This is called after each batch is executed.
       *
       * @param processedResources
       *           count of the already processed files and folders.
       */
      void logMigratedBatch(long processedResources);

      /**
       * This is called when the migration job finishes successfully.
       */
      void logMigrationDone();
   }

   private final IDocumentRepositoryService jcrVfs;

   private final IMigrationInformationCallback infoCallback;

   /**
    * @param jcrVfs
    *           an document repository instance with proper read write access permissions.
    */
   public MetaDataMigrationJob(IDocumentRepositoryService jcrVfs)
   {
      this.jcrVfs = jcrVfs;
      this.infoCallback = null;
   }

   /**
    * @param jcrVfs
    *           an document repository instance with proper read write access permissions.
    * @param infoCallback
    *           a callback that provides progress and logging information about the
    *           migration job.
    */
   public MetaDataMigrationJob(IDocumentRepositoryService jcrVfs,
         IMigrationInformationCallback infoCallback)
   {
      this.jcrVfs = jcrVfs;
      this.infoCallback = infoCallback;
   }

   /**
    * Migrates meta data for a single file.
    *
    * @param fileId
    *           the path or id of the file to migrate.
    */
   public void migrateFile(String fileId)
   {
      IFile file = jcrVfs.getFile(fileId);
      doDummyUpdateOnFile(file);
   }

   /**
    * Migrates meta data for a single folder.
    *
    * @param folderId
    *           the path or id of a folder to migrate
    */
   public void migrateFolder(String folderId)
   {
      IFolder folder = jcrVfs.getFolder(folderId);
      doDummyUpdateOnFolder(folder);
   }

   /**
    * Migrates the whole repository's meta data. Only current head versions of files can
    * be processed since versions in the version store are not modifiable.
    *
    * @param batchsize
    *           the count of files and folders which should be processed in one batch.
    * @param batchCommit
    *           if <code>true</code> the session commit is done after the batch is
    *           processed.<br>
    *           This is much faster but has race conditions if there is concurrent access
    *           on the repository.<br>
    *           if <code>false</code> the session commit is done for each file and folder.
    *           This is slower but minimizes the chance for lost updates if there is concurrent access on the repository.
    */
   public void migrateGlobalMetaData(int batchsize, boolean batchCommit)
   {
      try
      {
         migrateGlobalMetaData(batchsize, batchCommit, 1);
      }
      catch (InterruptedException e)
      {
         // does not happen without multi-threading.
      }
   }

   protected void migrateGlobalMetaData(int batchsize, boolean batchCommit,
         int threadCount) throws InterruptedException
   {
      String resourceQuery = "/jcr:root/vfs:metaData/*";
      String resourceOrderQuery = resourceQuery + " order by @jcr:path ascending";
      if (threadCount < 1)
      {
         threadCount = 1;
      }

      IResourceQueryResult rs = ((AbstractJcrDocumentRepositoryService) jcrVfs).findResources(
            resourceOrderQuery, batchsize, 0, true, true, IFolder.LOD_NO_MEMBERS);

      if (infoCallback != null)
      {
         infoCallback.logMigrationStart(rs.getTotalSize());
      }

      long currentBatch = 0;
      while (rs != null && rs.getResult() != null && rs.getResult().size() > 0)
      {
         List< ? extends IResource> resources = rs.getResult();
         currentBatch += resources.size();

         if (resources.size() > threadCount * 10 && threadCount > 1)
         {
            runUpdateWorkers(resources, batchCommit, threadCount);
         }
         else
         {
            new ResourceUpdateWorker(resources, batchCommit).run();
         }

         if (infoCallback != null)
         {
            infoCallback.logMigratedBatch(currentBatch);
         }

         rs = ((AbstractJcrDocumentRepositoryService) jcrVfs).findResources(
               resourceQuery, batchsize, 0, true, true, IFolder.LOD_NO_MEMBERS);
      }

      if (infoCallback != null)
      {
         infoCallback.logMigrationDone();
      }

   }

   private void runUpdateWorkers(List< ? extends IResource> files, boolean batchCommit,
         int threadCount) throws InterruptedException
   {
      List<List< ? extends IResource>> subLists = splitEqually(files, threadCount);

      // Thread synchronization
      CountDownLatch threadSync = new CountDownLatch(subLists.size());

      for (List< ? extends IResource> subList : subLists)
      {
         new Thread(new ResourceUpdateWorker(subList, batchCommit, threadSync)).run();
      }

      // Thread gate
      threadSync.await();
      threadSync = null;
   }

   private <T extends Object> List<List< ? extends T>> splitEqually(
         List< ? extends T> list, int slitCount)
   {
      List<List< ? extends T>> subLists = new LinkedList<List< ? extends T>>();

      int equalPart = list.size() / slitCount;

      int index = 0;
      Iterator< ? extends T> iterator = list.iterator();
      List<T> subList = new ArrayList<T>();
      while (iterator.hasNext())
      {

         T entry = iterator.next();

         if (index == 0)
         {
            subList = new ArrayList<T>(equalPart);
         }

         subList.add(entry);

         index++ ;
         if (index > equalPart || !iterator.hasNext())
         {
            subLists.add(subList);
            index = 0;
         }
      }

      return subLists;
   }

   public class ResourceUpdateWorker implements Runnable
   {

      private final List< ? extends IResource> resources;

      private final CountDownLatch threadSync;

      private final boolean batchCommit;

      public ResourceUpdateWorker(List< ? extends IResource> resources,
            boolean batchCommit)
      {
         this.resources = resources;
         this.batchCommit = batchCommit;
         this.threadSync = null;
      }

      public ResourceUpdateWorker(List< ? extends IResource> resources,
            boolean batchCommit, CountDownLatch threadSync)
      {
         this.resources = resources;
         this.batchCommit = batchCommit;
         this.threadSync = threadSync;
      }

      public void run()
      {
         if (batchCommit)
         {
            // run batch update
            ((AbstractJcrDocumentRepositoryService) jcrVfs).updateResourceInfos(resources);
         }

         for (IResource resource : resources)
         {
            if (resource instanceof IFile)
            {
               if (batchCommit)
               {
                  if (infoCallback != null)
                  {
                     infoCallback.logMigratedFilePath(resource.getPath());
                  }
               }
               else
               {
                  // do single update
                  doDummyUpdateOnFile((IFile) resource);
               }
            }
            else if (resource instanceof IFolder)
            {
               if (batchCommit)
               {
                  if (infoCallback != null)
                  {
                     infoCallback.logMigratedFolderPath(resource.getPath());
                  }
               }
               else
               {
                  // do single update
                  doDummyUpdateOnFolder((IFolder) resource);
               }
            }
         }

         if (threadSync != null)
         {
            threadSync.countDown();
         }
      }
   }

   private IFile doDummyUpdateOnFile(IFile file)
   {
      IFile updatedFile = jcrVfs.updateFile(file, (byte[]) null, null, false, false);

      if (infoCallback != null)
      {
         infoCallback.logMigratedFilePath(updatedFile.getPath());
      }
      return updatedFile;
   }

   private IFolder doDummyUpdateOnFolder(IFolder folder)
   {
      IFolder updatedFolder = jcrVfs.updateFolder(folder);
      if (infoCallback != null)
      {
         infoCallback.logMigratedFolderPath(updatedFolder.getPath());
      }
      return updatedFolder;
   }

}
