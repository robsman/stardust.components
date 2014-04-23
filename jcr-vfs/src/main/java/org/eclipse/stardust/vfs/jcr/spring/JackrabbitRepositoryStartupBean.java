package org.eclipse.stardust.vfs.jcr.spring;

import java.io.IOException;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.naming.NamingException;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.eclipse.stardust.vfs.impl.jcr.jackrabbit.JackrabbitRepositoryContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

public class JackrabbitRepositoryStartupBean
      implements InitializingBean, DisposableBean, ApplicationContextAware,
      FactoryBean<Repository>
{
   private String jndiName;

   private String repositoryConfig;

   private String repositoryHome;

   private ApplicationContext applicationContext;
   
   private boolean bindToJndi;

   public JackrabbitRepositoryStartupBean()
   {
      bindToJndi = true;
   }

   public Repository getObject()
   {
      return JackrabbitRepositoryContext.getRepository(jndiName);
   }

   public Class< ? > getObjectType()
   {
      Repository repository = JackrabbitRepositoryContext.getRepository(
            jndiName);
      return (repository != null ? repository.getClass() : null);
   }

   public boolean isSingleton()
   {
      return true;
   }

   @Override
   public void afterPropertiesSet() throws Exception
   {
      connect(jndiName, repositoryConfig, repositoryHome);
   }

   private synchronized void connect(String jndiName, String repositoryConfig,
         String repositoryHome) throws IOException, NamingException, RepositoryException
   {
      if (jndiName == null)
      {
         throw new IllegalArgumentException("jndiName: " + jndiName);
      }
      if (repositoryConfig == null)
      {
         throw new IllegalArgumentException("repositoryConfig: " + repositoryConfig);
      }
      if (repositoryHome == null)
      {
         throw new IllegalArgumentException("repositoryHome: " + repositoryHome);
      }

      Repository repository = JackrabbitRepositoryContext.getRepository(
            jndiName);

      if (repository == null)
      {
         Resource resource = applicationContext.getResource(repositoryConfig);
         if (resource == null)
         {
            throw new IllegalArgumentException("repositoryConfig not found at: "
                  + repositoryConfig);
         }
         else
         {
            RepositoryConfig config = RepositoryConfig.create(resource.getInputStream(),
                  repositoryHome);
            repository = RepositoryImpl.create(config);

            JackrabbitRepositoryContext.putRepository(jndiName, repository);
            if (bindToJndi)
            {
               JackrabbitRepositoryContext.bind(jndiName, repository);
            }
         }
      }
   }

   @Override
   public synchronized void destroy() throws Exception
   {
      Repository repository = JackrabbitRepositoryContext.getRepository(
            jndiName);
      if (repository != null)
      {
         if (bindToJndi)
         {
            JackrabbitRepositoryContext.unbind(jndiName);
         }
         if (repository instanceof JackrabbitRepository)
         {
            ((JackrabbitRepository) repository).shutdown();
         }
         JackrabbitRepositoryContext.removeRepository(repository);
      }
   }

   @Override
   public void setApplicationContext(ApplicationContext context) throws BeansException
   {
      this.applicationContext = context;
   }

   public String getJndiName()
   {
      return jndiName;
   }

   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public String getRepositoryConfig()
   {
      return repositoryConfig;
   }

   public void setRepositoryConfig(String repositoryConfig)
   {
      this.repositoryConfig = repositoryConfig;
   }

   public String getRepositoryHome()
   {
      return repositoryHome;
   }

   public void setRepositoryHome(String repositoryHome)
   {
      this.repositoryHome = repositoryHome;
   }

   public Repository getRepository()
   {
      return JackrabbitRepositoryContext.getRepository(jndiName);
   }

   public boolean isBindToJndi()
   {
      return bindToJndi;
   }

   public void setBindToJndi(boolean bindToJndi)
   {
      this.bindToJndi = bindToJndi;
   }
   
}
