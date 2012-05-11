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
 * $Id: FileInfo.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.vfs.IFileInfo;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;


/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class FileInfo extends AbstractResourceInfo implements IFileInfo
{

   private String contentType;

   private final Map<String, Serializable> annotations;

   private String revisionComment;

   public FileInfo(String name)
   {
      super(name);
      this.annotations = CollectionUtils.newMap();
   }

   public String getContentType()
   {
      return contentType;
   }

   public void setContentType(String type)
   {
      this.contentType = type;
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

   public String getRevisionComment()
   {
      return revisionComment;
   }

}
