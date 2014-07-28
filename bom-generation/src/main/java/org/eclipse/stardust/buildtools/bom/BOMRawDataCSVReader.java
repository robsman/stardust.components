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
import java.util.Map;

/**
 * This class provides functionality to read in the dependency reference file (refer to
 * resources/bom-raw-data.csv).
 */
public class BOMRawDataCSVReader extends CSVReader
{
   private final Map<Dependency, DependencyInfo> depInfos;

   public BOMRawDataCSVReader(final File file,
         final Map<Dependency, DependencyInfo> depInfos)
   {
      super(file, true);
      if (depInfos == null)
      {
         throw new NullPointerException("Map of Dependency Infos must not be null.");
      }
      this.depInfos = depInfos;
   }

   @Override
   Dependency readDependency(String[] line)
   {
      Dependency deps = new Dependency(line[0], line[1], line[2]);
      return deps;
   }

   @Override
   void addDependencyInfo(DependencyInfo depInfo)
   {
      depInfos.put(depInfo.dep(), depInfo);
   }
}
