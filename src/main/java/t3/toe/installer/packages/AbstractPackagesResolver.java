/**
 * (C) Copyright 2016-2017 teecube
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.versions.api.ArtifactVersions;
import org.codehaus.mojo.versions.api.DefaultVersionsHelper;

import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosFactory;
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This class resolves all TIBCO installation packages found in a given directory.
* It can be subclassed to display found packages or to install/deploy them in Maven.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public abstract class AbstractPackagesResolver extends CommonMojo {

	protected enum T3Plugins {
		TOE_INSTALLER ("TOE Products Installer"),
		TOE_DOMAINS ("TOE Domains Manager"),
		TIC_BW5 ("TIC BW5 Maven plugin"),
		TIC_BW6 ("TIC BW6 Maven plugin"),
		TAC_ARCHETYPES ("TAC Archetypes");

		private String productName;

		T3Plugins(String productName) {
			this.productName = productName;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}
	};

	@org.apache.maven.plugins.annotations.Parameter(property = InstallerMojosInformation.Packages.pluginsToIncludeInArchive, defaultValue = InstallerMojosInformation.Packages.pluginsToIncludeInArchive_default)
	protected List<T3Plugins> plugins;

	protected List<CommonInstaller> installers;

	@Parameter(property = InstallerMojosInformation.installationPackageDirectory, defaultValue = InstallerMojosInformation.installationPackageDirectory_default)
	protected File installationPackageDirectory;

	@Parameter(property = InstallerMojosInformation.Packages.toeDomainsVersion, defaultValue = InstallerMojosInformation.Packages.toeDomainsVersion_default)
	protected String toeDomainsVersion;

	@Parameter(property = InstallerMojosInformation.Packages.toeInstallerVersion, defaultValue = InstallerMojosInformation.Packages.toeInstallerVersion_default)
	protected String toeInstallerVersion;
	
	@Parameter(property = InstallerMojosInformation.Packages.ticBW5Version, defaultValue = InstallerMojosInformation.Packages.ticBW5Version_default)
	protected String ticBW5Version;
	
	@Parameter(property = InstallerMojosInformation.Packages.ticBW6Version, defaultValue = InstallerMojosInformation.Packages.ticBW6Version_default)
	protected String ticBW6Version;

	@Parameter(property = InstallerMojosInformation.Packages.tacArchetypesVersion, defaultValue = InstallerMojosInformation.Packages.tacArchetypesVersion_default)
	protected String tacArchetypesVersion;

	@Parameter(property = InstallerMojosInformation.Packages.tacArchetypesArtifactsId, defaultValue = InstallerMojosInformation.Packages.tacArchetypesArtifactsId_default)
	protected String tacArchetypesArtifactsId;

	@Parameter (property = InstallerMojosInformation.Packages.offlineDirectory, defaultValue = InstallerMojosInformation.Packages.offlineDirectory_default)
	protected File offlineDirectory; 

	@Parameter (property = InstallerMojosInformation.Packages.offlineArchiveLocalRepository, defaultValue = InstallerMojosInformation.Packages.offlineArchiveLocalRepository_default)
	protected File offlineArchiveLocalRepository; 

	@Parameter (property = InstallerMojosInformation.Packages.offlineArchive, defaultValue = InstallerMojosInformation.Packages.offlineArchive_default)
	protected File offlineArchive; 

	@Override
	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() throws MojoExecutionException {
		return new InstallerLifecycleParticipant();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		installers = new ArrayList<CommonInstaller>();
		for (Class<? extends CommonInstaller> installerClass : InstallerMojosFactory.getInstallersClasses()) {
			CommonInstaller installer;
			try {
				installer = installerClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
			installer.setSession(session);
			installer.setInstallationPackageDirectory(this.installationPackageDirectory);
			installer.initStandalonePOMNoDefaultParameters();

			File installationPackage = installer.getInstallationPackage();
			if (installationPackage != null && installationPackage.exists()) {
				installers.add(installer);
			}
		}

		getLog().info("");
		if (installers.size() > 0) {
			getLog().info("Found " + installers.size() + " TIBCO installation packages:");
			for (CommonInstaller installer : installers) {
				getLog().info("-> " + installer.getProductName() + " version " + installer.getInstallationPackageVersion() + " @ " + installer.getInstallationPackage());
			}
		} else {
			getLog().info("No TIBCO installation package was found.");
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
		}

		result.setBuild(build);

		return result;
	}

}
