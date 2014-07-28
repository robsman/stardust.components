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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class takes two files
 * <ul>
 * <li>a CSV file containing all third party dependencies</li>
 * <li>a CSV file containing a set of reference dependencies plus additional information
 * located in resources/bom-raw-data.csv</li>
 * </ul>
 * and generates two text reports. One report is listing the intersection of the two input
 * files and the second report lists the third party dependencies not contained in the
 * reference file.
 */
public class BOMGenerator
{
   private static final int NUMBER_OF_EXPECTED_PARAMS = 3;

   private static final String IGNORED_CSV_HEADER;

   private final String allDepsFile;

   private final String rawDataBomFile;

   private final String outputFile;

   private final String ignoredOutputFile;

   private String rawDataHeader;

   static
   {
      final StringBuilder sb = new StringBuilder();
      sb.append("Group ID").append(CSVConstants.TARGET_DELIMITER);
      sb.append("Artifact ID").append(CSVConstants.TARGET_DELIMITER);
      sb.append("Type").append(CSVConstants.TARGET_DELIMITER);
      sb.append("Classifier").append(CSVConstants.TARGET_DELIMITER);
      sb.append("Version ID").append(CSVConstants.TARGET_DELIMITER);
      sb.append("Scope").append(CSVConstants.TARGET_DELIMITER);
      sb.append("ProjectList");
      sb.append("\n");
      IGNORED_CSV_HEADER = sb.toString();
   }

   /**
    * This method is the entry point for the generation process. It assumes the following
    * arguments to be passed:
    * <ul>
    * <li><b>args[0]: allIppDepsFile</b> the absolute path to the CSV file that contains
    * all thirdparty dependencies</li>
    * <li><b>args[1]: rawDataBomFile</b> the absolute path to the CSV file that contains
    * the BOM raw data</li>
    * <li><b>args[2]: outputFile</b> the absolute path to the BOM file to write</li>
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
               "Exactly three arguments need to be passed: 'allIppDepsFile', "
                     + "'rawDataBomFile' and 'outputFile'.");
      }

      final BOMGenerator generator = new BOMGenerator(args[0], args[1], args[2]);
      generator.generate();
   }

   BOMGenerator(final String allDeps, final String rawDataBom, final String outputFile)
   {
      this.allDepsFile = allDeps;
      this.rawDataBomFile = rawDataBom;
      this.outputFile = outputFile;
      String path = outputFile.replace(File.separatorChar, '/');
      int lastSeparatorIdx = path.lastIndexOf('/');
      this.ignoredOutputFile = outputFile.substring(0, lastSeparatorIdx + 1) + "ignored-"
            + outputFile.substring(lastSeparatorIdx + 1);
   }

   protected void generate() throws IOException
   {
      final Set<DependencyInfo> ippDeps = readDeps();
      final Map<Dependency, DependencyInfo> rawDataBom = readRawDataBom();
      final OutputStreamWriter writer = new FileWriter(outputFile);
      final OutputStreamWriter writerIgnoredDeps = new FileWriter(ignoredOutputFile);
      try
      {
         writer.write(rawDataHeader);
         writerIgnoredDeps.write(IGNORED_CSV_HEADER);
         writeBomData(ippDeps, rawDataBom, writer, writerIgnoredDeps);
         writer.flush();
         writerIgnoredDeps.flush();
      }
      finally
      {
         writer.close();
         writerIgnoredDeps.close();
      }
   }

   /**
    *
    * Takes a file containing dependencies and returns it in an ordered way. File is
    * expected to contain dependencies in the following format:
    * log4j|log4j|jar||1.2.15-eclipse01
    * |compile|org.eclipse.stardust.engine.carnot-base-report.txt|
    *
    * @return set of ordered dependencies
    * @throws IOException
    */
   Set<DependencyInfo> readDeps() throws IOException
   {
      final Set<DependencyInfo> deps = new TreeSet<DependencyInfo>();
      new DependencyCSVReader(new File(allDepsFile), deps, false)
      {
         @Override
         public Dependency readDependency(String[] splittedLine)
         {
            Dependency dep = new Dependency(splittedLine[0], splittedLine[1],
                  splittedLine[2], splittedLine[3], splittedLine[4], splittedLine[5],
                  splittedLine[6]);
            return dep;
         }

         @Override
         public void add(final DependencyInfo dep, final Set<DependencyInfo> deps)
         {
            deps.add(dep);
         }
      }.read(CSVConstants.TARGET_DELIMITER, 7);
      return deps;
   }

   /**
    * Takes a csv file containing dependencies and additional information. It separates
    * the dependency identifiers and additional information and returns it in an ordered
    * way using dependency identifiers as key.
    *
    * @return set of ordered dependencies
    * @throws IOException
    */
   private Map<Dependency, DependencyInfo> readRawDataBom() throws IOException
   {
      final Map<Dependency, DependencyInfo> depInfos =
            new TreeMap<Dependency, DependencyInfo>();
      CSVReader csvReader = new BOMRawDataCSVReader(new File(rawDataBomFile), depInfos);
      csvReader.read(CSVConstants.TARGET_DELIMITER, 3);
      rawDataHeader = csvReader.getHeader() + "\n";
      return depInfos;
   }

   /**
    *
    * Writes the intersection dependency file and the file with not matching dependencies.
    *
    * @param allDeps
    *           csv file containing merged and sorted dependencies.Format is:
    *           groupId|artifactID|type|classifier|version|projects
    * @param rawDataBom
    *           csv file containing reference dependencies plus additional
    *           information.Format is: groupId|artifactID|version|<additional info>
    * @param csvFile
    *           result file containing intersection of the first two file contents
    * @param csvFileIgnoredDeps
    *           csvFile result file containing all dependencies that could not be matched
    *           with reference data
    */
   private void writeBomData(final Set<DependencyInfo> allDeps,
         final Map<Dependency, DependencyInfo> rawDataBom, OutputStreamWriter csvFile,
         OutputStreamWriter csvFileIgnoredDeps)
   {
      PrintWriter depsFile = new PrintWriter(csvFile, true);
      PrintWriter ignoredDepsFile = new PrintWriter(csvFileIgnoredDeps, true);
      for (final DependencyInfo d : allDeps)
      {
         if (rawDataBom.containsKey(d.dep()))
         {
            final DependencyInfo depInfo = rawDataBom.get(d.dep());
            depsFile.print(depInfo.toString() + "|" + d.dep().project() + "\n");
         }
         else
            ignoredDepsFile.print(d.toStringExtended() + "\n");
      }
   }
}
