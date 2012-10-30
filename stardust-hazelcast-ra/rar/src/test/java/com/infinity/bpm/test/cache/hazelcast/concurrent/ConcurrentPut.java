package com.infinity.bpm.test.cache.hazelcast.concurrent;

import static com.infinity.bpm.test.cache.hazelcast.concurrent.CacheWriter.Command.CMD_TERMINATE;
import static com.infinity.bpm.test.cache.hazelcast.concurrent.CacheWriter.Command.CMD_TX_COMMIT;
import static com.infinity.bpm.test.cache.hazelcast.concurrent.CacheWriter.Command.CMD_TX_START;
import static com.infinity.bpm.test.cache.hazelcast.concurrent.ConcurrentPut.Mode.MODE_IPP_HZ;
import static com.infinity.bpm.test.cache.hazelcast.concurrent.ConcurrentPut.Mode.MODE_RAW_HZ;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Transaction;

public class ConcurrentPut
{
   private static final String CACHE_ID = "ipp-2nd-level-cache";

   private static HazelcastInstance hz;

   private IMap<Long, Long> cache;
   
   private ExecutorService executor;
   
   enum Mode
   {
      MODE_RAW_HZ, MODE_IPP_HZ
   }
   
   private final Mode mode = Mode.MODE_IPP_HZ;

   private CacheWriter writer1;

   private CacheWriter writer2;

   @Test
   public void nontransactionalConcurrentPutsWithDifferentKeysMustNotBlock() throws Exception
   {
      assertThat(cache.get(1L), is(nullValue()));
      assertThat(cache.get(2L), is(nullValue()));
      assertThat(cache.get(3L), is(nullValue()));
      assertThat(cache.get(4L), is(nullValue()));
      
      writer1.put(1L, 1L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(notNullValue()));
      assertThat(cache.get(2L), is(nullValue()));
      assertThat(cache.get(3L), is(nullValue()));
      assertThat(cache.get(4L), is(nullValue()));
      
      writer2.put(2L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(notNullValue()));
      assertThat(cache.get(2L), is(notNullValue()));
      assertThat(cache.get(3L), is(nullValue()));
      assertThat(cache.get(4L), is(nullValue()));
      
      writer1.put(3L, 1L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(notNullValue()));
      assertThat(cache.get(2L), is(notNullValue()));
      assertThat(cache.get(3L), is(notNullValue()));
      assertThat(cache.get(4L), is(nullValue()));
      
      writer2.put(4L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(notNullValue()));
      assertThat(cache.get(2L), is(notNullValue()));
      assertThat(cache.get(3L), is(notNullValue()));
      assertThat(cache.get(4L), is(notNullValue()));
   }

   @Test
   public void nontransactionalConcurrentMaybePutsWithDifferentKeysMustNotBlock() throws Exception
   {
      assertThat(cache.get(1L), is(nullValue()));
      assertThat(cache.get(2L), is(nullValue()));
      assertThat(cache.get(3L), is(nullValue()));
      assertThat(cache.get(4L), is(nullValue()));
      
      writer1.maybePut(1L, 1L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(notNullValue()));
      assertThat(cache.get(2L), is(nullValue()));
      assertThat(cache.get(3L), is(nullValue()));
      assertThat(cache.get(4L), is(nullValue()));
      
      writer2.maybePut(2L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(notNullValue()));
      assertThat(cache.get(2L), is(notNullValue()));
      assertThat(cache.get(3L), is(nullValue()));
      assertThat(cache.get(4L), is(nullValue()));
      
      writer1.maybePut(3L, 1L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(notNullValue()));
      assertThat(cache.get(2L), is(notNullValue()));
      assertThat(cache.get(3L), is(notNullValue()));
      assertThat(cache.get(4L), is(nullValue()));
      
      writer2.maybePut(4L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(notNullValue()));
      assertThat(cache.get(2L), is(notNullValue()));
      assertThat(cache.get(3L), is(notNullValue()));
      assertThat(cache.get(4L), is(notNullValue()));
   }

   @Test
   public void nontransactionalConcurrentPutsWithSameKeyMustNotBlock() throws Exception
   {
      assertThat(cache.get(1L), is(nullValue()));
      
      writer1.put(1L, 1L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(1L));
      
      writer2.put(1L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(2L));
      
      writer1.put(1L, 3L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(3L));
      
      writer2.put(1L, 4L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(4L));
   }

   @Test
   public void nontransactionalConcurrentMaybePutsWithSameKeyMustNotBlock() throws Exception
   {
      assertThat(cache.get(1L), is(nullValue()));
      
      writer1.maybePut(1L, 1L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(1L));
      
      writer2.maybePut(1L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(2L));
      
      writer1.maybePut(1L, 3L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(3L));
      
      writer2.maybePut(1L, 4L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(4L));
   }

   @Test
   public void transactionalConcurrentPutsWithDifferentKeyMustNotBlock() throws Exception
   {
      assertThat(cache.get(1L), is(nullValue()));
      assertThat(cache.get(2L), is(nullValue()));
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      
      writer1.sendCommand(CMD_TX_START);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      
      writer1.put(1L, 1L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(nullValue()));
      assertThat(cache.get(2L), is(nullValue()));
      
      writer2.sendCommand(CMD_TX_START);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      
      writer2.put(2L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache.get(1L), is(nullValue()));
      assertThat(cache.get(2L), is(nullValue()));
      
      writer1.sendCommand(CMD_TX_COMMIT);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      
      assertThat(cache.get(1L), is(notNullValue()));
      assertThat(cache.get(2L), is(nullValue()));
      
      writer2.sendCommand(CMD_TX_COMMIT);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      
      assertThat(cache.get(1L), is(notNullValue()));
      assertThat(cache.get(2L), is(notNullValue()));
   }
   
