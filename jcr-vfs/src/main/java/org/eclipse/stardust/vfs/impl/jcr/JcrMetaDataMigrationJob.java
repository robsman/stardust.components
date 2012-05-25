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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.core.query.lucene.QueryResultImpl;
import org.eclipse.stardust.vfs.IFolder;
import org.eclipse.stardust.vfs.IMigrationJob;
import org.eclipse.stardust.vfs.IMigrationJobInfo;


/**
 * The MetaDataMigrationJob can be used to migrate file and folder global meta data to
 * local meta data. Global meta data storage structure was used in jcr-vfs-1.6.2 and
 * earlier. Since the introduction of the much better performing local meta data structure
 * in jcr-vfs-1.6.3 global meta data is deprecated.
 *
 * @author roland.stamm
 *
 */
public class JcrMetaDataMigrationJob implements IMigrationJob
{
   private transient final JcrVfsOperations jcrVfs;

   private long totalCount = -1;

   private long resourcesDone = -1;

   private static IMigrationJobInfo migrationInfo = new IMigrationJobInfo()
   {
      public String getName()
      {
         return "JcrMetaDataMigrationJob";
      }

      public String getDescription()
      {
         return "Migrates global metaData to local metaData";
      }

      public int getFromVersion()
      {
         return 0;
      }

      public int getToVersion()
      {
         return 1;
      }

      public String toString()
      {
         return getName() + "(Version " + getFromVersion() + " -> " + getToVersion()
               + ")";
      };

   };

   public JcrMetaDataMigrationJob(JcrVfsOperations jcrVfs)
   {
      this.jcrVfs = jcrVfs;
   }

   public void processBatch(int batchSize, boolean evaluateTotalCount)
         throws RepositoryException
   {
      String resourceQuery = "/jcr:root/vfs:metaData/*";
      String resourceOrderQuery = resourceQuery + " order by @jcr:path ascending";

      if (evaluateTotalCount)
      {
         QueryResult countQueryResult = jcrVfs.queryByXPath(resourceOrderQuery, 1, 0);
         if (countQueryResult instanceof QueryResultImpl)
         {
            this.totalCount = ((QueryResultImpl) countQueryResult).getTotalSize();
         }
      }

      if (batchSize > 0)
      {
         QueryResult queryResult = jcrVfs.queryByXPath(resourceQuery, batchSize, 0);

         NodeIterator nodes = queryResult.getNodes();

         this.resourcesDone = 0;
         jcrVfs.visitMembers(nodes, new FsNodeVisitorAdapter()
         {
            @Override
            public void visitFile(Node nFile) throws RepositoryException
            {
               jcrVfs.updateFile(nFile, jcrVfs.getFileSnapshot(nFile), null, null);
               resourcesDone++ ;
            }

            @Override
            public void visitFolder(Node nFolder) throws RepositoryException
            {
               jcrVfs.updateFolder(nFolder,
                     jcrVfs.getFolderSnapshot(nFolder, IFolder.LOD_NO_MEMBERS));
               resourcesDone++ ;
            }

         });
      }
   }

   public long getTotalCount()
   {
      return this.totalCount;
   }

   public long getResourcesDone()
   {
      return this.resourcesDone;
   }

   public IMigrationJobInfo getMigrationInfo()
   {
      return migrationInfo;
   }

}
