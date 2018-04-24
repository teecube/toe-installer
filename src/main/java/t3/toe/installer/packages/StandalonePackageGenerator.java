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
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.DefaultSettingsWriter;
import org.codehaus.mojo.versions.api.ArtifactVersions;
import org.codehaus.mojo.versions.api.DefaultVersionsHelper;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.impl.maven.bootstrap.MavenSettingsBuilder;
import org.xml.sax.SAXException;
import t3.Messages;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.*;
import t3.toe.installer.environments.products.ProductToInstall;
import t3.toe.installer.environments.products.ProductsToInstall;
import t3.utils.POMManager;
import t3.utils.Utils;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

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

	@Parameter (property = InstallerMojosInformation.Packages.Standalone.includeTopologyTIBCOInstallationPackages, defaultValue = InstallerMojosInformation.Packages.Standalone.includeTopologyTIBCOInstallationPackages_default)
	protected Boolean includeTopologyTIBCOInstallationPackages;

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

	@Override
	protected Boolean getGenerateTopology() throws MojoExecutionException {
		return false;
	}

	@Override
	protected File getTopologyGeneratedFile() {
		return standaloneTopologyGeneratedFile;
	}

	private static final String messageIncludeTIBCOInstallationPackagesFromTopology  = "TIBCO and custom installation packages from topology file";
	private static final String messageIncludeLocalTIBCOInstallationPackages  = "TIBCO installation packages resolved locally";
	private static final String messageIncludePlugins  = "Maven plugins";
	private static final String messageGenerateSettings  = "Maven settings.xml to use included Maven plugins";
	private static final String messageGenerateTopology  = "environments topology file";
	private static final String messageGenerateStandaloneArchive  = "a standalone archive wrapping all elements above";

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Creating a standalone package in directory '" + standaloneDirectory.getAbsolutePath() + "'");
		if (generateStandaloneArchive) {
			getLog().info("Created standalone package will be archived to '" + standaloneArchive.getAbsolutePath() + "'");
		}
		getLog().info("");

		getLog().info("List of included elements:");
		int elementIncludedIndex = 0;
		if (includeTIBCOInstallationPackagesFromTopology()) {
			getLog().info(++elementIncludedIndex + ". " + messageIncludeTIBCOInstallationPackagesFromTopology);
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

		if (includeTIBCOInstallationPackagesFromTopology()) {
			getLog().info("");
			getLog().info(++elementIncludedIndex + ". Include " + messageIncludeTIBCOInstallationPackagesFromTopology);
			getLog().info("");

			getLog().info("Using topology file '" + topologyTemplateFile.getAbsolutePath() + "'");
			getLog().info("");

			EnvironmentsMarshaller environmentsMarshaller = EnvironmentsMarshaller.getEnvironmentMarshaller(topologyTemplateFile);
			EnvironmentsToInstall environmentsToInstall = new EnvironmentsToInstall(environmentsMarshaller.getObject().getEnvironment(), topologyTemplateFile);

			List<ProductToInstall<?>> uniqueProductsList = new ArrayList<ProductToInstall<?>>();

			boolean atLeastOneMavenArtifactResolved = false;
			for (EnvironmentToInstall environment : environmentsToInstall) {
				ProductsToInstall productsToInstall = new ProductsToInstall(environment, this);

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
				if (productToInstall.getPackage().getMavenRemote() != null || productToInstall.getPackage().getMavenRemoteTIBCO() != null) {
					// product package was defined as a Maven artifact in topology template, hence deploy this artifact in standalone Maven repository
					String groupId = "";
					String artifactId = "";
					String version = "";
					String packaging = "";
					String classifier = "";
					if (productToInstall.getPackage().getMavenRemote() != null) {
						MavenArtifactPackage mavenRemote = productToInstall.getPackage().getMavenRemote();
						groupId = mavenRemote.getGroupId();
						artifactId = mavenRemote.getArtifactId();
						version = mavenRemote.getVersion();
						packaging = mavenRemote.getPackaging();
						if (StringUtils.isNotEmpty(mavenRemote.getClassifier())) {
							classifier = mavenRemote.getClassifier();
						}
					} else if (productToInstall.getPackage().getMavenRemoteTIBCO() != null) {
						MavenTIBCOArtifactPackage mavenRemoteTIBCO = productToInstall.getPackage().getMavenRemoteTIBCO();
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

						FileUtils.copyFileToDirectory(productToInstall.getResolvedInstallationPackage(), standaloneLocalPackages);
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
	}

	protected List<Map.Entry<File, List<String>>> getPOMsFromProject(MavenProject project, File tmpDirectory) throws MojoExecutionException {
        List<Map.Entry<File, List<String>>> result = new ArrayList<Map.Entry<File, List<String>>>();

        for (Plugin plugin : project.getModel().getBuild().getPlugins()) {
            Model model = project.getModel().clone();
            for (Iterator<Plugin> iterator = model.getBuild().getPlugins().iterator(); iterator.hasNext(); ) {
                Plugin p = iterator.next();
                if (!p.getKey().equals(plugin.getKey()) || (p.getExecutions().isEmpty())) {
                    iterator.remove();
                }
            }

            if (model.getBuild().getPlugins().isEmpty()) {
                continue;
            }
            try {
                File tmpPom = File.createTempFile("pom", ".xml", tmpDirectory);
                POMManager.writeModelToPOM(model, tmpPom);
                List<String> goals = new ArrayList<String>();
                for (String goal : model.getBuild().getPlugins().get(0).getExecutions().get(0).getGoals()) {
                    goals.add(model.getBuild().getPlugins().get(0).getExecutions().get(0).getId() + ":" + goal);
                }
                Map.Entry<File, List<String>> entry = new AbstractMap.SimpleEntry<File, List<String>>(tmpPom, goals);
                result.add(entry);
            } catch (IOException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

	private boolean includeTIBCOInstallationPackagesFromTopology() {
		return (includeTopologyTIBCOInstallationPackages && topologyTemplateFile != null && topologyTemplateFile.exists());
	}

	private boolean includePlugins() {
		return includePluginsInStandalone && !plugins.isEmpty();
	}

	protected void goOffline(MavenProject project, File localRepositoryPath, String mavenVersion) throws MojoExecutionException {
		localRepositoryPath.mkdirs();
		File tmpDirectory = Files.createTempDir();

		File userSettingsFile = this.session.getRequest().getUserSettingsFile();
		if (userSettingsFile == null || !userSettingsFile.exists()) {
			userSettingsFile = this.session.getRequest().getGlobalSettingsFile();
		}
		// create a settings.xml with <pluginGroups>
		File globalSettingsFile = new File(tmpDirectory, "settings.xml");
		copyResourceToFile("/maven/default-t3-settings.xml", globalSettingsFile);

		if (userSettingsFile == null || !userSettingsFile.exists()) {
			userSettingsFile = globalSettingsFile;
		}

		// create a maven-metadata-local.xml for Maven plugin group
		writeLocalMavenMetadata(localRepositoryPath, "org/apache/maven/plugins", "/maven/maven-plugins-maven-metadata.xml");

		// create a maven-metadata-local.xml for tic plugin group
		writeLocalMavenMetadata(localRepositoryPath, "io/teecube/tic", "/maven/tic-maven-metadata-local.xml");

		// create a maven-metadata-local.xml for toe plugin group
		writeLocalMavenMetadata(localRepositoryPath, "io/teecube/toe", "/maven/toe-maven-metadata-local.xml");

		System.setProperty(MavenSettingsBuilder.ALT_LOCAL_REPOSITORY_LOCATION, session.getLocalRepository().getBasedir().replace("\\", "/"));

        ConfigurableMavenResolverSystem mavenResolver = Maven.configureResolver();

        for (Plugin plugin : project.getBuild().getPlugins()) {
            MavenResolvedArtifact mra = mavenResolver.resolve(plugin.getKey() + ":jar:" + plugin.getVersion()).withoutTransitivity().asSingle(MavenResolvedArtifact.class);
			org.eclipse.aether.artifact.Artifact artifact = getArtifactFromPlugin(plugin);
			artifact = artifact.setFile(mra.asFile());
			installArtifact(project, localRepositoryPath, artifact);
		}

		// create one POM per plugin with an execution in project/model/build
		List<Map.Entry<File, List<String>>> pomsWithGoal = getPOMsFromProject(project, tmpDirectory);

		PrintStream oldSystemErr = System.err;
		PrintStream oldSystemOut = System.out;
		try {
			silentSystemStreams();

			for (Map.Entry<File, List<String>> pomWithGoals : pomsWithGoal) {

				BuiltProject result = executeGoal(pomWithGoals.getKey(), globalSettingsFile, userSettingsFile, localRepositoryPath, mavenVersion, pomWithGoals.getValue());
				if (result == null || result.getMavenBuildExitCode() != 0) {
					File goOfflineDirectory = new File(directory, "go-offline");
					goOfflineDirectory.mkdirs();

					File logOutput = new File(goOfflineDirectory, "go-offline.log");
					try {
						FileUtils.writeStringToFile(logOutput, result.getMavenLog(), StandardCharsets.UTF_8);
					} catch (IOException e) {

					}

					if (result.getMavenLog().contains("[ERROR] " + Messages.ENFORCER_RULES_FAILURE) ||
							result.getMavenLog().contains("Nothing to merge.") ||
							result.getMavenLog().contains("Unable to load topology from file")) {
						continue;
					}
					getLog().error("Something went wrong in Maven build to go offline. Log file is: '" + logOutput.getAbsolutePath() + "'");

					throw new MojoExecutionException("Unable to execute plugins goals to go offline.");
				}
			}
		} finally {
			System.setErr(oldSystemErr);
			System.setOut(oldSystemOut);
		}
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
		if (StringUtils.isNotEmpty(artifact.getClassifier())) {
			configuration.add(new Element("classifier", artifact.getClassifier()));
			configuration.add(new Element("generatePom", "true"));
			configuration.add(new Element("packaging", "jar"));
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
			ArtifactVersion lowerBound = new DefaultArtifactVersion("0.0.0");
			ArtifactVersion upperBound = new DefaultArtifactVersion("1000.0.0"); // upper bound : we have time
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

		plugin = addHelpGoal(plugin, "deploy");

		return plugin;
	}

	private Plugin getMavenInstallPlugin() {
		Plugin plugin = new Plugin();
		plugin.setGroupId("org.apache.maven.plugins");
		plugin.setArtifactId("maven-install-plugin");
		plugin.setVersion("2.5.2"); // must be in sync with POM !

		plugin = addHelpGoal(plugin, "install");

		return plugin;
	}

	private Plugin getMavenDependencyPlugin() {
		Plugin plugin = new Plugin();
		plugin.setGroupId("org.apache.maven.plugins");
		plugin.setArtifactId("maven-dependency-plugin");
		plugin.setVersion("3.0.2"); // must be in sync with POM !

		plugin = addHelpGoal(plugin, "dependency");

		return plugin;
	}

	private Plugin getMavenReleasePlugin() {
		Plugin plugin = new Plugin();
		plugin.setGroupId("org.apache.maven.plugins");
		plugin.setArtifactId("maven-release-plugin");
		plugin.setVersion("2.5.3"); // must be in sync with POM !

		plugin = addHelpGoal(plugin, "release");

		return plugin;
	}

	private Plugin getMavenSuperPOMPlugin(String name, String version) {
		Plugin plugin = new Plugin();
		plugin.setGroupId("org.apache.maven.plugins");
		plugin.setArtifactId(name);
		plugin.setVersion(version);

		plugin = addHelpGoal(plugin, name);

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

		return getPlugin(artifact, groupId, artifactId, version, prefix, goals);
	}

	private Plugin getPlugin(Artifact artifact, String groupId, String artifactId, String version, String prefix, List<String> goals) throws MojoExecutionException {
		Plugin result = getPluginFromArtifact(artifact);

		PluginExecution pluginExecution = new PluginExecution();
		pluginExecution.setId(prefix);
		pluginExecution.setPhase("validate");
		pluginExecution.setGoals(goals);

		result.addExecution(pluginExecution);

		return result;
	}

	private Plugin addHelpGoal(Plugin plugin, String id) {
		PluginExecution pluginExecution = new PluginExecution();
		pluginExecution.setId(id);
		pluginExecution.setPhase("validate");

		List<String> goals = new ArrayList<String>();
		goals.add("help");
		pluginExecution.setGoals(goals);

		plugin.addExecution(pluginExecution);

		return plugin;
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
			result.addAll(getTacArchetypes(helper, tacArchetypesVersion));
		}
		result.add(getMavenDeployPlugin());
		result.add(getMavenInstallPlugin());
		result.add(getMavenDependencyPlugin());
		result.add(getMavenReleasePlugin());

		result.add(getMavenSuperPOMPlugin("maven-assembly-plugin", "2.2-beta-5"));
		result.add(getMavenSuperPOMPlugin("maven-clean-plugin", "2.5"));
		result.add(getMavenSuperPOMPlugin("maven-install-plugin", "2.4"));
		result.add(getMavenSuperPOMPlugin("maven-deploy-plugin", "2.7"));
		result.add(getMavenSuperPOMPlugin("maven-site-plugin", "3.3"));
		result.add(getMavenSuperPOMPlugin("maven-antrun-plugin", "1.3"));
		result.add(getMavenSuperPOMPlugin("maven-dependency-plugin", "2.8"));

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

	private void doGenerateStandaloneTopology() throws IOException, MojoExecutionException {
		getLog().info("");
		if (topologyTemplateFile != null && topologyTemplateFile.exists()) {
			FileUtils.copyFile(topologyTemplateFile, standaloneTopologyGeneratedFile);
			getLog().info("Generating topology file using topology template '" + topologyTemplateFile + "'");
		} else if (localTIBCOInstallationPackagesResolved) {
			getLog().info("Generating topology file for the locally resolved TIBCO installation packages...");
			// copy an empty topology to target file
			InputStream emptyEnvironments = EnvironmentInstallerMojo.class.getResourceAsStream("/xml/environments.xml");
			FileUtils.copyInputStreamToFile(emptyEnvironments, standaloneTopologyGeneratedFile);
		} else {
			getLog().warn("No locally resolved TIBCO installation packages nor topology template file found. Topology file will be empty.");
		}

		try {
			super.doGenerateStandaloneTopology(standaloneTopologyGeneratedFile, TopologyType.REMOTE, true);
		} catch (JAXBException | SAXException | MojoExecutionException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	protected void doExecute() throws MojoExecutionException {

	}


	@Override
	protected void generateTopology() throws MojoExecutionException {
		super.generateTopology();
	}
}
