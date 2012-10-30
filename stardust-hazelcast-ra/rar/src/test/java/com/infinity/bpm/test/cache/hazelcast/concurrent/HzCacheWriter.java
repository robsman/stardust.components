package com.infinity.bpm.test.cache.hazelcast.concurrent;

import static com.infinity.bpm.test.cache.hazelcast.concurrent.CacheWriter.Command.CMD_MAYBE_PUT;
import static com.infinity.bpm.test.cache.hazelcast.concurrent.CacheWriter.Command.CMD_PUT;
import static java.util.Collections.singletonMap;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Transaction;

import ag.carnot.base.Pair;

public class HzCacheWriter implements Runnable, CacheWriter
{
   private final HazelcastInstance hz;
   
   private final String cacheId;
   
   public final BlockingQueue<Object> input;

   public Transaction hzTx = null;

   public HzCacheWriter(HazelcastInstance hz, String cacheId)
   {
      this.hz = hz;
      this.cacheId = cacheId;
      this.input = new LinkedBlockingQueue<Object>();
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
                           if (Transaction.TXN_STATUS_ACTIVE == getTxStatus())
                           {
                              if ((CMD_MAYBE_PUT == cmd) && !cache.tryLock(entry.getKey()))
                              {
                                 System.out.println("Skipping cache write of [" + entry + "] due to concurrent lock.");
                              }
                              else
                              {
                                 System.out.println("Writing [" + entry + "] into cache (in TX).");
                                 cache.put(entry.getKey(), entry.getValue());
                              }
                           }
                           else
                           {
                              System.out.println("Writing [" + entry + "] into cache (no TX).");
                              cache.put(entry.getKey(), entry.getValue());
                           }
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