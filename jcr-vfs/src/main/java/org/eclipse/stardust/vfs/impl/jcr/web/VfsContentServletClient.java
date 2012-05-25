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
 * $Id: VfsContentServletClient.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * @author sauer
 * @version $Revision: 54136 $
 */
public class VfsContentServletClient
{

   private final String contentServletUri;

   private HttpClient httpClient;

   public VfsContentServletClient(String contentServletUri)
   {
      this.contentServletUri = contentServletUri;

      this.httpClient = new HttpClient();
   }

   public int getConnectionTimeout()
   {
      return httpClient.getHttpConnectionManager().getParams().getConnectionTimeout();
   }

   public void setConnectionTimeout(int connectionTimeout)
   {
      httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(
            connectionTimeout);
   }

   public int retrieveContent(String fileUri, IFileContentHandler handler)
         throws ContentDownloadFailedException
   {
      // apply URL encoding
      GetMethod get;
      try
      {
         get = new GetMethod(contentServletUri + "/"
               + URLEncoder.encode(fileUri, "UTF-8"));
      }
      catch (UnsupportedEncodingException uee)
      {
         throw new ContentDownloadFailedException("Platform dows not support UTF-8 encoding.");
      }

      try
      {
         int status = httpClient.executeMethod(get);

         if (HttpStatus.SC_OK == status)
         {
            final long contentLength = get.getResponseContentLength();
            final Header contentType = get.getResponseHeader("Content-Type");
            final Header fileNameHeader = get.getResponseHeader("Content-Disposition");
            String fileName = null;
            if(fileNameHeader != null)
            {
               Pattern regex = Pattern.compile("filename\\s*=\\s*(.*)\\s*;");
               Matcher match = regex.matcher(fileNameHeader.getValue());
               if(match.find())
               {
                  try
                  {
                     fileName = match.group(1);
                  }
                  catch (Exception e) 
                  {
                     // no matching group - leave fileName empty
                  }
               }
            }

            // TODO
            final String encoding = null;

            final InputStream content = get.getResponseBodyAsStream();
            try
            {
               handler.handleFileContent(content, fileName, contentLength,
                     (null != contentType) ? contentType.getValue() : null, encoding);
            }
            finally
            {
               content.close();
            }

            //System.out.println("Succeeded.");
         }
         else
         {
            // TODO failed
            //System.out.println("Failed, status code: " + status + ".");
         }
         
         return status;
      }
      catch (HttpException he)
      {
         throw new ContentDownloadFailedException(he);
      }
      catch (IOException ioe)
      {
         throw new ContentDownloadFailedException(ioe);
      }
      finally
      {
         // cleaning up
         get.releaseConnection();
      }
   }

   public int uploadContent(final String fileUri, final InputStream content,
         final long contentLength, String contentType, String encoding)
         throws ContentUploadFailedException
   {
      final PartSource partSrc = new PartSource()
      {
         public InputStream createInputStream() throws IOException
         {
            return content;
         }

         public String getFileName()
         {
            return fileUri;
         }

         public long getLength()
         {
            return contentLength;
         }

      };

      final FilePart fileUpload = new FilePart(fileUri, partSrc, contentType, encoding);

      // apply URL encoding
      PostMethod post;
      try
      {
         post = new PostMethod(contentServletUri + "/"
               + URLEncoder.encode(fileUri, "UTF-8"));
      }
      catch (UnsupportedEncodingException uee)
      {
         throw new ContentDownloadFailedException("Platform dows not support UTF-8 encoding.");
      }

      try
      {
         // TODO?
         post.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);

         post.setRequestEntity(new MultipartRequestEntity(new Part[] {fileUpload},
               post.getParams()));

         int status = httpClient.executeMethod(post);

         if (HttpStatus.SC_OK == status)
         {
            // TODO ok
            //System.out.println("Succeeded.");
         }
         else
         {
            // TODO failed
            //System.out.println("Failed, status code: " + status + ".");
         }

         return status;
      }
      catch (HttpException he)
      {
         throw new ContentUploadFailedException(he);
      }
      catch (IOException ioe)
      {
         throw new ContentUploadFailedException(ioe);
      }
      finally
      {
         post.releaseConnection();
      }
   }

   public static interface IFileContentHandler
   {
      void handleFileContent(InputStream content, String fileName, long contentLength, String contentType,
            String contentEncoding) throws IOException;
   }

}
