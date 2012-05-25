/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.stardust.vfs.jcr.jca;

// Copied from org.apache.jackrabbit.jca.JCAManagedConnectionFactory.
// Modified to be able to inject Repository instead of bootstrapping one from JCARepositoryManager.

import java.io.PrintWriter;
import java.util.Set;

import javax.jcr.Repository;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.apache.jackrabbit.jca.AnonymousConnection;
import org.apache.jackrabbit.jca.JCAConnectionManager;
import org.apache.jackrabbit.jca.JCAConnectionRequestInfo;

/**
 * Implements the JCA ManagedConnectionFactory contract.
 *
 * @author sauer
 * @version $Revision$
 */
public final class JackrabbitRepositoryMCF implements ManagedConnectionFactory
{

   private static final long serialVersionUID = 1L;

   private Repository repository;

   /**
    * Flag indicating whether the session should be bound to the transaction lifecyle. In
    * other words, if this flag is true the handle will be closed when the transaction
    * ends.
    */
   private Boolean bindSessionToTransaction = Boolean.TRUE;

   /**
    * Log writer.
    */
   private transient PrintWriter logWriter;

   public Repository getRepository()
   {
      return repository;
   }

   public void setRepository(Repository repository)
   {
      this.repository = repository;
   }

   public PrintWriter getLogWriter()
   {
      return logWriter;
   }

   public void setLogWriter(PrintWriter logWriter)
   {
      this.logWriter = logWriter;
   }

   /**
    * Creates a Connection Factory instance.
    */
   public Object createConnectionFactory() throws ResourceException
   {
      return createConnectionFactory(new JCAConnectionManager());
   }

   /**
    * Creates a Connection Factory instance.
    */
   public Object createConnectionFactory(ConnectionManager cm) throws ResourceException
   {
      JCARepositoryHandle handle = new JCARepositoryHandle(this, cm);
      log("Created repository handle (" + handle + ")");
      return handle;
   }

   /**
    * {@inheritDoc}
    * <p/>
    * Creates a new physical connection to the underlying EIS resource manager.
    * <p/>
    * WebSphere 5.1.1 will try to recover an XA resource on startup, regardless whether it
    * was committed or rolled back. On this occasion, <code>cri</code> will be
    * <code>null</code>. In order to be interoperable, we return an anonymous connection,
    * whose XA resource is recoverable-only.
    */
   public ManagedConnection createManagedConnection(Subject subject,
         ConnectionRequestInfo cri) throws ResourceException
   {

      if (cri == null)
      {
         return new AnonymousConnection();
      }
      return createManagedConnection((JCAConnectionRequestInfo) cri);
   }

   /**
    * Creates a new physical connection to the underlying EIS resource manager.
    */
   private ManagedConnection createManagedConnection(JCAConnectionRequestInfo cri)
         throws ResourceException
   {
      return new JCAManagedConnection(this, cri);
   }

   /**
    * Returns a matched connection from the candidate set of connections.
    */
   public ManagedConnection matchManagedConnections(Set set, Subject subject,
         ConnectionRequestInfo cri) throws ResourceException
   {
      for (Object connection : set)
      {
         if (connection instanceof JCAManagedConnection)
         {
            JCAManagedConnection mc = (JCAManagedConnection) connection;
            if (equals(mc.getManagedConnectionFactory()))
            {
               JCAConnectionRequestInfo otherCri = mc.getConnectionRequestInfo();
               if (cri == otherCri || (cri != null && cri.equals(otherCri)))
               {
                  return mc;
               }
            }
         }
      }

      return null;
   }

   /**
    * Log a message.
    */
   public void log(String message)
   {
      log(message, null);
   }

   /**
    * Log a message.
    */
   public void log(String message, Throwable exception)
   {
      if (logWriter != null)
      {
         logWriter.println(message);

         if (exception != null)
         {
            exception.printStackTrace(logWriter);
         }
      }
   }

   /**
    * Return the hash code.
    */
   public int hashCode()
   {
      return repository != null ? repository.hashCode() : 0;
   }

   /**
    * Return true if equals.
    */
   public boolean equals(Object o)
   {
      if (o == this)
      {
         return true;
      }
      else if (o instanceof JackrabbitRepositoryMCF)
      {
         return equals((JackrabbitRepositoryMCF) o);
      }
      else
      {
         return false;
      }
   }

   /**
    * Return true if equals.
    */
   private boolean equals(JackrabbitRepositoryMCF o)
   {
      return equals(repository, o.repository);
   }

   /**
    * Return true if equals.
    */
   private boolean equals(Object o1, Object o2)
   {
      if (o1 == o2)
      {
         return true;
      }
      else if ((o1 == null) || (o2 == null))
      {
         return false;
      }
      else
      {
         return o1.equals(o2);
      }
   }

   public Boolean getBindSessionToTransaction()
   {
      return bindSessionToTransaction;
   }

   public void setBindSessionToTransaction(Boolean bindSessionToTransaction)
   {
      this.bindSessionToTransaction = bindSessionToTransaction;
   }

}
