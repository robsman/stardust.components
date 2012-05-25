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
package org.eclipse.stardust.vfs.impl.jcr;

import static org.eclipse.stardust.vfs.impl.jcr.JcrDocumentRepositoryService.REPOSITORY_STRUCTURE_VERSION;

import javax.jcr.RepositoryException;

import org.eclipse.stardust.vfs.IMigrationReport;
import org.eclipse.stardust.vfs.MigrationManager;
import org.eclipse.stardust.vfs.RepositoryOperationFailedException;


public class JcrMigrationManager implements MigrationManager
{
   private JcrVfsOperations jcrVfs;

   public IMigrationReport processBatch(int batchSize, boolean evaluateTotalCount)
   {
      try
      {
         int currentVersion = jcrVfs.getRepositoryVersion();

         if (currentVersion >= REPOSITORY_STRUCTURE_VERSION)
         {
            JcrMigrationReport report = new JcrMigrationReport();
            report.setCurrentRepositoryVersion(currentVersion);
            report.setTargetRepositoryVersion(REPOSITORY_STRUCTURE_VERSION);
            return report;
         }
         else
         {
            return doMigrationBatch(batchSize, evaluateTotalCount, currentVersion,
                  currentVersion + 1);
         }
      }
      catch (RepositoryException e)
      {
         throw new RepositoryOperationFailedException(e);
      }
   }

   private IMigrationReport doMigrationBatch(int batchSize, boolean evaluateTotalCount,
         int currentVersion, int targetVersion) throws RepositoryException
   {
      JcrMigrationReport report = new JcrMigrationReport();
      report.setCurrentRepositoryVersion(currentVersion);
      report.setTargetRepositoryVersion(REPOSITORY_STRUCTURE_VERSION);

      if (currentVersion == 0 && targetVersion == 1)
      {
         JcrMetaDataMigrationJob jcrMetaDataMigrationJob = new JcrMetaDataMigrationJob(
               jcrVfs);
         report.setCurrentMigrationJobInfo(jcrMetaDataMigrationJob.getMigrationInfo());

         jcrMetaDataMigrationJob.processBatch(batchSize, evaluateTotalCount);
         long totalCount = jcrMetaDataMigrationJob.getTotalCount();
         long resourcesDone = jcrMetaDataMigrationJob.getResourcesDone();
         report.setTotalCount(totalCount);
         report.setResourcesDone(resourcesDone);

         if (resourcesDone == 0 || (totalCount > 0 && totalCount <= resourcesDone))
         {
            jcrVfs.setRepositoryVersion(targetVersion);
            report.setCurrentRepositoryVersion(targetVersion);
         }

         return report;
      }

      throw new RepositoryOperationFailedException(
            "No migration job available from version " + currentVersion + " to version "
                  + targetVersion);
   }

   void setJcrVfsOperations(JcrVfsOperations jcrVfs)
   {
      this.jcrVfs = jcrVfs;

   }

}
