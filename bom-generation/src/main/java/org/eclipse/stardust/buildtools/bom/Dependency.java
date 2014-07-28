/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.buildtools.bom;

/**
 *
 */
public class Dependency implements Comparable<Dependency>
{
   private final String groupId;

   private final String artifactId;

   private String type;

   private String classifier;

   private final String version;

   private String scope;

   private String project;

   public Dependency(final String groupId, final String artifactId, final String version)
   {
      ensureNeitherNullNorEmpty(groupId, "groupId");
      ensureNeitherNullNorEmpty(artifactId, "artifactId");
      ensureNeitherNullNorEmpty(version, "version");

      this.groupId = groupId;
      this.artifactId = artifactId;
      this.type = "";
      this.classifier = "";
      this.version = version;
      this.scope = "";
      this.project = "";
   }

   public Dependency(final String groupId, final String artifactId, final String type,
         final String classifier, final String version, final String scope,
         final String project)
   {
      ensureNeitherNullNorEmpty(groupId, "groupId");
      ensureNeitherNullNorEmpty(artifactId, "artifactId");
      ensureNeitherNullNorEmpty(version, "version");
      ensureNeitherNullNorEmpty(type, "type");
      ensureNeitherNullNorEmpty(scope, "scope");
      ensureNeitherNullNorEmpty(project, "project");

      this.groupId = groupId;
      this.artifactId = artifactId;
      this.type = type;
      this.classifier = classifier; // might be an empty String;
      this.version = version;
      this.scope = scope;
      this.project = project;

   }

   public String groupId()
   {
      return groupId;
   }

   public String artifactId()
   {
      return artifactId;
   }

   public String version()
   {
      return version;
   }

   public String project()
   {
      return project;
   }

   public void setProject(String project)
   {
      this.project = project;
   }

   public String getClassifier()
   {
      return classifier;
   }

   public void setClassifier(String classifier)
   {
      this.classifier = classifier;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String scope()
   {
      return scope;
   }

   public void setScope(String scope)
   {
      this.scope = scope;
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result = 37 * result + groupId.hashCode();
      result = 37 * result + artifactId.hashCode();
      result = 37 * result + version.hashCode();
      return result;
   }

   @Override
   public boolean equals(final Object obj)
   {
      if (!(obj instanceof Dependency))
      {
         return false;
      }

      final Dependency that = (Dependency) obj;

      return this.groupId.equals(that.groupId) && this.artifactId.equals(that.artifactId)
            && this.version.equals(that.version);
   }

   @Override
   public String toString()
   {
      StringBuffer csvLine = new StringBuffer(groupId)
            .append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(artifactId).append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(version);
      return csvLine.toString();
   }

   public String toLongString()
   {
      StringBuffer csvLine = new StringBuffer(groupId)
            .append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(artifactId).append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(type).append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(classifier).append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(version);
      return csvLine.toString();
   }

   @Override
   public int compareTo(final Dependency that)
   {
      final int groupIdCompare = this.groupId.compareTo(that.groupId);
      if (groupIdCompare == 0)
      {
         final int artifactIdCompare = this.artifactId.compareTo(that.artifactId);
         if (artifactIdCompare == 0)
         {
            return this.version.compareTo(that.version);
         }
         else
         {
            return artifactIdCompare;
         }
      }
      else
      {
         return groupIdCompare;
      }
   }

   private void ensureNeitherNullNorEmpty(final String value, final String name)
   {
      if (value == null)
      {
         throw new NullPointerException("'" + name + "' must not be null.");
      }
      if ("".equals(value))
      {
         throw new IllegalArgumentException("'" + name + "' must not be empty.");
      }
   }

   public Object toStringExtended()
   {
      StringBuffer csvLine = new StringBuffer(groupId)
            .append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(artifactId).append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(type).append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(classifier).append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(version).append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(scope).append(CSVConstants.TARGET_DELIMITER);
      csvLine.append(project).append(CSVConstants.TARGET_DELIMITER);
      return csvLine.toString();
   }
}
