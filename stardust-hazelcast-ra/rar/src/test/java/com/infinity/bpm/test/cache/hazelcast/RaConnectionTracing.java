package com.infinity.bpm.test.cache.hazelcast;

import org.junit.Test;

import com.infinity.bpm.rt.integration.cache.hazelcast.jca.IppManagedConnectionFactoryImpl;

public class RaConnectionTracing
{

   @Test
   public void initTraceEventsNoDetail()
   {
      System.setProperty(IppManagedConnectionFactoryImpl.PRP_CONNECTION_TRACING_EVENTS,
            "CREATE, TX_START, TX_COMPLETE, CLEANUP, DESTROY");

      IppManagedConnectionFactoryImpl cf = new IppManagedConnectionFactoryImpl();
   }

   @Test
   public void initTraceEventsWithDetail()
   {
      System.setProperty(IppManagedConnectionFactoryImpl.PRP_CONNECTION_TRACING_EVENTS,
            "CREATE, TX_START, TX_COMPLETE, CLEANUP, DESTROY");
      System.setProperty(IppManagedConnectionFactoryImpl.PRP_CONNECTION_TRACING_CALL_STACK,
            "true");

      IppManagedConnectionFactoryImpl cf = new IppManagedConnectionFactoryImpl();
   }

}
