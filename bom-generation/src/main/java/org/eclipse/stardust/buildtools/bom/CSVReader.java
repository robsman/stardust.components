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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class provides functionality to process a csv file containing dependencies.
 * Each line is transformed into a DependencyInfo object.
 */

public abstract class CSVReader
{
   private final File file;

   private final boolean firstLineIsHeader;

   private String header;

   private static final transient Logger logger = LoggerFactory
         .getLogger(CSVReader.class);

   public CSVReader(final File file, boolean firstLineIsHeader)
   {
      if (file == null)
      {
         throw new NullPointerException("File must not be null.");
      }
      this.file = file;
      this.firstLineIsHeader = firstLineIsHeader;
      this.header = null;
   }

   public File getFile()
   {
      return file;
   }

   public String getHeader()
   {
      return header;
   }

   /**
    * Reads in a file and expects dependency coordinates and additional information in
    * each line. Each line is transformed into a DependencyInfo object and further handled
    * by the caller class.
    *
    * @param delimitter
    * @param dependencyIDColumnsCount
    * @throws IOException
    */
   public void read(String delimitter, int dependencyIDColumnsCount) throws IOException
   {
      final BufferedReader reader = new BufferedReader(new FileReader(file));

      try
      {
         String line = reader.readLine();
         if (line == null || line.isEmpty())
         {
            throw new NullPointerException("File " + file.getAbsolutePath()
                  + " must not be empty.");
         }

         final int numberOfCSVColumns = line.replaceAll("[^" + delimitter + "]", "")
               .length() + 1;
         logger.debug("file: " + file.getAbsolutePath());
         logger.debug("numberOfCSVColumns: " + numberOfCSVColumns);
         final int numberOfInfoColumns = numberOfCSVColumns - dependencyIDColumnsCount;
         if (firstLineIsHeader)
         {
            header = line;
            line = reader.readLine();
         }

         while (line != null)
         {
            final String[] splittedLine = line.split("\\" + delimitter, -1);
            if (splittedLine.length != numberOfCSVColumns)
            {
               throw new UnsupportedOperationException("Unsupported CSV format found: "
                     + line);
            }
            final Dependency dep = readDependency(splittedLine);

            final String[] depInfoStrings = new String[numberOfInfoColumns];
            System.arraycopy(splittedLine, dependencyIDColumnsCount, depInfoStrings, 0,
                  numberOfInfoColumns);
            final DependencyInfo depInfo = new DependencyInfo(dep, depInfoStrings);
            addDependencyInfo(depInfo);

            line = reader.readLine();
         }
      }
      finally
      {
         reader.close();
      }
   }

   abstract void addDependencyInfo(DependencyInfo depInfo);

   abstract Dependency readDependency(String[] splittedLine);
}
