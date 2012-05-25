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

import java.util.List;

import org.eclipse.stardust.vfs.IQueryResult;
import org.eclipse.stardust.vfs.IResource;
import org.eclipse.stardust.vfs.IResourceQueryResult;


public class JcrRepositoryResourceQueryResult implements IResourceQueryResult
{
   private List<? extends IResource> result;

   private long totalSize;

   public JcrRepositoryResourceQueryResult(List<? extends IResource> result, long totalSize)
   {
      this.result = result;
      this.totalSize = totalSize;
   }

   public List<? extends IResource> getResult()
   {
      return result;
   }

   public long getTotalSize()
   {
      return totalSize;
   }
}
