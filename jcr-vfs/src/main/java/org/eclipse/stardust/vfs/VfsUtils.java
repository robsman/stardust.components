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
 * $Id: VfsUtils.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs;

import java.io.File;
import java.io.IOException;

import org.eclipse.stardust.vfs.impl.FileInfo;
import org.eclipse.stardust.vfs.impl.FolderInfo;
import org.springframework.core.io.ClassPathResource;


/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class VfsUtils
{

   public static final String NS_URI_JCR_VFS_1_0 = "http://www.sungard.com/infinity/vfs/1.0";

   public static final String NS_PREFIX_VFS = "vfs";

   public static final String REPOSITORY_PATH_PREFIX = "/";

   public static final String REPOSITORY_ROOT = REPOSITORY_PATH_PREFIX;

   public static final String VERSION_UNVERSIONED = "UNVERSIONED";

   public static final String VFS_META_DATA_MIXIN = "vfsMetaData";

   public static final String VFS_META_DATA = "metaData";

   public static final String VFS_META_DATA_ATTIC = "metaDataAttic";

   public static final String VFS_ID = "id";

   public static final String VFS_NAME = "name";

   public static final String VFS_DESCRIPTION = "description";

   public static final String VFS_REVISION_COMMENT = "revisionComment";

   public static final String VFS_OWNER = "owner";

   public static final String VFS_LOCK_OWNER = "lockOwner";

   public static final String VFS_ATTRIBUTES = "attributes";

   public static final String VFS_ATTRIBUTES_TYPE_ID = "attributesTypeId";

   public static final String VFS_ATTRIBUTES_TYPE_SCHEMA_LOCATION = "attributesTypeSchemaLocation";

   public static final String VFS_ANNOTATIONS = "annotations";

   public static final byte[] NO_CONTENT = null;


   /**
    * @deprecated Superceeded by {@link #createFileInfo(String)}.
    */
   @Deprecated
   public static TransientFile transientFile(String name)
   {
      return new TransientFile(name);
   }

   public static IFileInfo createFileInfo(String name)
   {
      return new FileInfo(name);
   }

   public static LocalFile localFile(File file)
   {
      return localFile(file, null);
   }

   public static LocalFile localFile(File file, String encoding)
   {
      return new LocalFile(file, encoding);
   }

   public static LocalFile localCpFile(String filePath) throws IOException
   {
      return localFile(new ClassPathResource(filePath).getFile());
   }

   /**
    * @deprecated Superceeded by {@link #createFolderInfo(String)}.
    */
   @Deprecated
   public static TransientFolder newFolder(String name)
   {
      return new TransientFolder(name);
   }

   public static IFolderInfo createFolderInfo(String name)
   {
      return new FolderInfo(name);
   }

   private VfsUtils()
   {
      // utility class
   }

}
