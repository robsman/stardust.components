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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class merges a set of CSV files into one CSV file. The resulting CSV file is
 * sorted and contains no duplicates.
 *
 */
public class CSVDependencyMerger
{
   private static final int NUMBER_OF_EXPECTED_PARAMS = 2;

   private final String inputDir;

   private final String outputFile;

   /**
    * This method is the entry point for the merge process. It assumes the following
    * arguments to be passed:
    * <ul>
    * <li><b>args[0]: inputDir</b> the absolute path to the directory where the CSV files
    * to merge can be found</li>
    * <li><b>args[1]: outputFile</b> the absolute path to the merged file to write</li>
    * </ul>
    *
    * @param args
    * @throws IOException
    */
   public static void main(final String... args) throws IOException
   {
      if (args.length != NUMBER_OF_EXPECTED_PARAMS)
      {
         throw new IllegalArgumentException(
               "Exactly two arguments need to be passed: 'inputDir' and 'outputFile'.");
      }

      final CSVDependencyMerger merger = new CSVDependencyMerger(args[0], args[1]);
      merger.merge();
   }

   CSVDependencyMerger(final String inputDir, final String outputFile)
   {
      this.inputDir = inputDir;
      this.outputFile = outputFile;
   }

   void merge() throws IOException
   {
      final List<Dependency> dependencies = determineAllDependencies();
      writeDependenciesToFile(dependencies);
   }

   private List<Dependency> determineAllDependencies() throws IOException
   {
      final File input = new File(inputDir);
      final File[] csvFiles = input.listFiles(new FileExtensionFilter(
            CSVConstants.SRC_FILE_EXTENSION));

      final List<Dependency> deps = new ArrayList<Dependency>();

      if (csvFiles == null || csvFiles.length == 0)
      {
         throw new IllegalArgumentException("Report directory - " + inputDir
               + "- has to contain reports.");
      }

      for (final File f : csvFiles)
      {
         MavenDependencyReader reader = new MavenDependencyReader(f, deps);
         reader.read();

      }
      return deps;
   }

   private void writeDependenciesToFile(final List<Dependency> dependencies)
         throws IOException
   {
      final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

      try
      {
         for (final Dependency dep : dependencies)
         {
            writer.write(dep.toStringExtended() + "\n");
         }
      }
      finally
      {
         writer.close();
      }
   }

   private static final class FileExtensionFilter implements FilenameFilter
   {
      private final String extension;

      private final boolean ignoreCase;

      public FileExtensionFilter(final String extension)
      {
         this(extension, true);
      }

      public FileExtensionFilter(final String extension, final boolean ignoreCase)
      {
         if (extension == null)
         {
            throw new NullPointerException("File extension must not be null.");
         }
         if ("".equals(extension))
         {
            throw new IllegalArgumentException("File extension must not be empty.");
         }

         this.extension = extension;
         this.ignoreCase = ignoreCase;
      }

      @Override
      public boolean accept(final File ignored, final String fileName)
      {
         if (ignoreCase)
         {
            return extension.equalsIgnoreCase(getFileExtension(fileName));
         }
         else
         {
            return extension.equals(getFileExtension(fileName));
         }
      }

      private String getFileExtension(final String fileName)
      {
         final String fileNameExtensionDelimiter = ".";

         if (fileName.lastIndexOf(fileNameExtensionDelimiter) == -1)
         {
            return null;
         }
         return fileName.substring(fileName.lastIndexOf(fileNameExtensionDelimiter) + 1);
      }
   }
}
