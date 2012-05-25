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
 * $Id: TestContentServletDownload.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr.web.test;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.stardust.vfs.impl.jcr.web.VfsContentServletClient;
import org.eclipse.stardust.vfs.impl.jcr.web.VfsContentServletClient.IFileContentHandler;
import org.junit.Test;


/**
 * @author sauer
 * @version $Revision: 54136 $
 */
public class TestContentServletDownload
{

   @Test
   public void downloadLicense()
   {
      VfsContentServletClient client = new VfsContentServletClient(
            "http://localhost:8080/carnot-processportal/dms-content-proxy");

      client.retrieveContent("nase/hase.txt", new IFileContentHandler()
      {
         public void handleFileContent(InputStream content, String fileName, long contentLength,
               String contentType, String contentEncoding) throws IOException
         {
            System.out.println("Retrieved " + contentLength + " bytes of content.");
         }

      });
   }

}
