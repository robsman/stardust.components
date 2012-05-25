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
 * $Id: AbstractJcrDocumentRepositoryService.java 30796 2009-10-01 15:02:36Z alexander.bouriakov $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;


/**
 * AuthorizableOrganizationDetails represents a hierarchical 
 * organisation structure where every node can have several 
 * memberOf relations. 
 * 
 * @author rsauer
 * @version $Revision: 30796 $
 */
public class AuthorizableOrganizationDetails implements Serializable
{

   private static final long serialVersionUID = 1L;

   public static final String DIRECT_GROUPS_ATT = "directGroups";
   
   private final String groupId;

   private final Set<AuthorizableOrganizationDetails> memberOf;

   @SuppressWarnings("unchecked")
   public AuthorizableOrganizationDetails(String groupId)
   {
      this(groupId, Collections.EMPTY_SET);
   }

   public AuthorizableOrganizationDetails(String groupId,
         Set<AuthorizableOrganizationDetails> memberOf)
   {
      this.groupId = groupId;
      this.memberOf = Collections.unmodifiableSet(memberOf);
   }

   public String getGroupId()
   {
      return groupId;
   }

   /**
    * Returns the group definitions the current node is a _direct_ member of.
    * @return only the _direct_ memberships are returned.
    */
   public Set<AuthorizableOrganizationDetails> memberOf()
   {
      return memberOf;
   }

   /**
    * Recursively collects all groups the current node is member of.
    * @return ids of all groups the current node is member of.
    */
   public Set<String> getMemberOfGroupIds()
   {
      Set<String> groupIds = CollectionUtils.newSet();
      groupIds.add(this.groupId);
      for (AuthorizableOrganizationDetails d : memberOf)
      {
         groupIds.addAll(d.getMemberOfGroupIds());
      }
      return groupIds;
   }

   @Override
   public String toString()
   {
      return "AuthorizableOrganizationDetails [groupId=" + groupId + ", memberOf="
            + getMemberOfGroupIds() + "]";
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
      result = prime * result + ((memberOf == null) ? 0 : memberOf.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      AuthorizableOrganizationDetails other = (AuthorizableOrganizationDetails) obj;
      if (groupId == null)
      {
         if (other.groupId != null)
            return false;
      }
      else if ( !groupId.equals(other.groupId))
         return false;
      if (memberOf == null)
      {
         if (other.memberOf != null)
            return false;
      }
      else if ( !memberOf.equals(other.memberOf))
         return false;
      return true;
   }

}
