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
import org.eclipse.aether.resolution.ArtifactResult;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.impl.maven.bootstrap.MavenSettingsBuilder;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import t3.Messages;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.EnvironmentToInstall;
import t3.toe.installer.environments.EnvironmentsMarshaller;
import t3.toe.installer.environments.products.ProductToInstall;
import t3.toe.installer.environments.products.ProductsToInstall;
import t3.utils.POMManager;
import t3.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

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
		getLog().info("Creating a standalone Maven repository in '" + standaloneLocalRepository.getAbsolutePath() + "'");

		MavenProject goOfflineProject = generateGoOfflineProject();

		if (includeTopologyTIBCOInstallationPackages && topologyTemplateFile != null && topologyTemplateFile.exists()) {
			getLog().info("");
			getLog().info("Include packages from topology '" + topologyTemplateFile.getAbsolutePath() + "'");
			getLog().info("");

			EnvironmentsMarshaller environmentsMarshaller = EnvironmentsMarshaller.getEnvironmentMarshaller(topologyTemplateFile);
			List<EnvironmentToInstall> environmentsToInstall = EnvironmentToInstall.getEnvironmentsToInstall(environmentsMarshaller.getObject().getEnvironment(), topologyTemplateFile);

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
				getLog().info(productToInstall.getResolvedInstallationPackage().getAbsolutePath());
			}
		}

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

	protected void goOffline(MavenProject project, File localRepositoryPath, String mavenVersion) throws MojoExecutionException {
		localRepositoryPath.mkdirs();
		File tmpDirectory = Files.createTempDir();

		// create a settings.xml with <pluginGroups>
		File globalSettingsFile = new File(tmpDirectory, "settings.xml");
		copyResourceToFile("/maven/default-t3-settings.xml", globalSettingsFile);

		// create a maven-metadata-local.xml for Maven plugin group
		writeLocalMavenMetadata(localRepositoryPath, "org/apache/maven/plugins", "/maven/maven-plugins-maven-metadata.xml");

		// create a maven-metadata-local.xml for tic plugin group
		writeLocalMavenMetadata(localRepositoryPath, "io/teecube/tic", "/maven/tic-maven-metadata-local.xml");

		// create a maven-metadata-local.xml for toe plugin group
		writeLocalMavenMetadata(localRepositoryPath, "io/teecube/toe", "/maven/toe-maven-metadata-local.xml");

		List<MavenResolvedArtifact> mavenResolvedArtifacts = new ArrayList<MavenResolvedArtifact>();

		System.setProperty(MavenSettingsBuilder.ALT_LOCAL_REPOSITORY_LOCATION, session.getLocalRepository().getBasedir().replace("\\", "/"));

		ConfigurableMavenResolverSystem mavenResolver = Maven.configureResolver();

		List<ArtifactResult> poms = new ArrayList<ArtifactResult>();
		poms.addAll(getPomArtifact("org.apache:apache:pom:4"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:6"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:9"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:10"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:11"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:13"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:17"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:18"));
		poms.addAll(getPomArtifact("org.apache.ant:ant-parent:pom:1.8.1"));
		poms.addAll(getPomArtifact("org.apache.commons:commons-parent:pom:24"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:9"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:15"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:21"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:22"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:23"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:27"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:30"));
		poms.addAll(getPomArtifact("org.apache.maven.plugins:maven-plugins:pom:12"));
		poms.addAll(getPomArtifact("org.apache.maven.plugins:maven-plugins:pom:16"));
		poms.addAll(getPomArtifact("org.apache.maven.plugins:maven-plugins:pom:22"));
		poms.addAll(getPomArtifact("org.apache.maven.plugins:maven-plugins:pom:23"));
		poms.addAll(getPomArtifact("org.apache.maven.plugins:maven-plugins:pom:24"));
		poms.addAll(getPomArtifact("org.apache.maven.archetype:maven-archetype:pom:3.0.1"));
		poms.addAll(getPomArtifact("org.apache.maven.archetype:archetype-models:pom:3.0.1"));
		poms.addAll(getPomArtifact("org.codehaus.plexus:plexus-components:pom:1.1.15"));
		poms.addAll(getPomArtifact("org.codehaus.plexus:plexus-components:pom:1.1.18"));
		poms.addAll(getPomArtifact("org.sonatype.aether:aether-parent:pom:1.7"));
		poms.addAll(getPomArtifact("asm:asm-parent:pom:3.2"));
		poms.addAll(getPomArtifact("org.slf4j:slf4j-parent:pom:1.7.5"));
		poms.addAll(getPomArtifact("org.slf4j:slf4j-parent:pom:1.7.24"));
		poms.addAll(getPomArtifact("org.eclipse.tycho:tycho:pom:0.22.0"));
		poms.addAll(getPomArtifact("org.eclipse.tycho:sisu-equinox:pom:0.22.0"));
		poms.addAll(getPomArtifact("org.eclipse.tycho:tycho-bundles:pom:0.22.0"));
		poms.addAll(getPomArtifact("org.eclipse.tycho:tycho-p2:pom:0.22.0"));
		poms.addAll(getPomArtifact("org.apache.maven.shared:maven-shared-components:pom:22"));
		poms.addAll(getPomArtifact("org.apache.maven.release:maven-release:pom:2.3.2"));

		// plugins from super POM
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-antrun-plugin:jar:1.3").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-assembly-plugin:jar:2.2-beta-5").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-clean-plugin:jar:2.5").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-dependency-plugin:jar:2.8").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-deploy-plugin:jar:2.7").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-install-plugin:jar:2.4").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-release-plugin:jar:2.3.2").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-site-plugin:jar:3.3").withoutTransitivity().asList(MavenResolvedArtifact.class));

		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-archetype-plugin:jar:3.0.1").withTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-enforcer-plugin:jar:1.3.1").withTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.codehaus.plexus:plexus-component-annotations:jar:1.6").withTransitivity().asList(MavenResolvedArtifact.class));

