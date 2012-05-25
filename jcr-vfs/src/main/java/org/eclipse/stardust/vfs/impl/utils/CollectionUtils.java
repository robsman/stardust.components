/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: CollectionUtils.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.utils;

import java.util.*;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class CollectionUtils
{

   public static <E> E unwrapSingleton(List<E> result)
   {
      if (1 < result.size())
      {
         // TODO
      }

      return !result.isEmpty() ? result.get(0) : null;
   }

   /**
    * @deprecated Superseded by {@link #newList()}
    */
   @Deprecated
   public static <E> List<E> createList()
   {
      return newList();
   }

   public static <E> List<E> newList()
   {
      return new ArrayList<E>();
   }

   /**
    * @deprecated Superseded by {@link #newSet()}
    */
   @Deprecated
   public static <E> Set<E> createSet()
   {
      return newSet();
   }

   public static <E> Set<E> newSet()
   {
      return new HashSet<E>();
   }

   /**
    * @deprecated Superseded by {@link #newSortedSet()}
    */
   @Deprecated
   public static <E> SortedSet<E> createSortedSet()
   {
      return newSortedSet();
   }

   public static <E> SortedSet<E> newSortedSet()
   {
      return new TreeSet<E>();
   }

   /**
    * @deprecated Superseded by {@link #newMap()}
    */
   @Deprecated
   public static <K, V> Map<K, V> createMap()
   {
      return newMap();
   }

   public static <K, V> Map<K, V> newMap()
   {
      return new HashMap<K, V>();
   }

   /**
    * @deprecated Superseded by {@link #newSortedMap()}
    */
   @Deprecated
   public static <K, V> SortedMap<K, V> createSortedMap()
   {
      return newSortedMap();
   }

   public static <K, V> SortedMap<K, V> newSortedMap()
   {
      return new TreeMap<K, V>();
   }

   private CollectionUtils()
   {
      // utility class
   }
}
