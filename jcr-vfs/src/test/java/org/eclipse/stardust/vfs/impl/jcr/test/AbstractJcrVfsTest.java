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
 * $Id: AbstractJcrVfsTest.java 54754 2012-03-21 14:27:29Z nicolas.werlein $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr.test;

import static org.eclipse.stardust.vfs.impl.utils.StringUtils.isEmpty;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.jcr.RepositoryException;

import org.eclipse.stardust.vfs.*;
import org.eclipse.stardust.vfs.impl.jcr.JcrVfsPrincipal;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;
import org.junit.Test;


/**
 * @author rsauer
 * @version $Revision: 54754 $
 */
public abstract class AbstractJcrVfsTest
{

   private IDocumentRepositoryService jcrVfsWithAllPrivileges;

   private IDocumentRepositoryService jcrVfsWithReadPrivileges;

   public IDocumentRepositoryService getJcrVfsWithAllPrivileges()
   {
      return jcrVfsWithAllPrivileges;
   }

   public void setJcrVfsWithAllPrivileges(
         IDocumentRepositoryService jcrVfsWithAllPrivileges)
   {
      this.jcrVfsWithAllPrivileges = jcrVfsWithAllPrivileges;
   }

   public IDocumentRepositoryService getJcrVfsWithReadPrivileges()
   {
      return jcrVfsWithReadPrivileges;
   }

   public void setJcrVfs1(IDocumentRepositoryService jcrVfsWithReadPrivileges)
   {
      this.jcrVfsWithReadPrivileges = jcrVfsWithReadPrivileges;
   }

//   @Before
   public void initializeRepository() throws Exception
   {
      this.jcrVfsWithAllPrivileges.initializeRepository();
   }

   @Test
   public void testCleanup() throws Exception
   {
      try
      {
         jcrVfsWithAllPrivileges.removeFolder("/", true);
      }
      catch (RepositoryOperationFailedException e)
      {
         Throwable cause = e.getCause();
         if ( !(cause instanceof RepositoryException && cause.getMessage().startsWith(
               "Cannot remove the root node")))
         {
            throw e;
         }
      }
      IFolder rootFolder = jcrVfsWithAllPrivileges.getFolder("/");

      assertNotNull(rootFolder);
   }

   @Test
   public void testCreateFolder() throws Exception
   {
      IFolderInfo fNase = VfsUtils.createFolderInfo("nase");
      fNase.setOwner("blasius");

      jcrVfsWithAllPrivileges.createFolder(VfsUtils.REPOSITORY_ROOT, fNase);

      jcrVfsWithAllPrivileges.createFolder("/nase", VfsUtils.createFolderInfo("blase"));
   }

   @Test
   public void testGetFolder() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");

      assertEquals("blasius", fNase.getOwner());
      assertNotNull(fNase.getDateCreated());
      assertNotNull(fNase.getDateLastModified());

