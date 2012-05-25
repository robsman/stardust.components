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
package org.eclipse.stardust.vfs.impl.jcr.test.migration;

import junit.framework.Assert;

import org.eclipse.stardust.vfs.IDocumentRepositoryService;
import org.eclipse.stardust.vfs.IMigrationReport;
import org.eclipse.stardust.vfs.IQueryResult;
import org.eclipse.stardust.vfs.impl.utils.MetaDataMigrationJob;
import org.eclipse.stardust.vfs.impl.utils.MetaDataMigrationJob.IMigrationInformationCallback;
import org.junit.Test;


/**
 * Tests for MetaData migration.<br>
 * <p>
 * Note: Manual setup of a non migrated repository is required for every test.
 *
 * @author roland.stamm
 *
 */
public abstract class AbstractTestMigrationMetaData
{
   private IDocumentRepositoryService jcrVfsWithAllPrivileges;

   private IDocumentRepositoryService jcrVfsWithReadPrivileges;

   public IDocumentRepositoryService getJcrVfsWithAllPrivileges()
   {
      return jcrVfsWithAllPrivileges;
   }

   public void setJcrVfsWithAllPrivileges(
         IDocumentRepositoryService jcrVfsWithAllPrivileges)
   {
      this.jcrVfsWithAllPrivileges = jcrVfsWithAllPrivileges;
   }

   public IDocumentRepositoryService getJcrVfsWithReadPrivileges()
   {
      return jcrVfsWithReadPrivileges;
   }

   public void setJcrVfs1(IDocumentRepositoryService jcrVfsWithReadPrivileges)
   {
      this.jcrVfsWithReadPrivileges = jcrVfsWithReadPrivileges;
   }

   public void migrateOneFile()
   {
      MigrationInfoLogger infoCallback = new MigrationInfoLogger();
      MetaDataMigrationJob migrationJob = new MetaDataMigrationJob(
            jcrVfsWithAllPrivileges, infoCallback);
      migrationJob.migrateFile("/test.txt");
   }

   public void migrateGlobalMetaDataSingleCommit() throws InterruptedException
   {
      doMigration(1000, false);
   }

   public void migrateGlobalMetaDataBatchCommit() throws InterruptedException
   {
      doMigration(1000, true);
   }

   private void doMigration(int batchSize, boolean batchCommit)
   {
      // Initial file and folder count
      String fileQuery = "/jcr:root//element(*, nt:file) order by @jcr:path ascending";
      IQueryResult qr = jcrVfsWithAllPrivileges.findFiles(fileQuery, 1, 0);
      long files = qr.getTotalSize();

      String folderQuery = "/jcr:root//element(*, nt:folder) order by @jcr:path ascending";
      qr = jcrVfsWithAllPrivileges.findFiles(folderQuery, 1, 0);
      long folders = qr.getTotalSize();

      // Actual migration job
      MigrationInfoLogger infoCallback = new MigrationInfoLogger();
      MetaDataMigrationJob migrationJob = new MetaDataMigrationJob(
            jcrVfsWithAllPrivileges, infoCallback);
      migrationJob.migrateGlobalMetaData(batchSize, batchCommit);

      // Asserts: Count of files and folders did not change, globalMetaData node contains
      // no entry.
      qr = jcrVfsWithAllPrivileges.findFiles(fileQuery, 1, 0);
      long files2 = qr.getTotalSize();

      qr = jcrVfsWithAllPrivileges.findFiles(folderQuery, 1, 0);
      long folders2 = qr.getTotalSize();

      String metaDataQuery = "/jcr:root/vfs:metaData/* order by @jcr:path ascending";
      qr = jcrVfsWithAllPrivileges.findFiles(metaDataQuery, 1, 0);
      long globalMetaDataCount = qr.getTotalSize();

      Assert.assertEquals(files, files2);
      Assert.assertEquals(folders, folders2);
      Assert.assertEquals(0, globalMetaDataCount);
   }

   @Test
   public void fullMigrationRun()
   {

      int batchSize = 500;

      boolean done = false;
      while ( !done)
      {
         IMigrationReport report = jcrVfsWithAllPrivileges.migrateRepository(batchSize,
               false);
         if (report.getTargetRepositoryVersion() == report.getCurrentRepositoryVersion())
         {
            done = true;
         }
         System.out.println(report);

      }

   }

   private class MigrationInfoLogger implements IMigrationInformationCallback
   {

      private Logr log = new Logr()
      {

         public void info(String message)
         {
            System.out.println(message);
         }
      };

      private long folderCount;

      private long fileCount;

      private long fileTotal;

      public void logMigratedFolderPath(String path)
      {
         folderCount++ ;
         log.info(path);
      }

      public void logMigratedFilePath(String path)
      {
         fileCount++ ;
         log.info(path);
      }

      public void logMigrationStart(long fileTotal)
      {
         this.fileTotal = fileTotal;
         log.info("Migration Start! Total Files and Folders: " + fileTotal);
      }

      public void logMigratedBatch(long processedFiles)
      {
         log.info("Migration Batch processed! Resources done: " + processedFiles + " of "
               + fileTotal);
      }

      public void logMigrationDone()
      {
         log.info("Migration Done! Migrated Files: " + fileCount + " Migrated Folders: "
               + folderCount);
      }

   }

   private interface Logr
   {
      void info(String message);
   }

}
