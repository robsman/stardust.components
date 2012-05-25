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
 * $Id: ProxyVfsContentServlet.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr.web;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.util.Streams;
import org.eclipse.stardust.vfs.impl.jcr.web.VfsContentServletClient.IFileContentHandler;
import org.eclipse.stardust.vfs.impl.utils.StringUtils;


/**
 * @author sauer
 * @version $Revision: 54136 $
 */
public class ProxyVfsContentServlet extends AbstractVfsContentServlet
{

   static final long serialVersionUID = 1L;

   private String contentServletUri;
   
   private int connectionTimeout = 5000;

   @Override
   protected int doDownloadFileContent(String fileUri,
         final ContentDownloadController downloadManager) throws IOException,
         ContentUploadFailedException
   {
      System.out.println("Processing content download for file " + fileUri
            + ", forwarding to " + contentServletUri + ".");

      VfsContentServletClient client = new VfsContentServletClient(contentServletUri);
      client.setConnectionTimeout(connectionTimeout);

      UpstreamContentHandler upstreamHandler = new UpstreamContentHandler(downloadManager);
      client.retrieveContent(fileUri, upstreamHandler);

      return upstreamHandler.getStatus();
   }

   @Override
   protected int doUploadFileContent(String fileUri, InputStream contentStream,
         int contentLength, String contentType, String contentEncoding)
         throws IOException, ContentDownloadFailedException
   {
      System.out.println("Processing content upload for file " + fileUri
            + ", forwarding to " + contentServletUri + ".");

      VfsContentServletClient client = new VfsContentServletClient(contentServletUri);
      client.setConnectionTimeout(connectionTimeout);

      return client.uploadContent(fileUri, contentStream, contentLength, contentType,
            contentEncoding);
   }

   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);

      this.contentServletUri = config.getInitParameter("contentServletUri");
      
      final String connectionTimeout = config.getInitParameter("connectionTimeout");
      if ( !StringUtils.isEmpty(connectionTimeout))
      {
         this.connectionTimeout = Integer.parseInt(connectionTimeout);
      }
   }

   private static class UpstreamContentHandler implements IFileContentHandler
   {
      private final ContentDownloadController downloadManager;
      
      private int status;

      public UpstreamContentHandler(ContentDownloadController downloadManager)
      {
         this.downloadManager = downloadManager;
      }

      public int getStatus()
      {
         return status;
      }

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
         
         if (nBytesCopied == contentLength)
         {
            this.status = HttpServletResponse.SC_OK;
         }
         else
         {
            // TODO
            this.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
         }
      }
   }

}
