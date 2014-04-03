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
 * $Id: JcrVfsOperations.java 65704 2013-06-24 15:15:00Z thomas.wolfram $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr;

import static org.eclipse.stardust.vfs.impl.utils.CollectionUtils.newList;
import static org.eclipse.stardust.vfs.impl.utils.CollectionUtils.newSet;
import static org.eclipse.stardust.vfs.impl.utils.StringUtils.isEmpty;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;
import javax.jcr.version.OnParentVersionAction;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.XASessionImpl;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.authorization.AccessControlEntryImpl;
import org.apache.jackrabbit.jca.JCAManagedConnection;
import org.apache.jackrabbit.jca.JCASessionHandle;
import org.apache.jackrabbit.value.BooleanValue;
import org.apache.jackrabbit.value.DateValue;
import org.apache.jackrabbit.value.DoubleValue;
import org.apache.jackrabbit.value.LongValue;
import org.apache.jackrabbit.value.StringValue;
import org.eclipse.stardust.vfs.AccessControlException;
import org.eclipse.stardust.vfs.IAccessControlEntry;
import org.eclipse.stardust.vfs.IAccessControlEntry.EntryType;
import org.eclipse.stardust.vfs.IAccessControlPolicy;
import org.eclipse.stardust.vfs.IFile;
import org.eclipse.stardust.vfs.IFileInfo;
import org.eclipse.stardust.vfs.IFolder;
import org.eclipse.stardust.vfs.IFolderInfo;
import org.eclipse.stardust.vfs.IPrivilege;
import org.eclipse.stardust.vfs.IResourceInfo;
import org.eclipse.stardust.vfs.IllegalOperationException;
import org.eclipse.stardust.vfs.MetaDataLocation;
import org.eclipse.stardust.vfs.RepositoryOperationFailedException;
import org.eclipse.stardust.vfs.VfsUtils;
import org.eclipse.stardust.vfs.impl.jcr.jackrabbit.JcrVfsUserManager;
import org.eclipse.stardust.vfs.impl.spi.JcrItem;
import org.eclipse.stardust.vfs.impl.spi.JcrNamespaceRegistry;
import org.eclipse.stardust.vfs.impl.spi.JcrNode;
import org.eclipse.stardust.vfs.impl.spi.JcrNodeIterator;
import org.eclipse.stardust.vfs.impl.spi.JcrNodeType;
import org.eclipse.stardust.vfs.impl.spi.JcrProperty;
import org.eclipse.stardust.vfs.impl.spi.JcrPropertyDefinition;
import org.eclipse.stardust.vfs.impl.spi.JcrPropertyIterator;
import org.eclipse.stardust.vfs.impl.spi.JcrQuery;
import org.eclipse.stardust.vfs.impl.spi.JcrQueryManager;
import org.eclipse.stardust.vfs.impl.spi.JcrQueryResult;
import org.eclipse.stardust.vfs.impl.spi.JcrRepository;
import org.eclipse.stardust.vfs.impl.spi.JcrSession;
import org.eclipse.stardust.vfs.impl.spi.JcrVersionHistory;
import org.eclipse.stardust.vfs.impl.spi.JcrWorkspace;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;
import org.eclipse.stardust.vfs.impl.utils.CompareHelper;
import org.eclipse.stardust.vfs.impl.utils.StringUtils;


/**
 * @author rsauer
 * @version $Revision: 65704 $
 */
public class JcrVfsOperations
{

   private static final String MIME_TYPE_METADATA_LIST_NODE = "jcr-vfs/metadata-list-node";

   private static boolean PARANOID = true;

   static final Collection<String> NT_FILES = Arrays.asList(NodeTypes.NT_FILE,
         NodeTypes.NT_LINKED_FILE);

   static final Collection<String> NT_FOLDERS = Arrays.asList(
         NodeTypes.NT_REPOSITORY_ROOT, NodeTypes.NT_FOLDER);

   public static final String PREFIX_JCR_UUID = "{jcrUuid}";

   public static final String PREFIX_JCR_REVISION = "{jcrRev}";

   public static final String PREFIX_JCR_PATH = VfsUtils.REPOSITORY_PATH_PREFIX;

   public static final String LEGACY_PREFIX_JCR_PATH = "{jcrPath}";

   public static final String ROOT_VERSION = "jcr:rootVersion";

   private static final String VFS_REPOSITORY_VERSION_PROPERTY = "vfs:repositoryVersion";

   private final Session session;

   private String vfsNsPrefix;

   private boolean hasVfsMetaDataMixin;

   public JcrVfsOperations(Session session)
   {
      this.session = session;
   }

   public static boolean isUuidBasedId(String id)
   {
      return !StringUtils.isEmpty(id) && id.startsWith(JcrVfsOperations.PREFIX_JCR_UUID);
   }

   public static boolean isRevisionBasedId(String id)
   {
      return !StringUtils.isEmpty(id)
            && id.startsWith(JcrVfsOperations.PREFIX_JCR_REVISION);
   }

   public static boolean isPathBasedId(String id)
   {
      return !StringUtils.isEmpty(id)
            && (id.startsWith(JcrVfsOperations.PREFIX_JCR_PATH) || id.startsWith(JcrVfsOperations.LEGACY_PREFIX_JCR_PATH));
   }

   public static String getUuidFromId(String id)
   {
      return isUuidBasedId(id)
            ? id.substring(JcrVfsOperations.PREFIX_JCR_UUID.length())
            : null;
   }

   public static String getRevisionUuidFromId(String id)
   {
      return isRevisionBasedId(id)
            ? id.substring(JcrVfsOperations.PREFIX_JCR_REVISION.length())
            : null;
   }

   public static String getPathFromId(String id)
   {
      if (VfsUtils.REPOSITORY_ROOT.equals(id))
      {
         return id;
      }
      else
      {
         return id.startsWith(JcrVfsOperations.LEGACY_PREFIX_JCR_PATH)
               ? id.substring(JcrVfsOperations.LEGACY_PREFIX_JCR_PATH.length())
               : id;
      }
   }

   public static boolean isFolder(Node node) throws RepositoryException
   {
      if (node == null)
      {
         return false;
      }

      return isFolder(JcrNode.getPrimaryNodeType(node));
   }

   public static boolean isFolder(NodeType nodeType) throws RepositoryException
   {
      return (null != nodeType)
            && JcrVfsOperations.NT_FOLDERS.contains(JcrNodeType.getName(nodeType));
   }

   public static boolean isFile(Node node) throws RepositoryException
   {
      if (node == null)
      {
         return false;
      }

      NodeType ntPrimary = JcrNode.getPrimaryNodeType(node);

      if (isFrozenNode(ntPrimary))
      {
         Property prp = JcrNode.getProperty(node, JcrProperties.JCR_FROZEN_PRIMARY_TYPE);

         return NT_FILES.contains(JcrProperty.getString(prp));
      }

      return isFile(ntPrimary);
   }

   public static boolean isFile(NodeType nodeType) throws RepositoryException
   {
      return (null != nodeType)
            && JcrVfsOperations.NT_FILES.contains(JcrNodeType.getName(nodeType));
   }

   public static boolean isFrozenNode(Node node) throws RepositoryException
   {
      return isFrozenNode(JcrNode.getPrimaryNodeType(node));
   }

   public static boolean isFrozenNode(NodeType nodeType) throws RepositoryException
   {
      return (null != nodeType)
            && NodeTypes.NT_FROZEN_NODE.equals(JcrNodeType.getName(nodeType));
   }

   public String getId(Node nResource) throws RepositoryException
   {
      return getId(nResource, true);
   }

   public String getId(Node nResource, boolean readOnly) throws RepositoryException
   {
      String uuid = null;

      if (isFrozenNode(nResource))
      {
         String frozenUuid = getStringProperty(nResource, JcrProperties.JCR_FROZEN_UUID);

         if ( !StringUtils.isEmpty(frozenUuid))
         {
            uuid = PREFIX_JCR_UUID + frozenUuid;
         }
      }
      else if (JcrNode.isNodeType(nResource, JcrProperties.MIXIN_REFERENCEABLE))
      {
         try
         {
            uuid = JcrVfsOperations.PREFIX_JCR_UUID + JcrNode.getIdentifier(nResource);
         }
         catch (UnsupportedRepositoryOperationException uroe)
         {
            uuid = null;
         }
      }

      if (StringUtils.isEmpty(uuid))
      {
         // fall back to full path
         final String path = JcrItem.getPath(nResource);

         uuid = path.startsWith(JcrVfsOperations.PREFIX_JCR_PATH)
               ? path
               : JcrVfsOperations.PREFIX_JCR_PATH + path;
      }

      if (StringUtils.isEmpty(uuid))
      {
         // TODO use full path?
         uuid = getName(nResource);
      }

      return uuid;
   }

   public String getRevisionId(Version revision) throws RepositoryException
   {
      String uuid = null;

      if (null != revision)
      {
         uuid = PREFIX_JCR_REVISION + JcrNode.getIdentifier(revision);
      }

      return uuid;
   }

   public String getName(Node nResource) throws RepositoryException
   {
      String name = null;

      Node nMetaData = getMetaDataNode(nResource);
      if (null != nMetaData)
      {
         String prpVfsName = getVfsNsPrefix() + VfsUtils.VFS_NAME;

         if (JcrNode.hasProperty(nMetaData, prpVfsName))
         {
            name = getStringProperty(nMetaData, prpVfsName);
         }
      }

      return !StringUtils.isEmpty(name) ? name : JcrItem.getName(nResource);
   }

   public void setName(Node nResource, String name) throws RepositoryException
   {
      Node nMetaData = getMetaDataNode(nResource);

      if (null != nMetaData)
      {
         String prpName = getVfsNsPrefix() + VfsUtils.VFS_NAME;

         setStringProperty(nResource, prpName, name);
      }
   }

   public Node findNode(String id) throws RepositoryException, ItemNotFoundException,
         PathNotFoundException
   {
      Node node;

      if (isUuidBasedId(id))
      {
         node = JcrSession.getNodeByUUID(session, getUuidFromId(id));
      }
      else if (isRevisionBasedId(id))
      {
         Node revision = JcrSession.getNodeByUUID(session, getRevisionUuidFromId(id));
         if (revision instanceof Version)
         {
            Version version = (Version) revision;

            node = JcrNode.getNode(version, JcrProperties.JCR_FROZEN_NODE);
         }
         else
         {
            // TODO throw exception?
            node = null;
         }
      }
      else if (isPathBasedId(id))
      {
         node = findNodeByPath(getPathFromId(id));
      }
      else
      {
         // TODO
         node = null;
      }

      return node;
   }

   public Node findFile(String id)
   {
      Node nFile;
      try
      {
         nFile = findNode(id);

         nFile = isFile(nFile) ? nFile : null;
      }
      catch (RepositoryException e)
      {
         // TODO trace
         nFile = null;
      }

      return nFile;
   }

   public Node findFolder(String id)
   {
      Node nFolder;
      try
      {
         nFolder = findNode(id);

         nFolder = isFolder(nFolder) ? nFolder : null;
      }
      catch (RepositoryException e)
      {
         // TODO trace
         nFolder = null;
      }

      return nFolder;
   }

   public Node findNodeByPath(String path) throws RepositoryException,
         PathNotFoundException
   {
      final Node nRoot = JcrSession.getRootNode(session);

      if (VfsUtils.REPOSITORY_ROOT.equals(path))
      {
         return nRoot;
      }
      else
      {
         String rootRelativePath = path.startsWith(VfsUtils.REPOSITORY_PATH_PREFIX)
               ? path.substring(VfsUtils.REPOSITORY_PATH_PREFIX.length())
               : path;

         return JcrNode.getNode(nRoot, rootRelativePath);
      }
   }

