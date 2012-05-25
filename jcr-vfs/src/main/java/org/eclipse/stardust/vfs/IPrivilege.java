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
 * $Id: IFile.java 24736 2008-09-09 16:00:18Z rsauer $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs;

/**
 * IPrivilege represents a privilege in a JCR.
 * 
 * @author rsauer
 * @version $Revision: 24736 $
 */
public interface IPrivilege 
{
   
   /**
    * @see org.apache.jackrabbit.api.jsr283.security.Privilege#JCR_READ
    */
   public static final String READ_PRIVILEGE = "jcr:read";

   /**
    * @see org.apache.jackrabbit.api.jsr283.security.Privilege#JCR_WRITE
    */
   public static final String MODIFY_PRIVILEGE = "jcr:write";

   /**
    * @see org.apache.jackrabbit.api.jsr283.security.Privilege#JCR_ADD_CHILD_NODES
    */
   public static final String CREATE_PRIVILEGE = "jcr:addChildNodes";

   /**
    * @see org.apache.jackrabbit.api.jsr283.security.Privilege#JCR_REMOVE_NODE
    */
   public static final String DELETE_PRIVILEGE = "jcr:removeNode";

   /**
    * @see org.apache.jackrabbit.api.jsr283.security.Privilege#JCR_REMOVE_CHILD_NODES
    */
   public static final String DELETE_CHILDREN_PRIVILEGE = "jcr:removeChildNodes";

   /**
    * @see org.apache.jackrabbit.api.jsr283.security.Privilege#JCR_READ_ACCESS_CONTROL
    */
   public static final String READ_ACL_PRIVILEGE = "jcr:readAccessControl";

   /**
    * @see org.apache.jackrabbit.api.jsr283.security.Privilege#JCR_MODIFY_ACCESS_CONTROL
    */
   public static final String MODIFY_ACL_PRIVILEGE = "jcr:modifyAccessControl";

   /**
    * @see org.apache.jackrabbit.api.jsr283.security.Privilege#JCR_ALL
    */
   public static final String ALL_PRIVILEGE = "jcr:all";
   
   public String getName();

}
