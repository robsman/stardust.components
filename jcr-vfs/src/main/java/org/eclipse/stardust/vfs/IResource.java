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
 * $Id: IResource.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public interface IResource extends IResourceInfo
{

   /**
    * Indicates the repository this resource is located in.
    *
    * @return The repository ID.
    *
    * @throws IllegalOperationException
    */
   String getRepositoryId();

   /**
    *
    * @return
    *
    * @throws IllegalOperationException
    */
   String getId();

   String getPath();

   String getParentId();

   String getParentPath();

   String getName();

   void setName(String name);

   String getDescription();

   void setDescription(String description);

   String getOwner();

   void setOwner(String owner);

   Date getDateCreated();

   /**
    * @deprecated This field is maintained by the document repository.
    */
   void setDateCreated(Date dateCreated);

   Date getDateLastModified();

   /**
    * @deprecated This field is maintained by the document repository.
    */
   void setDateLastModified(Date dateLastModified);

   Map<String, Serializable> getProperties();

   void setProperties(Map<? extends String, ? extends Serializable> properties);

   Serializable getProperty(String name);

   void setProperty(String name, Serializable value);

   String getPropertiesTypeId();

   void setPropertiesTypeId(String propertiesTypeId);

   String getPropertiesTypeSchemaLocation();

   void setPropertiesTypeSchemaLocation(String propertiesTypeSchemaLocation);

}
