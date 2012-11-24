package com.example.pluginquerydsl;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

/**
 * 
 */
@Alias("querydsl")
@RequiresProject
public class QuerydslPlugin implements Plugin
{
   @Inject
   private Project project;

   @Inject
   private Event<InstallFacets> install;

   @SetupCommand
   public void setup()
   {
      if (!project.hasFacet(QuerydslFacet.class))
      {
         install.fire(new InstallFacets(QuerydslFacet.class));
      }
   }
}
