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

/**
 * This class allows to augment a dependency with additional information. It expects to be
 * provided with
 * <ul>
 * <li>a dependency object</li>
 * <li>the information to add stored in a String array</li>
 * </ul>
 *
 */
public class DependencyInfo implements Comparable<DependencyInfo>
{
   private final Dependency dep;

   private final String[] info;

   public DependencyInfo(final Dependency dep, final String[] info)
   {
      if (dep == null)
      {
         throw new NullPointerException("Dependency must not be null.");
      }
      if (info == null)
      {
         throw new NullPointerException("Info must not be null.");
      }

      this.dep = dep;
      this.info = info.clone();
      for (int i = 0, itemCount = this.info.length; i < itemCount; i++)
      {
         String item = this.info[i];
         this.info[i] = (item == null || item.trim().length() == 0) ? null : item.trim();
      }
   }

   public Dependency dep()
   {
      return dep;
   }

   public String[] info()
   {
      return info.clone();
   }

   @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder();
      result.append(dep.toString());
      for (final String s : info)
      {
         result.append(CSVConstants.TARGET_DELIMITER);
         result.append(s == null ? CSVConstants.EMPTY_COL : s);
      }
      return result.toString();
   }

   @Override
   public int compareTo(final DependencyInfo that)
   {
      return that == null ? -1 : this.dep.compareTo(that.dep);
   }

   public String toStringExtended()
   {
      final StringBuilder result = new StringBuilder();
      result.append(dep.toStringExtended());
      for (final String s : info)
      {
         result.append(CSVConstants.TARGET_DELIMITER);
         result.append(s == null ? CSVConstants.EMPTY_COL : s);
      }
      return result.toString();
   }
}
