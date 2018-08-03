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

import com.google.common.io.Files;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.version.PluginVersionNotFoundException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.DefaultSettingsWriter;
import org.codehaus.mojo.versions.api.ArtifactVersions;
import org.codehaus.mojo.versions.api.DefaultVersionsHelper;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.impl.maven.bootstrap.MavenSettingsBuilder;
import org.xml.sax.SAXException;
import t3.LifecyclesUtils;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.*;
import t3.toe.installer.environments.products.ProductToInstall;
import t3.toe.installer.environments.products.ProductsToInstall;
import t3.utils.MavenRunner;
import t3.utils.POMManager;
import t3.utils.Utils;
import t3.utils.ZipUtils;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
* <p>
* This goal generates a <strong>self-contained</strong> and <strong>ready-to-use</strong>&nbsp;<em>standalone package
* </em>.
* </p>
* <p>
* This <em>standalone package</em> can be composed of:
*  <ul>
*   <li>locally resolved TIBCO installation packages
 *   (see <a href="packages-display-mojo.html">packages-display goal</a>)</li>
*   <li>T3 plugins and their dependencies</li>
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

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.includeTopologyInstallationPackages, defaultValue = InstallerMojosInformation.Packages.Standalone.includeTopologyInstallationPackages_default)
	protected Boolean includeTopologyInstallationPackages;

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.directory, defaultValue = InstallerMojosInformation.Packages.Standalone.directory_default)
	protected File standaloneDirectory;

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.localRepository, defaultValue = InstallerMojosInformation.Packages.Standalone.localRepository_default)
	protected File standaloneLocalRepository;

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.localPackages, defaultValue = InstallerMojosInformation.Packages.Standalone.localPackages_default)
	protected File standaloneLocalPackages;

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.topologyGeneratedFile, defaultValue = InstallerMojosInformation.Packages.Standalone.topologyGeneratedFile_default)
	protected File standaloneTopologyGeneratedFile;

	/* Archive */
	@Parameter (property = InstallerMojosInformation.Packages.Standalone.Archive.archive, defaultValue = InstallerMojosInformation.Packages.Standalone.Archive.archive_default)
	protected File standaloneArchive;

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.Archive.generate, defaultValue = InstallerMojosInformation.Packages.Standalone.Archive.generate_default)
	protected Boolean generateStandaloneArchive;

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.ignoreDefaultSettings, defaultValue = InstallerMojosInformation.Packages.Standalone.ignoreDefaultSettings_default)
	protected boolean ignoreDefaultSettings;

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

	private boolean localTIBCOInstallationPackagesResolved = false;

	private static final String messageIncludeInstallationPackagesFromTopology = "TIBCO and custom installation packages from topology file";
	private static final String messageIncludeLocalTIBCOInstallationPackages  = "TIBCO installation packages resolved locally";
	private static final String messageIncludePlugins  = "Maven plugins";
	private static final String messageGenerateSettings  = "Maven settings.xml to use included Maven plugins";
	private static final String messageGenerateTopology  = "environments topology file";
	private static final String messageGenerateStandaloneArchive  = "a standalone archive wrapping all elements above";

	@Override
	protected void doExecute() throws MojoExecutionException {
		// nothing to do
	}

	@Override
	protected void generateTopology() throws MojoExecutionException {
		super.generateTopology();
	}

	@Override
	protected Boolean getGenerateTopology() throws MojoExecutionException {
		return false;
	}

	@Override
	protected File getTopologyGeneratedFile() {
		return standaloneTopologyGeneratedFile;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Creating a standalone package in directory '" + standaloneDirectory.getAbsolutePath() + "'");
		if (generateStandaloneArchive) {
			getLog().info("Created standalone package will be archived to '" + standaloneArchive.getAbsolutePath() + "'");
		}
		getLog().info("");

		getLog().info("List of included elements:");
		int elementIncludedIndex = 0;
		if (includeInstallationPackagesFromTopology()) {
			getLog().info(++elementIncludedIndex + ". " + messageIncludeInstallationPackagesFromTopology);
		}
		if (includeLocalTIBCOInstallationPackages) {
			getLog().info(++elementIncludedIndex + ". " + messageIncludeLocalTIBCOInstallationPackages);
		}
		if (includePlugins()) {
			getLog().info(++elementIncludedIndex + ". " + messageIncludePlugins);
		}
		if (generateSettings) {
			getLog().info(++elementIncludedIndex + ". " + messageGenerateSettings);
		}
		if (generateStandaloneTopology) {
			getLog().info(++elementIncludedIndex + ". " + messageGenerateTopology);
		}
		if (generateStandaloneArchive) {
			getLog().info(++elementIncludedIndex + ". " + messageGenerateStandaloneArchive);
		}

		if (elementIncludedIndex == 0) {
			getLog().info("");
			getLog().warn("Configuration specifies no element to include. Skipping generation of standalone package.");
			return;
		}

		getLog().info("");
		getLog().info("---");

		elementIncludedIndex = 0;

		if (includeInstallationPackagesFromTopology()) {
			getLog().info("");
			getLog().info(++elementIncludedIndex + ". Include " + messageIncludeInstallationPackagesFromTopology);
			getLog().info("");

			getLog().info("Using topology file '" + topologyTemplateFile.getAbsolutePath() + "'");
			getLog().info("");

			EnvironmentsMarshaller environmentsMarshaller = EnvironmentsMarshaller.getEnvironmentMarshaller(topologyTemplateFile);
			EnvironmentsToInstall environmentsToInstall = new EnvironmentsToInstall(environmentsMarshaller.getObject().getEnvironment(), topologyTemplateFile);

			List<ProductToInstall<?>> uniqueProductsList = new ArrayList<ProductToInstall<?>>();

			boolean atLeastOneMavenArtifactResolved = false;
			for (EnvironmentToInstall environment : environmentsToInstall) {
				ProductsToInstall productsToInstall = new ProductsToInstall(environment, this, false);

				for (ProductToInstall product : productsToInstall) {
					if (!uniqueProductsList.contains(product)) {
						uniqueProductsList.add(product);
					}
				}

				if (!atLeastOneMavenArtifactResolved && productsToInstall.isAtLeastOneMavenArtifactResolved()) {
					atLeastOneMavenArtifactResolved = true;
				}
			}

			if (atLeastOneMavenArtifactResolved) {
				getLog().info("");
			}

			for (ProductToInstall<?> productToInstall : uniqueProductsList) {
				if (productToInstall.getPackage().getMavenArtifact() != null || productToInstall.getPackage().getMavenTIBCOArtifact() != null) {
					// product package was defined as a Maven artifact in topology template, hence deploy this artifact in standalone Maven repository
					String groupId = "";
					String artifactId = "";
					String version = "";
					String packaging = "";
					String classifier = "";
					if (productToInstall.getPackage().getMavenArtifact() != null) {
						MavenArtifactPackage mavenRemote = productToInstall.getPackage().getMavenArtifact();
						groupId = mavenRemote.getGroupId();
						artifactId = mavenRemote.getArtifactId();
						version = mavenRemote.getVersion();
						packaging = mavenRemote.getPackaging();
						if (StringUtils.isNotEmpty(mavenRemote.getClassifier())) {
							classifier = mavenRemote.getClassifier();
						}
					} else if (productToInstall.getPackage().getMavenTIBCOArtifact() != null) {
						MavenTIBCOArtifactPackage mavenRemoteTIBCO = productToInstall.getPackage().getMavenTIBCOArtifact();
						groupId = mavenRemoteTIBCO.getGroupId();
						artifactId = mavenRemoteTIBCO.getArtifactId();
						version = mavenRemoteTIBCO.getVersion();
						packaging = mavenRemoteTIBCO.getPackaging();
						if (StringUtils.isNotEmpty(mavenRemoteTIBCO.getClassifier())) {
							classifier = mavenRemoteTIBCO.getClassifier();
						}
					}
					String coords = groupId + ":" + artifactId + (StringUtils.isNotEmpty(packaging) ? ":" + packaging : "") + (StringUtils.isNotEmpty(classifier) ? ":" + classifier : "") + ":" + version;
					org.eclipse.aether.artifact.Artifact artifact = new org.eclipse.aether.artifact.DefaultArtifact(coords);
					artifact = artifact.setFile(productToInstall.getResolvedInstallationPackage());

					getLog().info("Adding '" + coords + "' to standalone local repository directory");
					installArtifact(project, standaloneLocalRepository, artifact);
				} else {
					// copy product package in separate packages directory
					if (!standaloneLocalPackages.exists()) {
						standaloneLocalPackages.mkdirs();
					}
					try {
						getLog().info("Adding '" + productToInstall.getResolvedInstallationPackage() + "' to standalone local packages directory");

						FileUtils.copyFileToDirectory(productToInstall.getResolvedInstallationPackage(), new File(standaloneLocalPackages + "/" + productToInstall.getName()));
					} catch (IOException e) {
						throw new MojoExecutionException(e.getLocalizedMessage(), e);
					}
				}
			}
		}

		if (includeLocalTIBCOInstallationPackages) {
			getLog().info("");
			getLog().info(++elementIncludedIndex + ". Include " + messageIncludeLocalTIBCOInstallationPackages);

			super.execute();
			localTIBCOInstallationPackagesResolved = installPackagesToLocalRepository(standaloneLocalRepository);
		}

		if (includePlugins()) {
			getLog().info("");
			getLog().info(++elementIncludedIndex + ". Include " + messageIncludePlugins);
			getLog().info("");
			getLog().info("Following plugins (and their dependencies) will be included:");

			MavenProject goOfflineProject = generateGoOfflineProject();

			for (T3Plugins plugin : plugins) {
				getLog().info("-> " + plugin.getProductName());
			}
			getLog().info("");
			java.util.logging.Logger logTransferListener = java.util.logging.Logger.getLogger("org.jboss.shrinkwrap.resolver.impl.maven.logging.LogTransferListener");
			logTransferListener.setLevel(Level.OFF);
			getLog().info("This might take some minutes...");

			goOffline(goOfflineProject, standaloneLocalRepository, "3.3.9");
		}

		if (generateSettings) {
			getLog().info("");
			getLog().info(++elementIncludedIndex + ". Include " + messageGenerateSettings);

			generateOfflineSettings();
		}

		if (generateStandaloneTopology) {
			getLog().info("");
			getLog().info(++elementIncludedIndex + ". Include " + messageGenerateTopology);

			try {
				doGenerateStandaloneTopology();
			} catch (IOException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}

		if (generateStandaloneArchive) {
			getLog().info("");
			getLog().info(++elementIncludedIndex + ". Generate " + messageGenerateStandaloneArchive);
			getLog().info("");

			if (!standaloneDirectory.exists()) {
				getLog().warn("Standalone package directory '" + standaloneDirectory.getAbsolutePath() + "' does not exist. Skipping standalone archive generation.");
				return;
			}

			getLog().info("Copying standalone package directory '" + standaloneDirectory.getAbsolutePath() + "' to standalone archive '" + standaloneArchive.getAbsolutePath() + "'");
			try {
				standaloneArchive.delete();
				Utils.addFilesToZip(standaloneDirectory, standaloneArchive);
			} catch (IOException | ArchiveException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}

		getLog().info("");
		getLog().info("---");
		getLog().info("");

		getLog().info("Standalone package was generated in directory: '" + standaloneDirectory.getAbsolutePath() + "'");
		if (generateStandaloneArchive) {
			getLog().info("Standalone package archive is: '" + standaloneArchive.getAbsolutePath() + "'");
		}
	}

	protected List<File> getPOMsFromProject(MavenProject project, File tmpDirectory) throws MojoExecutionException {
        List<File> result = new ArrayList<File>();

        for (Plugin plugin : project.getModel().getBuild().getPlugins()) {
            Model model = project.getModel().clone();
            for (Iterator<Plugin> iterator = model.getBuild().getPlugins().iterator(); iterator.hasNext(); ) {
                Plugin p = iterator.next();
                if (!(p.getKey().equals(plugin.getKey()) && p.getVersion().equals(plugin.getVersion())) || (p.getExecutions().isEmpty())) {
                    iterator.remove();
                }
            }

            if (model.getBuild().getPlugins().isEmpty()) {
                continue;
            }

			for (PluginExecution pluginExecution : model.getBuild().getPlugins().get(0).getExecutions()) {
				try {
					Model m = model.clone();
					for (Iterator<PluginExecution> iterator = m.getBuild().getPlugins().get(0).getExecutions().iterator(); iterator.hasNext(); ) {
						PluginExecution pe = iterator.next();
						if (!pe.getId().equals(pluginExecution.getId())) {
							iterator.remove();
						}
					}

					File tmpPom = File.createTempFile("pom", ".xml", tmpDirectory);
					POMManager.writeModelToPOM(m, tmpPom);

					File baseDir = new File(tmpDirectory, plugin.getGroupId() + "/" + plugin.getArtifactId());
					if (!baseDir.exists()) {
						baseDir = tmpDirectory;
					} else {
						File overridePom = new File(baseDir, tmpPom.getName());
						FileUtils.copyFile(tmpPom, overridePom);
						tmpPom = overridePom;
					}

					result.add(tmpPom);
				} catch (IOException e) {
					throw new MojoExecutionException(e.getLocalizedMessage(), e);
				}
			}
        }
        return result;
    }

	private void doGenerateStandaloneTopology() throws IOException, MojoExecutionException {
		getLog().info("");
		if (topologyTemplateFile != null && topologyTemplateFile.exists()) {
			FileUtils.copyFile(topologyTemplateFile, standaloneTopologyGeneratedFile);
			getLog().info("Generating topology file '" + standaloneTopologyGeneratedFile + "'");
			getLog().info(" using topology template '" + topologyTemplateFile + "'");
		} else {
			// copy an empty topology to target file
			InputStream emptyEnvironments = EnvironmentInstallerMojo.class.getResourceAsStream("/xml/environments.xml");
			FileUtils.copyInputStreamToFile(emptyEnvironments, standaloneTopologyGeneratedFile);

			if (localTIBCOInstallationPackagesResolved) {
				getLog().info("Generating topology file for the locally resolved TIBCO installation packages...");
			} else {
				getLog().warn("No locally resolved TIBCO installation packages nor topology template file found. Topology file will be empty.");
			}
		}

		try {
			super.doGenerateStandaloneTopology(standaloneTopologyGeneratedFile, TopologyType.REMOTE, true);
		} catch (JAXBException | SAXException | MojoExecutionException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
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

	private void generateOfflineSettings() throws MojoExecutionException {
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

	protected void goOffline(MavenProject project, File localRepositoryPath, String mavenVersion) throws MojoExecutionException {
		localRepositoryPath.mkdirs();
		File tmpDirectory = Files.createTempDir();

		File providedSettingsFile = this.session.getRequest().getUserSettingsFile();
		if (providedSettingsFile == null || !providedSettingsFile.exists()) {
			providedSettingsFile = this.session.getRequest().getGlobalSettingsFile();
		}
		// create a settings.xml with <pluginGroups>
		File defaultSettingsFile = new File(tmpDirectory, "settings.xml");
		copyResourceToFile("/maven/default-t3-settings.xml", defaultSettingsFile);

		if (providedSettingsFile == null || !providedSettingsFile.exists()) {
			providedSettingsFile = defaultSettingsFile;
		}

		// create a maven-metadata-local.xml for Maven plugin group
		writeLocalMavenMetadata(localRepositoryPath, "org/apache/maven/plugins", "/maven/maven-plugins-maven-metadata.xml");

		// create a maven-metadata-local.xml for tic plugin group
		writeLocalMavenMetadata(localRepositoryPath, "io/teecube/tic", "/maven/tic-maven-metadata-local.xml");

		// create a maven-metadata-local.xml for toe plugin group
		writeLocalMavenMetadata(localRepositoryPath, "io/teecube/toe", "/maven/toe-maven-metadata-local.xml");

		System.setProperty(MavenSettingsBuilder.ALT_LOCAL_REPOSITORY_LOCATION, session.getLocalRepository().getBasedir().replace("\\", "/"));

		ConfigurableMavenResolverSystem mavenResolver = Maven.configureResolver();

		// actually install plugins (to generate their metadata in order to use them on the command line)
		List<Plugin> plugins = new ArrayList<Plugin>();

		// magic Plexus Utils v1.1 as defined in https://github.com/apache/maven/blob/master/maven-core/src/main/java/org/apache/maven/plugin/internal/PlexusUtilsInjector.java
//		MavenResolvedArtifact plexusUtils11 = mavenResolver.resolve("org.codehaus.plexus:plexus-utils:jar:1.1").withoutTransitivity().asSingle(MavenResolvedArtifact.class);
//		installArtifact(project, localRepositoryPath, getArtifactFromMavenResolvedArtifact(plexusUtils11));

		List<File> poms = new ArrayList<File>();

		for (Plugin plugin : project.getBuild().getPlugins()) {
			MavenResolvedArtifact mra = mavenResolver.resolve(plugin.getKey() + ":jar:" + plugin.getVersion()).withoutTransitivity().asSingle(MavenResolvedArtifact.class);
			if (plugin.getGroupId().startsWith("io.teecube")) {
				if (!mra.asFile().exists()) {
					getLog().warn("The plugin file does not exist.");
				} else {
					MavenResolvedArtifact[] pluginDependencies = mavenResolver.resolve(plugin.getKey() + ":jar:" + plugin.getVersion()).withTransitivity().as(MavenResolvedArtifact.class);
					try {
						addIndirectPlugins(mra, pluginDependencies, plugins, poms, tmpDirectory);
					} catch (IOException e) {
						throw new MojoExecutionException(e.getLocalizedMessage(), e);
					}
				}
			}
            org.eclipse.aether.artifact.Artifact artifact = getArtifactFromPlugin(plugin);
            artifact = artifact.setFile(mra.asFile());
            installArtifact(project, localRepositoryPath, artifact);
        }
		project.getBuild().getPlugins().addAll(plugins);

		// create one POM per plugin with an execution in project/model/build
		poms.addAll(getPOMsFromProject(project, tmpDirectory));

		MavenRunner mavenRunner = new MavenRunner();
		if (!ignoreDefaultSettings) {
			mavenRunner.setGlobalSettingsFile(defaultSettingsFile);
		}
		mavenRunner.setUserSettingsFile(providedSettingsFile);
		mavenRunner.setLocalRepositoryDirectory(localRepositoryPath);
		mavenRunner.setMavenVersion(mavenVersion);
		mavenRunner.setGoals("validate");
		mavenRunner.setFailAtEnd(true);
		mavenRunner.setIgnoreFailure(true);
		mavenRunner.setQuiet(true);

		for (File pom : poms) {
			mavenRunner.setPomFile(pom);
			BuiltProject result = mavenRunner.run();
			if (result == null || result.getMavenBuildExitCode() != 0) {
				File goOfflineDirectory = new File(directory, "go-offline");
				goOfflineDirectory.mkdirs();

				File logOutput = new File(goOfflineDirectory, "go-offline.log");
				try {
					FileUtils.writeStringToFile(logOutput, result.getMavenLog(), StandardCharsets.UTF_8);
				} catch (IOException e) {

				}

				String ignoreMessage = "";
				File ignoreMessageFile = new File(pom.getParentFile(), "ignore.message");
				if (ignoreMessageFile.exists()) {
					try {
						ignoreMessage = FileUtils.readFileToString(ignoreMessageFile);
					} catch (IOException e) {
						throw new MojoExecutionException(e.getLocalizedMessage(), e);
					}
				}
				if (result.getMavenLog().contains("Property groupId is missing.") || // this one for archetypes
					(StringUtils.isNotEmpty(ignoreMessage) && result.getMavenLog().contains(ignoreMessage))) {
					continue;
				}
				getLog().error("Something went wrong in Maven build to go offline. Log file is: '" + logOutput.getAbsolutePath() + "'");

				throw new MojoExecutionException("Unable to execute plugins goals to go offline.");
			}
		}
	}

	private void addIndirectPlugins(MavenResolvedArtifact initialPlugin, MavenResolvedArtifact[] pluginDependencies, List<Plugin> plugins, List<File> poms, File tmpDirectory) throws IOException, MojoExecutionException {
		BufferedReader pluginsConfigurationReader = getReaderOfPluginsConfiguration(initialPlugin.asFile());
		File plexusComponents = getPlexusComponents(initialPlugin.asFile(), tmpDirectory);

		try {
			List<String> lifecylePlugins = new ArrayList<String>();
			List<LifecyclesUtils.Lifecycle<LifecyclesUtils.Phase>> lifecyles = LifecyclesUtils.parse(plexusComponents, project, session);
			for (LifecyclesUtils.Lifecycle<LifecyclesUtils.Phase> lifecyle : lifecyles) {
				for (LifecyclesUtils.Phase phase : lifecyle.getPhases()) {
					for (String goal : phase.getGoals()) {
						Matcher matcher = indirectPluginPattern.matcher(goal);
						if (matcher.matches()) {
							String groupId = matcher.group(1);
							String artifactId = matcher.group(2);
							String pluginKey = groupId + ":" + artifactId;

							if (!goal.startsWith("io.teecube") && !lifecylePlugins.contains(pluginKey)) {
								lifecylePlugins.add(pluginKey);
								addIndirectPlugin(goal, initialPlugin, pluginDependencies, plugins, poms, tmpDirectory);
							}
						}
					}
				}
			}
		} catch (SAXException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

		if (pluginsConfigurationReader != null) {
			String line = null;
			while ((line = pluginsConfigurationReader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;

				addIndirectPlugin(line, initialPlugin, pluginDependencies, plugins, poms, tmpDirectory);
			}
		}
	}

	private boolean copyPluginsConfigurationDependency(File pluginJarFile, String pluginGroupId, String pluginArtifactId, File outputDirectory) throws MojoExecutionException {
		String directoryToCopy = "plugins-configuration/dependencies/" + pluginGroupId + "/" + pluginArtifactId + "/";
		return ZipUtils.extractDirectoryFromZip(pluginJarFile, directoryToCopy, outputDirectory);
	}

	private BufferedReader getReaderOfPluginsConfiguration(File pluginJarFile) throws MojoExecutionException {
		String fileName = "plugins-configuration/dependencies/plugins.list";

		return getReaderOfPluginsConfiguration(pluginJarFile, fileName);
	}

	private File getPlexusComponents(File pluginJarFile, File outputDirectory) throws MojoExecutionException {
		String fileName = "META-INF/plexus/components.xml";

		return ZipUtils.extractFileFromZip(pluginJarFile, fileName, outputDirectory);
	}

	private BufferedReader getReaderOfPluginsConfiguration(File pluginJarFile, String fileName) throws MojoExecutionException {
		ZipInputStream zipStream = null;
		try {
			zipStream = new ZipInputStream(new FileInputStream(pluginJarFile));
			ZipEntry entry = null;
			byte[] buffer = new byte[2048];

			while ((entry = zipStream.getNextEntry()) != null ) {
				if (fileName.equals(entry.getName())) {
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					int len;
					while ((len = zipStream.read(buffer)) > 0) {
						byteArrayOutputStream.write(buffer, 0, len);
					}

					String pluginsList = byteArrayOutputStream.toString("UTF8");
					return new BufferedReader(new StringReader(pluginsList));
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

		return null;
	}

	private final static String indirectPluginRegex = "([^:]*):([^:]*):?([^:]*):?([^:]*)";
	private final static Pattern indirectPluginPattern = Pattern.compile(indirectPluginRegex);

	private void addIndirectPlugin(String pluginCoordinate, MavenResolvedArtifact initialPlugin, MavenResolvedArtifact[] pluginDependencies, List<Plugin> plugins, List<File> poms, File tmpDirectory) throws IOException, MojoExecutionException {
		Matcher matcher = indirectPluginPattern.matcher(pluginCoordinate);
		if (!matcher.matches()) {
			return;
		}

		String groupId = matcher.group(1);
		String artifactId = matcher.group(2);
		String version = StringUtils.isNotEmpty(matcher.group(3)) ? matcher.group(3) : null;
		String goal = StringUtils.isNotEmpty(matcher.group(4)) ? matcher.group(4) : "help";
		if (groupId.equals("org.apache.maven.plugins")) {
			goal = "help";
		}

		boolean found = false;
		Plugin plugin = null;
		File baseDirectory = new File(tmpDirectory.getAbsolutePath() + "/plugins/" + groupId + "/" + artifactId);

		if (copyPluginsConfigurationDependency(initialPlugin.asFile(), groupId, artifactId, baseDirectory)) {
			File customPom = new File(baseDirectory, "pom.xml");
			Utils.replaceByLine(customPom, "\\$\\{plugin.version\\}", version, true, sourceEncoding);
			if (!customPom.exists()) {
				getLog().warn("Plugin '" + pluginCoordinate + "' has no custom POM.");
			} else {
				found = true;
				poms.add(customPom);
			}
		}// else {
			for (MavenResolvedArtifact pluginDependency : pluginDependencies) {
				if (pluginDependency.getCoordinate().getArtifactId().equals(artifactId) && pluginDependency.getCoordinate().getGroupId().equals(groupId)) {
					plugin = getMavenPlugin(groupId, artifactId, pluginDependency.getCoordinate().getVersion(), goal);
					if (plugin != null) {
						found = true;
						plugins.add(plugin);
					}
				}
			}
//		}
		if (!found) {
			getLog().warn("Plugin '" + pluginCoordinate + "' was not found.");
		}
	}

	private boolean includeInstallationPackagesFromTopology() {
		return (includeTopologyInstallationPackages && topologyTemplateFile != null && topologyTemplateFile.exists());
	}

	private boolean includePlugins() {
		return includePluginsInStandalone && !plugins.isEmpty();
	}

	private void installArtifact(MavenProject project, File localRepositoryPath, org.eclipse.aether.artifact.Artifact artifact) throws MojoExecutionException {
		boolean installPomSeparately = false;
		List<Element> configuration = new ArrayList<Element>();

		if (artifact.getArtifactId().equals("velocity") && artifact.getVersion().equals("1.5")) {
			configuration.add(new Element("generatePom", "true"));
			configuration.add(new Element("packaging", "jar"));
			installPomSeparately = true;
		}

		configuration.add(new Element("localRepositoryPath", localRepositoryPath.getAbsolutePath()));
		configuration.add(new Element("createChecksum", "true"));
		configuration.add(new Element("updateReleaseInfo", "true"));
		configuration.add(new Element("groupId", artifact.getGroupId()));
		configuration.add(new Element("artifactId", artifact.getArtifactId()));
		configuration.add(new Element("version", artifact.getVersion()));
		configuration.add(new Element("file", artifact.getFile().getAbsolutePath()));
		File pomFile = new File(artifact.getFile().getParentFile(), artifact.getArtifactId() + "-" + artifact.getVersion() + ".pom");
		if (StringUtils.isNotEmpty(artifact.getExtension())) {
			configuration.add(new Element("packaging", artifact.getExtension()));
		} else {
			configuration.add(new Element("packaging", "jar"));
		}
		if (StringUtils.isNotEmpty(artifact.getClassifier())) {
			configuration.add(new Element("classifier", artifact.getClassifier()));
			configuration.add(new Element("generatePom", "true"));
			installPomSeparately = true;
		} else if (!installPomSeparately) {
			if (!pomFile.exists()) return;
			configuration.add(new Element("pomFile", pomFile.getAbsolutePath()));
		}

		executeMojo(
			plugin(
				groupId("org.apache.maven.plugins"),
				artifactId(mavenPluginInstallArtifactId),
				version(mavenPluginInstallVersion) // version defined in pom.xml of this plugin
			),
			goal("install-file"),
			configuration(
				configuration.toArray(new Element[0])
			),
			getEnvironment(project, session, pluginManager),
			true
		);

		File artifactDirectory = new File(localRepositoryPath, artifact.getGroupId().replace(".", "/") + "/" + artifact.getArtifactId() + "/" + artifact.getVersion());
		Collection<File> bundleFiles = FileUtils.listFiles(artifactDirectory, new String[]{"bundle"}, false);
		if (!bundleFiles.isEmpty()) {
			for (File bundleFile : bundleFiles) {
				String filenameNoExt = FilenameUtils.removeExtension(bundleFile.getAbsolutePath());
				bundleFile.renameTo(new File(filenameNoExt + ".jar"));
				File md5File = new File(filenameNoExt + ".bundle.md5");
				File sha1File = new File(filenameNoExt + ".bundle.sha1");
				md5File.renameTo(new File(filenameNoExt + ".jar.md5"));
				sha1File.renameTo(new File(filenameNoExt + ".jar.sha1"));
			}
		}
		Collection<File> archetypeFiles = FileUtils.listFiles(artifactDirectory, new String[]{"maven-archetype"}, false);
		if (!archetypeFiles.isEmpty()) {
			for (File archetypeFile : archetypeFiles) {
				String filenameNoExt = FilenameUtils.removeExtension(archetypeFile.getAbsolutePath());
				archetypeFile.renameTo(new File(filenameNoExt + ".jar"));
				File md5File = new File(filenameNoExt + ".maven-archetype.md5");
				File sha1File = new File(filenameNoExt + ".maven-archetype.sha1");
				md5File.renameTo(new File(filenameNoExt + ".jar.md5"));
				sha1File.renameTo(new File(filenameNoExt + ".jar.sha1"));
			}
		}

		if (installPomSeparately) {
			configuration.clear();

			configuration.add(new Element("localRepositoryPath", localRepositoryPath.getAbsolutePath()));
			configuration.add(new Element("createChecksum", "true"));
			configuration.add(new Element("updateReleaseInfo", "true"));
			configuration.add(new Element("groupId", artifact.getGroupId()));
			configuration.add(new Element("artifactId", artifact.getArtifactId()));
			configuration.add(new Element("version", artifact.getVersion()));
			configuration.add(new Element("file", pomFile.getAbsolutePath()));
			configuration.add(new Element("packaging", "pom"));

			executeMojo(
				plugin(
					groupId("org.apache.maven.plugins"),
					artifactId(mavenPluginInstallArtifactId),
					version(mavenPluginInstallVersion) // version defined in pom.xml of this plugin
				),
				goal("install-file"),
				configuration(
					configuration.toArray(new Element[0])
				),
				executionEnvironment(project, session, pluginManager),
				true
			);
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
			ArtifactVersion lowerBound = new DefaultArtifactVersion("0.1.0"); // versions < 0.1.0 do not have a help goal and are not supported
			ArtifactVersion upperBound = new DefaultArtifactVersion("1000.0.0"); // upper bound : we have time
			ArtifactVersion newest = artifactVersions.getNewestVersion(lowerBound, upperBound);
			if (newest != null) {
				getLog().debug("Newest version for " + pluginArtifact.getGroupId() + ":" + pluginArtifact.getArtifactId() + " is " + newest.toString());
				version = newest.toString();			
			} else {
				throw new MojoExecutionException("Unable to find a suitable version", new PluginVersionNotFoundException(groupId, artifactId));
			}
		}

		pluginArtifact.setVersion(version);
		return pluginArtifact;
	}

	private org.eclipse.aether.artifact.Artifact getArtifactFromMavenResolvedArtifact(MavenResolvedArtifact mavenResolvedArtifact) {
		String coords = mavenResolvedArtifact.getCoordinate().getGroupId() + ":" +
						mavenResolvedArtifact.getCoordinate().getArtifactId() + ":" +
						mavenResolvedArtifact.getExtension() + ":" +
						mavenResolvedArtifact.getResolvedVersion();
		org.eclipse.aether.artifact.Artifact artifact = new org.eclipse.aether.artifact.DefaultArtifact(coords);
		File resolvedFile = mavenResolvedArtifact.asFile();
		if (resolvedFile != null) {
			artifact = artifact.setFile(resolvedFile);
		}

		return artifact;
	}

	private org.eclipse.aether.artifact.Artifact getArtifactFromPlugin(Plugin plugin) {
		String coords = plugin.getGroupId() + ":" + plugin.getArtifactId() + ":maven-plugin:" + plugin.getVersion();
		org.eclipse.aether.artifact.Artifact artifact = new org.eclipse.aether.artifact.DefaultArtifact(coords);

		return artifact;
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

	private Plugin getTeecubePlugin(DefaultVersionsHelper helper, String groupId, String artifactId, String version) throws MojoExecutionException {
		Artifact artifact = getArtifact(helper, groupId, artifactId, version, "maven-plugin");

		Plugin plugin = getTeecubePlugin(artifact);
		plugin = addGoal(plugin, artifactId, "help");

		return plugin;
	}

	private Plugin getTeecubePlugin(Artifact artifact) {
		return getPluginFromArtifact(artifact);
	}

	private Plugin addGoal(Plugin plugin, String id, String goal) {
		PluginExecution pluginExecution = new PluginExecution();
		pluginExecution.setId(id);
		pluginExecution.setPhase("validate");

		List<String> goals = new ArrayList<String>();
		goals.add(goal);
		pluginExecution.setGoals(goals);

		plugin.addExecution(pluginExecution);

		return plugin;
	}

	private Plugin getToeDomainsPlugin(DefaultVersionsHelper helper, String version) throws MojoExecutionException {
		return getTeecubePlugin(helper, "io.teecube.toe", "toe-domains-plugin", version);
	}

	private Plugin getToeInstallerPlugin(DefaultVersionsHelper helper, String version) throws MojoExecutionException {
		return getTeecubePlugin(helper, InstallerLifecycleParticipant.pluginGroupId, InstallerLifecycleParticipant.pluginArtifactId, version);
	}

	private Plugin getTicBW5Plugin(DefaultVersionsHelper helper, String version) throws MojoExecutionException {
		return getTeecubePlugin(helper, "io.teecube.tic", "tic-bw5", version);
	}
	
	private Plugin getTicBW6Plugin(DefaultVersionsHelper helper, String version) throws MojoExecutionException {
		return getTeecubePlugin(helper, "io.teecube.tic", "tic-bw6", version);
	}

	private Plugin getMavenPlugin(String groupId, String artifactId, String version, String goal) {
		Plugin plugin = new Plugin();
		plugin.setGroupId(groupId);
		plugin.setArtifactId(artifactId);
		plugin.setVersion(version);

		plugin = addGoal(plugin, artifactId, goal);

		return plugin;
	}

	private Plugin getMavenPlugin(String artifactId, String version) {
		return getMavenPlugin("org.apache.maven.plugins", artifactId, version, "help");
	}

	private Plugin getTacArchetypes(DefaultVersionsHelper helper, String tacArchetypesVersion) throws MojoExecutionException {
		// generate one execution of maven-archetype-plugin per achetype
		Plugin plugin = new Plugin();
		plugin.setGroupId("org.apache.maven.plugins");
		plugin.setArtifactId("maven-archetype-plugin");
		plugin.setVersion("3.0.1");

		List<PluginExecution> executions = new ArrayList<PluginExecution>();
		List<String> generateGoal = new ArrayList<String>();
		generateGoal.add("generate");

		String[] artifactsId = tacArchetypesArtifactsId.split(",");
		for (int i = 0; i < artifactsId.length; i++) {
			String currentArtifactId = artifactsId[i];

			PluginExecution execution = new PluginExecution();
			execution.setId(currentArtifactId);
			execution.setPhase("validate");
			execution.setGoals(generateGoal);

			Xpp3Dom configuration = new Xpp3Dom("configuration");
			Xpp3Dom archetypeGroupId = new Xpp3Dom("archetypeGroupId");
			archetypeGroupId.setValue("io.teecube.tac.archetypes");
			Xpp3Dom archetypeArtifactId = new Xpp3Dom("archetypeArtifactId");
			archetypeArtifactId.setValue(currentArtifactId);
			Xpp3Dom interactiveMode = new Xpp3Dom("interactiveMode");
			interactiveMode.setValue("false");

			configuration.addChild(archetypeGroupId);
			configuration.addChild(archetypeArtifactId);
			configuration.addChild(interactiveMode);

			execution.setConfiguration(configuration);

			executions.add(execution);
		}

		plugin.setExecutions(executions);

		return plugin;
	}

	private List<Plugin> getPluginArtifacts() throws MojoExecutionException {
		List<Plugin> result = new ArrayList<Plugin>();

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
			result.add(getTacArchetypes(helper, tacArchetypesVersion)); // this include maven-archetype-plugin
		} else {
			// required to create project from Maven archetypes
			result.add(getMavenPlugin("maven-archetype-plugin", "3.0.1"));
		}

		// Maven plugins from Super POM (from Maven 3.3.3 to Maven 3.5.3)
		result.add(getMavenPlugin("maven-antrun-plugin", "1.3"));
		result.add(getMavenPlugin("maven-assembly-plugin", "2.2-beta-5"));
		result.add(getMavenPlugin("maven-clean-plugin", "2.5"));
		result.add(getMavenPlugin("maven-dependency-plugin", "2.8"));
		result.add(getMavenPlugin("maven-deploy-plugin", "2.7"));
		result.add(getMavenPlugin("maven-install-plugin", "2.4"));
		result.add(getMavenPlugin("maven-release-plugin", "2.3.2"));
		result.add(getMavenPlugin("maven-release-plugin", "2.5.3"));
		result.add(getMavenPlugin("maven-site-plugin", "3.3"));

		return result;
	}

	//<editor-fold desc="Components and parameters for DefaultVersionsHelper">
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
	//</editor-fold>

}
