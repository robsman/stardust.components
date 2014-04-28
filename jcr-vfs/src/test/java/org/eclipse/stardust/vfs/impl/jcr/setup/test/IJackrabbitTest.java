/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.vfs.impl.jcr.setup.test;

import org.eclipse.stardust.vfs.IDocumentRepositoryService;

public interface IJackrabbitTest
{
   void setJcrVfsWithAllPrivileges(IDocumentRepositoryService jcrVfsWithAllPrivileges);

   void setJcrVfsWithReadPrivileges(IDocumentRepositoryService jcrVfsWithReadPrivileges);
}
