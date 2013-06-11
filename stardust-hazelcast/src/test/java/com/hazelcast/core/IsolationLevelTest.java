package com.hazelcast.core;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;

public class IsolationLevelTest
{
   private static final Logger LOG = Logger.getLogger(IsolationLevelTest.class);
   
   private static final String MAP_ID = "wurstkuchen";
   
   private static final Config HZ_CONFIG;

   
   private static volatile HazelcastInstance hzInstance;
   
   private static volatile CountDownLatch latch1;
   private static volatile CountDownLatch latch2;
   
   private static boolean ok = true;
   
   
   static {
      System.setProperty("hazelcast.logging.type", "log4j");
      HZ_CONFIG = new XmlConfigBuilder(IsolationLevelTest.class.getClassLoader().getResourceAsStream("hazelcast-isolation-level.xml")).build();
   }
   
   @BeforeClass
   public static void setUp() throws IOException
   {
      hzInstance = Hazelcast.newHazelcastInstance(HZ_CONFIG);
      
      latch1 = new CountDownLatch(1);
      latch2 = new CountDownLatch(1);
   }
   
   @Test
   public void testIt() throws InterruptedException
   {
      final ExecutorService executorService = Executors.newFixedThreadPool(2);
      
      executorService.submit(new Worker1());
      executorService.submit(new Worker2());
      
      latch2.await();
      
      assertTrue(ok);
   }
   
   private static final class Worker1 implements Callable<Void>
   {
      public Void call() throws InterruptedException
      {
         final Transaction tx = hzInstance.getTransaction();
         try
         {
            tx.begin();
            
            hzInstance.getMap(MAP_ID).get(1);
            
            latch1.countDown();
            latch2.await();
            
            tx.commit();
         }
         catch (final Exception e)
         {
            tx.rollback();
            LOG.error("Worker 1 died ...", e);
            ok = false;
            throw new RuntimeException(e);
         }
         
         return null;
      }
   }
   
   private static final class Worker2 implements Callable<Void>
   {
      public Void call() throws InterruptedException
      {
         latch1.await();

         final Transaction tx = hzInstance.getTransaction();
         try
         {
            tx.begin();
            
            hzInstance.getMap(MAP_ID).get(1);
            
            tx.commit();
         }
         catch (final Exception e)
         {
            tx.rollback();
            LOG.error("Worker 2 died ...", e);
            ok = false;
            throw new RuntimeException(e);
         }
         finally
         {
            latch2.countDown();
         }
         
         return null;
      }
   }   
}
