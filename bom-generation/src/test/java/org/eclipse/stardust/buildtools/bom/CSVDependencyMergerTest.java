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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVDependencyMergerTest
{

   final Logger logger = LoggerFactory.getLogger(MavenDependencyReaderTest.class);

   final static String TEST_DIR = "src/test/resources/";

   final static String TEST_INPUT_DIR = TEST_DIR + "testinput/";

   final String TEST_LIST_DIR = TEST_INPUT_DIR + "deplist/";

   final static String TEST_RESULT_DIR = TEST_DIR + "actual/";

   final static String TEST_EXPECTED_DIR = TEST_DIR + "expected/";

   final String TEST_MERGE_OUTPUT_FILE = "mergeOutput.txt";

   /**
    * Clean up result directory before test starts
    *
    * @throws Exception
    */
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      final File input = new File(TEST_RESULT_DIR);
      assertNotNull("Directoty " + TEST_RESULT_DIR + " might not exist.", input);
      File[] files = input.listFiles();

      for (File file : files)
      {
         file.delete();
      }

   }

   /**
    * @throws java.lang.Exception
    */
   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {}

   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception
   {}

   /**
    * @throws java.lang.Exception
    */
   @After
   public void tearDown() throws Exception
   {}

   @Test
   public void testMerge() throws IOException
   {
      CSVDependencyMerger merger = new CSVDependencyMerger(TEST_LIST_DIR, TEST_RESULT_DIR
            + "/" + TEST_MERGE_OUTPUT_FILE);

      merger.merge();
      assertNotNull(new File(TEST_RESULT_DIR + "/" + TEST_MERGE_OUTPUT_FILE));
      File result = new File(TEST_RESULT_DIR + "/" + TEST_MERGE_OUTPUT_FILE);
      File expected = new File(TEST_EXPECTED_DIR + "/" + TEST_MERGE_OUTPUT_FILE);

      assertEquals("The files differ!", FileUtils.readFileToString(result, "utf-8")
            .replaceAll("\\r\\n", "\n"), FileUtils.readFileToString(expected, "utf-8")
            .replaceAll("\\r\\n", "\n"));
   }

}
