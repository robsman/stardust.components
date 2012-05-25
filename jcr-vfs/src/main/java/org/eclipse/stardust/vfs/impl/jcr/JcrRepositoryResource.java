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
 * $Id: JcrRepositoryResource.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr;

import org.eclipse.stardust.vfs.IResource;
import org.eclipse.stardust.vfs.IllegalOperationException;
import org.eclipse.stardust.vfs.impl.AbstractResourceInfo;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class JcrRepositoryResource extends AbstractResourceInfo implements IResource
{

   private String repositoryId;
   
   private final String id;
   private final String path;
   
   public JcrRepositoryResource(String id, String name, String path)
   {
      super(name);
      
      this.id = id;
      this.path = path;
   }

   public String getRepositoryId()
   {
      return repositoryId;
   }
   
   public String getId()
   {
      return id;
   }

   public String getPath()
   {
      return path;
   }
   
   public String getParentId()
   {
      raiseIllegalOperationException();
      
      return null;
   }
   
   public String getParentPath()
   {
      raiseIllegalOperationException();
      
      return null;
   }
   
   protected void raiseIllegalOperationException()
   {
      throw new IllegalOperationException(
            "This operation is not available on a non-repository resource.");
   }

}
