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
 * $Id: IFile.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public interface IFile extends IResource, IFileInfo
{

   String getRevisionId();

   String getRevisionName();

   String getRevisionComment();

   List<String> getVersionLabels();

   long getSize();

   String getEncoding();

   String getLockOwner();

   String getContentType();

   void setContentType(String type);

   Map<String, Serializable> getAnnotations();

   void setAnnotations(Map<? extends String, ? extends Serializable> annotations);

}
