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
 * $Id: AbstractVfsContentServlet.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.eclipse.stardust.vfs.impl.utils.StringUtils;


/**
 * @author sauer
 * @version $Revision: 54136 $
 */
public abstract class AbstractVfsContentServlet extends HttpServlet
{

   static final long serialVersionUID = 1L;
   
   private int downloadBufferSize = 16 * 1024;

   protected abstract int doDownloadFileContent(String fileUri,
         ContentDownloadController downloadManager) throws IOException, ContentUploadFailedException;

   protected abstract int doUploadFileContent(String fileUri, InputStream contentStream,
         int contentLength, String contentType, String contentEncoding)
         throws IOException, ContentDownloadFailedException;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException
   {
      final String fileUri = extractFileUri(req);

      if ( !StringUtils.isEmpty(fileUri))
      {
         // make sure any previous content is discarded
         resp.resetBuffer();
         
         resp.setBufferSize(downloadBufferSize);
         
         ContentDownloadController downloadManager = new ContentDownloadController(resp);
         
         try
         {
            final int status = doDownloadFileContent(fileUri, downloadManager);
            resp.setStatus(status);
            resp.flushBuffer();
         }
         catch (ContentDownloadFailedException cdfe)
         {
            throw new ServletException(MessageFormat.format(
                  "Content download for file ''{0}'' failed", fileUri), cdfe);
         }
      }
      else
      {
         super.doGet(req, resp);
      }
   }

   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException
   {
      if (ServletFileUpload.isMultipartContent(req))
      {
         try
         {
            final ServletFileUpload upload = new ServletFileUpload();

            // extract file ID from request URI

            final String fileUri = extractFileUri(req);

            if ( !StringUtils.isEmpty(fileUri))
            {
               final FileItemIterator fileUploads = upload.getItemIterator(req);
               if (fileUploads.hasNext())
               {
                  FileItemStream fileUpload = fileUploads.next();

                  InputStream contentStream = fileUpload.openStream();
                  try
                  {
                     if (fileUpload.isFormField())
                     {
                        System.out.println("Form field " + fileUpload.getFieldName()
                              + " with value " + Streams.asString(contentStream)
                              + " detected.");
                     }
                     else
                     {
                        System.out.println("File field " + fileUpload.getFieldName()
                              + " with file name " + fileUpload.getName() + " detected.");

                        // final String fileId = fileUpload.getName();

                        String contentType = fileUpload.getContentType();
                        String contentEncoding = null;

                        int contentTypeTerminator = contentType.indexOf(";");
                        if ( -1 != contentTypeTerminator)
                        {
                           // extract from contentType;

                           final String contentTypeExtras = contentType.substring(contentTypeTerminator + 1);

                           ParameterParser extrasParser = new ParameterParser();
                           extrasParser.setLowerCaseNames(true);

                           @SuppressWarnings("unchecked")
                           Map<String, String> parsedExtras = extrasParser.parse(
                                 contentTypeExtras.trim(), ';');

                           contentEncoding = parsedExtras.get("charset");

                           contentType = contentType.substring(0, contentTypeTerminator);
                        }

                        // TODO how to know the content length?
                        int contentLength = -1;

                        final int status = doUploadFileContent(fileUri, contentStream,
                              contentLength, contentType, contentEncoding);
                        
                        resp.setStatus(status);
                     }
                  }
                  catch (ContentUploadFailedException cufe)
                  {
                     throw new ServletException(MessageFormat.format(
                           "Content upload for file ''{0}'' failed", fileUri), cufe);
                  }
                  finally
                  {
                     if (null != contentStream)
                     {
                        contentStream.close();
                     }
                  }
               }

               // TODO how to handle multiple file uploads?
               while (fileUploads.hasNext())
               {
                  FileItemStream fileUpload = fileUploads.next();

                  System.out.println("Ignoring content upload for file "
                        + fileUpload.getName());
               }
            }
         }
         catch (FileUploadException fue)
         {
            throw new ServletException("Content upload for file failed", fue);
         }
      }
      else
      {
         super.doPost(req, resp);
      }
   }

   private String extractFileUri(HttpServletRequest req)
   {
      final String servletPath = req.getServletPath();
      final String requestUri = req.getRequestURI();
      final String contextPath = req.getContextPath();

      final StringBuffer prefixBuilder = new StringBuffer();
      prefixBuilder.append(contextPath);

      if ( !contextPath.endsWith("/") && !servletPath.startsWith("/"))
      {
         prefixBuilder.append("/");
      }
      prefixBuilder.append(servletPath);
      if ( !servletPath.endsWith("/"))
      {
         prefixBuilder.append("/");
      }

      final String prefix = prefixBuilder.toString();

      String fileUri = requestUri.startsWith(prefix) ? requestUri.substring(prefix.length()) : null;

      try
      {
         fileUri = URLDecoder.decode(fileUri, "UTF-8");
      }
      catch (UnsupportedEncodingException uee)
      {
         // ignore
      }

      return fileUri;
   }
   
   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);

      final String downloadBufferSize = config.getInitParameter("downloadBufferSize");
      if ( !StringUtils.isEmpty(downloadBufferSize))
      {
         this.downloadBufferSize = Integer.parseInt(downloadBufferSize);
      }
   }

   public static class ContentDownloadController
   {

      private final HttpServletResponse resp;

      public ContentDownloadController(HttpServletResponse resp)
      {
         this.resp = resp;
      }
      
      public OutputStream getContentOutputStream() throws IOException
      {
         return resp.getOutputStream();
      }
      
      public void setContentLength(int contentLength)
      {
         resp.setContentLength(contentLength);
      }

      public void setContentType(String contentType)
      {
         resp.setContentType(contentType);
      }

      public void setContentEncoding(String contentEncoding)
      {
         // TODO
      }
      
      public void setFilename(String fileName)
      {
         resp.setHeader("Content-Disposition","inline; filename=" + fileName + ";" );
      }
   }

}
