/*
 * Copyright (c) 2008-2012, Hazel Bilisim Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jca;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.hazelcast.core.Transaction;
import com.hazelcast.impl.ThreadContext;

public class XAResourceImpl implements XAResource {
	private ManagedConnectionImpl managedConnection;
	private int transactionTimeout = -1;
	private final static Map<Xid, ThreadContext> transactionCache = new ConcurrentHashMap<Xid, ThreadContext>();

	public XAResourceImpl(final ManagedConnectionImpl managedConnectionImpl) {
		this.managedConnection = managedConnectionImpl;
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)
	 */
	public void start(final Xid xid, final int flag) throws XAException {
		managedConnection.log(Level.FINEST, "Start XA transaction with XID '" + xid + "' and flag '" + flag + "'.");

		final Transaction tx = managedConnection.getHazelcastInstance().getTransaction();
		/* only start tx if not already done */
		if (Transaction.TXN_STATUS_ACTIVE != tx.getStatus()) {
		   if (transactionTimeout != -1) {
		      tx.setTimeout(transactionTimeout * 1000);
		   }
			tx.begin();
		}
		transactionCache.put(xid, ThreadContext.get());
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.XAResource#end(javax.transaction.xa.Xid, int)
	 */
	public void end(final Xid xid, final int flag) throws XAException {
		managedConnection.log(Level.FINEST, "End XA transaction with XID '" + xid + "' and flag '" + flag + "'.");

      /* nothing to do */
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.XAResource#prepare(javax.transaction.xa.Xid)
	 */
	public int prepare(final Xid xid) throws XAException {
		managedConnection.log(Level.FINEST, "Prepare XA transaction with XID '" + xid + "'.");

		/* nothing to do: locks are already held by Hazelcast */
		return XA_OK;
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.XAResource#commit(javax.transaction.xa.Xid, boolean)
	 */
	public void commit(final Xid xid, final boolean onePhase) throws XAException {
		doInRestoredThreadContext(xid, new Action() {
			public void run(final Transaction tx) {
				if (tx != null) {
					commitInternal(xid, onePhase, tx);
				} else {
					managedConnection.log(Level.SEVERE, "Failed to commit XA transaction with XID '" + xid + "' (onePhase = " + onePhase + "): Transaction cannot be found.");
				}
			}
		});
	}

	private void commitInternal(final Xid xid, final boolean onePhase, final Transaction tx) {
	   managedConnection.log(Level.FINEST, "Commit XA transaction with XID '" + xid + "' (onePhase = " + onePhase + ").");
      try {
         tx.commit();
         managedConnection.log(Level.FINEST, "Commited XA transaction with XID '" + xid + "' (onePhase = " + onePhase + ").");
      } catch (final Exception e) {
         managedConnection.log(Level.SEVERE, "Failed to commit XA transaction with XID '" + xid + "' (onePhase = " + onePhase + "): ", e);
         rollbackInternal(xid, tx);
      }
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.XAResource#rollback(javax.transaction.xa.Xid)
	 */
	public void rollback(final Xid xid) throws XAException {
		doInRestoredThreadContext(xid, new Action() {
			public void run(final Transaction tx) {
				if (tx != null) {
				   rollbackInternal(xid, tx);
				} else {
				   /* nothing we can do here: locks will be released only after tx timeout */
					managedConnection.log(Level.SEVERE, "Failed to rollback XA transaction with XID '" + xid + "': Transaction cannot be found.");
				}
			}
		});
	}

	private void rollbackInternal(final Xid xid, final Transaction tx) {
	   managedConnection.log(Level.FINEST, "Rollback XA transaction with XID '" + xid + "'.");
      try {
         tx.rollback();
         managedConnection.log(Level.FINEST, "Rolledback XA transaction with XID '" + xid + "'.");
      } catch (final Exception e) {
         /* nothing we can do here: locks will be released only after tx timeout */
         managedConnection.log(Level.SEVERE, "Failed to rollback XA transaction with XID '" + xid + "': ", e);
      }
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.XAResource#recover(int)
	 */
	public Xid[] recover(final int flag) throws XAException {
		managedConnection.log(Level.FINEST, "Recover XA transaction with flag '" + flag + "'.");

		return new Xid[0];
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.XAResource#forget(javax.transaction.xa.Xid)
	 */
	public void forget(final Xid xid) throws XAException {
		managedConnection.log(Level.FINEST, "Forget XA transaction with XID '" + xid + "'.");

		transactionCache.remove(xid);
		/* nothing we can do here */
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.XAResource#isSameRM(javax.transaction.xa.XAResource)
	 */
	public boolean isSameRM(final XAResource xaResource) throws XAException {
		managedConnection.log(Level.FINEST, "XA isSameRM(): " + xaResource);

		final boolean isSameRm = (xaResource instanceof XAResourceImpl);
		managedConnection.log(Level.FINEST, "This is " + (isSameRm ? "" : "not") + " the same RM as " + xaResource + ".");
		return isSameRm;
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.XAResource#setTransactionTimeout(int)
	 */
	public boolean setTransactionTimeout(final int seconds) {
		managedConnection.log(Level.FINEST, "Set XA transaction timeout to " + seconds + " seconds.");

		this.transactionTimeout = seconds;
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.XAResource#getTransactionTimeout()
	 */
	public int getTransactionTimeout() throws XAException {
		managedConnection.log(Level.FINEST, "Get XA transaction timeout: '" + transactionTimeout + "'.");

		return transactionTimeout;
	}

	/**
	 * Used in {@link XAResourceImpl#doInRestoredThreadContext(Xid, Action)}
	 */
	/* package-private */ interface Action {
		void run(final Transaction tx);
	}

	/* package-private */ void doInRestoredThreadContext(final Xid xid, final Action action) {
		final ThreadContext tc = transactionCache.remove(xid);
		if (tc != null) {
		   final ThreadContext oldTc = ThreadContext.get();
		   try {
		      /* Order is important ... */
		      ThreadContext.get().setCurrentFactory(tc.getCurrentFactory());
		      /* ... and changed from 1.9 to 2.4! */
		      ThreadContext.get().setCallContext(tc.getCallContext());
		      action.run(tc.getTransaction());
		   } finally {
		      ThreadContext.get().setCurrentFactory(oldTc.getCurrentFactory());
		      ThreadContext.get().setCallContext(oldTc.getCallContext());
		   }
		} else {
			action.run(null);
		}
	}
}
