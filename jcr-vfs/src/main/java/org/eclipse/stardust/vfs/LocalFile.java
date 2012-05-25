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
 * $Id: LocalFile.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs;

import java.io.*;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.vfs.impl.FileInfo;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;


/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class LocalFile extends FileInfo
{

   private File localFile;

   private String encoding;

   private List<InputStream> contentStreams = Collections.emptyList();

   protected LocalFile(File localFile)
   {
      this(localFile, null);
   }

   protected LocalFile(File localFile, String encoding)
   {
      super(localFile.getName());

      this.localFile = localFile;
   }

   public long getSize()
   {
      return localFile.length();
   }

   @Deprecated
   public String getEncoding()
   {
      return encoding;
   }

   public BufferedInputStream openContentStream() throws FileNotFoundException
   {
      if ((null != localFile) && localFile.exists())
      {
         BufferedInputStream contentStream = new BufferedInputStream(new FileInputStream(
               localFile));

         if (contentStreams.isEmpty())
         {
            this.contentStreams = CollectionUtils.createList();
         }
         contentStreams.add(contentStream);

         return contentStream;
      }
      else
      {
         throw new IllegalOperationException("Have no file to read content from.");
      }
   }

   public void closeContentStreams()
   {
      for (InputStream contentStream : contentStreams)
      {
         try
         {
            contentStream.close();
         }
         catch (IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }

      this.contentStreams = Collections.emptyList();
   }

}
