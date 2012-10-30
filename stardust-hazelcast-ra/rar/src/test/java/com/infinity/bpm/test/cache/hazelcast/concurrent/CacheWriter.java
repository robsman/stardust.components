package com.infinity.bpm.test.cache.hazelcast.concurrent;

public interface CacheWriter extends Runnable
{
   boolean sendCommand(Command cmd);

   boolean put(Object key, Object value);

   boolean maybePut(Object key, Object value);
   
   int getTxStatus();

   public enum Command
   {
      CMD_PUT, CMD_MAYBE_PUT, CMD_TX_START, CMD_TX_COMMIT, CMD_TX_ROLLBACK, CMD_TERMINATE,
   }
}