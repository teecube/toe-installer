/**
 * (C) Copyright 2016-2018 teecube
 * (http://teecu.be) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package t3.toe.installer.packages;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.DefaultSettingsWriter;
import org.codehaus.mojo.versions.api.ArtifactVersions;
import org.codehaus.mojo.versions.api.DefaultVersionsHelper;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.Environment;
import t3.toe.installer.environments.Environments;
import t3.toe.installer.environments.EnvironmentsMarshaller;
import t3.toe.installer.environments.Product;
import t3.utils.POMManager;
import t3.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
* <p>
* This goal generates a <strong>self-contained</strong> and <strong>ready-to-use</strong>&nbsp;
* <em>standalone package</em>.
* </p>
* <p>
* This <em>standalone package</em> can be composed of:
*  <ul>
*   <li>T3 plugins and their dependencies</li>
*   <li>resolved TIBCO installation packages (see <a href="packages-display-mojo.html">packages-display goal</a>)</li>
*   <li>a Maven settings.xml preconfigured to use T3 plugins</li>
*   <li>a preconfigured <a href="env-install-mojo.html">environment installation definition</a></li>
*  </ul>
* Each of these elements can be included or not in the <em>standalone package</em>.
* </p>
* <p>
* This <em>standalone package</em> allows to:
*  <ul>
*   <li>use T3 plugins with no required Internet access</li>
*   <li>install TIBCO products according to an environment installation definition</li>
*   <li>use installed TIBCO products with T3 plugins additions</li>
*   <li>deliver a self-contained and ready-to-use package to clients</li>
*  </ul>
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "standalone-package", requiresProject = false)
public class StandalonePackageGenerator extends AbstractPackagesResolver {

	@Parameter(property = InstallerMojosInformation.Packages.Standalone.generateSettings, defaultValue = InstallerMojosInformation.Packages.Standalone.generateSettings_default)
	protected Boolean generateSettings;

	@Parameter(property = InstallerMojosInformation.Packages.Standalone.topologyGenerate, defaultValue = InstallerMojosInformation.Packages.Standalone.topologyGenerate_default)
	protected Boolean generateStandaloneTopology;

	@Parameter(property = InstallerMojosInformation.Packages.Standalone.includeLocalTIBCOInstallationPackages, defaultValue = InstallerMojosInformation.Packages.Standalone.includeLocalTIBCOInstallationPackages_default)
	protected Boolean includeLocalTIBCOInstallationPackages;

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.includeTopologyTIBCOInstallationPackages, defaultValue = InstallerMojosInformation.Packages.Standalone.includeTopologyTIBCOInstallationPackages_default)
	protected Boolean includeTopologyTIBCOInstallationPackages;

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.directory, defaultValue = InstallerMojosInformation.Packages.Standalone.directory_default)
	protected File standaloneDirectory;

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.localRepository, defaultValue = InstallerMojosInformation.Packages.Standalone.localRepository_default)
	protected File standaloneLocalRepository;

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.topologyGeneratedFile, defaultValue = InstallerMojosInformation.Packages.Standalone.topologyGeneratedFile_default)
	protected File standaloneTopologyGeneratedFile;

	/* Archive */
	@Parameter (property = InstallerMojosInformation.Packages.Standalone.Archive.archive, defaultValue = InstallerMojosInformation.Packages.Standalone.Archive.archive_default)
	protected File standaloneArchive;

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.Archive.generate, defaultValue = InstallerMojosInformation.Packages.Standalone.Archive.generate_default)
	protected Boolean generateStandaloneArchive;

	/* Plugins */
	@Parameter (property = InstallerMojosInformation.Packages.Standalone.Plugins.include, defaultValue = InstallerMojosInformation.Packages.Standalone.Plugins.include_default)
	protected Boolean includePluginsInStandalone;

	@org.apache.maven.plugins.annotations.Parameter(property = InstallerMojosInformation.Packages.Standalone.Plugins.list, defaultValue = InstallerMojosInformation.Packages.Standalone.Plugins.list_default)
	protected List<T3Plugins> plugins;

	@Parameter(property = InstallerMojosInformation.Packages.Standalone.Plugins.toeDomainsVersion, defaultValue = InstallerMojosInformation.Packages.Standalone.Plugins.toeDomainsVersion_default)
	protected String toeDomainsVersion;

	@Parameter(property = InstallerMojosInformation.Packages.Standalone.Plugins.toeInstallerVersion, defaultValue = InstallerMojosInformation.Packages.Standalone.Plugins.toeInstallerVersion_default)
	protected String toeInstallerVersion;

	@Parameter(property = InstallerMojosInformation.Packages.Standalone.Plugins.ticBW5Version, defaultValue = InstallerMojosInformation.Packages.Standalone.Plugins.ticBW5Version_default)
	protected String ticBW5Version;

	@Parameter(property = InstallerMojosInformation.Packages.Standalone.Plugins.ticBW6Version, defaultValue = InstallerMojosInformation.Packages.Standalone.Plugins.ticBW6Version_default)
	protected String ticBW6Version;

	@Parameter(property = InstallerMojosInformation.Packages.Standalone.Plugins.tacArchetypesVersion, defaultValue = InstallerMojosInformation.Packages.Standalone.Plugins.tacArchetypesVersion_default)
	protected String tacArchetypesVersion;

	@Parameter(property = InstallerMojosInformation.Packages.Standalone.Plugins.tacArchetypesArtifactsId, defaultValue = InstallerMojosInformation.Packages.Standalone.Plugins.tacArchetypesArtifactsId_default)
	protected String tacArchetypesArtifactsId;


	@Override
	protected Boolean getGenerateTopology() {
		return generateStandaloneTopology;
	}

	@Override
	protected File getTopologyGeneratedFile() {
		return standaloneTopologyGeneratedFile;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		MavenProject goOfflineProject = generateGoOfflineProject();

		/*
		File f = new File("C:/tools/pom.xml");
		try {
			POMManager.writeModelToPOM(goOfflineProject.getModel(), f);
			MavenFormatStage r = Maven.resolver().loadPomFromFile(f).importCompileAndRuntimeDependencies().resolve().withTransitivity();
			File[] archive = r.asFile();
			MavenCoordinate[] coordinates = r.as(MavenCoordinate.class);
			getLog().info(archive.toString());
			getLog().info(coordinates.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/

		getLog().info("Creating a standalone Maven repository in '" + standaloneLocalRepository.getAbsolutePath() + "'");

		if (includePluginsInStandalone && !plugins.isEmpty()) {
			getLog().info("");
			getLog().info("This repository will include following plugins:");
			for (T3Plugins plugin : plugins) {
				getLog().info("-> " + plugin.getProductName());
			}
			getLog().info("");
			java.util.logging.Logger logTransferListener = java.util.logging.Logger.getLogger("org.jboss.shrinkwrap.resolver.impl.maven.logging.LogTransferListener");
			logTransferListener.setLevel(Level.OFF);
			getLog().info("This might take some minutes...");
			
			goOffline(goOfflineProject, standaloneLocalRepository, "3.3.9");
		}

		if (includeTopologyTIBCOInstallationPackages && topologyTemplateFile != null && topologyTemplateFile.exists()) {
			EnvironmentsMarshaller environmentsMarshaller = EnvironmentsMarshaller.getEnvironmentMarshaller(topologyTemplateFile);
			Environments environments = environmentsMarshaller.getObject();
			for (Environment environment : environments.getEnvironment()) {
				for (Product product : environment.getProducts().getTibcoProductOrCustomProduct()) {
					
				}
			}
		}

		if (includeLocalTIBCOInstallationPackages) {
			super.execute();
			installPackagesToLocalRepository(standaloneLocalRepository);
		}

		if (generateSettings) {
			generateOfflineSettings();
		}

		if (generateStandaloneArchive) {
			getLog().info("");
			getLog().info("Copying standalone directory '" + standaloneDirectory.getAbsolutePath() + "' to archive '" + standaloneArchive.getAbsolutePath() + "'");
			try {
				standaloneArchive.delete();
				Utils.addFilesToZip(standaloneDirectory, standaloneArchive);
			} catch (IOException | ArchiveException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private Artifact getArtifact(DefaultVersionsHelper helper, String groupId, String artifactId, String version, String type) throws MojoExecutionException {
		DefaultArtifactHandler mavenPluginArtifactHandler = new DefaultArtifactHandler("jar");
		mavenPluginArtifactHandler.getExtension(); // force loading of extension

		Artifact pluginArtifact = new DefaultArtifact(groupId, artifactId, "", Artifact.SCOPE_COMPILE, type, null, mavenPluginArtifactHandler);

		if (version == null || version.isEmpty()) {
			ArtifactVersions artifactVersions;
			try {
				artifactVersions = helper.lookupArtifactVersions(pluginArtifact, true);
			} catch (org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
			ArtifactVersion lowerBound = new DefaultArtifactVersion("0.0.0");
			ArtifactVersion upperBound = new DefaultArtifactVersion("10.0.0"); // upper bound : we have time
			ArtifactVersion newest = artifactVersions.getNewestVersion(lowerBound, upperBound);
			if (newest != null) {
				getLog().debug("Newest version for " + pluginArtifact.getGroupId() + ":" + pluginArtifact.getArtifactId() + " is " + newest.toString());
				version = newest.toString();			
			} else {
				version = "0.0.1";
			}
		}

		pluginArtifact.setVersion(version);
		return pluginArtifact;
	}

	private Plugin getPluginFromArtifact(Artifact artifact) {
		Plugin plugin = new Plugin();
		plugin.setGroupId(artifact.getGroupId());
		plugin.setArtifactId(artifact.getArtifactId());
		plugin.setVersion(artifact.getVersion());

		List<PluginExecution> executions = new ArrayList<PluginExecution>();
		plugin.setExecutions(executions);

		return plugin;
	}

	private Plugin getToeDomainsPlugin(DefaultVersionsHelper helper, String version) throws MojoExecutionException {
		List<String> goals = new ArrayList<String>();
		goals.add("bw6-domain-create");
		return getPlugin(helper, "io.teecube.toe", "toe-domains-plugin", version, "toe-domains", goals);
	}

	private Plugin getToeInstallerPlugin(DefaultVersionsHelper helper, String version) throws MojoExecutionException {
		List<String> goals = new ArrayList<String>();
		goals.add("envinfo-list");
		return getPlugin(helper, InstallerLifecycleParticipant.pluginGroupId, InstallerLifecycleParticipant.pluginArtifactId, version, "toe", goals);
	}

	private Plugin getTicBW5Plugin(DefaultVersionsHelper helper, String version) throws MojoExecutionException {
		List<String> goals = new ArrayList<String>();
		goals.add("properties-merge");
		return getPlugin(helper, "io.teecube.tic", "tic-bw5", version, "bw5", goals);
	}
	
	private Plugin getTicBW6Plugin(DefaultVersionsHelper helper, String version) throws MojoExecutionException {
		List<String> goals = new ArrayList<String>();
		goals.add("studio-proxy-uninstall");
		goals.add("p2maven-install");
		return getPlugin(helper, "io.teecube.tic", "tic-bw6", version, "bw6", goals);
	}

	private Plugin getMavenDeployPlugin() {
		Plugin plugin = new Plugin();
		plugin.setGroupId("org.apache.maven.plugins");
		plugin.setArtifactId("maven-deploy-plugin");
		plugin.setVersion("2.8.2"); // must be in sync with POM !

		return plugin;
	}

	private Plugin getMavenInstallPlugin() {
		Plugin plugin = new Plugin();
		plugin.setGroupId("org.apache.maven.plugins");
		plugin.setArtifactId("maven-install-plugin");
		plugin.setVersion("2.5.2"); // must be in sync with POM !
		
		return plugin;
	}

	private Plugin getMavenDependencyPlugin() {
		Plugin plugin = new Plugin();
		plugin.setGroupId("org.apache.maven.plugins");
		plugin.setArtifactId("maven-dependency-plugin");
		plugin.setVersion("3.0.2"); // must be in sync with POM !
		
		return plugin;
	}

	private List<Plugin> getTacArchetypes(DefaultVersionsHelper helper, String tacArchetypesVersion) throws MojoExecutionException {
		List<Plugin> result = new ArrayList<Plugin>();
		String[] artifactsId = tacArchetypesArtifactsId.split(",");
		for (int i = 0; i < artifactsId.length; i++) {
			String artifactId = artifactsId[i];

			Artifact artifact = getArtifact(helper, "io.teecube.tac.archetypes", artifactId, tacArchetypesVersion, "maven-archetype");
			Plugin plugin = getPluginFromArtifact(artifact);
			result.add(plugin);
		}
		return result;
	}

	private Plugin getPlugin(DefaultVersionsHelper helper, String groupId, String artifactId, String version, String prefix, List<String> goals) throws MojoExecutionException {
		Artifact artifact = getArtifact(helper, groupId, artifactId, version, "maven-plugin");
		Plugin result = getPluginFromArtifact(artifact);

		PluginExecution pluginExecution = new PluginExecution();
		pluginExecution.setId(prefix);
		pluginExecution.setPhase("validate");
		pluginExecution.setGoals(goals);

		result.addExecution(pluginExecution);

		return result;
	}

	/* @Component and @Parameter for DefaultVersionsHelper */
    @SuppressWarnings("deprecation")
	@Component
    protected org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

    @Component
    protected org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    @Component
    protected MavenProjectBuilder projectBuilder;

    @org.apache.maven.plugins.annotations.Parameter( defaultValue = "${reactorProjects}", required = true, readonly = true )
    protected List<?> reactorProjects;

    @SuppressWarnings("deprecation")
	@Component
    protected org.apache.maven.artifact.metadata.ArtifactMetadataSource artifactMetadataSource;

    @org.apache.maven.plugins.annotations.Parameter( defaultValue = "${project.remoteArtifactRepositories}", readonly = true )
    protected List<?> remoteArtifactRepositories;

    @org.apache.maven.plugins.annotations.Parameter( defaultValue = "${project.pluginArtifactRepositories}", readonly = true )
    protected List<?> remotePluginRepositories;

    @org.apache.maven.plugins.annotations.Parameter( defaultValue = "${localRepository}", readonly = true )
    protected ArtifactRepository localRepository;

    @Component
    private WagonManager wagonManager;

    @org.apache.maven.plugins.annotations.Parameter( defaultValue = "${settings}", readonly = true )
    protected Settings settings;

    @org.apache.maven.plugins.annotations.Parameter( property = "maven.version.rules.serverId", defaultValue = "serverId" )
    private String serverId;

    @org.apache.maven.plugins.annotations.Parameter( property = "maven.version.rules" )
    private String rulesUri;

    @SuppressWarnings("deprecation")
	@Component
    protected org.apache.maven.project.path.PathTranslator pathTranslator;

    @Component
    protected ArtifactResolver artifactResolver;

    /* end of @Component & @Parameter for DefaultVersionsHelper */

	private Set<Plugin> getPluginArtifacts() throws MojoExecutionException {
		HashSet<Plugin> result = new HashSet<Plugin>();

		DefaultVersionsHelper helper = new DefaultVersionsHelper(artifactFactory, artifactResolver, artifactMetadataSource, remoteArtifactRepositories, remotePluginRepositories, localRepository, wagonManager, settings, serverId, rulesUri, getLog(), session, pathTranslator);

		if (plugins.contains(T3Plugins.TOE_DOMAINS)) {
			result.add(getToeDomainsPlugin(helper, toeDomainsVersion));
		}
		if (plugins.contains(T3Plugins.TOE_INSTALLER)) {
			result.add(getToeInstallerPlugin(helper, toeInstallerVersion));
		}
		if (plugins.contains(T3Plugins.TIC_BW5)) {
			result.add(getTicBW5Plugin(helper, ticBW5Version));
		}
		if (plugins.contains(T3Plugins.TIC_BW6)) {
			result.add(getTicBW6Plugin(helper, ticBW6Version));
		}
		if (plugins.contains(T3Plugins.TAC_ARCHETYPES)) {
			result.addAll(getTacArchetypes(helper, tacArchetypesVersion));
		}
		result.add(getMavenDeployPlugin());
		result.add(getMavenInstallPlugin());
		result.add(getMavenDependencyPlugin());

		return result;
	}

	protected MavenProject generateGoOfflineProject() throws MojoExecutionException {
		MavenProject result = new MavenProject();

		result.setModelVersion("4.0.0");
		result.setGroupId("go-offline");
		result.setArtifactId("go-offline");
		result.setVersion("1");
		result.setPackaging("pom");

		Build build = new Build();
		for (Plugin plugin : getPluginArtifacts()) {
			build.addPlugin(plugin);

			Dependency d = new Dependency();
			d.setGroupId(plugin.getGroupId());
			d.setArtifactId(plugin.getArtifactId());
			d.setVersion(plugin.getVersion());

			result.getDependencies().add(d);
		}

		result.setBuild(build);

		return result;
	}

	private void generateOfflineSettings() throws MojoExecutionException {
		getLog().info("");
		getLog().info("Generating a standalone Maven settings.xml in '" + standaloneDirectory.getAbsolutePath() + "'");

		Settings defaultSettings = new Settings();

		defaultSettings.setLocalRepository("./repository"); // use only offline repository
		defaultSettings.setOffline(true); // offline to use only offline repository
		// <pluginGroups> to define T3 plugins' groupIds
		List<String> pluginGroups = new ArrayList<String>();
		pluginGroups.add("io.teecube.tic");
		pluginGroups.add("io.teecube.toe");
		defaultSettings.setPluginGroups(pluginGroups );

		// write the settings.xml in target/offline
		DefaultSettingsWriter settingsWriter = new DefaultSettingsWriter();
		try {
			settingsWriter.write(new File(standaloneDirectory, "settings.xml"), null, defaultSettings);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	protected void doExecute() throws MojoExecutionException {
		
	}

}
