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
package org.eclipse.stardust.vfs.impl.utils;

import java.lang.reflect.Field;
import java.util.List;

import javax.jcr.Session;

import org.apache.jackrabbit.jca.JCAManagedConnection;
import org.apache.jackrabbit.jca.JCASessionHandle;

public class SessionUtils
{
   /**
    * This call checks if JTA handles exist and ensures no logout is performed if
    * no handles exist. (Prevents stuck sessions on Weblogic)
    * @param session
    */
   public static void logout(Session session)
   {
      if (session instanceof JCASessionHandle)
      {
         JCAManagedConnection managedConnection = ((JCASessionHandle) session).getManagedConnection();

         List<JCASessionHandle> handles = null;
         try
         {
            handles = (List<JCASessionHandle>) getFieldValue(managedConnection, "handles");
         }
         catch (Exception e)
         {
            handles = null;
         }

         if (handles == null || !handles.isEmpty())
         {
            session.logout();
         }
      }
      else
      {
         session.logout();
      }

   }

   private static Field getField(Class clazz, String name)
   {
      Field field = null;

      if (null != clazz)
      {
         try
         {
            field = clazz.getDeclaredField(name);
            field.setAccessible(true);
         }
         catch (NoSuchFieldException e)
         {
            // ignore, bubble up to super class
         }
         catch (SecurityException e)
         {
            throw new RuntimeException(e);
         }
         if (null == field)
         {
            field = getField(clazz.getSuperclass(), name);
         }
      }
      return field;
   }

   private static Object getFieldValue(Object instance, String name)
   {
      Field field = getField(instance.getClass(), name);
      if (null != field)
      {
         field.setAccessible(true);
         try
         {
            return field.get(instance);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      else
      {
         throw new RuntimeException();
      }

   }

}
