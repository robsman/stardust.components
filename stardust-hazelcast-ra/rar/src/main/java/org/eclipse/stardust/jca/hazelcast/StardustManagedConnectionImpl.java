package org.eclipse.stardust.jca.hazelcast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;

import org.eclipse.stardust.jca.hazelcast.StardustManagedConnectionFactoryImpl.HzConnectionEvent;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.Transaction;
import com.hazelcast.impl.CallContext;
import com.hazelcast.impl.ThreadContext;
import com.hazelcast.jca.ManagedConnectionImpl;

public class StardustManagedConnectionImpl extends ManagedConnectionImpl
{
   protected static final ConcurrentMap<CallContext, CallContext> predecessors = new ConcurrentHashMap<CallContext, CallContext>();

   protected final StardustManagedConnectionFactoryImpl factory;

   protected List<ConnectionEventListener> lsListeners = null;

   protected final ConnectionRequestInfo cxRequestInfo;

   protected Connection conn;

   protected CallContext callContext;

   protected Transaction tx;

   protected String txThreadId;

   public StardustManagedConnectionImpl(ConnectionRequestInfo cxRequestInfo, StardustManagedConnectionFactoryImpl factory)
   {
      super();

      this.factory = factory;
      this.cxRequestInfo = cxRequestInfo;

      factory.logHzConnectionEvent(this, HzConnectionEvent.CREATE);

      try
      {
         this.conn = (Connection) super.getConnection(null, cxRequestInfo);
      }
      catch (ResourceException re)
      {
         // will not happen
         log(this, "Failed retrieving connection from managed connection");
      }
   }

   public ConnectionRequestInfo getCxRequestInfo()
   {
      return cxRequestInfo;
   }

   @Override
   public void begin() throws ResourceException
   {
      if (null == tx)
      {
         factory.logHzConnectionEvent(this, HzConnectionEvent.TX_START);

         CallContext callContext = ThreadContext.get().getCallContext();
         Transaction tx = callContext.getTransaction();
         if ((null != tx) && (Transaction.TXN_STATUS_ACTIVE == tx.getStatus()))
         {
            log(this, "Suspending outer TX (IPP)");

            CallContext innerCallContext = new CallContext(callContext.getThreadId(), false);

            predecessors.put(innerCallContext, callContext);
            ThreadContext.get().setCallContext(innerCallContext);
         }

         super.begin();

         this.callContext = ThreadContext.get().getCallContext();
         this.tx = Hazelcast.getTransaction();
         this.txThreadId = Thread.currentThread().toString();
      }
      else
      {
         log(this, "Ignoring duplicate TX begin event");
      }
   }

   @Override
   public void commit() throws ResourceException
   {
      factory.logHzConnectionEvent(this, HzConnectionEvent.TX_COMPLETE);

      CallContext callContext = ThreadContext.get().getCallContext();
      CallContext outerCallContext = predecessors.get(callContext);

      if (tx == callContext.getTransaction())
      {
         super.commit();

         if (null != outerCallContext)
         {
            log(this, "Restoring outer TX (IPP)");
            ThreadContext.get().setCallContext(outerCallContext);
         }
      }
      else
      {
         log(this, "txn.commit (IPP)");
         tx.commit();
         fireConnectionEvent(ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);

         // TODO finalize TX on original thread
         callContext.finalizeTransaction();
         if (null != outerCallContext)
         {
            log(this, "Restoring outer TX (IPP)");
            callContext.setTransaction(outerCallContext.getTransaction());
         }

         String threadIx = Thread.currentThread().toString();
         log(this, "Finalizing TX on thread " + threadIx + " that was started on thread " + txThreadId + " (IPP)");
      }

      if (null != outerCallContext)
      {
         predecessors.remove(callContext, outerCallContext);
      }

      this.callContext = null;
      this.tx = null;
      this.txThreadId = null;
   }

   @Override
   public void rollback() throws ResourceException
   {
      factory.logHzConnectionEvent(this, HzConnectionEvent.TX_COMPLETE);

      CallContext callContext = ThreadContext.get().getCallContext();
      CallContext outerCallContext = predecessors.get(callContext);

      if (tx == callContext.getTransaction())
      {
         super.rollback();

         if (null != outerCallContext)
         {
            log(this, "Restoring outer TX (IPP)");
            ThreadContext.get().setCallContext(outerCallContext);
         }
      }
      else
      {
         log(this, "txn.rollback (IPP)");
         tx.rollback();
         fireConnectionEvent(ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);

         // TODO finalize TX on original thread
         callContext.finalizeTransaction();
         if (null != outerCallContext)
         {
            log(this, "Restoring outer TX (IPP)");
            callContext.setTransaction(outerCallContext.getTransaction());
         }

         String threadIx = Thread.currentThread().toString();
         log(this, "Finalizing TX on thread " + threadIx + " that was started on thread " + txThreadId + " (IPP)");
      }

      if (null != outerCallContext)
      {
         predecessors.remove(callContext, outerCallContext);
      }

      this.callContext = null;
      this.tx = null;
      this.txThreadId = null;
   }

   @Override
   public void cleanup() throws ResourceException
   {
      factory.logHzConnectionEvent(this, HzConnectionEvent.CLEANUP);

      super.cleanup();
   }

   @Override
   public void destroy() throws ResourceException
   {
      factory.logHzConnectionEvent(this, HzConnectionEvent.DESTROY);

      super.destroy();
   }

   @Override
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      log(this, "addConnectionEventListener (IPP)");
      if (lsListeners == null)
         lsListeners = new ArrayList<ConnectionEventListener>();
      lsListeners.add(listener);
   }

   @Override
   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      if (lsListeners == null)
         return;
      lsListeners.remove(listener);
   }

   @Override
   public void fireConnectionEvent(int event)
   {
      if (lsListeners == null)
         return;
      ConnectionEvent connnectionEvent = new ConnectionEvent(this, event);
      connnectionEvent.setConnectionHandle(conn);
      for (ConnectionEventListener listener : lsListeners)
      {
         if (event == ConnectionEvent.LOCAL_TRANSACTION_STARTED)
         {
            if (factory.deliverStarted)
               listener.localTransactionStarted(connnectionEvent);
         }
         else if (event == ConnectionEvent.LOCAL_TRANSACTION_COMMITTED)
         {
            if (factory.deliverCommitted)
               listener.localTransactionCommitted(connnectionEvent);
         }
         else if (event == ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK)
         {
            if (factory.deliverRolledback)
               listener.localTransactionRolledback(connnectionEvent);
         }
         else if (event == ConnectionEvent.CONNECTION_CLOSED)
         {
            if (factory.deliverClosed)
               listener.connectionClosed(connnectionEvent);
         }
      }
   }

}