   public NodeIterator findNodesByName(String namePattern)
         throws RepositoryException
   {
      NodeIterator nodes = null;

      try
      {
         // session.exportDocumentView("/", new FileOutputStream("/tmp/repo.xml"), true,
         // false);

         String vfsMetaData = getVfsNsPrefix() + VfsUtils.VFS_META_DATA;
         String prpVfsName = getVfsPropertyName(VfsUtils.VFS_NAME);

         String xpathQuery;
         if (hasVfsMetaDataMixin)
         {
            xpathQuery = "/jcr:root//*[jcr:like(" + vfsMetaData + "/"
                  + prpVfsName + ", '" + namePattern + "')]";
         }
         else
         {
            xpathQuery = "/jcr:root/" + vfsMetaData + "/*[jcr:like(" + prpVfsName + ", '"
                  + namePattern + "')]";
         }

         nodes = findNodesByXPath(xpathQuery);
      }
      catch (RepositoryException re)
      {
         throw re;
      }

      return nodes;
   }

   public NodeIterator findNodesByXPath(String xPathQuery) throws RepositoryException
   {
      QueryResult result = queryByXPath(xPathQuery, -1, -1);

      return JcrQueryResult.getNodes(result);
   }

   public QueryResult queryByXPath(String xPathQuery, long limit, long offset) throws RepositoryException
   {
      try
      {
         // session.exportDocumentView("/", new FileOutputStream("/tmp/repo.xml"), true,
         // false);

         final Workspace workspace = JcrSession.getWorkspace(session);
         final QueryManager queryManager = JcrWorkspace.getQueryManager(workspace);

         final Query query = JcrQueryManager.createQuery(queryManager, xPathQuery,
               Query.XPATH);

         if (limit >= 0 || offset >= 0)
         {
            query.setLimit(limit);
            query.setOffset(offset);
         }

         return JcrQuery.execute(query);
      }
      catch (RepositoryException re)
      {
         throw re;
      }
   }

   public JcrRepositoryFile getFileSnapshot(Node nFile)
   {
      try
      {
         final String uuid = getId(nFile);
         final String name = getName(nFile);
         final String path;

         Node nFileTip = null;
         boolean isFrozenNode = isFrozenNode(nFile);

         if (isFrozenNode)
         {
            nFileTip = findFile(uuid);
            if (null != nFileTip)
            {
               // resolve path from the HEAD revision of the file
               path = JcrItem.getPath(nFileTip);
            }
            else
            {
               // if the HEAD revision was deleted, there is no path information available
               path = "";
            }
         }
         else
         {
            path = JcrItem.getPath(nFile);
         }
         JcrRepositoryFile doc = new JcrRepositoryFile(uuid, name, path);

         final Version version = getVersion(nFile);
         if (null != version)
         {
            doc.setRevisionId(PREFIX_JCR_REVISION + JcrNode.getIdentifier(version));
            doc.setRevisionName(JcrItem.getName(version));

            VersionHistory fileHistory = version.getContainingHistory();
            String[] labels = fileHistory.getVersionLabels(version);
            if ((null != labels) && (0 < labels.length))
            {
               doc.setVersionLabels(Arrays.asList(labels));
            }
         }

         updateFileSnapshot(doc, nFile);

         if (isFrozenNode)
         {
            if (null != nFileTip)
            {
               // resolve dateCreated from the HEAD revision of the file
               Date dateCreated = doc.getDateCreated();
               if (dateCreated == null)
               {
                  doc.setDateCreated(getDateProperty(nFileTip, JcrProperties.JCR_CREATED));
               }
            }
         }

         return doc;
      }
      catch (RepositoryException e)
      {
         throw new RepositoryOperationFailedException("Failed retrieving file snapshot.",
               e);
      }
   }

   public JcrRepositoryFile updateFileSnapshot(JcrRepositoryFile doc, Node nFile)
   {
      try
      {
         if (PARANOID)
         {
            final String uuid = getId(nFile);

            if ( !StringUtils.isEmpty(uuid) && !CompareHelper.areEqual(uuid, doc.getId()))
            {
               throw new RuntimeException(MessageFormat.format(
                     "The document's ID ''{0}'' does not match the file's ID ''{1}''",
                     doc.getId(), uuid));
            }
         }

         final String name = getName(nFile);
         if ( !CompareHelper.areEqual(name, doc.getName()))
         {
            doc.setName(name);
         }


         final Node nVfsMetaData = getMetaDataNode(nFile);

         if (null != nVfsMetaData)
         {
            doc.setRevisionComment(getStringProperty(nVfsMetaData,
                  getVfsPropertyName(VfsUtils.VFS_REVISION_COMMENT)));
         }

         updateResourceSnapshot(doc, nFile, nVfsMetaData);

         Node nVfsAnnotations = getVfsAttributesNode(nVfsMetaData, VfsUtils.VFS_ANNOTATIONS);

         Map<String, Serializable> annotations = new HashMap<String, Serializable>(
               doc.getAnnotations());
         updateAttributesNodeSnapshot(nVfsAnnotations, annotations);
         doc.setAnnotations(annotations);

         Node nFileContent = getContentNode(nFile);
         if (null != nFileContent)
         {
            final String mimeType = getMimeType(nFileContent);
            if ( !CompareHelper.areEqual(mimeType, doc.getContentType()))
            {
               doc.setContentType(mimeType);
            }

            Date dateLastModified = getLastModified(nFileContent);
            if ( !CompareHelper.areEqual(dateLastModified, doc.getDateLastModified()))
            {
               doc.setDateLastModified(dateLastModified);
            }

            Property pFileContent = JcrNode.getProperty(nFileContent,
                  JcrProperties.JCR_DATA);
            doc.setSize(JcrProperty.getLength(pFileContent));
         }
         else
         {
            doc.setContentType(null);
            // TODO lastModificationTime
         }
      }
      catch (RepositoryException e)
      {
         throw new RepositoryOperationFailedException("Failed updating file snapshot.", e);
      }

      return doc;
   }

   public JcrRepositoryFolder getFolderSnapshot(Node nFolder, int levelOfDetail)
   {
      try
      {
         final String uuid = getId(nFolder);
         final String name = getName(nFolder);

         JcrRepositoryFolder result = new JcrRepositoryFolder(uuid, name,
               JcrItem.getPath(nFolder), levelOfDetail);

         return updateFolderSnapshot(result, nFolder);
      }
      catch (RepositoryException e)
      {
         throw new RepositoryOperationFailedException(
               "Failed retrieving folder snapshot.", e);
      }
   }

   public JcrRepositoryFolder updateFolderSnapshot(final JcrRepositoryFolder docs,
         Node nFolder) throws RepositoryException
   {
      final Node nVfsMetaData = getMetaDataNode(nFolder);

      updateResourceSnapshot(docs, nFolder, nVfsMetaData);

      if (nVfsMetaData != null)
      {
         Date dateLastModified = getLastModified(nVfsMetaData);
         if ( !CompareHelper.areEqual(dateLastModified, docs.getDateLastModified()))
         {
            docs.setDateLastModified(dateLastModified);
         }
      }

      if ((IFolder.LOD_LIST_MEMBERS == docs.getLevelOfDetail())
            || (IFolder.LOD_LIST_MEMBERS_OF_MEMBERS == docs.getLevelOfDetail()))
      {
         final List<JcrRepositoryFile> updatedFiles = newList();
         final List<JcrRepositoryFolder> updatedFolders = newList();

         try
         {
            visitMembers(nFolder, new FsNodeVisitorAdapter()
            {

               private final int LOD_SUB_FOLDERS = (IFolder.LOD_LIST_MEMBERS == docs.getLevelOfDetail())
                     ? IFolder.LOD_NO_MEMBERS
                     : IFolder.LOD_LIST_MEMBERS;

               @Override
               public void visitFile(Node nFile) throws RepositoryException
               {
                  final String id = getId(nFile);

                  IFile doc = docs.getFile(id);

                  JcrRepositoryFile updatedFile;
                  if (doc instanceof JcrRepositoryFile)
                  {
                     updatedFile = updateFileSnapshot((JcrRepositoryFile) doc, nFile);
                  }
                  else
                  {
                     updatedFile = getFileSnapshot(nFile);
                  }

                  updatedFiles.add(updatedFile);
               }

               @Override
               public void visitFolder(Node nFolder) throws RepositoryException
               {
                  final String id = getId(nFolder);

                  IFolder doc = docs.getFolder(id);

                  JcrRepositoryFolder updatedFolder;
                  if ((doc instanceof JcrRepositoryFolder)
                        && (LOD_SUB_FOLDERS == doc.getLevelOfDetail()))
                  {
                     updatedFolder = updateFolderSnapshot((JcrRepositoryFolder) doc,
                           nFolder);
                  }
                  else
                  {
                     updatedFolder = getFolderSnapshot(nFolder, LOD_SUB_FOLDERS);
                  }

                  updatedFolders.add(updatedFolder);
               }

            });
         }
         catch (RepositoryException e)
         {
            throw new RepositoryOperationFailedException(
                  "Failed updating folder snapshot.", e);
         }

         while (0 < docs.getFileCount())
         {
            docs.removeFile(0);
         }
         for (JcrRepositoryFile file : updatedFiles)
         {
            docs.addFile(file);
         }

         while (0 < docs.getFolderCount())
         {
            docs.removeFolder(0);
         }
         for (JcrRepositoryFolder subFolder : updatedFolders)
         {
            docs.addFolder(subFolder);
         }
      }
      else
      {
         // TODO remove any member details
      }

      return docs;
   }

   public JcrRepositoryResource updateResourceSnapshot(JcrRepositoryResource resource,
         Node nResource, Node nVfsMetaData) throws RepositoryException
   {
      Date dateCreated = getDateProperty(nResource, JcrProperties.JCR_CREATED);
      if ( !CompareHelper.areEqual(dateCreated, resource.getDateCreated()))
      {
         resource.setDateCreated(dateCreated);
      }

      if (null != nVfsMetaData)
      {
         resource.setDescription(getStringProperty(nVfsMetaData,
               getVfsPropertyName(VfsUtils.VFS_DESCRIPTION)));
         resource.setOwner(getStringProperty(nVfsMetaData,
               getVfsPropertyName(VfsUtils.VFS_OWNER)));
         resource.setPropertiesTypeId(getStringProperty(nVfsMetaData,
               getVfsPropertyName(VfsUtils.VFS_ATTRIBUTES_TYPE_ID)));
         resource.setPropertiesTypeSchemaLocation(getStringProperty(nVfsMetaData,
               getVfsPropertyName(VfsUtils.VFS_ATTRIBUTES_TYPE_SCHEMA_LOCATION)));

         Node nVfsAttributes = getVfsAttributesNode(nVfsMetaData, VfsUtils.VFS_ATTRIBUTES);

         Map<String, Serializable> properties = new HashMap<String, Serializable>(
               resource.getProperties());
         updateAttributesNodeSnapshot(nVfsAttributes, properties);
         resource.setProperties(properties);
      }

      return resource;
   }

