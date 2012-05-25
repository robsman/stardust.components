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
 * $Id: JcrProperties.java 54136 2012-02-29 12:33:54Z sven.rottstock $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.vfs.impl.jcr;

/**
 * @author rsauer
 * @version $Revision: 54136 $
 */
public interface JcrProperties
{
   String JCR_UUID = "jcr:uuid";

   String JCR_CONTENT = "jcr:content";

   String JCR_MIME_TYPE = "jcr:mimeType";

   String JCR_ENCODING = "jcr:encoding";

   String JCR_DATA = "jcr:data";

   String JCR_CREATED = "jcr:created";

   String JCR_LAST_MODIFIED = "jcr:lastModified";

   String JCR_LOCK_OWNER = "jcr:lockOwner";

   String JCR_LOCK_IS_DEEP = "jcr:lockIsDeep";

   String JCR_VERSION_HISTORY = "jcr:versionHistory";

   String JCR_BASE_VERSION = "jcr:baseVersion";

   String JCR_IS_CHECKED_OUT = "jcr:isCheckedOut";

   String JCR_PREDECESSORS = "jcr:predecessors";

   String JCR_MERGE_FAILED = "jcr:mergeFailed";

   String JCR_FROZEN_NODE = "jcr:frozenNode";

   String MIXIN_REFERENCEABLE = "mix:referenceable";

   String MIXIN_LOCKABLE = "mix:lockable";

   String MIXIN_VERSIONABLE = "mix:versionable";

   String JCR_FROZEN_PRIMARY_TYPE = "jcr:frozenPrimaryType";

   String JCR_FROZEN_MIXIN_TYPES = "jcr:frozenMixinTypes";

   String JCR_FROZEN_UUID = "jcr:frozenUuid";

}
