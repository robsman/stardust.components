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
 * $Id: JcrRepositoryFile.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.vfs.IFile;
import org.eclipse.stardust.vfs.VfsUtils;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;


/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class JcrRepositoryFile extends JcrRepositoryResource implements IFile
{

   private String revisionId;

   private String revisionName;

   private List<String> versionLabels;

   private String contentType;

   private long fileSize;

   private String encoding;

   private final Map<String, Serializable> annotations;

   public JcrRepositoryFile(String id, String name, String path)
   {
      super(id, name, path);

      this.annotations = CollectionUtils.newMap();
      this.revisionId = VfsUtils.VERSION_UNVERSIONED;
      this.revisionName = VfsUtils.VERSION_UNVERSIONED;
   }

   public String getRevisionId()
   {
      return revisionId;
   }

   void setRevisionId(String versionId)
   {
      this.revisionId = versionId;
   }

   public String getRevisionName()
   {
      return revisionName;
   }

   void setRevisionName(String versionName)
   {
      this.revisionName = versionName;
   }

   public List<String> getVersionLabels()
   {
      return (null != versionLabels) ? versionLabels : Collections.EMPTY_LIST;
   }

   void setVersionLabels(List<String> versionLabels)
   {
      this.versionLabels = versionLabels;
   }

   public long getSize()
   {
      return fileSize;
   }

   void setSize(long fileSize)
   {
      this.fileSize = fileSize;
   }

   public String getContentType()
   {
      return contentType;
   }

   public void setContentType(String type)
   {
      this.contentType = type;
   }

   public String getEncoding()
   {
      return encoding;
   }

   void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }

   public String getLockOwner()
   {
      raiseIllegalOperationException();

      return null;
   }

   public Map<String, Serializable> getAnnotations()
   {
      return Collections.unmodifiableMap(this.annotations);
   }

   public void setAnnotations(Map< ? extends String, ? extends Serializable> annotations)
   {
      this.annotations.clear();
      this.annotations.putAll(annotations);
   }

}