   private void updateAttributesNodeSnapshot(Node nVfsAttributes,
         Map<String, Serializable> properties) throws RepositoryException
   {
      if (nVfsAttributes != null)
      {
         final String vfsScope = getVfsNsPrefix();

         Set<String> resolvedAttributes = newSet();

         // handle primitive properties (and lists of primitives)
         for (PropertyIterator i = JcrNode.getProperties(nVfsAttributes, vfsScope + "*"); JcrPropertyIterator.hasNext(i);)
         {
            Property prp = JcrPropertyIterator.nextProperty(i);

            final String prpName = JcrItem.getName(prp).substring(vfsScope.length());
            final Serializable prpValue = getPropertyValue(prp);

            properties.put(prpName, prpValue);

            resolvedAttributes.add(prpName);
         }

         // handle complex properties and lists of complex properties
         for (NodeIterator i = JcrNode.getNodes(nVfsAttributes); JcrNodeIterator.hasNext(i);)
         {
            Node subNode = JcrNodeIterator.nextNode(i);
            if (isFromVfsNs(subNode))
            {
               String subPropertiesKey = stripNsPrefix(subNode);
               if (isMetadataListNode(subNode))
               {
                  // list of complex types
                  ArrayList<Serializable> propertiesList = new ArrayList<Serializable>();
                  for (NodeIterator listItems = JcrNode.getNodes(subNode,
                        subNode.getName()); JcrNodeIterator.hasNext(listItems);)
                  {
                     HashMap<String, Serializable> subProperties = new HashMap<String, Serializable>();
                     updateAttributesNodeSnapshot(JcrNodeIterator.nextNode(listItems),
                           subProperties);
                     propertiesList.add(subProperties);
                  }
                  properties.put(subPropertiesKey, propertiesList);
               }
               else
               {
                  // single complex type
                  HashMap<String, Serializable> subProperties = new HashMap<String, Serializable>();
                  properties.put(subPropertiesKey, subProperties);
                  updateAttributesNodeSnapshot(subNode, subProperties);
               }
               resolvedAttributes.add(subPropertiesKey);
            }
         }

         Set<String> obsoleteAttribute = newSet();
         for (String attrName : properties.keySet())
         {
            if ( !resolvedAttributes.contains(attrName))
            {
               obsoleteAttribute.add((attrName));
            }
         }

         for (String attrName : obsoleteAttribute)
         {
            properties.remove(attrName);
         }
      }
   }

   private boolean isMetadataListNode(Node node) throws RepositoryException
   {
      if (JcrNode.hasProperty(node, JcrProperties.JCR_MIME_TYPE))
      {
         String mimeType = JcrProperty.getString(JcrNode.getProperty(node,
               JcrProperties.JCR_MIME_TYPE));
         return MIME_TYPE_METADATA_LIST_NODE.equals(mimeType);
      }
      else
      {
         return false;
      }
   }

   private boolean isFromVfsNs(Node node) throws RepositoryException
   {
      return node.getName().startsWith(getVfsNsPrefix());
   }

   private String stripNsPrefix(Node node) throws RepositoryException
   {
      return node.getName().split(":")[1];
   }

   public Node addFile(Node nFolder, IFileInfo file, InputStream content, String encoding)
         throws RepositoryException
   {
      Node nFile = addHierarchyNode(nFolder, file.getName(), NodeTypes.NT_FILE);

      if (null != nFile)
      {
         JcrNode.addNode(nFile, JcrProperties.JCR_CONTENT, NodeTypes.NT_RESOURCE);

         InputStream emptyFakeContent = null;
         try
         {
            if (null == content)
            {
               // file without any content is not valid according to JCR, so provide
               // fake content of length 0

               emptyFakeContent = new ByteArrayInputStream(new byte[0]);
            }

            updateFile(nFile, file, (null != content) ? content : emptyFakeContent,
                  encoding);
         }
         finally
         {
            if (null != emptyFakeContent)
            {
               try
               {
                  emptyFakeContent.close();
               }
               catch (IOException ioe)
               {
                  // ignore
               }
            }
         }
      }

      return nFile;
   }

