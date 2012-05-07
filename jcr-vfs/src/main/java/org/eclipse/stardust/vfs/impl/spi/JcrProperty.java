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
 * $Id: JcrProperty.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.spi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class JcrProperty
{

   public static PropertyDefinition getDefinition(Property property)
         throws RepositoryException
   {
      return property.getDefinition();
   }

   public static Node getNode(Property property) throws ValueFormatException,
         RepositoryException
   {
      return property.getNode();
   }

   public static int getType(Property property) throws RepositoryException
   {
      return property.getType();
   }

   public static String getString(Property property) throws ValueFormatException,
         RepositoryException
   {
      return property.getString();
   }

   public static ArrayList<String> getStrings(Property property)
         throws ValueFormatException, RepositoryException
   {
      Value[] values = property.getValues();
      ArrayList<String> list = new ArrayList<String>(values.length);
      for (int i = 0; i < values.length; i++ )
      {
         list.add(values[i].getString());
      }
      return list;
   }

   public static Calendar getDate(Property property) throws ValueFormatException,
         RepositoryException
   {
      return property.getDate();
   }

   public static void setValue(Property property, Calendar value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      property.setValue(value);
   }

   public static void setValue(Property property, Value [] value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      property.setValue(value);
   }

   public static boolean getBoolean(Property property) throws ValueFormatException,
         RepositoryException
   {
      return property.getBoolean();
   }

   public static ArrayList<Boolean> getBooleans(Property property) throws ValueFormatException,
         RepositoryException
   {
      Value[] values = property.getValues();
      ArrayList<Boolean> list = new ArrayList<Boolean>(values.length);
      for (int i = 0; i < values.length; i++ )
      {
         list.add(new Boolean(values[i].getBoolean()));
      }
      return list;
   }

   public static long getLong(Property property) throws ValueFormatException,
         RepositoryException
   {
      return property.getLong();
   }

   public static BigDecimal getDecimal(Property property) throws ValueFormatException,
         RepositoryException
   {
      return property.getDecimal();
   }

   public static ArrayList<Long> getLongs(Property property) throws ValueFormatException,
         RepositoryException
   {
      Value[] values = property.getValues();
      ArrayList<Long> list = new ArrayList<Long>(values.length);
      for (int i=0; i<values.length; i++)
      {
         list.add(new Long(values[i].getLong()));
      }
      return list;
   }

   public static double getDouble(Property property) throws ValueFormatException,
         RepositoryException
   {
      return property.getDouble();
   }

   public static ArrayList<Double> getDoubles(Property property) throws ValueFormatException,
         RepositoryException
   {
      Value[] values = property.getValues();
      ArrayList<Double> list = new ArrayList<Double>(values.length);
      for (int i = 0; i < values.length; i++ )
      {
         list.add(new Double(values[i].getDouble()));
      }
      return list;
   }

   public static void setValue(Property property, boolean value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      property.setValue(value);
   }

   public static void setValue(Property property, long value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      property.setValue(value);
   }

   public static void setValue(Property property, double value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      property.setValue(value);
   }

   public static void setValue(Property property, BigDecimal value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      property.setValue(value);
   }

   public static void setValue(Property property, String value)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      property.setValue(value);
   }

   public static long getLength(Property property) throws ValueFormatException,
         RepositoryException
   {
      return property.getLength();
   }

   public static Binary getBinary(Property property) throws ValueFormatException,
         RepositoryException
   {
      return property.getBinary();
   }

}
