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
 * $Id: FolderContent.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl;

import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.vfs.IResource;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;
import org.eclipse.stardust.vfs.impl.utils.CompareHelper;


/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class FolderContent<E extends IResource>
{

   private List<E> content = CollectionUtils.newList();

   public int getSize()
   {
      return content.size();
   }

   public E get(int index)
   {
      return content.get(index);
   }

   public E findById(String id)
   {
      for (E member : content)
      {
         if (CompareHelper.areEqual(id, member.getId()))
         {
            return member;
         }
      }
      return null;
   }

   public E findByName(String name)
   {
      for (E member : content)
      {
         if (CompareHelper.areEqual(name, member.getName()))
         {
            return member;
         }
      }
      return null;
   }

   public List<E> getAll()
   {
      return Collections.unmodifiableList(content);
   }

   public void add(E member)
   {
      content.add(member);
   }

   public void remove(int index)
   {
      content.remove(index);
   }

}
