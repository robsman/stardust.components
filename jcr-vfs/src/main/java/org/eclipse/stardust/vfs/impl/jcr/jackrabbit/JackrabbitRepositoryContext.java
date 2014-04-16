package org.eclipse.stardust.vfs.impl.jcr.jackrabbit;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.jcr.Repository;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.stardust.vfs.impl.utils.RepositoryHelper;

public class JackrabbitRepositoryContext
{
   private static InitialContext jndiContext;

   private static final Map<String, Repository> repositoryContext = new HashMap<String, Repository>();

   public synchronized static InitialContext getJndiContext()
   {
      if (jndiContext == null)
      {
         Hashtable<Object, Object> env = new Hashtable<Object, Object>();
         env.put(Context.INITIAL_CONTEXT_FACTORY,
               RepositoryHelper.DUMMY_INITIAL_CONTEXT_FACTORY_CLASS_NAME);

         env.put(Context.PROVIDER_URL, "localhost");
         try
         {
            jndiContext = new InitialContext(env);
         }
         catch (NamingException e)
         {
            throw new RuntimeException(e);
         }
      }

      return jndiContext;
   }

   public static void unbind(String jndiName)
   {
      if (jndiContext != null)
      {
         try
         {
            Object object = jndiContext.lookup(jndiName);
            if (object != null)
            {
               jndiContext.unbind(jndiName);
            }
         }
         catch (NamingException e)
         {

         }

      }
   }

   public synchronized static Map<String, Repository> getRepositoryContext()
   {
      return repositoryContext;
   }
   
   public synchronized static Repository getRepository(String jndiName)
   {
      return repositoryContext.get(jndiName);
   }
}