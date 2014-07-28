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

public final class CSVConstants
{
   public static String SRC_FILE_EXTENSION = "txt";
   public static String SRC_DELIMITER = ":";
   public static String TARGET_FILE_EXTENSION = "csv";
   public static String TARGET_DELIMITER = "|";
   public static String EMPTY_COL = "";

   public static int MVN_REPORT_COLUMN_COUNT=5;
   public static int RAW_DATA_EPENDENCY_ID_COUNT=3;

   private CSVConstants()
   {
      /* this class' purpose is only to provide constants */
   }
}
