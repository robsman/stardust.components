package org.eclipse.stardust.vfs.impl.jcr.jackrabbit;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.security.DefaultAccessManager;

public class JcrVfsAccessManager extends DefaultAccessManager
{
   
   public void checkRepositoryPermission(int permissions) throws AccessDeniedException,
         RepositoryException
   {
      // disabled
   }

}
