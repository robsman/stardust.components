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
 * $Id: NonRepositoryResource.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl;

import org.eclipse.stardust.vfs.IResource;
import org.eclipse.stardust.vfs.IllegalOperationException;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 * 
 * @deprecated
 */
@Deprecated
public class NonRepositoryResource extends AbstractResourceInfo implements IResource
{

   public NonRepositoryResource(String name)
   {
      super(name);
   }

   public String getRepositoryId()
   {
      raiseIllegalOperationException();
      
      return null;
   }
   
   public String getId()
   {
      raiseIllegalOperationException();
      
      return null;
   }

   public String getPath()
   {
      raiseIllegalOperationException();
      
      return null;
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
   
   protected static void raiseIllegalOperationException()
   {
      throw new IllegalOperationException(
            "This operation is not available on a non-repository resource.");
   }

}