      assertEquals(0, fNase.getFileCount());
   }

   @Test
   public void testUpdateFolder() throws Exception
   {
      IFolder fNase1 = jcrVfsWithAllPrivileges.getFolder("/nase");

      assertEquals("blasius", fNase1.getOwner());
      assertNotNull(fNase1.getDateCreated());
      assertNotNull(fNase1.getDateLastModified());

      fNase1.setOwner("Meister Lampe");
      jcrVfsWithAllPrivileges.updateFolder(fNase1);

      IFolder fNase2 = jcrVfsWithAllPrivileges.getFolder("/nase");

      assertEquals("Meister Lampe", fNase2.getOwner());

      assertEquals(fNase1.getDateCreated(), fNase2.getDateCreated());
      assertTrue(fNase1.getDateLastModified().before(fNase2.getDateLastModified()));
   }

   @Test
   public void testCreateFile() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");

      LocalFile file1 = VfsUtils.localFile("src/test/res/LICENSE.txt");

      // in future we might leverage Java activation to derive the content type from the
      // file
      file1.setContentType("text/plain");

      // uploading with a different name
      file1.setName("hase.txt");

      try
      {
         jcrVfsWithAllPrivileges.createFile(fNase, file1, file1.openContentStream(), null);
      }
      finally
      {
         // this will close any previously opened FileInputStream for file1
         file1.closeContentStreams();
      }

      LocalFile file2 = VfsUtils.localFile("src/test/res/EPLv1.pdf");
      file2.setContentType("application/pdf");

      // uploading with a different name
      file2.setName("hase.pdf");

      try
      {
         jcrVfsWithAllPrivileges.createFile(fNase, file2, file2.openContentStream(), null);
      }
      finally
      {
         file2.closeContentStreams();
      }
   }

   @Test
   public void testCreateFileExistingContent() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");

      LocalFile file1 = VfsUtils.localFile("src/test/res/LICENSE.txt");

      // in future we might leverage Java activation to derive the content type from the
      // file
      file1.setContentType("text/plain");

      // uploading with a different name
      file1.setName("copy-of-hase.txt");

      String originalFilePath = "/nase/hase.txt";
      jcrVfsWithAllPrivileges.createFile(fNase, file1, originalFilePath);

      // ensure the same content
      String newFilePath = "/nase/copy-of-hase.txt";
      IFile newFile = jcrVfsWithAllPrivileges.getFile(newFilePath);
      IFile originalFile = jcrVfsWithAllPrivileges.getFile(originalFilePath);
      assertTrue(Arrays.equals(jcrVfsWithAllPrivileges.retrieveFileContent(originalFile),
            jcrVfsWithAllPrivileges.retrieveFileContent(newFile)));
      assertEquals(originalFile.getEncoding(), newFile.getEncoding());
   }

   @Test
   public void testMoveFileKeepMetadata() throws Exception
   {
      String originalFilePath = "/nase/copy-of-hase.txt";
      IFile originalFile = jcrVfsWithAllPrivileges.getFile(originalFilePath);
      String newFilePath = "/nase/moved-hase.txt";
      byte[] originalFileContent = jcrVfsWithAllPrivileges.retrieveFileContent(originalFilePath);

      jcrVfsWithAllPrivileges.moveFile(originalFilePath, newFilePath, null);

      // ensure the same content and the same metadata
      IFile newFile = jcrVfsWithAllPrivileges.getFile(newFilePath);
      assertTrue(Arrays.equals(jcrVfsWithAllPrivileges.retrieveFileContent(originalFile),
            originalFileContent));
      assertEquals(originalFile.getProperties(), newFile.getProperties());

      // ensure that the original file does not exist anymore
      originalFile = jcrVfsWithAllPrivileges.getFile(originalFilePath);
      assertNull(originalFile);
   }

   @Test
   public void testMoveFileNewMetadata() throws Exception
   {
      Map<String, String> newMetadata = CollectionUtils.newMap();
      newMetadata.put("testProperty1", "value1");

      String newFilePath = "/nase/copy-of-hase.txt";
      String originalFilePath = "/nase/moved-hase.txt";
      IFile originalFile = jcrVfsWithAllPrivileges.getFile(originalFilePath);
      byte[] originalFileContent = jcrVfsWithAllPrivileges.retrieveFileContent(originalFilePath);

      // move moved-hase.txt back to copy-of-hase.txt, but with new metadata
      jcrVfsWithAllPrivileges.moveFile(originalFilePath, newFilePath, newMetadata);

      // ensure the same content and the new metadata
      IFile newFile = jcrVfsWithAllPrivileges.getFile(newFilePath);
      assertTrue(Arrays.equals(jcrVfsWithAllPrivileges.retrieveFileContent(originalFile),
            originalFileContent));
      assertEquals(newMetadata, newFile.getProperties());

      // ensure that the original file does not exist anymore
      originalFile = jcrVfsWithAllPrivileges.getFile(originalFilePath);
      assertNull(originalFile);
   }

   @Test
   public void testUpdateFileExistingContent() throws Exception
   {
      String filePathToUpdate = "/nase/copy-of-hase.txt";
      IFile originalFile = jcrVfsWithAllPrivileges.getFile(filePathToUpdate);

      assertNotNull(originalFile);

      assertNull(originalFile.getDescription());
      assertNull(originalFile.getOwner());
      assertNull(originalFile.getProperty("meta-data"));

      originalFile.setDescription("Please ask for details.");
      originalFile.setOwner("me");
      originalFile.setProperty("meta-data", "some value");

      String contentOriginFilePath = "/nase/hase.pdf";
      jcrVfsWithAllPrivileges.updateFile(originalFile, contentOriginFilePath, false,
            false);

      IFile updatedFile = jcrVfsWithAllPrivileges.getFile(filePathToUpdate);
      assertNotNull(updatedFile);

      assertEquals("Please ask for details.", updatedFile.getDescription());
      assertEquals("me", updatedFile.getOwner());
      assertEquals("some value", updatedFile.getProperty("meta-data"));

      // ensure that the content is copied
      assertTrue(Arrays.equals(
            jcrVfsWithAllPrivileges.retrieveFileContent(contentOriginFilePath),
            jcrVfsWithAllPrivileges.retrieveFileContent(filePathToUpdate)));
      assertEquals(originalFile.getEncoding(), updatedFile.getEncoding());
   }

   @Test
   public void testGetFilledFolder() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");

      assertEquals(3, fNase.getFileCount());

      for (IFile file : fNase.getFiles())
      {
         System.out.println(file.getName() + " -> " + file.getSize() + " bytes");
      }
   }

   @Test
   public void testGetFiles() throws Exception
   {
      assertEquals(1, jcrVfsWithAllPrivileges.getFiles(Arrays.asList("/nase/hase.txt"))
            .size());
      assertEquals(2, jcrVfsWithAllPrivileges.getFiles(
            Arrays.asList("/nase/hase.txt", "/nase/hase.pdf")).size());
   }

   public void testFindFilesByName() throws Exception
   {
      assertEquals(1, jcrVfsWithAllPrivileges.findFilesByName("hase.txt").size());
      assertEquals(0, jcrVfsWithAllPrivileges.findFilesByName("does-not-exist.txt")
            .size());
      assertEquals(2, jcrVfsWithAllPrivileges.findFilesByName("hase.%").size());
      assertEquals(1, jcrVfsWithAllPrivileges.findFilesByName("%.pdf").size());
   }

   @Test
   public void testFindFiles() throws Exception
   {
      assertEquals(1, jcrVfsWithAllPrivileges.findFiles("/jcr:root/nase/hase.txt").size());
      assertEquals(3, jcrVfsWithAllPrivileges.findFiles("/jcr:root//element(*, nt:file)")
            .size());
   }

   @Test
   public void testRetrieveFileContent() throws Exception
   {
      IFile fHase = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");

      assertNotNull(fHase);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      jcrVfsWithAllPrivileges.retrieveFileContent(fHase, baos);

      LocalFile localFile = VfsUtils.localFile("src/test/res/LICENSE.txt");

      assertEquals(localFile.getSize(), baos.size());

      // compare content
      InputStream is = localFile.openContentStream();
      try
      {
         byte[] serverFile = baos.toByteArray();

         for (int i = 0; i < serverFile.length; i++ )
         {
            int lhs = is.read();
            assertEquals("File content differs.", (byte) lhs, serverFile[i]);
         }
      }
      finally
      {
         is.close();
      }
   }

   @Test
   public void testGetFolders() throws Exception
   {
      assertEquals(1, jcrVfsWithAllPrivileges.getFolders(Arrays.asList("/nase"),
            IFolder.LOD_LIST_MEMBERS).size());
      assertEquals(2, jcrVfsWithAllPrivileges.getFolders(
            Arrays.asList("/nase", "/nase/blase"), IFolder.LOD_NO_MEMBERS).size());
   }

   @Test
   public void testFindFoldersByName() throws Exception
   {
      assertEquals(1, jcrVfsWithAllPrivileges.findFoldersByName("nase",
            IFolder.LOD_LIST_MEMBERS).size());
      assertEquals(1, jcrVfsWithAllPrivileges.findFoldersByName("blase",
            IFolder.LOD_NO_MEMBERS).size());
      assertEquals(2, jcrVfsWithAllPrivileges.findFoldersByName("%ase",
            IFolder.LOD_NO_MEMBERS).size());
   }

   @Test
   public void testRenameFiles() throws Exception
   {
      LocalFile originalFile = VfsUtils.localFile("src/test/res/LICENSE.txt");
      originalFile.setContentType("text/plain");
      originalFile.setName("LICENSE-original.txt");

      try
      {
         jcrVfsWithAllPrivileges.createFile("/nase", originalFile,
               originalFile.openContentStream(), null);
      }
      finally
      {
         // this will close any previously opened FileInputStream for file1
         originalFile.closeContentStreams();
      }

      assertEquals(1, jcrVfsWithAllPrivileges.findFilesByName("LICENSE-original.txt")
            .size());

      IFile v0 = jcrVfsWithAllPrivileges.getFile("/nase/LICENSE-original.txt");
      assertNotNull(v0);
      assertEquals("LICENSE-original.txt", v0.getName());

      v0.setName("LICENSE-renamed.txt");
      jcrVfsWithAllPrivileges.updateFile(v0, false, false);

      IFile v1 = jcrVfsWithAllPrivileges.getFile("/nase/LICENSE-renamed.txt");
      assertNotNull(v1);
      assertEquals("LICENSE-renamed.txt", v1.getName());

      assertFalse(jcrVfsWithAllPrivileges.findFilesByName("LICENSE-renamed.txt")
            .isEmpty());
      assertTrue(jcrVfsWithAllPrivileges.findFilesByName("LICENSE-original.txt")
            .isEmpty());
   }

   @Test
   public void testRenameFolders() throws Exception
   {
      jcrVfsWithAllPrivileges.createFolder(VfsUtils.REPOSITORY_ROOT,
            VfsUtils.createFolderInfo("popase"));

      assertEquals(1, jcrVfsWithAllPrivileges.findFoldersByName("popase",
            IFolder.LOD_NO_MEMBERS).size());

      IFolder v0Popase = jcrVfsWithAllPrivileges.getFolder("/popase",
            IFolder.LOD_NO_MEMBERS);
      assertNotNull(v0Popase);
      assertEquals("popase", v0Popase.getName());

      LocalFile originalFile = VfsUtils.localFile("src/test/res/LICENSE.txt");
      try
      {
         originalFile.setContentType("text/plain");
         originalFile.setName("LICENSE-popase.txt");
         jcrVfsWithAllPrivileges.createFile("/popase", originalFile,
               originalFile.openContentStream(), null);
      }
      finally
      {
         // this will close any previously opened FileInputStream for file1
         originalFile.closeContentStreams();
      }

      assertEquals(1, jcrVfsWithAllPrivileges.findFilesByName("LICENSE-popase.txt")
            .size());

      assertNotNull(jcrVfsWithAllPrivileges.getFile("/popase/LICENSE-popase.txt"));

      v0Popase.setName("menase");
      jcrVfsWithAllPrivileges.updateFolder(v0Popase);

      IFolder v1Popase = jcrVfsWithAllPrivileges.getFolder("/menase",
            IFolder.LOD_LIST_MEMBERS);
      assertNotNull(v1Popase);
      assertEquals("menase", v1Popase.getName());
      assertEquals(1, v1Popase.getFileCount());
      assertEquals("LICENSE-popase.txt", v1Popase.getFile(0).getName());

      assertFalse(jcrVfsWithAllPrivileges.findFoldersByName("menase",
            IFolder.LOD_LIST_MEMBERS).isEmpty());
      assertTrue(jcrVfsWithAllPrivileges.findFoldersByName("popase",
            IFolder.LOD_LIST_MEMBERS).isEmpty());

      assertNotNull(jcrVfsWithAllPrivileges.getFile("/menase/LICENSE-popase.txt"));

      jcrVfsWithAllPrivileges.removeFolder(v1Popase, true);
   }

   @Test
   public void testFindFolders() throws Exception
   {
      assertEquals(1, jcrVfsWithAllPrivileges.findFolders("/jcr:root/nase",
            IFolder.LOD_LIST_MEMBERS).size());
      assertEquals(1, jcrVfsWithAllPrivileges.findFolders("/jcr:root//blase",
            IFolder.LOD_NO_MEMBERS).size());
      assertEquals(2, jcrVfsWithAllPrivileges.findFolders(
            "/jcr:root//element(*, nt:folder)", IFolder.LOD_NO_MEMBERS).size());
   }

   @Test
   public void testFileVersioning() throws Exception
   {
      LocalFile originalFile = VfsUtils.localFile("src/test/res/LICENSE.txt");
      originalFile.setContentType("text/plain");
      originalFile.setName("LICENSE.txt");
      originalFile.setProperties(Collections.singletonMap("meta", "meta"));
      try
      {
         jcrVfsWithAllPrivileges.createFile("/nase", originalFile,
               originalFile.openContentStream(), null);
      }
      finally
      {
         // this will close any previously opened FileInputStream for file1
         originalFile.closeContentStreams();
      }

      assertNotNull(jcrVfsWithAllPrivileges.getFile("/nase/LICENSE.txt"));

      IFile v0 = jcrVfsWithAllPrivileges.getFile("/nase/LICENSE.txt");

      assertEquals(VfsUtils.VERSION_UNVERSIONED, v0.getRevisionId());

      IFile v1 = jcrVfsWithAllPrivileges.createFileVersion(v0.getPath(), "Nasen-Version",
            false);
      assertEquals(v0.getId(), v1.getId());
      assertEquals(v0.getName(), v1.getName());
      assertEquals(v0.getPath(), v1.getPath());
      assertNotSame(VfsUtils.VERSION_UNVERSIONED, v1.getRevisionId());
      assertTrue(v1.getVersionLabels().contains("Nasen-Version"));

      IFile v2 = jcrVfsWithAllPrivileges.createFileVersion(v1.getPath(), "Nasen-Version",
            true);
      assertEquals(v0.getId(), v2.getId());
      assertEquals(v0.getName(), v2.getName());
      assertEquals(v0.getPath(), v2.getPath());
      assertNotSame(VfsUtils.VERSION_UNVERSIONED, v2.getRevisionId());
      assertTrue(v2.getVersionLabels().contains("Nasen-Version"));

      for (IFile revision : new IFile[] {v0, v1, v2})
      {
         System.out.println("ID: " + revision.getId() + ", revision-Name: "
               + revision.getRevisionName() + " (rev-ID: " + revision.getRevisionId()
               + ")");
      }

      IFile v1ById = jcrVfsWithAllPrivileges.getFile(v1.getRevisionId());
      assertNotNull(v1ById);
      assertEquals(v1.getId(), v1ById.getId());
      assertEquals(v1.getRevisionId(), v1ById.getRevisionId());
      assertEquals(v1.getName(), v1ById.getName());
      assertEquals(v1.getPath(), v1ById.getPath());
      assertTrue( !v1ById.getVersionLabels().contains("Nasen-Version"));

      IFile v2ById = jcrVfsWithAllPrivileges.getFile(v2.getRevisionId());
      assertNotNull(v2ById);
      assertEquals(v2.getId(), v2ById.getId());
      assertEquals(v2.getRevisionId(), v2ById.getRevisionId());
      assertEquals(v2.getName(), v2ById.getName());
      assertEquals(v2.getPath(), v2ById.getPath());
      assertTrue(v2ById.getVersionLabels().contains("Nasen-Version"));

      // test getting all revisions and verify them
      List< ? extends IFile> fileVersions = jcrVfsWithAllPrivileges.getFileVersions(v0.getId());
      assertEquals(fileVersions.size(), 2);

      IFile vv1 = fileVersions.get(0);
      assertEquals(v1.getId(), vv1.getId());
      assertEquals(v1.getRevisionId(), vv1.getRevisionId());
      assertEquals(v1.getName(), vv1.getName());
      assertEquals(v1.getPath(), vv1.getPath());

      IFile vv2 = fileVersions.get(1);
      assertEquals(v2.getId(), vv2.getId());
      assertEquals(v2.getRevisionId(), vv2.getRevisionId());
      assertEquals(v2.getName(), vv2.getName());
      assertEquals(v2.getPath(), vv2.getPath());

      // getting revision should also be possible using any revision of the document
      fileVersions = jcrVfsWithAllPrivileges.getFileVersions(v2.getId());
      assertEquals(fileVersions.size(), 2);

      IFile vvv1 = fileVersions.get(0);
      assertEquals(v1.getId(), vvv1.getId());
      assertEquals(v1.getRevisionId(), vvv1.getRevisionId());
      assertEquals(v1.getName(), vvv1.getName());
      assertEquals(v1.getPath(), vvv1.getPath());

      IFile vvv2 = fileVersions.get(1);
      assertEquals(v2.getId(), vvv2.getId());
      assertEquals(v2.getRevisionId(), vvv2.getRevisionId());
      assertEquals(v2.getName(), vvv2.getName());
      assertEquals(v2.getPath(), vvv2.getPath());

      // remove file
      assertNotNull(jcrVfsWithAllPrivileges.getFolder("/nase").findFile(v0.getName()));
      jcrVfsWithAllPrivileges.removeFile(v0.getPath());

      assertNull(jcrVfsWithAllPrivileges.getFile(v0.getId()));
      assertTrue(jcrVfsWithAllPrivileges.findFilesByName(v0.getName()).isEmpty());

      IFile v1FromAttic = jcrVfsWithAllPrivileges.getFile(v1.getRevisionId());
      assertNotNull(v1FromAttic);
      assertEquals(v1.getId(), v1FromAttic.getId());
      assertEquals(v1.getRevisionId(), v1FromAttic.getRevisionId());
      assertEquals(v1.getName(), v1FromAttic.getName());
      assertTrue(isEmpty(v1FromAttic.getPath()));

      IFile v2FromAttic = jcrVfsWithAllPrivileges.getFile(v2.getRevisionId());
      assertNotNull(v2FromAttic);
      assertEquals(v2.getId(), v2FromAttic.getId());
      assertEquals(v2.getRevisionId(), v2FromAttic.getRevisionId());
      assertEquals(v2.getName(), v2FromAttic.getName());
      assertTrue(isEmpty(v2FromAttic.getPath()));

      assertNull(jcrVfsWithAllPrivileges.getFolder("/nase").findFile(v0.getName()));
   }

   @Test
   public void testFileUpdateVersion() throws Exception
   {
      final String name = "LICENSE_UPDATED.txt";
      jcrVfsWithAllPrivileges.removeFile("/nase/"+name);
      LocalFile originalFile = VfsUtils.localFile("src/test/res/LICENSE.txt");
      originalFile.setContentType("text/plain");
      originalFile.setName(name);
      originalFile.setProperties(Collections.singletonMap("meta", "meta"));
      try
      {
         jcrVfsWithAllPrivileges.createFile("/nase", originalFile,
               originalFile.openContentStream(), null);
      }
      finally
      {
         // this will close any previously opened FileInputStream for file1
         originalFile.closeContentStreams();
      }

      assertNotNull(jcrVfsWithAllPrivileges.getFile("/nase/"+name));

      IFile v0 = jcrVfsWithAllPrivileges.getFile("/nase/"+name);

      assertEquals(VfsUtils.VERSION_UNVERSIONED, v0.getRevisionId());

      IFile v1 = jcrVfsWithAllPrivileges.createFileVersion(v0.getPath(), "Nasen-Version",
            false);
      assertEquals(v0.getId(), v1.getId());
      assertEquals(v0.getName(), v1.getName());
      assertEquals(v0.getPath(), v1.getPath());
      assertNotSame(VfsUtils.VERSION_UNVERSIONED, v1.getRevisionId());
      assertTrue(v1.getVersionLabels().contains("Nasen-Version"));


      // update file
      v1.setOwner("new owner");
      v1.setProperties(Collections.singletonMap("new", "new"));
//      jcrVfsWithAllPrivileges.retrieveFileContent(v1);
      v1 = jcrVfsWithAllPrivileges.updateFile(v1,"new content".getBytes(),null, false, false);
      v1.setOwner("new owner");
      v1.setProperties(Collections.singletonMap("new2", "new2"));

//      jcrVfsWithAllPrivileges.retrieveFileContent(v1);
//      v1 = jcrVfsWithAllPrivileges.updateFile(v1,"REALLY NEW CONTENT new content".getBytes(),null, true, false);


      IFile v2 = jcrVfsWithAllPrivileges.createFileVersion(v1.getPath(), "Nasen-Version",
            true);
      assertEquals(v0.getId(), v2.getId());
      assertEquals(v0.getName(), v2.getName());
      assertEquals(v0.getPath(), v2.getPath());
      assertNotSame(VfsUtils.VERSION_UNVERSIONED, v2.getRevisionId());
      assertTrue(v2.getVersionLabels().contains("Nasen-Version"));

      for (IFile revision : new IFile[] {v0, v1, v2})
      {
         System.out.println("ID: " + revision.getId() + ", revision-Name: "
               + revision.getRevisionName() + " (rev-ID: " + revision.getRevisionId()
               + ")");
      }

      IFile v1ById = jcrVfsWithAllPrivileges.getFile(v1.getRevisionId());
      assertNotNull(v1ById);
      assertEquals(v1.getId(), v1ById.getId());
      assertEquals(v1.getRevisionId(), v1ById.getRevisionId());
      assertEquals(v1.getName(), v1ById.getName());
      assertEquals(v1.getPath(), v1ById.getPath());
      assertTrue( !v1ById.getVersionLabels().contains("Nasen-Version"));

      IFile v2ById = jcrVfsWithAllPrivileges.getFile(v2.getRevisionId());
      assertNotNull(v2ById);
      assertEquals(v2.getId(), v2ById.getId());
      assertEquals(v2.getRevisionId(), v2ById.getRevisionId());
      assertEquals(v2.getName(), v2ById.getName());
      assertEquals(v2.getPath(), v2ById.getPath());
      assertTrue(v2ById.getVersionLabels().contains("Nasen-Version"));

      // test getting all revisions and verify them
      List< ? extends IFile> fileVersions = jcrVfsWithAllPrivileges.getFileVersions(v0.getId());
      assertEquals(2, fileVersions.size());

      IFile vv1 = fileVersions.get(0);
      assertEquals(v1.getId(), vv1.getId());
      assertEquals(v1.getRevisionId(), vv1.getRevisionId());
      assertEquals(v1.getName(), vv1.getName());
      assertEquals(v1.getPath(), vv1.getPath());

      IFile vv2 = fileVersions.get(1);
      assertEquals(v2.getId(), vv2.getId());
      assertEquals(v2.getRevisionId(), vv2.getRevisionId());
      assertEquals(v2.getName(), vv2.getName());
      assertEquals(v2.getPath(), vv2.getPath());

      // getting revision should also be possible using any revision of the document
      fileVersions = jcrVfsWithAllPrivileges.getFileVersions(v2.getId());
      assertEquals(fileVersions.size(), 2);

      IFile vvv1 = fileVersions.get(0);
      assertEquals(v1.getId(), vvv1.getId());
      assertEquals(v1.getRevisionId(), vvv1.getRevisionId());
      assertEquals(v1.getName(), vvv1.getName());
      assertEquals(v1.getPath(), vvv1.getPath());

      IFile vvv2 = fileVersions.get(1);
      assertEquals(v2.getId(), vvv2.getId());
      assertEquals(v2.getRevisionId(), vvv2.getRevisionId());
      assertEquals(v2.getName(), vvv2.getName());
      assertEquals(v2.getPath(), vvv2.getPath());

      // remove file
      assertNotNull(jcrVfsWithAllPrivileges.getFolder("/nase").findFile(v0.getName()));
      jcrVfsWithAllPrivileges.removeFile(v0.getPath());

      assertNull(jcrVfsWithAllPrivileges.getFile(v0.getId()));
      assertTrue(jcrVfsWithAllPrivileges.findFilesByName(v0.getName()).isEmpty());

      IFile v1FromAttic = jcrVfsWithAllPrivileges.getFile(v1.getRevisionId());
      assertNotNull(v1FromAttic);
      assertEquals(v1.getId(), v1FromAttic.getId());
      assertEquals(v1.getRevisionId(), v1FromAttic.getRevisionId());
      assertEquals(v1.getName(), v1FromAttic.getName());
      assertTrue(isEmpty(v1FromAttic.getPath()));

      IFile v2FromAttic = jcrVfsWithAllPrivileges.getFile(v2.getRevisionId());
      assertNotNull(v2FromAttic);
      assertEquals(v2.getId(), v2FromAttic.getId());
      assertEquals(v2.getRevisionId(), v2FromAttic.getRevisionId());
      assertEquals(v2.getName(), v2FromAttic.getName());
      assertTrue(isEmpty(v2FromAttic.getPath()));

      assertNull(jcrVfsWithAllPrivileges.getFolder("/nase").findFile(v0.getName()));
   }

   public void testLockFile() throws Exception
   {
      jcrVfsWithAllPrivileges.lockFile("/nase/hase.txt");

      // TODO verify file is locked
   }

   public void testUnlockFile() throws Exception
   {
      jcrVfsWithAllPrivileges.unlockFile("/nase/hase.txt");

      // TODO verify file is not locked
   }

   @Test
   public void testUpdateFile() throws Exception
   {
      IFile fHase = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");

      assertNotNull(fHase);

      assertNull(fHase.getDescription());
      assertNull(fHase.getOwner());
      assertNull(fHase.getProperty("meta-data"));

      fHase.setDescription("Please ask for details.");
      fHase.setOwner("me");
      fHase.setProperty("meta-data", "some value");

      jcrVfsWithAllPrivileges.updateFile(fHase, false, false);

      fHase = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");
      assertNotNull(fHase);

      assertEquals("Please ask for details.", fHase.getDescription());
      assertEquals("me", fHase.getOwner());
      assertEquals("some value", fHase.getProperty("meta-data"));

      fHase.setDescription(null);
      fHase.setOwner(null);
      fHase.setProperty("meta-data", null);

      jcrVfsWithAllPrivileges.updateFile(fHase, false, false);

      fHase = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");
      assertNotNull(fHase);

      assertNull(fHase.getDescription());
      assertNull(fHase.getOwner());
      assertNull(fHase.getProperty("meta-data"));
   }

   @Test
   public void testUpdateMetadata() throws Exception
   {
      IFile fHaseInitial = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");
      IFile fHase = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");

      assertNotNull(fHase);

      assertNull(fHase.getProperty("prop1"));
      assertNull(fHase.getProperty("prop2"));

      // Sr. No. 1
      fHase.setProperty("prp1", "val1");
      fHase.setProperty("prp2", "val2");

      fHase = jcrVfsWithAllPrivileges.updateFile(fHase, false, false);

      assertEquals("val1", fHase.getProperty("prp1"));
      assertEquals("val2", fHase.getProperty("prp2"));

      // Sr. No. 3 (wrt. prp1)
      fHase.setProperty("prp1", null);

      fHase = jcrVfsWithAllPrivileges.updateFile(fHase, false, false);

      assertNull(fHase.getProperty("prp1"));
      assertEquals("val2", fHase.getProperty("prp2"));

      // Sr. No. 2 (wrt. prp2)
      fHase = jcrVfsWithAllPrivileges.updateFile(fHaseInitial, false, false);

      assertNull(fHase.getProperty("prp1"));
      assertNull(fHase.getProperty("prp2"));

      // Sr. No. 4 (wrt. prp1)
      fHase.setProperty("prp1", null);

      fHase = jcrVfsWithAllPrivileges.updateFile(fHase, false, false);

      assertNull(fHase.getProperty("prp1"));
      assertNull(fHase.getProperty("prp2"));
   }

   @Test
   public void testMetadata() throws Exception
   {
      SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");

      IFileInfo fiMetadata = VfsUtils.createFileInfo("various-metadata.txt");

      fiMetadata.setProperty("p1_string", "p1_string_value");
      fiMetadata.setProperty("p2_boolean", Boolean.TRUE);
      fiMetadata.setProperty("p3_int", new Integer(3));
      fiMetadata.setProperty("p4_long", new Long(4));
      fiMetadata.setProperty("p5_short", new Short((short) 5));
      fiMetadata.setProperty("p6_byte", new Byte((byte) 6));
      fiMetadata.setProperty("p7_double", new Double(7.77));
      fiMetadata.setProperty("p8_float", new Float(8.88));
      fiMetadata.setProperty("p9_dateTime", df.parse("01.02.2003 01:02:03:004"));
      fiMetadata.setProperty("p10_date", df.parse("01.02.2004 01:02:03:0004"));
      fiMetadata.setProperty("p11_time", df.parse("01.02.2005 01:02:03:0004"));

      HashMap<String, Serializable> complexProperty1 = new HashMap<String, Serializable>();
      complexProperty1.put("pp1", "pp1_string_value");
      complexProperty1.put("pp2", Boolean.FALSE);
      fiMetadata.setProperty("p12_complex1", complexProperty1);

      HashMap<String, Serializable> complexProperty2 = new HashMap<String, Serializable>();
      complexProperty2.put("pp1", new Long(1));
      HashMap<String, Serializable> complexSubProperty1 = new HashMap<String, Serializable>();
      complexSubProperty1.put("ppp1", new Long(11));
      complexProperty2.put("pp2", complexSubProperty1);
      fiMetadata.setProperty("p13_complex2", complexProperty2);

      ArrayList<Long> longlistProperty = new ArrayList<Long>();
      longlistProperty.add(new Long(11));
      longlistProperty.add(new Long(22));
      fiMetadata.setProperty("p14_longlist", longlistProperty);

      ArrayList<Date> dateListProperty = new ArrayList<Date>();
      dateListProperty.add(df.parse("01.02.2004 01:02:03:0004"));
      dateListProperty.add(df.parse("01.02.2005 01:02:03:0004"));
      fiMetadata.setProperty("p15_datetimelist", dateListProperty);

      ArrayList<HashMap<String, Serializable>> complexListProperty = new ArrayList<HashMap<String, Serializable>>();
      HashMap<String, Serializable> listElement1 = new HashMap<String, Serializable>();
      listElement1.put("ppp1", new Long(11));
      HashMap<String, Serializable> listElement2 = new HashMap<String, Serializable>();
      listElement2.put("ppp1", new Long(22));
      complexListProperty.add(listElement1);
      complexListProperty.add(listElement2);
      fiMetadata.setProperty("p16_complex1", complexListProperty);

      IFile fMetadata = jcrVfsWithAllPrivileges.createFile("/nase", fiMetadata,
            VfsUtils.NO_CONTENT, null);

      assertEquals("various-metadata.txt", fMetadata.getName());
      assertEquals(16, fMetadata.getProperties().size());

      assertEquals("p1_string_value", fMetadata.getProperties().get("p1_string"));
      assertEquals("p1_string_value", fMetadata.getProperty("p1_string"));
      assertEquals(Boolean.TRUE, fMetadata.getProperty("p2_boolean"));
      assertEquals(3, ((Number) fMetadata.getProperty("p3_int")).intValue());
      assertEquals(4L, ((Number) fMetadata.getProperty("p4_long")).longValue());
      assertEquals(5, ((Number) fMetadata.getProperty("p5_short")).shortValue());
      assertEquals(6, ((Number) fMetadata.getProperty("p6_byte")).byteValue());
      assertEquals(7.77, ((Number) fMetadata.getProperty("p7_double")).doubleValue(),
            0.001);
      assertEquals(8.88F, ((Number) fMetadata.getProperty("p8_float")).floatValue(),
            0.001F);
      assertEquals(df.parse("01.02.2003 01:02:03:0004"),
            fMetadata.getProperty("p9_dateTime"));
      assertEquals(df.parse("01.02.2004 01:02:03:0004"),
            fMetadata.getProperty("p10_date"));
      assertEquals(df.parse("01.02.2005 01:02:03:0004"),
            fMetadata.getProperty("p11_time"));
      assertEquals(complexProperty1, fMetadata.getProperty("p12_complex1"));
      assertEquals(complexProperty2, fMetadata.getProperty("p13_complex2"));
      assertEquals(longlistProperty, fMetadata.getProperty("p14_longlist"));
      assertEquals(dateListProperty, fMetadata.getProperty("p15_datetimelist"));
      assertEquals(complexListProperty, fMetadata.getProperty("p16_complex1"));

      assertEquals(null, fMetadata.getProperty("property_NOT_EXISTS"));
   }

   @Test
   public void testAnnotations() throws Exception
   {
      IFileInfo fiMetadata = VfsUtils.createFileInfo("various-annotations.txt");

      Map<String, Serializable> annotations = new HashMap<String, Serializable>();

      annotations.put("p1_string", "p1_string_value");
      annotations.put("p1_long", Long.valueOf(1));
      ArrayList<Integer> intList = new ArrayList<Integer>(Arrays.asList(
            Integer.valueOf(1), Integer.valueOf(2)));
      annotations.put("p1_list", intList);

      Map<String,Serializable> map = new HashMap<String, Serializable>();
      map.put("a", "b");
      annotations.put("map", (Serializable) map);

      fiMetadata.setAnnotations(annotations);

      IFile fMetadata = jcrVfsWithAllPrivileges.createFile("/nase", fiMetadata,
            VfsUtils.NO_CONTENT, null);

      assertEquals("various-annotations.txt", fMetadata.getName());
      assertEquals(4, fMetadata.getAnnotations().size());
      assertEquals(0, fMetadata.getProperties().size());

      assertEquals("p1_string_value", fMetadata.getAnnotations().get("p1_string"));
      assertEquals(Long.valueOf(1), fMetadata.getAnnotations().get("p1_long"));
      assertEquals(intList.size(), ((List<Integer>)fMetadata.getAnnotations().get("p1_list")).size());
      assertEquals(map.size(), ((Map)fMetadata.getAnnotations().get("map")).size());

      Map<String, Serializable> emptyAnnotations = new HashMap<String, Serializable>();
      for (String key : fMetadata.getAnnotations().keySet())
      {
         emptyAnnotations.put(key, null);
      }
      fMetadata.setAnnotations(emptyAnnotations);

      fMetadata = jcrVfsWithAllPrivileges.updateFile(fMetadata, false, false);

      assertEquals(0, fMetadata.getAnnotations().size());
      assertEquals(0, fMetadata.getProperties().size());

      IFile fGetFile = jcrVfsWithAllPrivileges.getFile(fMetadata.getId());

      assertEquals(0, fGetFile.getAnnotations().size());
      assertEquals(0, fGetFile.getProperties().size());

      fMetadata.setAnnotations(Collections.EMPTY_MAP);

      fMetadata = jcrVfsWithAllPrivileges.updateFile(fMetadata, false, false);

      assertEquals(0, fMetadata.getAnnotations().size());
      assertEquals(0, fMetadata.getProperties().size());

      fGetFile = jcrVfsWithAllPrivileges.getFile(fMetadata.getId());

      assertEquals(0, fGetFile.getAnnotations().size());
      assertEquals(0, fGetFile.getProperties().size());

   }

   @Test
   public void testPropertiesTypeAttributes() throws Exception
   {
      IFileInfo inputDoc = VfsUtils.createFileInfo("documentType.txt");

      inputDoc.setPropertiesTypeId("Composite_1");
      inputDoc.setPropertiesTypeSchemaLocation("/docTypeSchemas/xsd");

      IFile outputDoc = jcrVfsWithAllPrivileges.createFile("/nase", inputDoc,
            VfsUtils.NO_CONTENT, null);

      assertEquals("documentType.txt", outputDoc.getName());
      assertEquals("Composite_1", outputDoc.getPropertiesTypeId());
      assertEquals("/docTypeSchemas/xsd", outputDoc.getPropertiesTypeSchemaLocation());
   }

   @Test
   public void testUpdateFileVersion() throws Exception
   {
      IFile fHasev0 = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");

      assertNotNull(fHasev0);

      assertNull(fHasev0.getProperty("meta-data"));

      fHasev0.setProperty("meta-data", "some value");

      IFile fHasev1 = jcrVfsWithAllPrivileges.updateFile(fHasev0, true, false);

      IFile fHasev2 = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");

      assertNotNull(fHasev2);

      assertEquals("some value", fHasev2.getProperty("meta-data"));

      fHasev2.setProperty("meta-data", null);

      fHasev2 = jcrVfsWithAllPrivileges.updateFile(fHasev2, true, false);

      IFile fHasev3 = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");

      assertNotNull(fHasev3);

      assertNull(fHasev3.getProperty("meta-data"));

      IFile v1Copy = jcrVfsWithAllPrivileges.getFile(fHasev1.getRevisionId());
      assertNotNull(v1Copy);
      assertEquals("some value", v1Copy.getProperty("meta-data"));

      IFile v3Copy = jcrVfsWithAllPrivileges.getFile(fHasev2.getRevisionId());
      assertNotNull(v3Copy);
      assertNull(v3Copy.getProperty("meta-data"));

      System.out.println("ID (v0): " + fHasev1.getId() + ", v"
            + fHasev0.getRevisionName() + " (" + fHasev0.getRevisionId() + ")");
      System.out.println("ID (v1): " + fHasev1.getId() + ", v"
            + fHasev1.getRevisionName() + " (" + fHasev1.getRevisionId() + ")");
      System.out.println("ID (v2): " + fHasev2.getId() + ", v"
            + fHasev2.getRevisionName() + " (" + fHasev2.getRevisionId() + ")");
      System.out.println("ID (v3): " + fHasev3.getId() + ", v"
            + fHasev3.getRevisionName() + " (" + fHasev3.getRevisionId() + ")");
   }

   @Test
   public void testUpdateFileContent() throws Exception
   {
      IFile fHase = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");

      assertNotNull(fHase);

      byte[] fakeContent = {1, 2, 3, 9, 8, 5, 6};
      jcrVfsWithAllPrivileges.updateFile(fHase, fakeContent, null, false, false);

      fHase = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");

      assertNotNull(fHase);

      byte[] updatedContent = jcrVfsWithAllPrivileges.retrieveFileContent(fHase);
      for (int i = 0; i < fakeContent.length; i++ )
      {
         assertEquals(fakeContent[i], updatedContent[i]);
      }

      LocalFile originalFile = VfsUtils.localFile("src/test/res/LICENSE.txt");
      try
      {
         jcrVfsWithAllPrivileges.updateFile(fHase, originalFile.openContentStream(),
               null, false, false);
      }
      finally
      {
         originalFile.closeContentStreams();
      }

      fHase = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");

      assertNotNull(fHase);

      assertEquals(originalFile.getSize(), fHase.getSize());
   }

   @Test
   public void testFolderPrivileges() throws Exception
   {
      // folder
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");
      Set<IPrivilege> folderPrivileges = jcrVfsWithAllPrivileges.getPrivileges(fNase.getId());
      assertEquals(1, folderPrivileges.size());

      assertTrue(folderPrivileges.contains(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.ALL_PRIVILEGE)));
   }

   @Test
   public void testFilePrivileges() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");
      IFile file = fNase.findFile("hase.txt");
      Set<IPrivilege> filePrivileges = jcrVfsWithAllPrivileges.getPrivileges(file.getId());
      assertEquals(1, filePrivileges.size());

      assertTrue(filePrivileges.contains(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.ALL_PRIVILEGE)));
   }

   @Test
   public void testGetFolderDefaultEffectivePolicies() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");

      Set<IAccessControlPolicy> effectivePolicies = jcrVfsWithAllPrivileges.getEffectivePolicies(fNase.getId());
      assertEquals(1, effectivePolicies.size());

      Set<IAccessControlEntry> aces = effectivePolicies.iterator()
            .next()
            .getAccessControlEntries();
      assertEquals(2, aces.size());

      Set<IPrivilege> privileges; // = getPrivilegesForPrincipal(aces,
      // "{ipp.role}Administrator");
      // assertEquals(1, privileges.size());
      // assertEquals(Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.ALL_PRIVILEGE)),
      // privileges);
      //
      privileges = getPrivilegesForPrincipal(aces, "administrators");
      assertEquals(1, privileges.size());
      assertEquals(
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.ALL_PRIVILEGE)),
            privileges);

      privileges = getPrivilegesForPrincipal(aces, "everyone");
      assertEquals(1, privileges.size());
      assertEquals(
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.READ_PRIVILEGE)),
            privileges);
   }

   @Test
   public void testGetFolderDefaultPolicies() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");

      Set<IAccessControlPolicy> policies = jcrVfsWithAllPrivileges.getPolicies(fNase.getId());
      assertEquals(0, policies.size());
   }

   @Test
   public void testAddFolderPolicy() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");

      Set<IAccessControlPolicy> policies = jcrVfsWithAllPrivileges.getApplicablePolicies(fNase.getId());
      assertEquals(1, policies.size());

      IAccessControlPolicy policy = policies.iterator().next();
      policy.addAccessControlEntry(
            new JcrVfsPrincipal("user1"),
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_PRIVILEGE)));

      jcrVfsWithAllPrivileges.setPolicy(fNase.getPath(), policy);

      policies = jcrVfsWithAllPrivileges.getPolicies(fNase.getPath());
      assertEquals(1, policies.size());
      Set<IAccessControlEntry> aces = policies.iterator()
            .next()
            .getAccessControlEntries();
      assertEquals(1, aces.size());

      Set<IPrivilege> privileges = getPrivilegesForPrincipal(aces, "user1");
      assertEquals(1, privileges.size());
      assertEquals(
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_PRIVILEGE)),
            privileges);
   }

   @Test
   public void testChangeFolderPolicy() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");

      Set<IAccessControlPolicy> policies = jcrVfsWithAllPrivileges.getPolicies(fNase.getId());
      assertEquals(1, policies.size());

      IAccessControlPolicy policy = policies.iterator().next();
      policy.addAccessControlEntry(
            new JcrVfsPrincipal("user1"),
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_CHILDREN_PRIVILEGE)));

      jcrVfsWithAllPrivileges.setPolicy(fNase.getPath(), policy);

      policies = jcrVfsWithAllPrivileges.getPolicies(fNase.getPath());
      assertEquals(1, policies.size());
      Set<IAccessControlEntry> aces = policies.iterator()
            .next()
            .getAccessControlEntries();
      assertEquals(1, aces.size());

      Set<IPrivilege> privileges = getPrivilegesForPrincipal(aces, "user1");
      assertEquals(2, privileges.size());
      Set<IPrivilege> expected = CollectionUtils.newSet();
      expected.add(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_CHILDREN_PRIVILEGE));
      expected.add(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_PRIVILEGE));
      assertEquals(expected, privileges);
   }

   @Test
   public void testFileInheritedPolicy() throws Exception
   {
      IFile fNase = jcrVfsWithAllPrivileges.getFile("/nase/hase.txt");

      // no own policies
      Set<IAccessControlPolicy> policies = jcrVfsWithAllPrivileges.getPolicies(fNase.getPath());
      assertEquals(0, policies.size());

      policies = jcrVfsWithAllPrivileges.getEffectivePolicies(fNase.getPath());
      assertEquals(2, policies.size());

      Set<IAccessControlEntry> allAces = CollectionUtils.newSet();
      Iterator<IAccessControlPolicy> iterator = policies.iterator();
      allAces.addAll(iterator.next().getAccessControlEntries());
      allAces.addAll(iterator.next().getAccessControlEntries());

      assertEquals(3, allAces.size());

      Set<IPrivilege> privileges = getPrivilegesForPrincipal(allAces, "user1");
      assertEquals(2, privileges.size());
      Set<IPrivilege> expected = CollectionUtils.newSet();
      expected.add(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_CHILDREN_PRIVILEGE));
      expected.add(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_PRIVILEGE));
      assertEquals(expected, privileges);

      privileges = jcrVfsWithAllPrivileges.getPrivileges(fNase.getPath());
      assertEquals(1, privileges.size());
      assertEquals(
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.ALL_PRIVILEGE)),
            privileges);
   }

   @Test
   public void testRemoveFolderPolicy() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");

      Set<IAccessControlPolicy> policies = jcrVfsWithAllPrivileges.getPolicies(fNase.getId());
      assertEquals(1, policies.size());

      IAccessControlPolicy policy = policies.iterator().next();
      policy.removeAllAccessControlEntries();

      jcrVfsWithAllPrivileges.setPolicy(fNase.getPath(), policy);

      // ensure it was deleted
      policies = jcrVfsWithAllPrivileges.getPolicies(fNase.getPath());
      assertEquals(0, policies.size());
   }

   @Test
   public void testGroupPermissions() throws Exception
   {
      String groupName = "{ipp.organisation}Group_1_Level_1";

      IFile fHase = jcrVfsWithAllPrivileges.getFile("/nase/hase.pdf");

      Set<IAccessControlPolicy> policies = jcrVfsWithAllPrivileges.getPolicies(fHase.getPath());
      assertEquals(0, policies.size());

      policies = jcrVfsWithAllPrivileges.getApplicablePolicies(fHase.getPath());
      assertEquals(1, policies.size());

      // give for /nase/hase.pdf all privileges to delete it
      IAccessControlPolicy policy = policies.iterator().next();
      Set<IPrivilege> privilegesToSet = CollectionUtils.newSet();
      privilegesToSet.add(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_PRIVILEGE));
      privilegesToSet.add(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_CHILDREN_PRIVILEGE));
      privilegesToSet.add(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.READ_ACL_PRIVILEGE));
      privilegesToSet.add(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.MODIFY_ACL_PRIVILEGE));
      privilegesToSet.add(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.MODIFY_PRIVILEGE));
      policy.addAccessControlEntry(new JcrVfsPrincipal(groupName), privilegesToSet);
      jcrVfsWithAllPrivileges.setPolicy(fHase.getPath(), policy);

      // test setting policies for a group
      Set<IPrivilege> privileges = jcrVfsWithReadPrivileges.getPrivileges(fHase.getPath());
      assertEquals(7, privileges.size());
      assertTrue(privileges.contains(jcrVfsWithReadPrivileges.getPrivilegeByName(IPrivilege.READ_PRIVILEGE)));
      assertTrue(privileges.contains(jcrVfsWithReadPrivileges.getPrivilegeByName(IPrivilege.DELETE_PRIVILEGE)));
      assertTrue(privileges.contains(jcrVfsWithReadPrivileges.getPrivilegeByName(IPrivilege.DELETE_CHILDREN_PRIVILEGE)));
      assertTrue(privileges.contains(jcrVfsWithReadPrivileges.getPrivilegeByName(IPrivilege.READ_ACL_PRIVILEGE)));
      assertTrue(privileges.contains(jcrVfsWithReadPrivileges.getPrivilegeByName(IPrivilege.MODIFY_ACL_PRIVILEGE)));
      assertTrue(privileges.contains(jcrVfsWithReadPrivileges.getPrivilegeByName(IPrivilege.MODIFY_PRIVILEGE)));
      assertTrue(privileges.contains(jcrVfsWithReadPrivileges.getPrivilegeByName(IPrivilege.CREATE_PRIVILEGE)));

      // give for /nase a DELETE_CHILDREN_PRIVILEGE additionally
      policies = jcrVfsWithAllPrivileges.getApplicablePolicies("/nase");
      assertEquals(1, policies.size());
      policy = policies.iterator().next();
      privilegesToSet = CollectionUtils.newSet();
      privilegesToSet.add(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_CHILDREN_PRIVILEGE));
      policy.addAccessControlEntry(new JcrVfsPrincipal(groupName), privilegesToSet);
      jcrVfsWithAllPrivileges.setPolicy("/nase", policy);
   }

   @Test
   public void testSetAndUpdatePrivileges() throws Exception
   {
      String filePath = "/nase/copy-of-hase.txt";

      Set<IAccessControlPolicy> policySet = jcrVfsWithAllPrivileges.getApplicablePolicies(filePath);
      IAccessControlPolicy policy = policySet.iterator().next();
      policy.addAccessControlEntry(
            new JcrVfsPrincipal("motu"),
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.MODIFY_PRIVILEGE)));
      jcrVfsWithAllPrivileges.setPolicy(filePath, policy);

      policySet = jcrVfsWithAllPrivileges.getApplicablePolicies(filePath); // is coming as
      // empty so
      // used
      // getPolicies
      // method
      assertTrue(policySet.isEmpty());

      policySet = jcrVfsWithAllPrivileges.getPolicies(filePath); // found one
      policy = policySet.iterator().next();
      policy.addAccessControlEntry(
            new JcrVfsPrincipal("motu"),
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.READ_ACL_PRIVILEGE)));
      jcrVfsWithAllPrivileges.setPolicy(filePath, policy); // Exception at this point
   }

   @Test
   public void testRevert()
   {
      IFile v0 = jcrVfsWithAllPrivileges.getFile("/nase/copy-of-hase.txt");

      assertEquals(VfsUtils.VERSION_UNVERSIONED, v0.getRevisionId());

      v0.setProperty("updateNr", "1");
      jcrVfsWithAllPrivileges.updateFile(v0, "update1".getBytes(), null, true, false);

      IFile v1 = jcrVfsWithAllPrivileges.getFile("/nase/copy-of-hase.txt");
      assertEquals("1", v1.getProperties().get("updateNr"));

      v1.setProperty("updateNr", "2");
      IFile v2 = jcrVfsWithAllPrivileges.updateFile(v1, "update2".getBytes(), null, true,
            false);
      assertEquals("2", v1.getProperties().get("updateNr"));

      List< ? extends IFile> fileVersions = jcrVfsWithAllPrivileges.getFileVersions("/nase/copy-of-hase.txt");
      assertEquals(fileVersions.size(), 2);

      // re-get
      v1 = fileVersions.get(0);
      assertEquals("1", v1.getProperties().get("updateNr"));

      // try to "revert" to existing version, take content from existing version
      jcrVfsWithAllPrivileges.updateFile(v1, v1.getRevisionId(), true, false);

      // verify revert
      assertTrue(Arrays.equals("update1".getBytes(),
            jcrVfsWithAllPrivileges.retrieveFileContent("/nase/copy-of-hase.txt")));

   }

   @Test
   public void testRemoveVersion()
   {
      String doc = "deleteVersion.txt";
      String folder = "/";
      String filePath = folder + doc;

      jcrVfsWithAllPrivileges.removeFile(filePath);

      IFile file = null;
      if (file == null)
      {
         file = jcrVfsWithAllPrivileges.createFile("/", VfsUtils.createFileInfo(doc),
               (byte[]) null, null);
      }

      file.setDescription("I'm a description in 1.0.");

      file = jcrVfsWithAllPrivileges.updateFile(file, true, null, false);

      file.setDescription("I'm a description in 1.1.");

      file = jcrVfsWithAllPrivileges.updateFile(file, true, null, false);

      file.setDescription("I'm a description in 1.2.");

      file = jcrVfsWithAllPrivileges.updateFile(file, true, null, false);

      List< ? extends IFile> fileVersions = jcrVfsWithAllPrivileges.getFileVersions(file.getId());
      assertEquals(3, fileVersions.size());
      System.out.println(fileVersions.get(0).getRevisionName());
      System.out.println(fileVersions.get(1).getRevisionName());
      System.out.println(fileVersions.get(2).getRevisionName());

      jcrVfsWithAllPrivileges.removeFileVersion(fileVersions.get(0).getId(), fileVersions.get(0)
            .getRevisionId());
      System.out.println("Remove " + fileVersions.get(0).getRevisionName());

      List< ? extends IFile> fileVersions2 = jcrVfsWithAllPrivileges.getFileVersions(file.getId());
      assertEquals(2, fileVersions2.size());
      System.out.println(fileVersions2.get(0).getRevisionName());
      System.out.println(fileVersions2.get(1).getRevisionName());

      jcrVfsWithAllPrivileges.removeFileVersion(fileVersions2.get(1).getId(), fileVersions2.get(1)
            .getRevisionId());
      System.out.println("Remove " + fileVersions2.get(1).getRevisionName());

      List< ? extends IFile> fileVersions3 = jcrVfsWithAllPrivileges.getFileVersions(file.getId());
      assertEquals(1, fileVersions3.size());
      System.out.println(fileVersions3.get(0).getRevisionName());

      try
      {
         jcrVfsWithAllPrivileges.removeFileVersion(fileVersions3.get(0).getId(),
               fileVersions3.get(0).getRevisionId());
      }
      catch (RepositoryOperationFailedException e)
      {
         System.out.println("Failed Remove " + fileVersions3.get(0).getRevisionName()+ " "+ e.getMessage()+ " "+e.getCause().getMessage());
      }

      List< ? extends IFile> fileVersions4 = jcrVfsWithAllPrivileges.getFileVersions(file.getId());
      assertEquals(1, fileVersions4.size());
      System.out.println(fileVersions4.get(0).getRevisionName());


      file.setDescription("I'm a description in 1.2.");

      file = jcrVfsWithAllPrivileges.updateFile(file, true, null, false);

      List< ? extends IFile> fileVersions5 = jcrVfsWithAllPrivileges.getFileVersions(file.getId());
      assertEquals(2, fileVersions5.size());
      System.out.println("Added version.");
      System.out.println(fileVersions5.get(0).getRevisionName());
      System.out.println(fileVersions5.get(1).getRevisionName());

      jcrVfsWithAllPrivileges.removeFileVersion(fileVersions5.get(0).getId(), fileVersions5.get(0)
            .getRevisionId());
      System.out.println("Remove " + fileVersions5.get(0).getRevisionName());

      List< ? extends IFile> fileVersions6 = jcrVfsWithAllPrivileges.getFileVersions(file.getId());
      assertEquals(1, fileVersions6.size());
      System.out.println(fileVersions6.get(0).getRevisionName());

      jcrVfsWithAllPrivileges.removeFile(file);
   }

   @Test
   public void testRemoveFile1() throws Exception
   {
      IFolder fNase = jcrVfsWithReadPrivileges.getFolder("/nase");

      // try
      // {
      // jcrVfsWithReadPrivileges.removeFile(fNase.findFile("hase.txt"));
      // fail("Should not be able bo remove hase.txt");
      // }
      // catch (Exception e)
      // {
      // // expecting exception
      // }

      // this one should work
      jcrVfsWithReadPrivileges.removeFile(fNase.findFile("hase.pdf"));
   }

   @Test
   public void testRemoveFile2() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");

      jcrVfsWithAllPrivileges.removeFile(fNase.findFile("hase.txt"));
      jcrVfsWithAllPrivileges.removeFile(fNase.findFile("copy-of-hase.txt"));
   }

   @Test
   public void testRemoveFolder() throws Exception
   {
      IFolder fNase = jcrVfsWithAllPrivileges.getFolder("/nase");

      jcrVfsWithAllPrivileges.removeFolder(fNase, true);
   }

   @Test
   public void testRootFolder() throws Exception
   {
      IFolder rootFolder = jcrVfsWithAllPrivileges.getFolder("/");
      assertEquals("", rootFolder.getName());
   }

   @Test
   public void testSetPolicyForVersionedFile() throws Exception
   {
      LocalFile file1 = VfsUtils.localFile("src/test/res/LICENSE.txt");
      String fileName = "TEST_LICENSE.txt";

      // in future we might leverage Java activation to derive the content type from the
      // file
      file1.setContentType("text/plain");

      // uploading with a different name
      file1.setName(fileName);

      IFile v0 = null;
      try
      {
         v0 = jcrVfsWithAllPrivileges.createFile("/", file1, file1.openContentStream(),
               null);
      }
      finally
      {
         // this will close any previously opened FileInputStream for file1
         file1.closeContentStreams();
      }

      assertEquals(VfsUtils.VERSION_UNVERSIONED, v0.getRevisionId());

      IFile v1 = jcrVfsWithAllPrivileges.createFileVersion(v0.getPath(), "v1", false);

      assertTrue(v1.getVersionLabels().contains("v1"));

      Set<IAccessControlPolicy> policies = jcrVfsWithAllPrivileges.getApplicablePolicies(v1.getId());

      assertEquals(1, policies.size());

      jcrVfsWithAllPrivileges.removeFile(v0.getPath());

   }

   @Test
   public void testDeletePermission()
   {
      String groupName = "user1";
      String doc = "delete_me.txt";
      String folder = "/";
      String filePath = folder + doc;

      jcrVfsWithAllPrivileges.removeFile(filePath);
      IFile file = jcrVfsWithAllPrivileges.getFile(filePath);

      if (file == null)
      {
         jcrVfsWithAllPrivileges.createFile("/", VfsUtils.createFileInfo(doc),
               (byte[]) null, null);
      }

      // Privilege for parent folder.
      Set<IAccessControlPolicy> policySet = jcrVfsWithAllPrivileges.getPolicies(folder);
      IAccessControlPolicy policy = policySet.iterator().next();
         policy.addAccessControlEntry(
            new JcrVfsPrincipal(groupName),
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_CHILDREN_PRIVILEGE)));
      jcrVfsWithAllPrivileges.setPolicy(folder, policy);

      // Privileges for file.
      Set<IAccessControlPolicy> policySet2 = jcrVfsWithAllPrivileges.getApplicablePolicies(filePath);
      IAccessControlPolicy policy2 = policySet2.iterator().next();
      policy2.addAccessControlEntry(
            new JcrVfsPrincipal(groupName),
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_PRIVILEGE)));
      policy2.addAccessControlEntry(
            new JcrVfsPrincipal(groupName),
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.DELETE_CHILDREN_PRIVILEGE)));
      policy2.addAccessControlEntry(
            new JcrVfsPrincipal(groupName),
            Collections.singleton(jcrVfsWithAllPrivileges.getPrivilegeByName(IPrivilege.READ_ACL_PRIVILEGE)));
      jcrVfsWithAllPrivileges.setPolicy(filePath, policy2);

      policySet = jcrVfsWithAllPrivileges.getApplicablePolicies(filePath); // is coming as
      // empty so
      // used
      // getPolicies
      // method
      assertTrue(policySet.isEmpty());

      jcrVfsWithReadPrivileges.removeFile(filePath);

      assertNull(jcrVfsWithAllPrivileges.getFile(filePath));

      Set<IAccessControlPolicy> policySet3 = jcrVfsWithAllPrivileges.getPolicies(folder);

      IAccessControlPolicy next = policySet3.iterator().next();
      next.removeAllAccessControlEntries();

      jcrVfsWithAllPrivileges.setPolicy(folder, next);
   }

   private Set<IPrivilege> getPrivilegesForPrincipal(Set<IAccessControlEntry> aces,
         String principalName)
   {
      Set<IPrivilege> result = CollectionUtils.newSet();
      for (IAccessControlEntry ace : aces)
      {
         if (principalName.equals(ace.getPrincipal().getName()))
         {
            result.addAll(ace.getPrivileges());
         }
      }
      return result;
   }

}