//        org.jboss.shrinkwrap.resolver.impl.maven.logging.LogTransferListener
		// add plugins from project
		for (Plugin plugin : project.getBuild().getPlugins()) {
			mavenResolvedArtifacts.addAll(mavenResolver.resolve(plugin.getKey() + ":jar:" + plugin.getVersion()).withTransitivity().asList(MavenResolvedArtifact.class));
		}

		// add all as artifacts
		List<org.eclipse.aether.artifact.Artifact> artifacts = new ArrayList<org.eclipse.aether.artifact.Artifact>();
		for (MavenResolvedArtifact mavenResolvedArtifact : mavenResolvedArtifacts) {
			org.eclipse.aether.artifact.Artifact artifact = new org.eclipse.aether.artifact.DefaultArtifact(mavenResolvedArtifact.getCoordinate().getGroupId() + ":" + mavenResolvedArtifact.getCoordinate().getArtifactId() + ":" + mavenResolvedArtifact.getCoordinate().getType() + ":" + mavenResolvedArtifact.getCoordinate().getVersion());
			File resolvedFile = mavenResolvedArtifact.asFile();
			if (resolvedFile.getAbsolutePath().contains("..")) continue;
			String name = resolvedFile.getName();
			String shortName = artifact.getArtifactId() + "-" + artifact.getVersion();
			int lengthWithoutExtension = name.length() - artifact.getExtension().length() - 1;
			if (lengthWithoutExtension > 0) {
				name = name.substring(0, lengthWithoutExtension);
				if (!name.equals(shortName)) {
					String classifier = name.substring(shortName.length() + 1);
					artifact = new org.eclipse.aether.artifact.DefaultArtifact(mavenResolvedArtifact.getCoordinate().getGroupId() + ":" + mavenResolvedArtifact.getCoordinate().getArtifactId() + ":" + mavenResolvedArtifact.getCoordinate().getType() + ":" + classifier + ":" + mavenResolvedArtifact.getCoordinate().getVersion());
				}
			}
			artifact = artifact.setFile(resolvedFile);

			artifacts.add(artifact);
		}
		for (ArtifactResult pom : poms) {
			artifacts.add(pom.getArtifact());
		}

		// install artifacts
		for (org.eclipse.aether.artifact.Artifact artifact : artifacts) {
			boolean installPomSeparately = false;
			List<MojoExecutor.Element> configuration = new ArrayList<MojoExecutor.Element>();

			if (artifact.getArtifactId().equals("velocity") && artifact.getVersion().equals("1.5")) {
				configuration.add(new MojoExecutor.Element("generatePom", "true"));
				configuration.add(new MojoExecutor.Element("packaging", "jar"));
				installPomSeparately = true;
			}

			configuration.add(new MojoExecutor.Element("localRepositoryPath", localRepositoryPath.getAbsolutePath()));
			configuration.add(new MojoExecutor.Element("createChecksum", "true"));
			configuration.add(new MojoExecutor.Element("updateReleaseInfo", "true"));
			configuration.add(new MojoExecutor.Element("groupId", artifact.getGroupId()));
			configuration.add(new MojoExecutor.Element("artifactId", artifact.getArtifactId()));
			configuration.add(new MojoExecutor.Element("version", artifact.getVersion()));
			configuration.add(new MojoExecutor.Element("file", artifact.getFile().getAbsolutePath()));
			File pomFile = new File(artifact.getFile().getParentFile(), artifact.getArtifactId() + "-" + artifact.getVersion() + ".pom");
			if (StringUtils.isNotEmpty(artifact.getClassifier())) {
				configuration.add(new MojoExecutor.Element("classifier", artifact.getClassifier()));
				configuration.add(new MojoExecutor.Element("generatePom", "true"));
				configuration.add(new MojoExecutor.Element("packaging", "jar"));
				installPomSeparately = true;
			} else if (!installPomSeparately) {
				if (!pomFile.exists()) continue;
				configuration.add(new MojoExecutor.Element("pomFile", pomFile.getAbsolutePath()));
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

		List<String> goals = new ArrayList<String>();
		for (Plugin plugin : project.getBuild().getPlugins()) {
			for (PluginExecution execution : plugin.getExecutions()) {
				String prefix = execution.getId();
				for (String goal : execution.getGoals()) {
					goals.add(prefix + ":" + goal);
				}
			}
		}

		// create a default empty POM (because it's needed...)
		File tmpPom = new File(tmpDirectory, "pom.xml");
		try {
			POMManager.writeModelToPOM(project.getModel(), tmpPom);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
		List<Map.Entry<File, List<String>>> pomsWithGoal = getPOMsFromProject(project, tmpDirectory);

		PrintStream oldSystemErr = System.err;
		PrintStream oldSystemOut = System.out;
		try {
			silentSystemStreams();

			for (Map.Entry<File, List<String>> pomWithGoals : pomsWithGoal) {
				BuiltProject result = executeGoal(pomWithGoals.getKey(), globalSettingsFile, localRepositoryPath, mavenVersion, pomWithGoals.getValue());
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

			/*
			Dependency d = new Dependency();
			d.setGroupId(plugin.getGroupId());
			d.setArtifactId(plugin.getArtifactId());
			d.setVersion(plugin.getVersion());

			result.getDependencies().add(d);
			*/
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
