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
 * $Id: AbstractResourceInfo.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.vfs.IResourceInfo;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;


/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public abstract class AbstractResourceInfo implements IResourceInfo
{

   private String name;

   private String description;

   private String owner;

   private Date dateCreated;

   private Date dateLastModified;

   private final Map<String, Serializable> properties;

   private String propertyTypeId;

   private String propertyTypeSchemaLocation;

   public AbstractResourceInfo(String name)
   {
      this.name = name;

      this.properties = CollectionUtils.newMap();
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public Date getDateCreated()
   {
      return (null != dateCreated) ? new Date(dateCreated.getTime()) : null;
   }

   public void setDateCreated(Date dateCreated)
   {
      this.dateCreated = (null != dateCreated) //
            ? new Date(dateCreated.getTime())
            : null;
   }

   public Date getDateLastModified()
   {
      return (null != dateLastModified) ? new Date(dateLastModified.getTime()) : null;
   }

   public void setDateLastModified(Date dateLastModified)
   {
      this.dateLastModified = (null != dateLastModified) //
            ? new Date(dateLastModified.getTime())
            : null;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getOwner()
   {
      return owner;
   }

   public void setOwner(String owner)
   {
      this.owner = owner;
   }

   public Map<String, Serializable> getProperties()
   {
      return Collections.unmodifiableMap(properties);
   }

   public void setProperties(Map<? extends String, ? extends Serializable> properties)
   {
      this.properties.clear();
      this.properties.putAll(properties);
   }

   public Serializable getProperty(String name)
   {
      return properties.get(name);
   }

   public void setProperty(String name, Serializable value)
   {
      if (null != value)
      {
         // TODO verify supported types
         properties.put(name, value);
      }
      else
      {
         properties.remove(name);
      }
   }

   public String getPropertiesTypeId()
   {
      return propertyTypeId;
   }

   public void setPropertiesTypeId(String propertyTypeId)
   {
      this.propertyTypeId = propertyTypeId;
   }

   public String getPropertiesTypeSchemaLocation()
   {
      return propertyTypeSchemaLocation;
   }

   public void setPropertiesTypeSchemaLocation(String propertyTypeSchemaLocation)
   {
      this.propertyTypeSchemaLocation = propertyTypeSchemaLocation;
   }



}
