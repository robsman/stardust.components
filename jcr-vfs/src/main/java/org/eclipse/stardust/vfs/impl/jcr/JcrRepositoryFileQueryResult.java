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

import org.eclipse.stardust.vfs.IFile;
import org.eclipse.stardust.vfs.IQueryResult;


public class JcrRepositoryFileQueryResult implements IQueryResult
{
   private List<? extends IFile> result;

   private long totalSize;

   public JcrRepositoryFileQueryResult(List<? extends IFile> result, long totalSize)
   {
      this.result = result;
      this.totalSize = totalSize;
   }

   public List<? extends IFile> getResult()
   {
      return result;
   }

   public long getTotalSize()
   {
      return totalSize;
   }
}
