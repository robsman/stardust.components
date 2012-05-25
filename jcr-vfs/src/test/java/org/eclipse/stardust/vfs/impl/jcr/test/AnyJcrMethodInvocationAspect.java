/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: AnyJcrMethodInvocationAspect.java 54754 2012-03-21 14:27:29Z nicolas.werlein $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr.test;



/**
 * @author rsauer
 * @version $Revision: 54754 $
 */
//@Aspect
public class AnyJcrMethodInvocationAspect
{

//   @Pointcut("call(* javax.jcr..*.*(..))")
//   void onAnyJcrMethodInvocation()
//   {
//   }
//
//   @Around("onAnyJcrMethodInvocation()")
//   public Object aroundSessionGetRootNode(ProceedingJoinPoint pjp) throws Throwable
//   {
//      String withinPackage = pjp.getSourceLocation().getWithinType().getPackage().getName();
//
//      if ( !JcrSession.class.getPackage().getName().equals(withinPackage))
//      {
//         System.out.println("About to invoke javax.jcr.* method from " + pjp.getSourceLocation() + " -> " + pjp);
//      }
//
//      return pjp.proceed();
//   }

}
