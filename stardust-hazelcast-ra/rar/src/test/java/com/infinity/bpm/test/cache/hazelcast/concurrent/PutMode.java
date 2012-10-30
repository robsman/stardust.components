package com.infinity.bpm.test.cache.hazelcast.concurrent;

public enum PutMode
{
   MANDATORY, OPTIONAL;

   private static final ThreadLocal<PutMode> CURRENT_MODE = new ThreadLocal<PutMode>();

   public static PutMode currentPutMode()
   {
      PutMode mode = CURRENT_MODE.get();

      return (null != mode) ? mode : MANDATORY;
   }
   
   public static PutMode setPutMode(PutMode mode)
   {
      PutMode backup = currentPutMode();

      if ((null != mode) && (MANDATORY != mode))
      {
         CURRENT_MODE.set(mode);
      }
      else
      {
         CURRENT_MODE.remove();
      }
      return backup;
   }
}
