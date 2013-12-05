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
package org.eclipse.stardust.documentation;

import com.sun.javadoc.*;

import java.io.*;

/**
 * Doclet to create Eclipse toc for JavaDoc.
 * 
 */
public class CustomJavaDocToc extends Doclet
{
	private static String TAB = "   ";

	private static String docref = "html/reference/api/engine/";
	private static String destdir;
	private static String scope;
	private static String tocname;
	private static String label;

	public static boolean start(RootDoc root)
	{
		PrintWriter out;
		String filename;
		String packagename = null;
		String packageref = null;
		ClassDoc[] pclasses = null;
		ClassDoc[] classes = root.specifiedClasses();
		readOptions(root.options());
		setParams();

		filename = destdir.concat("/").concat(tocname).concat(".xml");
		try
		{
			File tocfile = new File(filename);
			out = new PrintWriter(new FileWriter(tocfile));
			out.println("<?xml version='1.0' encoding='UTF-8'?>");
			out.println("<?NLS TYPE='org.eclipse.help.toc'?>");
			out.println("<toc label=\"" + label + "\" link_to=\"toc.xml#tocapi" + "\">");
			out.println("<topic label=\"" + label + "\" href=\"" + docref
			      + "index.html\">");
			out.println(TAB + "<topic label=\"Overview\" href=\"" + docref
			      + "overview-summary.html\">");
			PackageDoc[] packages = root.specifiedPackages();
			for (int i = 0; i < packages.length; i++)
			{
				packagename = packages[i].name();
				packageref = packagename.replace(".", "/");
				pclasses = packages[i].allClasses();
				out.println(TAB + TAB + "<topic label=\"" + packagename
				      + " Package\" href=\"" + docref + packageref
				      + "/package-summary.html\"/>");
			}

			out.println(TAB + "</topic>");
			out.println("</topic>");
			out.println("</toc>");

			out.close();
		}

		catch (IOException e)
		{
		}

		return true;
	}

	/**
	 * Reads options. Custom flags need to be read in the method to be recognized.
	 * 
	 */
	private static void readOptions(String[][] options)
	{

		for (int i = 0; i < options.length; i++)
		{
			String[] opt = options[i];
			if (opt.length == optionLength(opt[0]))
			{
				if (opt[0].equals("-scope"))
				{
					scope = opt[1];
				} else if (opt[0].equals("-destdir"))
				{
					destdir = opt[1];
				}
			}
		}
	}

	/**
	 * Sets parameters depending on scope of JavaDoc.
	 * Current scopes are:
	 * 	engine - for Engine API
	 * 	spi - for Engine SPI examples
	 * 	swing - for Swing Portal API
	 * 	webservices - for Web Services Engine API
	 * 
	 */
	public static void setParams()
	{
		if (scope.equals("spi"))
		{
			tocname = "tocspi";
			label = "@productnameonly@ Engine SPI";
			docref = "html/reference/api/spi/";
		}
		else if (scope.equals("webservices"))
		{
			tocname = "tocws";
			label = "@productnameonly@ Web Services Engine API";
			docref = "html/reference/api/wsengine/";
		}
		else if (scope.equals("swing"))
		{
			tocname = "tocswing";
			label = "@productnameonly@ Swing Portal API";
			docref = "html/reference/api/tds/";
		} 
      else if (scope.equals("engine"))
		{
			tocname = "tocapi";
			label = "@productnameonly@ Engine API";
			docref = "html/reference/api/engine/";
		}
      else
      {
			tocname = "toc" + scope;
			label = "@productnameonly@ " + Character.toUpperCase(scope.charAt(0)) + scope.substring(1) + " API";
			docref = "html/reference/api/" + scope + "/";
      }
	}
	
	/**
	 * Determines number of options for custom flags.
	 * 
	 */
	public static int optionLength(String option)
	{
		if (option.equals("-scope"))
		{
			return 2;
		}
		if (option.equals("-destdir"))
		{
			return 2;
		}
		return 0;
	}
}
