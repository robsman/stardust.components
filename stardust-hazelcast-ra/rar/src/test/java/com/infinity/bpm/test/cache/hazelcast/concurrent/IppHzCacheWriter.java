package com.infinity.bpm.test.cache.hazelcast.concurrent;

import static com.infinity.bpm.test.cache.hazelcast.concurrent.CacheWriter.Command.CMD_MAYBE_PUT;
import static com.infinity.bpm.test.cache.hazelcast.concurrent.CacheWriter.Command.CMD_PUT;
import static java.util.Collections.singletonMap;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Transaction;

import ag.carnot.base.Pair;

import com.infinity.bpm.rt.integration.cache.hazelcast.HazelcastCacheAdapter;

public class IppHzCacheWriter implements Runnable, CacheWriter
{
   private final HazelcastInstance hz;
   
   private final String cacheId;
   
   public final BlockingQueue<Object> input;

   public Transaction hzTx = null;
   
   private HazelcastCacheAdapter hzCacheAdapter;

   public IppHzCacheWriter(HazelcastInstance hz, String cacheId, final String txMode)
   {
      this.hz = hz;
      this.cacheId = cacheId;
      this.input = new LinkedBlockingQueue<Object>();

      this.hzCacheAdapter = new HazelcastCacheAdapter(txMode, null, Collections.emptyMap())
      {
         @Override
         protected void beforeCacheAccess(boolean read)
         {
            // replacing default TX control with a more lightweight variant
            if ("rw".equals(txMode) || ("w".equals(txMode) && !read))
            {
               if (null == hzTx)
               {
                  IppHzCacheWriter.this.hzTx = IppHzCacheWriter.this.hz.getTransaction();
               }

               if (Transaction.TXN_STATUS_NO_TXN == hzTx.getStatus())
               {
                  hzTx.begin();
               }
               else if (Transaction.TXN_STATUS_ACTIVE != hzTx.getStatus())
               {
                  throw new IllegalStateException("Expecting an active TX, but status was " + hzTx.getStatus());
               }
            }
         }

         @Override
         public Object put(Object key, Object value)
         {
            try
            {
               beforeCacheAccess(false);
               
               Transaction hzTx = Hazelcast.getTransaction();
               if ((null != hzTx) && (Transaction.TXN_STATUS_ACTIVE == hzTx.getStatus()))
               {
                  PutMode putMode = PutMode.currentPutMode();
                  if ((PutMode.OPTIONAL == putMode) && !delegate.tryLock(key))
                  {
                     // TODO just return gracefully?
                     throw new ConcurrentModificationException(key.toString());
                  }
               }
               return delegate.put(key, value);
            }
            finally
            {
               afterCacheAccess(false);
            }
         }
      };
   }

   @Override
   public boolean sendCommand(Command cmd)
   {
      return input.offer(cmd);
   }
   
   @Override
   public boolean put(Object key, Object value)
   {
      return input.offer(new Pair<Command, Map<?, ?>>(CMD_PUT, singletonMap(key, value)));
   }
   
   @Override
   public boolean maybePut(Object key, Object value)
   {
      return input.offer(new Pair<Command, Map<?, ?>>(CMD_MAYBE_PUT, singletonMap(key, value)));
   }
   
   @Override
   public int getTxStatus()
   {
      return (null != hzTx) ? hzTx.getStatus() : Transaction.TXN_STATUS_NO_TXN;
   }

   public void run()
   {
      IMap<Object, Object> cache = hz.getMap(cacheId);

      try
      {
         while (true)
         {
            try
            {
               Object value = input.poll(100, TimeUnit.MILLISECONDS);
               if (null != value)
               {
                  if (Command.CMD_TERMINATE == value)
                  {
                     break;
                  }
                  else if (Command.CMD_TX_START == value)
                  {
                     if (null == hzTx)
                     {
                        this.hzTx = Hazelcast.getTransaction();
                     }
                     
                     if (Transaction.TXN_STATUS_NO_TXN == hzTx.getStatus())
                     {
                        System.out.println("Starting TX.");
                        hzTx.begin();
                     }
                  }
                  else if (Command.CMD_TX_COMMIT == value)
                  {
                     if ((null != hzTx)
                           && (Transaction.TXN_STATUS_ACTIVE == hzTx.getStatus()))
                     {
                        System.out.println("Committing TX.");
                        hzTx.commit();
                        this.hzTx = null;
                     }
                  }
                  else if (Command.CMD_TX_ROLLBACK == value)
                  {
                     if ((null != hzTx)
                           && (Transaction.TXN_STATUS_ACTIVE == hzTx.getStatus()))
                     {
                        System.out.println("Aborting TX.");
                        hzTx.rollback();
                        this.hzTx = null;
                     }
                  }
                  else if (value instanceof Pair)
                  {
                     Pair<Command, Map<Object, Object>> request = (Pair<Command, Map<Object, Object>>) value;
                     Command cmd = request.getFirst();
                     Map<Object, Object> entries = request.getSecond();
                     
                     for (Map.Entry<Object, Object> entry : entries.entrySet())
                     {
                        try
                        {
                           PutMode previousPutMode = PutMode.setPutMode(CMD_MAYBE_PUT == cmd
                                 ? PutMode.OPTIONAL
                                 : PutMode.MANDATORY);
                           try
                           {
                              hzCacheAdapter.put(entry.getKey(), entry.getValue());
                           }
                           finally
                           {
                              PutMode.setPutMode(previousPutMode);
                           }
                        }
                        catch (ConcurrentModificationException cme)
                        {
                           System.out.println("Skipping cache write of [" + entry + "] due to concurrent lock.");
                        }
                        catch (Exception e)
                        {
                           e.printStackTrace();
                        }
                     }
                  }
               }
            }
            catch (InterruptedException ie)
            {
               System.out.println("Terminating.");
               break;
            }
            catch (Throwable e)
            {
               e.printStackTrace();
            }
         }
      }
      finally
      {
         if ((null != hzTx) && (Transaction.TXN_STATUS_ACTIVE == hzTx.getStatus()))
         {
            System.out.println("Aborting TX (about to terminate).");
            hzTx.rollback();
            this.hzTx = null;
         }
      }
   }
}