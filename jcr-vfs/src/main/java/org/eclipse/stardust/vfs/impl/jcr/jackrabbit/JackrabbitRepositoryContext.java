package org.eclipse.stardust.vfs.impl.jcr.jackrabbit;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Repository;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

public class JackrabbitRepositoryContext
{
   private static InitialContext jndiContext;

   private static final Map<String, Repository> repositoryContext = new HashMap<String, Repository>();

   private static InitialContext getJndiContext()
   {
      if (jndiContext == null)
      {
         try
         {
            jndiContext = new InitialContext();
         }
         catch (NamingException e)
         {
            throw new RuntimeException(e);
         }
      }

      return jndiContext;
   }
   
   public static Repository lookup(String jndiName) throws NamingException
   {
      return (Repository) getJndiContext().lookup(jndiName);
   }

   public static void bind(String jndiName, Object value)
   {
      InitialContext context = getJndiContext();

      ensureContextsExist(jndiName, context);
      
      try
      {
         context.bind(jndiName, value);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }

   private static void ensureContextsExist(String jndiName, InitialContext rootContext)
   {
      try
      {
         Name name = rootContext.getNameParser("").parse(jndiName);

         int size = name.size();
         if (size > 1)
         {
            Context currentSubContext = rootContext;

            for (int i = 0; i < size - 1; i++ )
            {
               String subContextName = name.get(i);
               try
               {
                  currentSubContext.lookup(subContextName);
               }
               catch (NamingException e)
               {
                  currentSubContext = rootContext.createSubcontext(subContextName);
               }
            }
         }
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
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

   public synchronized static void putRepository(String jndiName,
         Repository repository)
   {
      repositoryContext.put(jndiName, repository);
   }

   public synchronized static Repository getRepository(String jndiName)
   {
      return repositoryContext.get(jndiName);
   }

   public synchronized static void removeRepository(Repository repository)
   {
      repositoryContext.remove(repository);
   }
}