package com.infinity.bpm.test.cache.hazelcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;


public class NearCache
{
   private static final String CACHE_ID_NEAR = "ipp-2nd-level-cache-near";

   private static HazelcastInstance hz;
   
   @Test
   public void nearCachePutGetRemove()
   {
      IMap<Object, Object> cache = hz.getMap(CACHE_ID_NEAR);
      
      assertNotNull(cache);
      
      Object value = cache.get("my-key");
      assertNull(value);
      
      value = cache.put("my-key", "my-value");
      assertNull(value);

      value = cache.get("my-key");
      assertEquals("my-value", value);

      value = cache.remove("my-key");
      assertEquals("my-value", value);

      value = cache.get("my-key");
      assertNull(value);
   }

   @Test
   public void nearCachePutContainsNonexistentKey()
   {
      IMap<Object, Object> cache = hz.getMap(CACHE_ID_NEAR);
      
      assertNotNull(cache);
      
      Object value = cache.get("my-key");
      assertNull(value);
      
      boolean foundKey = cache.containsKey("my-key");
      assertFalse(foundKey);

      value = cache.remove("my-key");
      assertNull(value);

      value = cache.get("my-key");
      assertNull(value);
   }

   @Test
   public void nearCachePutContainsExistentKey()
   {
      IMap<Object, Object> cache = hz.getMap(CACHE_ID_NEAR);
      
      assertNotNull(cache);
      
      Object value = cache.get("my-key");
      assertNull(value);
      
      value = cache.put("my-key", "my-value");
      assertNull(value);

      boolean foundKey = cache.containsKey("my-key");
      assertTrue(foundKey);

      value = cache.remove("my-key");
      assertEquals("my-value", value);

      value = cache.get("my-key");
      assertNull(value);
   }

   @BeforeClass
   public static void init() throws InterruptedException
   {
      hz = Hazelcast.getDefaultInstance();
      
      Thread.sleep(100);
   }
   
   @AfterClass
   public static void shutdown()
   {
      hz = null;
      
      Hazelcast.shutdownAll();
   }
   
}
