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
 * $Id: TestContentServletUpload.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr.web.test;

import java.io.ByteArrayInputStream;

import org.eclipse.stardust.vfs.impl.jcr.web.VfsContentServletClient;
import org.junit.Test;


/**
 * @author sauer
 * @version $Revision: 54136 $
 */
public class TestContentServletUpload
{

   @Test
   public void uploadLicense()
   {
      VfsContentServletClient client = new VfsContentServletClient(
            "http://localhost:8080/carnot-processportal/dms-content-proxy");

      byte[] content = "abcdefg".getBytes();

      client.uploadContent("nase/hase.txt", new ByteArrayInputStream(content),
            content.length, "text/plain", "UTF-8");
   }

}
