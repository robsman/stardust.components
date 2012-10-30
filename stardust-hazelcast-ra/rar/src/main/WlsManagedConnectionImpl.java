package com.hazelcast.jca;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.Transaction;
import com.hazelcast.impl.ThreadContext;

import java.util.ArrayList;
import java.util.List;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;

public class WlsManagedConnectionImpl extends ManagedConnectionImpl
{
  private List<ConnectionEventListener> lsListeners = null;
  private final ConnectionRequestInfo cxRequestInfo;
  private Connection conn;

  public WlsManagedConnectionImpl(ConnectionRequestInfo cxRequestInfo)
  {
    this.cxRequestInfo = cxRequestInfo;
    try
    {
      this.conn = ((Connection)super.getConnection(null, cxRequestInfo));
    }
    catch (ResourceException re)
    {
      log(this, "Failed retrieving connection from managed connection");
    }
  }

  public ConnectionRequestInfo getCxRequestInfo()
  {
    return this.cxRequestInfo;
  }

  public void begin()
    throws ResourceException
  {
    Transaction tx = Hazelcast.getTransaction();
    if ((tx != null) && (tx.getStatus() == 0))
    {
      super.begin();
    }
    else
    {
      log(this, "Ignoring duplicate TX begin event");
    }
  }

  public void commit() throws ResourceException {
     log(this, "txn.commit");
   if (ThreadContext.get().getTransaction() != null) {
      ThreadContext.get().getTransaction().commit();
   } else {
      log(this, "No Hazelcast transaction - COMMIT ignored");
   }
     fireConnectionEvent(ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
 }

 public void rollback() throws ResourceException {
     log(this, "txn.rollback");
     if (ThreadContext.get().getTransaction() != null) {
      ThreadContext.get().getTransaction().rollback();
     } else {
      log(this, "No Hazelcast transaction - ROLLBACK ignored");
   }
   fireConnectionEvent(ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);
 }

  public void addConnectionEventListener(ConnectionEventListener listener)
  {
    log(this, "addConnectionEventListener (WLS)");
    if (this.lsListeners == null)
      this.lsListeners = new ArrayList();
    this.lsListeners.add(listener);
  }

  public void removeConnectionEventListener(ConnectionEventListener listener)
  {
    if (this.lsListeners == null)
      return;
    this.lsListeners.remove(listener);
  }

  public void fireConnectionEvent(int event)
  {
     boolean deliverStarted = false; //This is also false for WAS
     boolean deliverCommitted = false; //This was true for WAS - Fails during normal login
     boolean deliverRolledback = false; //This was true for WAS - Fails on setRollbackOnly calls (so need to provoke an error)
     boolean deliverClosed = true; //This is also true for WAS but maybe I never hit it? - Don't know how to force a close


    if (this.lsListeners == null)
      return;
    ConnectionEvent connnectionEvent = new ConnectionEvent(this, event);
    connnectionEvent.setConnectionHandle(this.conn);
    for (ConnectionEventListener listener : this.lsListeners)
    {
      if (event == 2)
      {
        if (deliverStarted)
          listener.localTransactionStarted(connnectionEvent);
      }
      else if (event == 3)
      {
        if (deliverCommitted)
          listener.localTransactionCommitted(connnectionEvent);
      }
      else if (event == 4)
      {
        if (deliverRolledback)
          listener.localTransactionRolledback(connnectionEvent);
      } else {
        if (event != 1)
          continue;
        if (deliverClosed)
          listener.connectionClosed(connnectionEvent);
      }
    }
  }
}