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
 * $Id: NodeTypes.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public interface NodeTypes
{

   String NT_REPOSITORY_ROOT = "rep:root";

   String NT_UNSTRUCTURED = "nt:unstructured";

   String NT_FOLDER = "nt:folder";

   String NT_FILE = "nt:file";

   String NT_LINKED_FILE = "nt:linkedFile";

   String NT_RESOURCE = "nt:resource";

   String NT_VERSION = "nt:version";

   String NT_VERSION_HISTORY = "nt:versionHistory";

   String NT_VERSION_LABELS = "nt:versionLabels";

   String NT_FROZEN_NODE = "nt:frozenNode";

}
