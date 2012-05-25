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
 * $Id: JcrDocumentRepositoryServiceBean.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.jcr;

import static org.eclipse.stardust.vfs.impl.utils.StringUtils.isEmpty;

import org.eclipse.stardust.vfs.impl.jcr.JcrDocumentRepositoryService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;


/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public class JcrDocumentRepositoryServiceBean extends JcrDocumentRepositoryService
      implements InitializingBean, BeanFactoryAware
{

   private BeanFactory beanFactory;

   private String jcrSessionFactoryName;

   public String getSessionFactoryName()
   {
      return jcrSessionFactoryName;
   }

   public void setSessionFactoryName(String sessionFactoryName)
   {
      this.jcrSessionFactoryName = sessionFactoryName;
   }

   @Override
   public ISessionFactory getSessionFactory()
   {
      ISessionFactory sessionFactory = super.getSessionFactory();
      
      if ((null == sessionFactory) && !isEmpty(jcrSessionFactoryName)
            && beanFactory.containsBean(jcrSessionFactoryName))
      {
         sessionFactory = (ISessionFactory) beanFactory.getBean(jcrSessionFactoryName,
               ISessionFactory.class);
         
         if (null != sessionFactory)
         {
            setSessionFactory(sessionFactory);
         }
      }
      
      return sessionFactory;
   }

   public void setBeanFactory(BeanFactory beanFactory) throws BeansException
   {
      this.beanFactory = beanFactory;
   }

   public void afterPropertiesSet() throws Exception
   {
      if ((null == super.getSessionFactory()) && isEmpty(jcrSessionFactoryName))
      {
         throw new IllegalArgumentException("Either 'sessionFactory' or 'sessionFactoryName' must be specified.");
      }
   }

}
