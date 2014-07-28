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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MavenDependencyReader is an abstract class and needs to define the add method to be
 * testable. The add method is copied from the definition
 * {@link org.eclipse.stardust.buildtools.bom.CSVDependencyMerger#determineAllDependencies}
 *
 * @author Caroline.Rieseler
 *
 */
public class MavenDependencyReaderTest
{

   final Logger logger = LoggerFactory.getLogger(MavenDependencyReaderTest.class);

   final static String TEST_DIR = "src/test/resources/";

   final static String TEST_INPUT_DIR = TEST_DIR + "testinput/";

   final String TEST_LIST_DIR = TEST_INPUT_DIR + "deplist/";

   final static String TEST_RESULT_DIR = TEST_DIR + "actual/";

   final static String TEST_EXPECTED_DIR = TEST_DIR + "expected/";

   final String TEST_READ_INPUT_FILE =
         "org.eclipse.stardust.engine.carnot-base-report.txt";

   final String TEST_READ_OUTPUT_FILE = "base-module-report.csv";

   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {

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
   public void testRead() throws IOException
   {
      final List<Dependency> deps = new ArrayList<Dependency>();
      final File input = new File(TEST_LIST_DIR + TEST_READ_INPUT_FILE);

      MavenDependencyReader reader = new MavenDependencyReader(input, deps);
      reader.read();

      assertEquals(8, deps.size());
   }
}
