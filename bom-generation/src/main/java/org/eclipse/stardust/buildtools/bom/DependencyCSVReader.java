/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.buildtools.bom;

import java.io.File;
import java.util.Set;

public abstract class DependencyCSVReader extends CSVReader
{
   private final Set<DependencyInfo> deps;

   public DependencyCSVReader(final File file, final Set<DependencyInfo> deps,
         boolean skipHeader)
   {
      super(file, skipHeader);
      if (deps == null)
      {
         throw new NullPointerException("Set of Dependencies must not be null.");
      }
      this.deps = deps;
   }

   @Override
   void addDependencyInfo(DependencyInfo depInfo)
   {
      add(depInfo, deps);
   }

   public abstract void add(final DependencyInfo depInfo, final Set<DependencyInfo> deps);
}
