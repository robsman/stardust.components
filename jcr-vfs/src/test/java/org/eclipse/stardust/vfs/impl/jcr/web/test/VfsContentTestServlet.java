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
 * $Id: VfsContentTestServlet.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr.web.test;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.util.Streams;
import org.eclipse.stardust.vfs.impl.jcr.web.AbstractVfsContentServlet;
import org.eclipse.stardust.vfs.impl.jcr.web.ContentDownloadFailedException;
import org.eclipse.stardust.vfs.impl.jcr.web.ContentUploadFailedException;
import org.eclipse.stardust.vfs.impl.jcr.web.VfsContentServletClient;
import org.eclipse.stardust.vfs.impl.jcr.web.VfsContentServletClient.IFileContentHandler;
import org.eclipse.stardust.vfs.impl.utils.StringUtils;


/**
 * @author sauer
 * @version $Revision: 54136 $
 */
public class VfsContentTestServlet extends AbstractVfsContentServlet
{

   static final long serialVersionUID = 1L;

   @Override
   protected int doDownloadFileContent(String fileUri,
         final ContentDownloadController downloadManager) throws IOException, ContentUploadFailedException
   {
      // TODO implement in terms of VFS

      System.out.println("Processing content download for file " + fileUri + ".");

      VfsContentServletClient client = new VfsContentServletClient("http://localhost:8080/montauk-web");

      client.retrieveContent("index.html", new IFileContentHandler()
      {
         public void handleFileContent(InputStream content, String fileName, long contentLength,
               String contentType, String contentEncoding) throws IOException
         {
            downloadManager.setContentLength((int) contentLength);
            downloadManager.setContentType(contentType);

            if ( !StringUtils.isEmpty(contentEncoding))
            {
               downloadManager.setContentEncoding(contentEncoding);
            }
            
            final long nBytesCopied = Streams.copy(content,
                  downloadManager.getContentOutputStream(), false);
            
            if( !StringUtils.isEmpty(fileName))
            {
               downloadManager.setFilename(fileName);
            }
            
            if (nBytesCopied != contentLength)
            {
               // TODO
            }
         }
      });
      
      return HttpServletResponse.SC_OK;
   }

   @Override
   protected int doUploadFileContent(String fileUri, InputStream contentStream,
         int contentLength, String contentType, String contentEncoding)
         throws IOException, ContentDownloadFailedException
   {
      System.out.println("Processing content upload for file " + fileUri + ".");

      byte[] buffer = new byte[4096];
      int nBytesRead = 0;
      do
      {
         nBytesRead = contentStream.read(buffer);
         if (0 < nBytesRead)
         {
            System.out.print(new String(buffer));
         }
         else
         {
            System.out.println();
         }
      }
      while (0 < nBytesRead);

      return HttpServletResponse.SC_OK;
   }

}
