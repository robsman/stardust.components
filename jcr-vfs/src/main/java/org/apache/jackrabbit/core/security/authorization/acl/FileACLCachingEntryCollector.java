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


// Parts copied from org.apache.jackrabbit.core.security.authorization.acl.CachingEntryCollector
// Modified #collectEntries(NodeImpl, EntryFilter)

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;


public class FileACLCachingEntryCollector extends CachingEntryCollector
{

   public FileACLCachingEntryCollector(SessionImpl systemSession, NodeId rootID)
         throws RepositoryException
   {
      super(systemSession, rootID);
   }

   /* copied from
    * @see org.apache.jackrabbit.core.security.authorization.acl.EntryCollector#collectEntries(org.apache.jackrabbit.core.NodeImpl, org.apache.jackrabbit.core.security.authorization.acl.EntryFilter)
    * and modified to stop retrieving the hierarchy in case node is of type nt:FILE and has own ACL entries.
    * This allows a nt:FILE to have completely separate ACL entries not checking the parent nt:FOLDER hierarchy.
    */
   @Override
   protected List<Entry> collectEntries(NodeImpl node, EntryFilter filter)
         throws RepositoryException
   {
      LinkedList<Entry> userAces = new LinkedList<Entry>();
      LinkedList<Entry> groupAces = new LinkedList<Entry>();

      if (node == null)
      {
         // repository level permissions
         NodeImpl root = (NodeImpl) systemSession.getRootNode();
         if (ACLProvider.isRepoAccessControlled(root))
         {
            NodeImpl aclNode = root.getNode(N_REPO_POLICY);
            filterEntries(filter, Entry.readEntries(aclNode, null), userAces, groupAces);
         }
      }
      else
      {
         Entries entries = getEntries(node);
         filterEntries(filter, entries.getACEs(), userAces, groupAces);

         // Only collect parent nodes if target node is not a FILE or no ACE is present.
         if (!node.isNodeType(NodeType.NT_FILE) || (entries.getACEs().isEmpty()))
         {
            NodeId next = node.getParentId();
            while (next != null)
            {
               entries = getEntries(next);
               filterEntries(filter, entries.getACEs(), userAces, groupAces);
               next = entries.getNextId();
            }
         }
      }

      List<Entry> entries = new ArrayList<Entry>(userAces.size() + groupAces.size());
      entries.addAll(userAces);
      entries.addAll(groupAces);

      return entries;
   }

   @Override
   protected Entries getEntries(NodeId nodeId) throws RepositoryException
   {
      NodeImpl node = getNodeById(nodeId);

      Entries entries = super.getEntries(nodeId);
      if (node.isNodeType(NodeType.NT_FILE) && !entries.getACEs().isEmpty())
      {
         // Do not retrieve parent of file having ACEs.
         entries.setNextId(null);
      }
      return entries;
   }

   @Override
   protected Entries getEntries(NodeImpl node) throws RepositoryException
   {
      Entries entries = super.getEntries(node);

      if (node.isNodeType(NodeType.NT_FILE) && !entries.getACEs().isEmpty())
      {
         // Do not retrieve parent of file having ACEs.
         entries.setNextId(null);
      }

      return entries;
   }

   @SuppressWarnings("unchecked")
   private static void filterEntries(EntryFilter filter, List<Entry> aces,
         LinkedList<Entry> userAces, LinkedList<Entry> groupAces)
   {
      if (!aces.isEmpty() && filter != null)
      {
         filter.filterEntries(aces, userAces, groupAces);
      }
   }

}
