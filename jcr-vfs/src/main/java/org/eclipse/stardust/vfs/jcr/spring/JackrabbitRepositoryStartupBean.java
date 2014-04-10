package org.eclipse.stardust.vfs.jcr.spring;

import java.io.IOException;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

public class JackrabbitRepositoryStartupBean
      implements InitializingBean, DisposableBean, ApplicationContextAware
{
   private String jndiName;

   private String repositoryConfig;

   private String repositoryHome;

   private JackrabbitRepository repository;

   private ApplicationContext applicationContext;

   private InitialContext jndiContext;

   public JackrabbitRepositoryStartupBean()
   {
   }

   @Override
   public void afterPropertiesSet() throws Exception
   {
      repository = connect(jndiName, repositoryConfig, repositoryHome);
   }

   private JackrabbitRepository connect(String jndiName, String repositoryConfig,
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

      JackrabbitRepository repository;
      jndiContext = new InitialContext();
      try
      {
         repository = (JackrabbitRepository) jndiContext.lookup(jndiName);
      }
      catch (NamingException e1)
      {
         repository = null;
      }

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

            jndiContext.bind(jndiName, repository);
         }
      }

      return repository;
   }

   @Override
   public void destroy() throws Exception
   {
      if (repository != null)
      {
         jndiContext.unbind(jndiName);
         jndiContext = null;
         
         repository.shutdown();
         repository = null;
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
      return repository;
   }

}
