package com.example.pluginquerydsl;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.maven.MavenPluginFacet;
import org.jboss.forge.maven.plugins.Configuration;
import org.jboss.forge.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.maven.plugins.ExecutionBuilder;
import org.jboss.forge.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.DependencyInstaller;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.spec.javaee.PersistenceFacet;

@RequiresFacet({ MavenPluginFacet.class, PersistenceFacet.class })
public class QuerydslFacet extends BaseFacet
{
   private final DependencyInstaller installer;

   private final Event<InstallFacets> install;

   @Inject
   public QuerydslFacet(final DependencyInstaller installer, final Event<InstallFacets> install)
   {
      this.installer = installer;
      this.install = install;
   }

   public Event<InstallFacets> getInstall()
   {
      return install;
   }

   public DependencyInstaller getInstaller()
   {
      return installer;
   }

   protected List<Dependency> getRequiredDependencies()
   {
      return Arrays.<Dependency> asList(
               DependencyBuilder.create("com.mysema.querydsl:querydsl-apt"),
               DependencyBuilder.create("com.mysema.querydsl:querydsl-jpa"),
               DependencyBuilder.create("org.slf4j:slf4j-log4j12")
               );
   }

   @Override
   public boolean isInstalled()
   {
      DependencyFacet deps = project.getFacet(DependencyFacet.class);
      for (Dependency requirement : getRequiredDependencies())
      {
         if (!deps.hasEffectiveDependency(requirement))
         {
            return false;
         }
      }
      return true;
   }

   @Override
   public boolean install()
   {
      if (installDependencies())
      {
         installAptMavenPlugin();
      }
      return true;
   }

   private boolean installDependencies()
   {
      for (Dependency requirement : getRequiredDependencies())
      {
         if (!getInstaller().isInstalled(project, requirement))
         {
            getInstaller().install(project, requirement, ScopeType.COMPILE);
         }
      }
      return true;
   }

   public void installAptMavenPlugin()
   {
      MavenPluginFacet plugins = project.getFacet(MavenPluginFacet.class);

      DependencyBuilder aptMavenPlugin = DependencyBuilder.create("com.mysema.maven:apt-maven-plugin");// .setVersion("1.0.6");

      if (!plugins.hasPlugin(aptMavenPlugin))
      {
         plugins.removePlugin(aptMavenPlugin);
      }

      Configuration configuration = ConfigurationBuilder
               .create()
               .addConfigurationElement(
                        ConfigurationElementBuilder.create().setName("outputDirectory")
                                 .setText("target/generated-sources/java"))
               .addConfigurationElement(
                        ConfigurationElementBuilder.create().setName("processor")
                                 .setText("com.mysema.query.apt.jpa.JPAAnnotationProcessor"));

      ExecutionBuilder execution = ExecutionBuilder.create().addGoal("process").setConfig(configuration);

      plugins.addPlugin(MavenPluginBuilder.create().setDependency(aptMavenPlugin).addExecution(execution));
   }
}
