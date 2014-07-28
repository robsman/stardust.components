/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.buildtools.bom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BOMGeneratorTest
{

   final static String TEST_DIR = "src/test/resources/";

   final static String TEST_INPUT_DIR = TEST_DIR + "testinput/";

   final String TEST_LIST_DIR = TEST_DIR + "deplist/";

   final static String TEST_RESULT_DIR = TEST_DIR + "actual/";

   final static String TEST_EXPECTED_DIR = TEST_DIR + "expected/";

   final String TEST_BOM_FILE = "bom.csv";

   final String TEST_IGNORED_BOM_FILE = "ignored-bom.csv";

   private static final transient Logger logger = LoggerFactory
         .getLogger(BOMGeneratorTest.class);
   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {}

   @Test
   public void testGenerate() throws IOException
   {
      String allIppdeps = TEST_INPUT_DIR
            + "merged-dependencies/set-of-ipp-dependencies.csv";
      String rawData = TEST_INPUT_DIR + "rawdata/bom-raw-data.csv";
      String outputFilePath = TEST_RESULT_DIR + TEST_BOM_FILE;

      BOMGenerator bomGenerator = new BOMGenerator(allIppdeps, rawData, outputFilePath);
      try
      {
         bomGenerator.generate();
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      assertNotNull(new File(TEST_RESULT_DIR + TEST_BOM_FILE));
      File actual = new File(TEST_RESULT_DIR + TEST_BOM_FILE);
      File expected = new File(TEST_EXPECTED_DIR + TEST_BOM_FILE);

      assertEquals("The files differ!", FileUtils.readFileToString(actual, "utf-8"),
            FileUtils.readFileToString(expected, "utf-8"));

   }

   @Test
   public void testReadAllDeps()
   {

      String allIppdeps = TEST_INPUT_DIR
            + "merged-dependencies/set-of-ipp-dependencies.csv";
      String rawData = TEST_INPUT_DIR + "rawdata/bom-raw-data.csv";
      String outputFilePath = TEST_RESULT_DIR + TEST_BOM_FILE;

      BOMGenerator bomGenerator = new BOMGenerator(allIppdeps, rawData, outputFilePath);
      Set<DependencyInfo> set = null;
      try
      {
         set = bomGenerator.readDeps();
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      logger.debug(set.toString());

   }

}
