/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.core.security.authorization.acl;

// Methods copied from org.apache.jackrabbit.core.security.authorization.acl.ACLProvider
// Modified to allow ACLs to be evaluated on FILEs without parent hierarchy.

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlPolicy;

import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.SecurityConstants;
import org.apache.jackrabbit.core.security.authorization.CompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.authorization.UnmodifiableAccessControlList;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;

import org.eclipse.stardust.vfs.impl.jcr.AuthorizableOrganizationDetails;
import org.eclipse.stardust.vfs.impl.jcr.JcrVfsPrincipal;

/**
 * ACL Provider that ignores the Folder hierarchy ACLs if a File has ACL entries.
 * Files with ACLs are evaluated only by their set ACLs.
 */
public class FileACLProvider extends ACLProvider
{

   @Override
   protected EntryCollector createEntryCollector(SessionImpl systemSession)
         throws RepositoryException
   {
      NodeImpl root = (NodeImpl) session.getRootNode();
      return new FileACLCachingEntryCollector(systemSession, root.getNodeId());
   }

   @Override
   public AccessControlPolicy[] getEffectivePolicies(Path absPath,
         CompiledPermissions permissions)
               throws ItemNotFoundException, RepositoryException
   {
      NodeImpl targetNode;
      List<AccessControlList> acls = new ArrayList<AccessControlList>();
      if (absPath != null) {

         targetNode = (NodeImpl) session.getNode(session.getJCRPath(absPath));
         NodeImpl node = getNode(targetNode, isAcItem(targetNode));
         if (node.isNodeType(NodeType.NT_FILE)
               && isAccessControlled(node))
         {
            if (permissions.grants(node.getPrimaryPath(), Permission.READ_AC))
            {
               acls.add(getACL(node, N_POLICY, node.getPath()));
            }
            else
            {
               throw new AccessDeniedException("Access denied at " + node.getPath());
            }
         }
      }

      // if file node has policies set, only those are effective.
      if (!acls.isEmpty())
      {
         return acls.toArray(new AccessControlList[acls.size()]);
      }
      else
      {
         return super.getEffectivePolicies(absPath, permissions);
      }
   }

   private AccessControlList getACL(NodeImpl accessControlledNode, Name policyName, String path) throws RepositoryException {
      // collect the aces of that node.
      NodeImpl aclNode = accessControlledNode.getNode(policyName);
      AccessControlList acl = new ACLTemplate(aclNode, path);

      return new UnmodifiableAccessControlList(acl);
  }

   @Override
   public boolean isAdminOrSystem(Set<Principal> principals)
   {
      boolean hasAdminGroup = false;

      for (Principal principal : principals)
      {
         // Check for JcrVfsPrincipal which can be in 'administrators' group.
         if (!hasAdminGroup && principal instanceof JcrVfsPrincipal)
         {
            JcrVfsPrincipal jcrVfsPrincipal = (JcrVfsPrincipal) principal;
            Set<AuthorizableOrganizationDetails> directGroups = jcrVfsPrincipal.getDirectGroups();
            if (directGroups != null)
            {
               for (AuthorizableOrganizationDetails authorizableOrganizationDetails : directGroups)
               {
                  if (SecurityConstants.ADMINISTRATORS_NAME
                        .equals(authorizableOrganizationDetails.getGroupId()))
                  {
                     hasAdminGroup = true;
                     break;
                  }
               }
            }
         }
      }

      return hasAdminGroup || super.isAdminOrSystem(principals);
   }

}