   @Test
   public void transactionalConcurrentPutsWithSameKeyMustBlock() throws Exception
   {
      assertThat(cache, not(hasKey(1L)));
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      
      writer1.sendCommand(CMD_TX_START);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      
      writer1.put(1L, 1L);
      Thread.sleep(100L);
      
      assertThat(cache, not(hasKey(1L)));
      
      writer2.sendCommand(CMD_TX_START);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      
      writer2.put(1L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache, not(hasKey(1L)));
      
      writer1.sendCommand(CMD_TX_COMMIT);
      Thread.sleep(200L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      
      assertThat(cache, hasKey(1L));
      assertThat(cache, hasEntry(1L, 1L));
      
      writer2.sendCommand(CMD_TX_COMMIT);
      Thread.sleep(200L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));

      // writer2 now should have waited with PUT
      assertThat(cache, hasKey(1L));
      assertThat(cache, hasEntry(1L, 2L));
   }
   
   @Test
   public void transactionalConcurrentMaybePutsWithSameKeyMustNotBlock() throws Exception
   {
      assertThat(cache, not(hasKey(1L)));
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      
      writer1.sendCommand(CMD_TX_START);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      
      writer1.put(1L, 1L);
      Thread.sleep(100L);
      
      assertThat(cache, not(hasKey(1L)));
      
      writer2.sendCommand(CMD_TX_START);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      
      writer2.maybePut(1L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache, not(hasKey(1L)));
      
      writer1.sendCommand(CMD_TX_COMMIT);
      Thread.sleep(200L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      
      assertThat(cache, hasKey(1L));
      assertThat(cache.get(1L), is(1L));
      
      writer2.sendCommand(CMD_TX_COMMIT);
      Thread.sleep(200L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));

      // writer2 should not have succeeded
      assertThat(cache, hasKey(1L));
      assertThat(cache.get(1L), is(1L));

      writer2.sendCommand(CMD_TX_START);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      
      writer2.put(1L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache, hasKey(1L));
      assertThat(cache.get(1L), is(1L));

      writer2.sendCommand(CMD_TX_COMMIT);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));

      // writer2 now should have succeeded
      assertThat(cache, hasKey(1L));
      assertThat(cache, hasEntry(1L, 2L));
   }
   
   @Test
   public void transactionalConcurrentMaybePutsWithInterleavedKeysMustNotBlock() throws Exception
   {
      assertThat(cache, not(hasKey(1L)));
      assertThat(cache, not(hasKey(2L)));
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      
      writer1.sendCommand(CMD_TX_START);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      
      writer1.maybePut(1L, 1L);
      Thread.sleep(100L);
      
      assertThat(cache, not(hasKey(1L)));
      assertThat(cache, not(hasKey(2L)));
      
      writer2.sendCommand(CMD_TX_START);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      
      writer2.maybePut(2L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache, not(hasKey(1L)));
      assertThat(cache, not(hasKey(2L)));
      
      writer1.maybePut(2L, 1L);
      Thread.sleep(100L);
      
      assertThat(cache, not(hasKey(1L)));
      assertThat(cache, not(hasKey(2L)));
      
      writer2.maybePut(1L, 2L);
      Thread.sleep(100L);
      
      assertThat(cache, not(hasKey(1L)));
      assertThat(cache, not(hasKey(2L)));
      
      writer1.sendCommand(CMD_TX_COMMIT);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_ACTIVE));
      
      assertThat(cache, hasKey(1L));
      assertThat(cache, hasEntry(1L, 1L));
      assertThat(cache, not(hasKey(2L)));
      
      writer2.sendCommand(CMD_TX_COMMIT);
      Thread.sleep(100L);
      
      assertThat(writer1.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      assertThat(writer2.getTxStatus(), is(Transaction.TXN_STATUS_NO_TXN));
      
      assertThat(cache, hasKey(1L));
      assertThat(cache, hasEntry(1L, 1L));
      assertThat(cache, hasKey(2L));
      assertThat(cache, hasEntry(2L, 2L));
   }
   
   @BeforeClass
   public static void bootstrap() throws InterruptedException
   {
      hz = Hazelcast.getDefaultInstance();

      Thread.sleep(100);
   }
   
   @Before
   public void init() throws InterruptedException
   {
      this.cache = hz.getMap(CACHE_ID);
      assertNotNull("Cache must be defined.", cache);
      
      if (MODE_RAW_HZ == mode)
      {
         this.writer1 = new HzCacheWriter(hz, CACHE_ID);
         this.writer2 = new HzCacheWriter(hz, CACHE_ID);
      }
      else if (MODE_IPP_HZ == mode)
      {
         this.writer1 = new IppHzCacheWriter(hz, CACHE_ID, "none");
         this.writer2 = new IppHzCacheWriter(hz, CACHE_ID, "none");
      }

      this.executor = Executors.newFixedThreadPool(2);
      executor.execute(writer1);
      executor.execute(writer2);
   }
   
   @After
   public void cleanup() throws Exception
   {
      writer1.sendCommand(CMD_TERMINATE);
      writer2.sendCommand(CMD_TERMINATE);
      Thread.sleep(200);

      executor.shutdown();
      
      if (null != cache)
      {
         System.out.println("Cleaning cache.");
         cache.destroy();
         
         Thread.sleep(100);
      }
   }

   @AfterClass
   public static void shutdown()
   {
      hz = null;

      Hazelcast.shutdownAll();
   }
}
