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

import org.eclipse.stardust.vfs.IMigrationJobInfo;
import org.eclipse.stardust.vfs.IMigrationReport;

public class JcrMigrationReport implements IMigrationReport
{

   private int currentVersion;

   private int targetVersion;

   private IMigrationJobInfo migrationJob;

   private long resourcesDone;

   private long totalCount;

   public int getCurrentRepositoryVersion()
   {
      return currentVersion;
   }

   public long getResourcesDone()
   {
      return resourcesDone;
   }

   public int getTargetRepositoryVersion()
   {
      return targetVersion;
   }

   public IMigrationJobInfo getCurrentMigrationJobInfo()
   {
      return migrationJob;
   }

   public long getTotalCount()
   {
      return totalCount;
   }

   public void setCurrentRepositoryVersion(int currentVersion)
   {
      this.currentVersion = currentVersion;
   }

   public void setResourcesDone(long resourcesDone)
   {
      this.resourcesDone = resourcesDone;
   }

   public void setTargetRepositoryVersion(int targetVersion)
   {
      this.targetVersion = targetVersion;
   }

   public void setCurrentMigrationJobInfo(IMigrationJobInfo migrationJob)
   {
      this.migrationJob = migrationJob;
   }

   public void setTotalCount(long totalCount)
   {
      this.totalCount = totalCount;
   }

   @Override
   public String toString()
   {
      String migrationJob = this.migrationJob == null
            ? ""
            : this.migrationJob.toString();
      return "Repository version: current "+ currentVersion + " -> target " + targetVersion + " (total " + totalCount + ", done "
            + resourcesDone + ") Current Job: " + migrationJob;
   }

}