   public Node addFile(Node nFolder, IFileInfo file, String contentFileId)
         throws RepositoryException
   {
      Node nFile = addHierarchyNode(nFolder, file.getName(), NodeTypes.NT_FILE);

      if (null != nFile)
      {
         JcrNode.addNode(nFile, JcrProperties.JCR_CONTENT, NodeTypes.NT_RESOURCE);

         // retrieve content from file located at contentFileId
         // TODO maybe the Node.copy() of JCR 2.0 can be used here instead?
         InputStream contentStream;
         Node nContentFile = findFile(contentFileId);

         if (null == nContentFile)
         {
            throw new RepositoryOperationFailedException(
                  "contentFileId must point to an existing file.");
         }
         final Node nFileContent = getContentNode(nContentFile);
         final Property prpContent = (null != nFileContent) ? JcrNode.getProperty(
               nFileContent, JcrProperties.JCR_DATA) : null;
         if (null == prpContent)
         {
            // file without any content is not valid according to JCR, so provide
            // fake content of length 0
            contentStream = new ByteArrayInputStream(new byte[0]);
         }
         else
         {
            contentStream = JcrProperty.getBinary(prpContent) != null? JcrProperty.getBinary(prpContent).getStream():new ByteArrayInputStream(new byte[0]);
         }
         try
         {
            // copy encoding property value from the content origin file
            String encoding = null;
            if (JcrNode.hasProperty(nContentFile, JcrProperties.JCR_ENCODING))
            {
               encoding = JcrNode.getProperty(nContentFile, JcrProperties.JCR_ENCODING)
                     .getString();
            }
            updateFile(nFile, file, contentStream, encoding);
         }
         finally
         {
            if (null != contentStream)
            {
               try
               {
                  contentStream.close();
               }
               catch (IOException e)
               {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
         }
      }

      return nFile;
   }

   public Node linkFile(Node nFolder, String name, Node nSrcFile)
         throws RepositoryException
   {
      Node nLinkedFile = null;

      if (isFolder(nFolder) && isFile(nSrcFile))
      {
         if (StringUtils.isEmpty(name))
         {
            name = getName(nSrcFile);
         }

         Node nSrcContent = getContentNode(nSrcFile);

         if (null != nSrcContent)
         {
            nLinkedFile = addHierarchyNode(nFolder, name, NodeTypes.NT_LINKED_FILE);

            JcrNode.setProperty(nLinkedFile, JcrProperties.JCR_CONTENT, nSrcContent);
         }
      }

      return nLinkedFile;
   }

   public String getVersionId(Node nFile) throws RepositoryException, VersionException
   {
      Version version = getVersion(nFile);

      return (null != version) ? version.getName() : VfsUtils.VERSION_UNVERSIONED;
   }

   public Version getVersion(Node nFile) throws RepositoryException, VersionException
   {
      Version version = null;

      if (null != nFile)
      {
         if (isFrozenNode(nFile))
         {
            version = (Version) JcrItem.getParent(nFile);
         }
         else if (JcrNode.isNodeType(nFile, JcrProperties.MIXIN_VERSIONABLE))
         {
            try
            {
               version = JcrNode.getBaseVersion(nFile);
            }
            catch (ItemNotFoundException infe)
            {
               // ignore
            }
         }
      }

      return version;
   }

   public void ensureVersioningIsEnabled(Node nFile) throws RepositoryException,
         VersionException
   {
      if (null != nFile)
      {
         try
         {
            if ( !JcrNode.isNodeType(nFile, JcrProperties.MIXIN_VERSIONABLE))
            {
               JcrNode.addMixin(nFile, JcrProperties.MIXIN_VERSIONABLE);
            }

            // Node nMetaData = getMetaDataNode(nFile);
            // if ((null != nMetaData)
            // && !JcrNode.isNodeType(nMetaData, JcrProperties.MIXIN_VERSIONABLE))
            // {
            // JcrNode.addMixin(nMetaData, JcrProperties.MIXIN_VERSIONABLE);
            // }
         }
         catch (NoSuchNodeTypeException nsnte)
         {
            throw new RepositoryOperationFailedException(
                  "Failed enabling versioning support for file.", nsnte);
         }
      }
   }

   public static void ensureVersionIsModifiable(Node nFile) throws RepositoryException
   {
      if (!nFile.isCheckedOut())
      {
        JcrNode.checkout(nFile);
      }
   }

   public void updateFile(Node nFile, IFileInfo file, InputStream content, String encoding)
         throws RepositoryException
   {
      if (null != nFile)
      {
         boolean updated = false;

         MetaDataMigrationInfo metaDataInfo = migrateMetaData(nFile);
         Node nMetaData = metaDataInfo.getMetaData();
         boolean metaDataMigrated = metaDataInfo.isMigrated();

         updated |= updateResource(nFile, nMetaData, file);

         Node nContent = getContentNode(nFile);

         String mimeType = file.getContentType();

         if (null != content)
         {
            // TODO use JAF to resolve mime type?
            if (StringUtils.isEmpty(mimeType))
            {
               mimeType = "application/octet-stream";
            }

            if ( !StringUtils.isEmpty(encoding))
            {
               JcrNode.setProperty(nContent, JcrProperties.JCR_ENCODING, encoding);
            }
            Binary binary = session.getValueFactory().createBinary(content);
            JcrNode.setProperty(nContent, JcrProperties.JCR_DATA, binary);

            updated |= true;
         }

         if ( !StringUtils.isEmpty(mimeType))
         {
            updated |= updateProperty(nContent, JcrProperties.JCR_MIME_TYPE, mimeType);
         }

         Node nVfsAnnotations = getVfsAttributesNode(nMetaData, VfsUtils.VFS_ANNOTATIONS);

         updated |= updateAttributesNode(nVfsAnnotations, file.getAnnotations());

         if ( !metaDataMigrated && (updated || JcrItem.isNew(nFile)))
         {
            updated |= updateProperty(nContent, JcrProperties.JCR_LAST_MODIFIED,
                  new Date());
         }
      }
   }

   public void updateFile(Node nFile, IFileInfo file, String contentFileId)
         throws RepositoryException
   {
      if (null != nFile)
      {
         boolean updated = false;

         MetaDataMigrationInfo metaDataInfo = migrateMetaData(nFile);
         Node nMetaData = metaDataInfo.getMetaData();
         boolean metaDataMigrated = metaDataInfo.isMigrated();

         updated |= updateResource(nFile, nMetaData, file);

         Node nContent = getContentNode(nFile);

         String mimeType = file.getContentType();

         // retrieve content from file located at contentFileId
         // TODO maybe the Node.copy() of JCR 2.0 can be used here instead?
         InputStream contentStream;
         Node nContentFile = findFile(contentFileId);
         if (null == nContentFile)
         {
            throw new RepositoryOperationFailedException(
                  "contentFileId must point to an existing file.");
         }
         final Node nFileContent = getContentNode(nContentFile);
         final Property prpContent = (null != nFileContent) ? JcrNode.getProperty(
               nFileContent, JcrProperties.JCR_DATA) : null;
         if (null == prpContent)
         {
            // file without any content is not valid according to JCR, so provide
            // fake content of length 0
            contentStream = new ByteArrayInputStream(new byte[0]);
         }
         else
         {
            contentStream = JcrProperty.getBinary(prpContent) != null? JcrProperty.getBinary(prpContent).getStream():new ByteArrayInputStream(new byte[0]);
         }
         try
         {
            // TODO use JAF to resolve mime type?
            if (StringUtils.isEmpty(mimeType))
            {
               mimeType = "application/octet-stream";
            }

            // copy encoding property value from the content origin file
            if (JcrNode.hasProperty(nContentFile, JcrProperties.JCR_ENCODING))
            {
               JcrNode.setProperty(nContent, JcrProperties.JCR_ENCODING,
                     JcrNode.getProperty(nContentFile, JcrProperties.JCR_ENCODING)
                           .getString());
            }
            Binary binary = session.getValueFactory().createBinary(contentStream);
            JcrNode.setProperty(nContent, JcrProperties.JCR_DATA, binary);

            updated |= true;
         }
         finally
         {
            if (null != contentStream)
            {
               try
               {
                  contentStream.close();
               }
               catch (IOException e)
               {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
         }

         if ( !StringUtils.isEmpty(mimeType))
         {
            updated |= updateProperty(nContent, JcrProperties.JCR_MIME_TYPE, mimeType);
         }

         Node nVfsAnnotations = getVfsAttributesNode(nMetaData, VfsUtils.VFS_ANNOTATIONS);

         updated |= updateAttributesNode(nVfsAnnotations, file.getAnnotations());

         if ( !metaDataMigrated && (updated || JcrItem.isNew(nFile)))
         {
            updated |= updateProperty(nContent, JcrProperties.JCR_LAST_MODIFIED,
                  new Date());
         }
      }
   }

   private MetaDataMigrationInfo migrateMetaData(Node nFile) throws RepositoryException
   {
      boolean migrated = false;
      ensureVersionIsModifiable(nFile);
      Node nMetaData = getMetaDataNode(nFile);
      ensureVersionIsModifiable(nMetaData);
      if ( !hasLocalMetaData(nFile) && !isFrozenNode(nFile) && hasVfsMetaDataMixin)
      {
         Node localMetaData = addLocalMetaData(nFile);
         ensureVersionIsModifiable(localMetaData);
         JcrItem.remove(nMetaData);
         nMetaData = localMetaData;
         migrated = true;
      }

      return new MetaDataMigrationInfo(nMetaData, migrated);
   }

   public void updateFolder(Node nFolder, IFolderInfo folder) throws RepositoryException
   {
      if (null != nFolder)
      {
         boolean updated = false;

         MetaDataMigrationInfo metaDataInfo = migrateMetaData(nFolder);
         Node nMetaData = metaDataInfo.getMetaData();

         updated |= updateResource(nFolder, nMetaData, folder);

         if (!metaDataInfo.isMigrated() && (updated || (JcrItem.isNew(nFolder))))
         {
            updated |= updateProperty(nMetaData, JcrProperties.JCR_LAST_MODIFIED,
                  new Date());
         }
      }
   }

   public boolean updateResource(Node nTarget, Node nMetaData, IResourceInfo source)
         throws RepositoryException
   {
      boolean updated = false;

      if ( !CompareHelper.areEqual(JcrItem.getName(nTarget), source.getName()))
      {
         Node nParent = JcrItem.getParent(nTarget);

         StringBuffer newPath = new StringBuffer(100);
         newPath.append(JcrItem.getPath(nParent));
         if ( !CompareHelper.areEqual("/", newPath.substring(newPath.length() - 1)))
         {
            newPath.append("/");
         }
         newPath.append(source.getName());

         // System.out.println("Old name: " + nTarget.getPath() + ", New name: " +
         // newPath);

         updated = true;
         JcrSession.move(session, JcrItem.getPath(nTarget), newPath.toString());
         JcrItem.save(nParent);
         JcrItem.save(nTarget);
      }

      updated |= updateProperty(nMetaData, getVfsPropertyName(VfsUtils.VFS_NAME),
            source.getName());
      updated |= updateProperty(nMetaData, getVfsPropertyName(VfsUtils.VFS_DESCRIPTION),
            source.getDescription());
      updated |= updateProperty(nMetaData, getVfsPropertyName(VfsUtils.VFS_OWNER),
            source.getOwner());
      updated |= updateProperty(nMetaData, getVfsPropertyName(VfsUtils.VFS_ATTRIBUTES_TYPE_ID),
            source.getPropertiesTypeId());
      updated |= updateProperty(nMetaData, getVfsPropertyName(VfsUtils.VFS_ATTRIBUTES_TYPE_SCHEMA_LOCATION),
            source.getPropertiesTypeSchemaLocation());

      final Node nVfsAttributes = getVfsAttributesNode(nMetaData, VfsUtils.VFS_ATTRIBUTES);

      updated |= updateAttributesNode(nVfsAttributes, source.getProperties());

      return updated;
   }

   public boolean updateAttributesNode(Node nVfsAttributes,
         final Map<String, ? extends Serializable> sourceProperties) throws RepositoryException
   {
      boolean updated = false;
      // update custom properties
      Set<String> resolvedProperties = newSet();

      final String vfsScope = getVfsNsPrefix();

      for (PropertyIterator i = JcrNode.getProperties(nVfsAttributes, vfsScope + "*"); JcrPropertyIterator.hasNext(i);)
      {
         Property prp = JcrPropertyIterator.nextProperty(i);

         final String prpName = JcrItem.getName(prp).substring(vfsScope.length());
         final Serializable newValue = sourceProperties.get(prpName);

         updated |= updateProperty(prp, newValue);

         resolvedProperties.add(prpName);
      }
      for (NodeIterator i = JcrNode.getNodes(nVfsAttributes); JcrNodeIterator.hasNext(i);)
      {
         Node node = JcrNodeIterator.nextNode(i);

         final String nodeName = JcrItem.getName(node).substring(vfsScope.length());
         final Serializable newValue = sourceProperties.get(nodeName);

         if (newValue == null)
         {
            node.remove();
            resolvedProperties.add(nodeName);
         }
      }

      for (String prpName : sourceProperties.keySet())
      {
         if ( !resolvedProperties.contains(prpName))
         {
            final Serializable newValue = sourceProperties.get(prpName);

            updated |= setPropertyValue(nVfsAttributes, vfsScope + prpName, newValue);
         }
      }

      return updated;
   }

   public Node createFolder(Node nParent, IFolderInfo folder) throws RepositoryException
   {
      Node nFolder = addHierarchyNode(nParent, folder.getName(), NodeTypes.NT_FOLDER);

      if (null != nFolder)
      {
         updateFolder(nFolder, folder);
      }

      return nFolder;
   }

   public void createFrozenVersion(Node nFile, String versionComment, String versionLabel, boolean moveLabel)
         throws RepositoryException
   {
      updateVersionComment(nFile, versionComment);

      session.save();

      // Workaround for NPE on 2nd versioning
      clearJackrabbitItemManagerCacheEntry(nFile);

      VersionManager versionManager = session.getWorkspace().getVersionManager();
      Version version = versionManager.checkin(nFile.getPath());

      if ( !isEmpty(versionLabel))
      {
         VersionHistory fileHistory = JcrNode.getVersionHistory(nFile);

         try
         {
            fileHistory.addVersionLabel(JcrItem.getName(version), versionLabel, moveLabel);
         }
         catch (AccessDeniedException ade)
         {
            Session session = createAdminSession();

            try
            {
               Item versionStorageRoot = session.getItem("/jcr:system/jcr:versionStorage");
               if (versionStorageRoot instanceof Node)
               {
                  IAccessControlPolicy acp = getPermission((Node) versionStorageRoot,
                        "everyone", IPrivilege.ALL_PRIVILEGE);

                  if (acp != null)
                  {
                     setNodePolicy((Node) versionStorageRoot, acp);
                  }
               }
            }
            catch (Exception e)
            {
            }
            finally
            {
               session.save();
               session.logout();
            }

            // retry version operation
            try
            {
               fileHistory.addVersionLabel(JcrItem.getName(version), versionLabel,
                     moveLabel);
            }
            catch (AccessDeniedException ade2)
            {

            }
         }

      }

      if ( !hasLocalMetaData(nFile))
      {
         // version meta-data node
         Node nMetaData = getMetaDataNode(nFile);

         // not using vfsResource mixin, need to version separately
         ensureVersioningIsEnabled(nMetaData);
         JcrItem.save(nMetaData);

         Version frozenMetaData = JcrNode.checkin(nMetaData);
         VersionHistory metaDataHistory = JcrNode.getVersionHistory(nMetaData);
         JcrVersionHistory.addVersionLabel(metaDataHistory,
               JcrItem.getName(frozenMetaData), getRevisionId(version), false);
      }
   }

   private void clearJackrabbitItemManagerCacheEntry(Node nFile)
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
      VersionHistory versionHistory = nFile.getVersionHistory();
      if (versionHistory != null)
      {
         ItemId versionHistoryId = ((org.apache.jackrabbit.core.version.VersionHistoryImpl) versionHistory).getId();
         // remove from item manager cache
         getItemManager(session).itemDestroyed(versionHistoryId, null);
      }
   }

   private void updateVersionComment(Node nFile, String versionComment) throws RepositoryException
   {
      MetaDataMigrationInfo metaDataInfo = migrateMetaData(nFile);
      Node nMetaData = metaDataInfo.getMetaData();

      updateProperty(nMetaData, getVfsPropertyName(VfsUtils.VFS_REVISION_COMMENT),
            versionComment);
   }

   private IAccessControlPolicy getPermission(Node node, String principalName,
         String iPriviligeName) throws RepositoryException
   {
      Set<IAccessControlPolicy> applicablePolicies = getNodeApplicablePolicies(node);
      Set<IAccessControlPolicy> policies = getNodePolicies(node);

      if (applicablePolicies.isEmpty() && policies.isEmpty())
      {
         // jcr security is disabled, do nothing
      }
      else if ( !applicablePolicies.isEmpty())
      {
         IAccessControlPolicy acp = applicablePolicies.iterator().next();
         acp.addAccessControlEntry(new JcrVfsPrincipal(principalName),
               Collections.singleton(getPrivilegeByName(
                     getAccessControlManager(node.getSession()), iPriviligeName)), EntryType.ALLOW);

         return acp;
      }
      else if ( !policies.isEmpty())
      {
         // do nothing if a policy is already set.

         // for (IAccessControlPolicy accessControlPolicy : policies)
         // {
         // Set<IAccessControlEntry> aces = accessControlPolicy.getAccessControlEntries();
         // for (IAccessControlEntry accessControlEntry : aces)
         // {
         // if (principalName.equals(accessControlEntry.getPrincipal().getName()))
         // {
         //
         // }
         // }
         // }
      }
      return null;
   }

   public Node addHierarchyNode(Node nFolder, String name, String nodeType)
         throws RepositoryException
   {
      Node hierarchyNode = null;

      if (null != nFolder)
      {
         hierarchyNode = JcrNode.addNode(nFolder, name, nodeType);
         try
         {
            if ( !JcrNode.isNodeType(hierarchyNode, JcrProperties.MIXIN_REFERENCEABLE))
            {
               JcrNode.addMixin(hierarchyNode, JcrProperties.MIXIN_REFERENCEABLE);
            }
         }
         catch (NoSuchNodeTypeException nsnte)
         {
            // TODO log
         }

         Node nMetaData = null;
         if (hasVfsMetaDataMixin)
         {
            nMetaData = addLocalMetaData(hierarchyNode);
         }
         else
         {
            String id = getId(hierarchyNode, false);
            if (isUuidBasedId(id))
            {
               nMetaData = getMetaDataNode(id);
            }
         }

         if (null != nMetaData)
         {
            String prpVfsName = getVfsPropertyName(VfsUtils.VFS_NAME);

            JcrNode.setProperty(nMetaData, prpVfsName, name);
         }
      }

      return hierarchyNode;
   }

   private Node addLocalMetaData(Node hierarchyNode) throws NoSuchNodeTypeException,
         VersionException, ConstraintViolationException, LockException,
         RepositoryException
   {
      Node nMetaData;
      // use local metaData node to improve efficiency
      JcrNode.addMixin(hierarchyNode, getVfsNsPrefix()
            + VfsUtils.VFS_META_DATA_MIXIN);
      nMetaData = getMetaDataNode(hierarchyNode);
      return nMetaData;
   }

   public void removeHierarchyNode(Node nResource) throws RepositoryException
   {
      boolean hasLocalMetaData = hasLocalMetaData(nResource);

      if (hasLocalMetaData)
      {
         JcrItem.remove(nResource);
      }
      else
      {
         final String resourceId = getId(nResource);
         Node nMetaData = getMetaDataNode(resourceId, false);

         // try to remove the node itself first to "test" the privileges
         JcrItem.remove(nResource);

         if (null != nMetaData)
         {
            // TODO remove is not version enabled

            assert (null == getMetaDataNodeFromAttic(resourceId));

            Node nMetaDataAttic = getMetaDataAtticRoot();

            JcrSession.move(session, JcrItem.getPath(nMetaData),
                  JcrItem.getPath(nMetaDataAttic) + "/" + JcrItem.getName(nMetaData));

            assert (null != getMetaDataNodeFromAttic(resourceId));
         }
      }

      // TODO clean meta data for folders

   }

   public Node getContentNode(Node nFile) throws RepositoryException
   {
      Node nContent = null;

      Item iContent;
      if (isFrozenNode(nFile))
      {
         iContent = JcrNode.getNode(nFile, JcrProperties.JCR_CONTENT);
      }
      else
      {
         iContent = JcrNode.getPrimaryItem(nFile);
      }

      if (iContent instanceof Node)
      {
         // this is the original file
         nContent = (Node) iContent;
      }
      else if (iContent instanceof Property)
      {
         // this is itself a link to the original file
         nContent = JcrProperty.getNode(((Property) iContent));
      }

      return nContent;
   }

   public void visitMembers(Node nFolder, IFsNodeVisitor visitor)
         throws RepositoryException
   {
      visitMembers(JcrNode.getNodes(nFolder), visitor);
   }

   public void visitMembers(NodeIterator nodes, IFsNodeVisitor visitor)
         throws RepositoryException
   {
      final String prpVfsId = getVfsPropertyName(VfsUtils.VFS_ID);

      while (JcrNodeIterator.hasNext(nodes))
      {
         Node member = JcrNodeIterator.nextNode(nodes);

         NodeType nodeType = JcrNode.getPrimaryNodeType(member);
         if (isFolder(nodeType))
         {
            visitor.visitFolder(member);
         }
         else if (isFile(nodeType))
         {
            visitor.visitFile(member);
         }
         else if (JcrNode.hasProperty(member, prpVfsId))
         {
            String id = getStringProperty(member, prpVfsId);

            // resolve ID to node
            try
            {
               Node targetNode = findNode(id);
               if (null != targetNode)
               {
                  NodeType targetNodeType = JcrNode.getPrimaryNodeType(targetNode);
                  if (isFolder(targetNodeType))
                  {
                     visitor.visitFolder(targetNode);
                  }
                  else if (isFile(targetNodeType))
                  {
                     visitor.visitFile(targetNode);
                  }
               }
            }
            catch (ItemNotFoundException infe)
            {
               // TODO
            }
         }
         else
         {
            // Try to resolve parents of vfs:* nodes max. 10 levels
            // This enables to find files even by targeting their meta data in XPath queries.
            // e.g. /jcr:root//element(*, nt:file)/vfs:metaData/vfs:attributes/vfs:someSpecificMetaData
            int level = 0;
            boolean stop = false;
            Node targetNode = null;
            if (member.getName().startsWith(getVfsNsPrefix()))
            {
               try
               {
                  targetNode = member.getParent();
               }
               catch (ItemNotFoundException e)
               {
                  // no parent node exists
                  stop = true;
               }
               while (level < 10 && !stop)
               {
                  if (targetNode != null)
                  {
                     NodeType targetNodeType = JcrNode.getPrimaryNodeType(targetNode);
                     // only visit files
                     if (isFrozenNode(targetNode))
                     {
                        stop = true;
                     }
                     else if (isFolder(targetNodeType))
                     {
                        stop = true;
                     }
                     else if (isFile(targetNodeType))
                     {
                        stop = true;
                        visitor.visitFile(targetNode);
                     }

                     if ( !stop)
                     {
                        try
                        {
                           targetNode = targetNode.getParent();
                        }
                        catch (ItemNotFoundException e)
                        {
                           // no parent node exists
                           stop = true;
                        }
                     }
                     level++ ;
                  }
               }
            }
         }
      }
   }

   public String getVfsNsPrefix() throws RepositoryException
   {
      return vfsNsPrefix;
   }

   public String getMimeType(Node nFileContent) throws RepositoryException
   {
      return getStringProperty(nFileContent, JcrProperties.JCR_MIME_TYPE);
   }

   public Date getLastModified(Node nFileContent) throws RepositoryException
   {
      return getDateProperty(nFileContent, JcrProperties.JCR_LAST_MODIFIED);
   }

   public static String getStringProperty(Node node, String propertyName)
         throws RepositoryException
   {
      Property property = JcrNode.hasProperty(node, propertyName) ? JcrNode.getProperty(
            node, propertyName) : null;

      return ((null != property) && (PropertyType.STRING == JcrProperty.getType(property)))
            ? JcrProperty.getString(property)
            : null;
   }

   public static String setStringProperty(Node node, String propertyName, String value)
         throws RepositoryException
   {
      JcrNode.setProperty(node, propertyName, value);

      return getStringProperty(node, propertyName);
   }

   public static Date getDateProperty(Node node, String propertyName)
         throws RepositoryException
   {
      Property property = JcrNode.hasProperty(node, propertyName) ? JcrNode.getProperty(
            node, propertyName) : null;

      return getDateProperty(property);
   }

   public static Date getDateProperty(Property property) throws RepositoryException
   {
      Calendar date = ((null != property) && (PropertyType.DATE == JcrProperty.getType(property)))
            ? JcrProperty.getDate(property)
            : null;

      return (null != date) ? date.getTime() : null;
   }

   public static ArrayList<Date> getDatesProperty(Property property)
         throws RepositoryException
   {
      if (null != property && PropertyType.DATE == JcrProperty.getType(property))
      {
         Value[] values = property.getValues();
         ArrayList<Date> list = new ArrayList<Date>(values.length);
         for (int i = 0; i < values.length; i++ )
         {
            list.add(values[i].getDate().getTime());
         }
         return list;
      }
      return null;
   }

   public static Serializable getPropertyValue(Node node, String prpName)
         throws RepositoryException
   {
      final Property prp = JcrNode.hasProperty(node, prpName) //
            ? JcrNode.getProperty(node, prpName)
            : null;

      return (null != prp) ? getPropertyValue(prp) : null;
   }

   public static Serializable getPropertyValue(Property prp) throws RepositoryException
   {
      if (prp.getDefinition().isMultiple())
      {
         switch (JcrProperty.getType(prp))
         {
         case PropertyType.BOOLEAN:
            return JcrProperty.getBooleans(prp);

         case PropertyType.LONG:
            return JcrProperty.getLongs(prp);

         case PropertyType.DOUBLE:
            return JcrProperty.getDoubles(prp);

         case PropertyType.STRING:
            return JcrProperty.getStrings(prp);

         case PropertyType.DATE:
            return getDatesProperty(prp);

         default:
            return null;
         }
      }
      else
      {
         switch (JcrProperty.getType(prp))
         {
         case PropertyType.BOOLEAN:
            return Boolean.valueOf(JcrProperty.getBoolean(prp));

         case PropertyType.LONG:
            return new Long(JcrProperty.getLong(prp));

         case PropertyType.DOUBLE:
            return new Double(JcrProperty.getDouble(prp));

         case PropertyType.DECIMAL:
            return JcrProperty.getDecimal(prp);

         case PropertyType.STRING:
            return JcrProperty.getString(prp);

         case PropertyType.DATE:
            return getDateProperty(prp);

         default:
            return null;
         }
      }
   }

   @SuppressWarnings("unchecked")
   public boolean setPropertyValue(Node node, String name, Serializable value)
         throws RepositoryException
   {
      Property prp = JcrNode.hasProperty(node, name)
            ? JcrNode.getProperty(node, name)
            : null;

      if (null == prp)
      {
         if (null == value)
         {
            return false;
         }
         if (value instanceof Boolean)
         {
            JcrNode.setProperty(node, name, ((Boolean) value).booleanValue());
         }
         else if ((value instanceof Byte) || (value instanceof Short)
               || (value instanceof Integer) || (value instanceof Long))
         {
            JcrNode.setProperty(node, name, ((Number) value).longValue());
         }
         else if ((value instanceof Float) || (value instanceof Double))
         {
            JcrNode.setProperty(node, name, ((Number) value).doubleValue());
         }
         else if (value instanceof BigDecimal)
         {
            JcrNode.setProperty(node, name, (BigDecimal) value);
         }
         else if (value instanceof String)
         {
            JcrNode.setProperty(node, name, (String) value);
         }
         else if (value instanceof Date)
         {
            Calendar cal = Calendar.getInstance();
            cal.setTime(((Date) value));

            JcrNode.setProperty(node, name, cal);
         }
         else if (value instanceof Map)
         {
            // complex type
            Node subNode;
            if (JcrNode.hasNode(node, name))
            {
               subNode = JcrNode.getNode(node, name);
            }
            else
            {
               subNode = JcrNode.addNode(node, name, NodeTypes.NT_UNSTRUCTURED);
            }
            updateAttributesNode(subNode, (Map<String, Serializable>) value);
         }
         else if (value instanceof List)
         {
            // list of complex types or primitives
            List list = (List) value;
            if (list.size() == 0)
            {
               // must empty complex types list here
               if (JcrNode.hasNode(node, name))
               {
                  JcrNode.getNode(node, name).remove();
               }
            }
            else
            {
               if (list.get(0) instanceof List || list.get(0) instanceof Map)
               {
                  Node listSubNode;
                  if (JcrNode.hasNode(node, name))
                  {
                     listSubNode = JcrNode.getNode(node, name);
                  }
                  else
                  {
                     listSubNode = JcrNode.addNode(node, name, NodeTypes.NT_UNSTRUCTURED);
                     JcrNode.setProperty(listSubNode, JcrProperties.JCR_MIME_TYPE,
                           MIME_TYPE_METADATA_LIST_NODE);
                  }

                  NodeIterator listElementNodes = JcrNode.getNodes(listSubNode, name);
                  // list of complex types
                  for (Map listElement : ((List<Map>) list))
                  {
                     Node subNode;
                     if (JcrNodeIterator.hasNext(listElementNodes))
                     {
                        subNode = JcrNodeIterator.nextNode(listElementNodes);
                     }
                     else
                     {
                        subNode = JcrNode.addNode(listSubNode, name,
                              NodeTypes.NT_UNSTRUCTURED);
                     }
                     updateAttributesNode(subNode, listElement);
                  }
                  // delete remaining if any
                  while (JcrNodeIterator.hasNext(listElementNodes))
                  {
                     JcrItem.remove(listElementNodes.nextNode());
                  }
               }
               else
               {
                  // list of primitives
                  JcrNode.setProperty(node, name, toJcrMultivalue(list));
               }
            }
         }
         else
         {
            throw new IllegalOperationException("Unsupported attribute type: "
                  + ((value == null) ? "null" : value.getClass().getName()));
         }

         // property was updated
         return true;
      }

      return setPropertyValue(prp, value);
   }

   @SuppressWarnings("unchecked")
   public static boolean setPropertyValue(Property prp, Serializable value)
         throws RepositoryException
   {
      if (value == null)
      {
         if ( !JcrPropertyDefinition.isMandatory(JcrProperty.getDefinition(prp)))
         {
            JcrItem.remove(prp);
         }
      }
      else if (value instanceof Boolean)
      {
         JcrProperty.setValue(prp, ((Boolean) value).booleanValue());
      }
      else if ((value instanceof Byte) || (value instanceof Short)
            || (value instanceof Integer) || (value instanceof Long))
      {
         JcrProperty.setValue(prp, ((Number) value).longValue());
      }
      else if ((value instanceof Float) || (value instanceof Double))
      {
         JcrProperty.setValue(prp, ((Number) value).doubleValue());
      }
      else if (value instanceof BigDecimal)
      {
         JcrProperty.setValue(prp, (BigDecimal) value);
      }
      else if (value instanceof String)
      {
         JcrProperty.setValue(prp, (String) value);
      }
      else if (value instanceof Date)
      {
         Calendar cal = Calendar.getInstance();
         cal.setTime(((Date) value));

         JcrProperty.setValue(prp, cal);
      }
      else if (value instanceof List)
      {
         // only a list of primitives expected here
         JcrProperty.setValue(prp, toJcrMultivalue((List<Serializable>) value));
      }
      else
      {
         throw new IllegalOperationException("Unsupported attribute type: " + value);
      }

      // property was updated
      return true;
   }

   private static Value[] toJcrMultivalue(List<Serializable> primitiveValueList)
   {
      Value[] result = new Value[primitiveValueList.size()];
      for (int i = 0; i < primitiveValueList.size(); i++ )
      {
         Serializable value = primitiveValueList.get(i);
         if (value instanceof Boolean)
         {
            result[i] = new BooleanValue(((Boolean) value).booleanValue());
         }
         else if ((value instanceof Byte) || (value instanceof Short)
               || (value instanceof Integer) || (value instanceof Long))
         {
            result[i] = new LongValue(((Number) value).longValue());
         }
         else if ((value instanceof Float) || (value instanceof Double))
         {
            result[i] = new DoubleValue(((Number) value).doubleValue());
         }
         else if (value instanceof String)
         {
            result[i] = new StringValue((String) value);
         }
         else if (value instanceof Date)
         {
            Calendar cal = Calendar.getInstance();
            cal.setTime(((Date) value));

            result[i] = new DateValue(cal);
         }
         else
         {
            throw new IllegalOperationException("Unsupported attribute type: " + value);
         }
      }
      return result;
   }

   public boolean updateProperty(Node node, String prpName, Serializable prpValue)
         throws RepositoryException
   {
      boolean updated = false;

      Property prp = JcrNode.hasProperty(node, prpName) //
            ? JcrNode.getProperty(node, prpName)
            : null;

      if (null == prp)
      {
         if (null != prpValue)
         {
            updated |= setPropertyValue(node, prpName, prpValue);
         }
      }
      else
      {
         updated = updateProperty(prp, prpValue);
      }

      return updated;
   }

   public boolean updateProperty(Property prp, Serializable prpValue)
         throws RepositoryException
   {
      boolean updated = false;

      if ( !CompareHelper.areEqual(getPropertyValue(prp), prpValue))
      {
         if (null == prpValue)
         {
            if ( !JcrPropertyDefinition.isMandatory(JcrProperty.getDefinition(prp)))
            {
               JcrItem.remove(prp);
               updated = true;
            }
         }
         else
         {
            setPropertyValue(prp, prpValue);
            updated = true;
         }
      }

      return updated;
   }

   public String getVfsPropertyName(String basename) throws RepositoryException
   {
      if (null == this.vfsNsPrefix)
      {
         ensureVfsNamespace();
      }

      // TODO cache
      return vfsNsPrefix + basename;
   }

   public void ensureRepositoryIsInitialized() throws RepositoryException
   {
      if (isEmpty(vfsNsPrefix))
      {
         ensureVfsNamespace();
      }

      if ( !hasVfsMetaDataMixin)
      {
         if (null == getMetaDataRoot())
         {
            throw new RepositoryOperationFailedException(
                  "Failed obtaining the vfs:metaData node.");
         }
         if (null == getMetaDataAtticRoot())
         {
            throw new RepositoryOperationFailedException(
                  "Failed obtaining the vfs:metaDataAttic node.");
         }
      }
   }

   // private void ensureAdministratorPrivileges() throws RepositoryException
   // {
   // org.apache.jackrabbit.api.jsr283.Session adminSession = createAdminSession();
   // AccessControlManager accessControlManager = adminSession.getAccessControlManager();
   // String restrictedArea = "/";
   // AccessControlPolicy[] policies = accessControlManager.getPolicies(restrictedArea);
   // Privilege[] allPrivileges =
   // {accessControlManager.privilegeFromName(Privilege.JCR_ALL)};
   // ((AccessControlList) policies[0]).addAccessControlEntry(
   // new JcrVfsPrincipal("{ipp.role}Administrator"), allPrivileges);
   // accessControlManager.setPolicy(restrictedArea, policies[0]);
   // adminSession.save();
   // adminSession.logout();
   // }

   public void ensureVfsNamespace() throws RepositoryException
   {
      NamespaceRegistry nsRegistry = JcrWorkspace.getNamespaceRegistry(JcrSession.getWorkspace(session));

      // TODO configure prefix per property

      boolean hasVfsUri = false;
      for (String uri : JcrNamespaceRegistry.getURIs(nsRegistry))
      {
         if (CompareHelper.areEqual(uri, VfsUtils.NS_URI_JCR_VFS_1_0))
         {
            hasVfsUri = true;
            break;
         }
      }

      if ( !hasVfsUri)
      {
         // this is a way of giving IPP administrators the JCR.ALL privilege
         // another way is to put the "administrators" group into SimpleCredentials
         // (see TestJcrVfsAgainstJackrabbit)
         // ensureAdministratorPrivileges();
         JcrNamespaceRegistry.registerNamespace(nsRegistry, VfsUtils.NS_PREFIX_VFS,
               VfsUtils.NS_URI_JCR_VFS_1_0);
      }
      else
      {
         // TODO check prefix is vfs

         // TODO verify vfs is mapped to NS URI
      }

      this.vfsNsPrefix = JcrNamespaceRegistry.getPrefix(nsRegistry,
            VfsUtils.NS_URI_JCR_VFS_1_0);
      if ( !isEmpty(vfsNsPrefix))
      {
         vfsNsPrefix += ":";
      }


      NodeTypeManager nodeTypeManager = JcrSession.getWorkspace(session)
            .getNodeTypeManager();
      if ( !isEmpty(vfsNsPrefix))
      {
         try
         {
            try
            {
               nodeTypeManager.getNodeType(vfsNsPrefix + VfsUtils.VFS_META_DATA_MIXIN);

               this.hasVfsMetaDataMixin = true;
            }
            catch (NoSuchNodeTypeException nsnte)
            {
               // cnd file loading removed and replaced by createMetaDataNodeType()
               NodeTypeDefinition nodeTypeTemplate = createMetaDataNodeType(nodeTypeManager);

               // TODO: handle exception
               nodeTypeManager.registerNodeType(nodeTypeTemplate, false);

               this.hasVfsMetaDataMixin = true;
            }
         }
         catch (Exception ioe)
         {
            // TODO Auto-generated catch block
            throw new RepositoryOperationFailedException(ioe);
         }
      }
   }

   private NodeTypeTemplate createMetaDataNodeType(NodeTypeManager nodeTypeManager)
         throws ConstraintViolationException, UnsupportedRepositoryOperationException,
         RepositoryException
   {
      // creates NodeTypeTemplate using the following .cnd
      // getClass().getResourceAsStream("vfsMetaDataMixin.cnd")
      //
      // /* jcr-vfs schema extension to support local metaData nodes */
      //
      // The namespace declaration
      // <vfs = 'http://www.sungard.com/infinity/vfs/1.0'>
      //
      // [vfs:vfsMetaData] mixin
      // + vfs:metaData (nt:unstructured) = nt:unstructured mandatory autocreated

      NodeTypeTemplate nodeTypeTemplate = nodeTypeManager.createNodeTypeTemplate();

      nodeTypeTemplate.setOrderableChildNodes(false);
      nodeTypeTemplate.setAbstract(false);
      nodeTypeTemplate.setQueryable(true);

      // [vfs:vfsMetaData]
      nodeTypeTemplate.setName(vfsNsPrefix + VfsUtils.VFS_META_DATA_MIXIN);
      // mixin
      nodeTypeTemplate.setMixin(true);

      // + vfs:metaData (nt:unstructured) = nt:unstructured mandatory autocreated

      // +
      NodeDefinitionTemplate nd = nodeTypeManager.createNodeDefinitionTemplate();

      // vfs:metaData
      nd.setName(vfsNsPrefix + VfsUtils.VFS_META_DATA);

      // (nt:unstructured)

      nd.setRequiredPrimaryTypeNames(Collections.singletonList(NodeTypes.NT_UNSTRUCTURED)
            .toArray(new String[1]));
      // = nt:unstructured
      nd.setDefaultPrimaryTypeName(NodeTypes.NT_UNSTRUCTURED);

      // mandatory
      nd.setMandatory(true);
      // autocreated
      nd.setAutoCreated(true);

      nd.setOnParentVersion(OnParentVersionAction.COPY);

      nodeTypeTemplate.getNodeDefinitionTemplates().add(nd);

      return nodeTypeTemplate;
   }

   public Node getMetaDataRoot() throws RepositoryException
   {
      final String nodeNameVfsMetaData = getVfsNsPrefix() + VfsUtils.VFS_META_DATA;
      final Node nRoot = JcrSession.getRootNode(session);

      final Node metaDataRoot;
      if ( !JcrNode.hasNode(nRoot, nodeNameVfsMetaData))
      {
         Session adminSession = createAdminSession();

         JcrNode.addNode(JcrSession.getRootNode(adminSession), nodeNameVfsMetaData,
               NodeTypes.NT_UNSTRUCTURED);

         // allow JCR.ALL to all on metaDataRoot
         AccessControlManager accessControlManager = getAccessControlManager(adminSession);
         AccessControlPolicyIterator policies = accessControlManager.getApplicablePolicies("/"
               + nodeNameVfsMetaData);
         // only set policy if available. if not -> probably the security is disabled
         if (policies.hasNext())
         {
            AccessControlList aclPolicy = ((AccessControlList) policies.nextAccessControlPolicy());
            Privilege[] allPrivileges = {accessControlManager.privilegeFromName(Privilege.JCR_ALL)};
            aclPolicy.addAccessControlEntry(new JcrVfsPrincipal("everyone"),
                  allPrivileges);
            accessControlManager.setPolicy("/" + nodeNameVfsMetaData, aclPolicy);
         }

         adminSession.save();
         adminSession.logout();
      }

      metaDataRoot = JcrNode.getNode(nRoot, nodeNameVfsMetaData);

      return metaDataRoot;
   }

   private Session createAdminSession() throws AccessDeniedException, RepositoryException
   {
      // TODO somehow retrieve adminId without introducing dependencies on Jackrabbit
      // and specific implementations
      JcrVfsUserManager userManager = (JcrVfsUserManager) getUserManager(session);
      SimpleCredentials credentials = new SimpleCredentials(
            userManager.getAdminId() /* "admin" */, "motu".toCharArray());
      return JcrRepository.login(session.getRepository(), credentials);
   }

   public Node getMetaDataAtticRoot() throws RepositoryException
   {
      final String nodeNameVfsMetaDataAttic = getVfsNsPrefix()
            + VfsUtils.VFS_META_DATA_ATTIC;

      final Node nRoot = JcrSession.getRootNode(session);

      final Node metaDataAtticRoot;
      if ( !JcrNode.hasNode(nRoot, nodeNameVfsMetaDataAttic))
      {
         Session adminSession = createAdminSession();

         JcrNode.addNode(JcrSession.getRootNode(adminSession), nodeNameVfsMetaDataAttic,
               NodeTypes.NT_UNSTRUCTURED);

         // allow JCR.ALL to all on metaDataRoot
         AccessControlManager accessControlManager = getAccessControlManager(adminSession);
         AccessControlPolicyIterator policies = accessControlManager.getApplicablePolicies("/"
               + nodeNameVfsMetaDataAttic);
         // only set policy if available. if not -> probably the security is disabled
         if (policies.hasNext())
         {
            AccessControlList aclPolicy = ((AccessControlList) policies.nextAccessControlPolicy());
            Privilege[] allPrivileges = {accessControlManager.privilegeFromName(Privilege.JCR_ALL)};
            aclPolicy.addAccessControlEntry(new JcrVfsPrincipal("everyone"),
                  allPrivileges);
            accessControlManager.setPolicy("/" + nodeNameVfsMetaDataAttic, aclPolicy);
         }

         adminSession.save();
         adminSession.logout();
      }

      metaDataAtticRoot = JcrNode.getNode(nRoot, nodeNameVfsMetaDataAttic);

      return metaDataAtticRoot;
   }

   private static AccessControlManager getAccessControlManager(Session session)
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
     return session.getAccessControlManager();
   }

   private UserManager getUserManager(Session session) throws RepositoryException
   {
      UserManager userManager = null;

      if (session instanceof SessionImpl)
      {
         userManager = ((SessionImpl) session).getUserManager();
      }
      else if (session instanceof JCASessionHandle)
      {
         JCAManagedConnection managedConnection = ((JCASessionHandle) session).getManagedConnection();
         Session xaSession = managedConnection.getSession((JCASessionHandle) session);
         if (xaSession instanceof XASessionImpl)
         {
            userManager = ((XASessionImpl) xaSession).getUserManager();
         }
         else
         {
            throw new RepositoryOperationFailedException(
                  "Expected (org.apache.jackrabbit.core.XASessionImpl) but found ("
                        + xaSession + ")");
         }
      }
      else if (session instanceof org.eclipse.stardust.vfs.jcr.jca.JCASessionHandle)
      {
         org.eclipse.stardust.vfs.jcr.jca.JCAManagedConnection managedConnection = ((org.eclipse.stardust.vfs.jcr.jca.JCASessionHandle) session).getManagedConnection();
         Session xaSession = managedConnection.getSession((org.eclipse.stardust.vfs.jcr.jca.JCASessionHandle) session);
         if (xaSession instanceof XASessionImpl)
         {
            userManager = ((XASessionImpl) xaSession).getUserManager();
         }
         else
         {
            throw new RepositoryOperationFailedException(
                  "Expected (org.apache.jackrabbit.core.XASessionImpl) but found ("
                        + xaSession + ")");
         }
      }
      else
      {
         throw new RepositoryOperationFailedException(
               "Only (org.apache.jackrabbit.api.jsr283.Session), (org.apache.jackrabbit.jca.JCASessionHandle), (org.eclipse.stardust.vfs.jcr.jca.JCAManagedConnection) are supported for jcr security. Found session class ("
                     + session + ")");
      }

      return userManager;
   }

   private ItemManager getItemManager(Session session) throws RepositoryException
   {
      ItemManager itemManager = null;

      if (session instanceof SessionImpl)
      {
         itemManager = ((SessionImpl) session).getItemManager();
      }
      else if (session instanceof JCASessionHandle)
      {
         JCAManagedConnection managedConnection = ((JCASessionHandle) session).getManagedConnection();
         Session xaSession = managedConnection.getSession((JCASessionHandle) session);
         if (xaSession instanceof XASessionImpl)
         {
            itemManager = ((XASessionImpl) xaSession).getItemManager();
         }
         else
         {
            throw new RepositoryOperationFailedException(
                  "Expected (org.apache.jackrabbit.core.XASessionImpl) but found ("
                        + xaSession + ")");
         }
      }
      else if (session instanceof org.eclipse.stardust.vfs.jcr.jca.JCASessionHandle)
      {
         org.eclipse.stardust.vfs.jcr.jca.JCAManagedConnection managedConnection = ((org.eclipse.stardust.vfs.jcr.jca.JCASessionHandle) session).getManagedConnection();
         Session xaSession = managedConnection.getSession((org.eclipse.stardust.vfs.jcr.jca.JCASessionHandle) session);
         if (xaSession instanceof XASessionImpl)
         {
            itemManager = ((XASessionImpl) xaSession).getItemManager();
         }
         else
         {
            throw new RepositoryOperationFailedException(
                  "Expected (org.apache.jackrabbit.core.XASessionImpl) but found ("
                        + xaSession + ")");
         }
      }
      else
      {
         throw new RepositoryOperationFailedException(
               "Only (org.apache.jackrabbit.api.jsr283.Session), (org.apache.jackrabbit.jca.JCASessionHandle), (org.eclipse.stardust.vfs.jcr.jca.JCAManagedConnection) are supported for jcr security. Found session class ("
                     + session + ")");
      }

      return itemManager;
   }

   public Node getMetaDataNode(Node nResource) throws RepositoryException
   {
      Node nMetaData;

      if (hasLocalMetaData(nResource))
      {
         nMetaData = JcrNode.getNode(nResource, getVfsNsPrefix() + VfsUtils.VFS_META_DATA);
      }
      else if (isFrozenNode(nResource))
      {
         // TODO
         String id = getId(nResource);

         nMetaData = getMetaDataNode(id, false);
         if (null == nMetaData)
         {
            nMetaData = getMetaDataNodeFromAttic(id);
         }

         if (null != nMetaData)
         {
            Version revision = getVersion(nResource);

            VersionHistory history = JcrNode.getVersionHistory(nMetaData);
            Version metaDataRevision = JcrVersionHistory.getVersionByLabel(history,
                  getRevisionId(revision));

            nMetaData = JcrNode.getNode(metaDataRevision, JcrProperties.JCR_FROZEN_NODE);
         }
      }
      else
      {
         String id = getId(nResource);

         nMetaData = getMetaDataNode(id);
      }

      return nMetaData;
   }

   private boolean hasLocalMetaData(Node nResource) throws RepositoryException
   {
      return JcrNode.hasNode(nResource, getVfsNsPrefix() + VfsUtils.VFS_META_DATA);
   }

   public Node getMetaDataNode(String uuid) throws RepositoryException
   {
      return getMetaDataNode(uuid, true);
   }

   public Node getMetaDataNode(String uuid, boolean create) throws RepositoryException
   {
      if (isUuidBasedId(uuid))
      {
         uuid = getUuidFromId(uuid);
      }

      if (VfsUtils.REPOSITORY_ROOT.equals(uuid))
      {
         // no metadata for root node
         return null;
      }

      final Node metaDataRoot = getMetaDataRoot();

      Node metaDataNode;
      if (JcrNode.hasNode(metaDataRoot, uuid))
      {
         metaDataNode = JcrNode.getNode(metaDataRoot, uuid);
      }
      else
      {
         if (create)
         {
            metaDataNode = JcrNode.addNode(metaDataRoot, uuid, NodeTypes.NT_UNSTRUCTURED);

            final String prpVfsId = getVfsPropertyName(VfsUtils.VFS_ID);
            JcrNode.setProperty(metaDataNode, prpVfsId, JcrVfsOperations.PREFIX_JCR_UUID
                  + uuid);
         }
         else
         {
            metaDataNode = null;
         }
      }

      return metaDataNode;
   }

   public Node getMetaDataNodeFromAttic(String uuid) throws RepositoryException
   {
      if (isUuidBasedId(uuid))
      {
         uuid = getUuidFromId(uuid);
      }

      final Node metaDataRoot = getMetaDataAtticRoot();

      Node metaDataNode = null;
      if (JcrNode.hasNode(metaDataRoot, uuid))
      {
         metaDataNode = JcrNode.getNode(metaDataRoot, uuid);
      }

      return metaDataNode;
   }

   public Node getVfsAttributesNode(Node nMetaData, String propertyName) throws RepositoryException
   {
      final String vfsAttributesId = getVfsPropertyName(propertyName);

      Node nVfsAttributes = null;
      if ( !JcrNode.hasNode(nMetaData, vfsAttributesId))
      {
         if (JcrNode.isCheckedOut(nMetaData) && !isFrozenNode(nMetaData))
         {
            nVfsAttributes = JcrNode.addNode(nMetaData, vfsAttributesId,
                  NodeTypes.NT_UNSTRUCTURED);
         }
      }
      else
      {
         nVfsAttributes = JcrNode.getNode(nMetaData, vfsAttributesId);
      }

      return nVfsAttributes;
   }

   public List<Node> findFileVersions(String fileId) throws RepositoryException
   {
      Node nFile = findFile(fileId);
      VersionHistory nodeHistory = JcrNode.getVersionHistory(nFile);

      List<Node> result = newList();
      Set<Version> visitedVersions = newSet();
      fillVersions(nodeHistory.getRootVersion(), result, visitedVersions);
      return result;
   }

   private void fillVersions(Version version, List<Node> result,
         Set<Version> visitedVersions) throws RepositoryException
   {
      if ( !ROOT_VERSION.equals(version.getName()) && !visitedVersions.contains(version))
      {
         result.add(findFile(getRevisionId(version)));
         visitedVersions.add(version);
      }

      for (Version successor : version.getSuccessors())
      {
         fillVersions(successor, result, visitedVersions);
      }
   }

   public void restoreNodeVersion(Node nFile) throws RepositoryException
   {

      VersionHistory fileHistory = JcrNode.getVersionHistory(nFile);
      VersionIterator versionIterator = fileHistory.getAllVersions();
      Version lastVersion = JcrNode.getBaseVersion(nFile);

      while (versionIterator.hasNext())
      {
         Version version = versionIterator.nextVersion();
         if (version.getIdentifier().equals(lastVersion.getIdentifier()))
         {
            // Move one version back.
            Version pred = version.getPredecessors()[0];
            if ( !pred.isSame(fileHistory.getRootVersion()))
            {
               JcrNode.restore(nFile, pred, true);
               JcrNode.checkin(nFile);
               try
               {
                  fileHistory.removeVersion(version.getName());
               }
               catch (javax.jcr.ReferentialIntegrityException e)
               {
                  throw new RepositoryOperationFailedException(
                        "Cannot delete root version.", e);
               }
            }
            else
            {
               // Removal of last version would lead to dangling reference.
               throw new RepositoryOperationFailedException(
                     "Cannot delete root version.", new ReferentialIntegrityException());
            }
            break;
         }
      }
   }

   public void removeNodeVersionByRevision(Node nFile, String fileRevisionIdToDelete)
         throws RepositoryException
   {

      VersionHistory versionHistory = JcrNode.getVersionHistory(nFile);
      VersionIterator versionIterator = versionHistory.getAllVersions();

      boolean success = false;
      while (versionIterator.hasNext())
      {
         Version version = versionIterator.nextVersion();

         if (version.getIdentifier().equals(
               JcrVfsOperations.getRevisionUuidFromId(fileRevisionIdToDelete)))
         {
            versionHistory.removeVersion(version.getName());
            success = true;
         }
      }
      if ( !success)
      {
         throw new ItemNotFoundException("Could not find version with revision id "
               + fileRevisionIdToDelete);
      }
   }

   public int getNodeVersionHistorySize(Node nFile) throws RepositoryException
   {
      int count = 0;
      VersionHistory nodeHistory = JcrNode.getVersionHistory(nFile);
      VersionIterator allVersions = nodeHistory.getAllVersions();

      while (allVersions.hasNext())
      {
         allVersions.next();
         count++ ;
      }

      return count;
   }

   public static Set<IPrivilege> getNodePrivileges(Node node) throws RepositoryException
   {
      AccessControlManager acm = getAccessControlManager(node.getSession());

      return toJcrPrivileges(acm, acm.getPrivileges(node.getPath()));
   }

   public static Set<IAccessControlPolicy> getNodePolicies(Node node)
         throws RepositoryException
   {
      AccessControlManager acm = getAccessControlManager(node.getSession());

      return toJcrPolicies(acm, acm.getPolicies(node.getPath()), false, false);
   }

   public static Set<IAccessControlPolicy> getNodeEffectivePolicies(Node node)
         throws RepositoryException
   {
      AccessControlManager acm = getAccessControlManager(node.getSession());

      return toJcrPolicies(acm, acm.getEffectivePolicies(node.getPath()), false, true);
   }

   public static Set<IAccessControlPolicy> getNodeApplicablePolicies(Node node)
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
      // needs to be checked out to retrieve applicable policies.
      ensureVersionIsModifiable(node);

      AccessControlManager acm = getAccessControlManager(node.getSession());

      return toJcrPolicies(acm, acm.getApplicablePolicies(node.getPath()), true, false);
   }

   private static Set<IAccessControlPolicy> toJcrPolicies(AccessControlManager acm,
         AccessControlPolicy[] policies, boolean isNew, boolean isReadonly)
         throws RepositoryException
   {
      return toJcrPolicies(acm, Arrays.asList(policies).iterator(), isNew, isReadonly);
   }

   private static Set<IAccessControlPolicy> toJcrPolicies(AccessControlManager acm,
         Iterator<AccessControlPolicy> policies, boolean isNew, boolean isReadonly)
         throws RepositoryException
   {
      Set<IAccessControlPolicy> jcrAcps = newSet();
      while (policies.hasNext())
      {
         AccessControlPolicy acp = policies.next();
         if (acp instanceof AccessControlList)
         {
            jcrAcps.add(toJcrPolicy(acm, (AccessControlList) acp, isNew, isReadonly));
         }
      }
      return Collections.unmodifiableSet(jcrAcps);

   }

   private static IAccessControlPolicy toJcrPolicy(AccessControlManager acm,
         AccessControlList acl, boolean isNew, boolean isReadonly)
         throws RepositoryException
   {
      Set<IAccessControlEntry> jcrAces = newSet();

      AccessControlEntry[] aces = acl.getAccessControlEntries();
      for (int i = 0; i < aces.length; i++ )
      {
         Set<IPrivilege> jcrPrivileges = toJcrPrivileges(acm, aces[i].getPrivileges());

         final AccessControlEntryImpl aceImpl = castToJackrabbitACE(aces[i]);
         jcrAces.add(new JcrVfsAccessControlEntry(new JcrVfsPrincipal(
               aces[i].getPrincipal().getName()), jcrPrivileges, aceImpl.isAllow() ? EntryType.ALLOW : EntryType.DENY));
      }

      if (isReadonly)
      {
         return new JcrVfsReadonlyAccessControlPolicy(jcrAces);
      }
      else
      {
         return new JcrVfsAccessControlPolicy(jcrAces, isNew);
      }

   }

   private static Set<IPrivilege> toJcrPrivileges(
         AccessControlManager accessControlManager, Privilege[] privileges)
         throws RepositoryException
   {
      Set<IPrivilege> jcrPrivileges = newSet();

      for (int i = 0; i < privileges.length; i++ )
      {
         IPrivilege jcrPrivilege = toJcrPrivilege(accessControlManager,
               privileges[i].getName());
         if (null != jcrPrivilege)
         {
            jcrPrivileges.add(jcrPrivilege);

            if (IPrivilege.MODIFY_PRIVILEGE.equals(jcrPrivilege.getName()))
            {
               // modify implies delete, delete_children, create with JCR, see
               // org.apache.jackrabbit.api.jsr283.security.Privilege#JCR_WRITE
               //
               jcrPrivileges.add(new JcrVfsPrivilege(IPrivilege.DELETE_PRIVILEGE));
               jcrPrivileges.add(new JcrVfsPrivilege(IPrivilege.DELETE_CHILDREN_PRIVILEGE));
               jcrPrivileges.add(new JcrVfsPrivilege(IPrivilege.CREATE_PRIVILEGE));
               // JCR_MODIFY_PROPERTIES not exposed.
            }
         }
      }

      return jcrPrivileges;
   }

   private static IPrivilege toJcrPrivilege(AccessControlManager acm, String privilegeName)
         throws RepositoryException
   {
      // filter out Privileges which are not to be exposed.

      Privilege nodeTypeManagementPrivilege = acm.privilegeFromName(Privilege.JCR_NODE_TYPE_MANAGEMENT);
      Privilege modifyPropertiesPrivilege = acm.privilegeFromName(Privilege.JCR_MODIFY_PROPERTIES);
      Privilege versionManagementPrivilege = acm.privilegeFromName(Privilege.JCR_VERSION_MANAGEMENT);

      if (nodeTypeManagementPrivilege.getName().equals(privilegeName))
      {
         // filter JCR_NODE_TYPE_MANAGEMENT;
         return null;
      }
      else if (modifyPropertiesPrivilege.getName().equals(privilegeName))
      {
         // filter JCR_MODIFY_PROPERTIES;
         return null;
      }
      else if (versionManagementPrivilege.getName().equals(privilegeName))
      {
         // filter JCR_VERSION_MANAGEMENT;
         return null;
      }
      else
      {
         return new JcrVfsPrivilege(privilegeName);
      }
   }

   private static void filterInPrivilege(Set<Privilege> privileges,
         AccessControlManager accessControlManager, IPrivilege jcrPrivilege)
         throws RepositoryException
   {
      // add JCR_NODE_TYPE_MANAGEMENT and JCR_MODIFY_PROPERTIES if JCR_ADD_CHILD_NODES is
      // added.

      privileges.add(accessControlManager.privilegeFromName(jcrPrivilege.getName()));

      if (IPrivilege.CREATE_PRIVILEGE.equals(jcrPrivilege.getName()))
      {
         privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_NODE_TYPE_MANAGEMENT));
         privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_MODIFY_PROPERTIES));
         privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_VERSION_MANAGEMENT));
      }
      else if (IPrivilege.MODIFY_PRIVILEGE.equals(jcrPrivilege.getName()))
      {
         privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_NODE_TYPE_MANAGEMENT));
         privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_VERSION_MANAGEMENT));
      }
      else if (IPrivilege.DELETE_PRIVILEGE.equals(jcrPrivilege.getName()))
      {
         privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_MODIFY_PROPERTIES));
      }
   }

   public void setNodePolicy(Node node, IAccessControlPolicy jcrPolicy)
         throws RepositoryException
   {
      AccessControlManager accessControlManager = getAccessControlManager(node.getSession());

      if (jcrPolicy.isNew())
      {
         // retrieve it from applicable and add
         AccessControlList policy = findApplicablePolicy(accessControlManager,
               accessControlManager.getApplicablePolicies(node.getPath()),
               jcrPolicy.getOriginalState());

         // remove all ACEs first
         while (0 < policy.getAccessControlEntries().length)
         {
            policy.removeAccessControlEntry(policy.getAccessControlEntries()[0]);
         }

         // add again from jcrPolicy
         for (IAccessControlEntry ace : jcrPolicy.getAccessControlEntries())
         {
            Set<Privilege> privileges = CollectionUtils.newSet();
            for (final IPrivilege jcrPrivilege : ace.getPrivileges())
            {
               filterInPrivilege(privileges, accessControlManager, jcrPrivilege);
            }

            final JackrabbitAccessControlList policyImpl = castToJackrabbitACL(policy);
            policyImpl.addEntry(ace.getPrincipal(),
                  privileges.toArray(new Privilege[privileges.size()]),
                  ace.getType() == EntryType.ALLOW ? true : false);
         }

         if (policy.getAccessControlEntries().length != 0)
         {
            ensureVersionIsModifiable(node);
            accessControlManager.setPolicy(node.getPath(), policy);
         }
      }
      else
      {
         // retrieve it from current and change
         AccessControlList policy = findApplicablePolicy(accessControlManager,
               accessControlManager.getPolicies(node.getPath()),
               jcrPolicy.getOriginalState());

         // remove all ACEs first
         while (0 < policy.getAccessControlEntries().length)
         {
            policy.removeAccessControlEntry(policy.getAccessControlEntries()[0]);
         }

         // add again from jcrPolicy
         for (IAccessControlEntry ace : jcrPolicy.getAccessControlEntries())
         {
            Set<Privilege> privileges = CollectionUtils.newSet();
            for (final IPrivilege jcrPrivilege : ace.getPrivileges())
            {
               filterInPrivilege(privileges, accessControlManager, jcrPrivilege);
            }

            final JackrabbitAccessControlList policyImpl = castToJackrabbitACL(policy);
            policyImpl.addEntry(ace.getPrincipal(),
                  privileges.toArray(new Privilege[privileges.size()]),
                  ace.getType() == EntryType.ALLOW ? true : false);
         }

         ensureVersionIsModifiable(node);
         if (policy.getAccessControlEntries().length == 0)
         {
            accessControlManager.removePolicy(node.getPath(), policy);
         }
         else
         {
            accessControlManager.setPolicy(node.getPath(), policy);
         }
      }
   }

   private static AccessControlList findApplicablePolicy(AccessControlManager acm,
         AccessControlPolicy[] policies, Set<IAccessControlEntry> originalState)
         throws RepositoryException
   {
      for (int i = 0; i < policies.length; i++ )
      {
         if (policies[i] instanceof AccessControlList)
         {
            AccessControlList policy = (AccessControlList) policies[i];
            if (toJcrPolicy(acm, policy, false, true).getAccessControlEntries().equals(
                  originalState))
            {
               return policy;
            }
         }
      }
      throw new AccessControlException("No applicable policy found for original state: "
            + originalState);
   }

   private static AccessControlList findApplicablePolicy(AccessControlManager acm,
         AccessControlPolicyIterator applicablePolicies,
         Set<IAccessControlEntry> originalState) throws RepositoryException
   {
      while (applicablePolicies.hasNext())
      {
         AccessControlPolicy p = applicablePolicies.nextAccessControlPolicy();
         if (p instanceof AccessControlList)
         {
            AccessControlList policy = (AccessControlList) p;
            if (toJcrPolicy(acm, policy, true, true).getAccessControlEntries().equals(
                  originalState))
            {
               return policy;
            }
         }
      }
      throw new AccessControlException("No applicable policy found for original state: "
            + originalState);
   }

   public IPrivilege getPrivilegeByName(String privilegeName)
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
      return getPrivilegeByName(getAccessControlManager(session), privilegeName);
   }

   public static IPrivilege getPrivilegeByName(AccessControlManager acm,
         String privilegeName) throws UnsupportedRepositoryOperationException,
         RepositoryException
   {
      Privilege privilege = acm.privilegeFromName(privilegeName);
      if (privilege == null)
      {
         throw new AccessControlException("Privilege '" + privilegeName
               + "' does not exist.");
      }
      return new JcrVfsPrivilege(privilege.getName());
   }

   public MetaDataLocation getMetaDataLocation()
   {
      if (hasVfsMetaDataMixin)
      {
         return MetaDataLocation.LOCAL;
      }
      else
      {
         return MetaDataLocation.GLOBAL;
      }
   }

   public int getRepositoryVersion() throws RepositoryException
   {
      final Node nRoot = JcrSession.getRootNode(session);

      if (JcrNode.hasProperty(nRoot, VFS_REPOSITORY_VERSION_PROPERTY))
      {
         return (int) JcrProperty.getLong(JcrNode.getProperty(nRoot, VFS_REPOSITORY_VERSION_PROPERTY));
      }
      return 0;
   }

   public void setRepositoryVersion(int targetVersion) throws RepositoryException
   {
      final Node nRoot = JcrSession.getRootNode(session);

      JcrNode.setProperty(nRoot, VFS_REPOSITORY_VERSION_PROPERTY, targetVersion);
   }

   private static AccessControlEntryImpl castToJackrabbitACE(final AccessControlEntry ace)
   {
      if ( !(ace instanceof AccessControlEntryImpl))
      {
         throw new IllegalArgumentException(
               "Given Access Control Entry is *NOT* the Jackrabbit implementation. Jackrabbit JCR implementation is required.");
      }
      
      return (AccessControlEntryImpl) ace;
   }
   
   private static JackrabbitAccessControlList castToJackrabbitACL(
         final AccessControlList acl)
   {
      if ( !(acl instanceof JackrabbitAccessControlList))
      {
         throw new IllegalArgumentException(
               "Given Access Control List is *NOT* the Jackrabbit implementation. Jackrabbit JCR implementation is required.");
      }
      
      return (JackrabbitAccessControlList) acl;
   }
   
   private class MetaDataMigrationInfo
   {
      private Node metaData;

      private boolean migrated;

      public MetaDataMigrationInfo(Node metaData, boolean migrated)
      {
         this.metaData = metaData;
         this.migrated = migrated;
      }

      public Node getMetaData()
      {
         return metaData;
      }

      public boolean isMigrated()
      {
         return migrated;
      }
   }

}
