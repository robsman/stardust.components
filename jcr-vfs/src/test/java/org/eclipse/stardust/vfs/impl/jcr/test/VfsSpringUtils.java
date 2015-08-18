/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC 
 *******************************************************************************/
package org.eclipse.stardust.vfs.impl.jcr.test;

import java.io.IOException;

import org.eclipse.stardust.vfs.LocalFile;
import org.eclipse.stardust.vfs.VfsUtils;

import org.springframework.core.io.ClassPathResource;

public class VfsSpringUtils
{
   public static LocalFile localCpFile(String filePath) throws IOException
   {
      return VfsUtils.localFile(new ClassPathResource(filePath).getFile());
   }
   
   private VfsSpringUtils()
   {
      // utility class
   }
}
