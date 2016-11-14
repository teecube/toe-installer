/**
 * (C) Copyright 2016-2016 teecube
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
package t3.toe.installer.configurers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.tibco.envinfo.TIBCOEnvironment;
import com.tibco.envinfo.TIBCOEnvironment.Environment;

import lombok.ast.libs.com.google.common.collect.Lists;
import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.plugin.annotations.Parameter;
import t3.site.GenerateGlobalParametersDocMojo;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosInformation;

/**
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public abstract class CommonConfigurer extends CommonMojo {

	@org.apache.maven.plugins.annotations.Parameter (defaultValue = "${localRepository}", readonly = true, required = true)
	protected ArtifactRepository localRepository;

	@Component
	private RepositorySystem system;

	@org.apache.maven.plugins.annotations.Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession systemSession;

	@Parameter(property = InstallerMojosInformation.environmentName, defaultValue = InstallerMojosInformation.environmentName_default)
	protected String environmentName;

	@Parameter(property = InstallerMojosInformation.writeToSettings, defaultValue = "false")
	protected Boolean writeToSettings;

	protected abstract String getGroupId();
	protected abstract String getArtifactId();
	protected abstract String getBootstrapClass();

	protected abstract String getProductInstallationGoal();
	protected abstract String getProductName();

	protected abstract Properties getProfileProperties(Environment environment);
	protected abstract boolean validateProfileProperties(Environment environment);

	@Override
	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() throws MojoExecutionException {
		return new InstallerLifecycleParticipant();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Environment currentEnvironment = null;
		if (environmentName != null && !environmentName.isEmpty()) {
			currentEnvironment = CommonInstaller.getCurrentEnvironment(environmentName);
			if (currentEnvironment == null && !InstallerMojosInformation.environmentName_default.equals(environmentName)) {
				getLog().info("");
				getLog().warn("Environment '" + environmentName + "' set but not found.");
				getLog().info("");
			} else if (currentEnvironment != null && !validateProfileProperties(currentEnvironment)) {
				getLog().info("");
				getLog().warn("The provided environment '" + environmentName + "' is not valid.");
				getLog().info("");
			} else if (currentEnvironment != null && validateProfileProperties(currentEnvironment)) {

			} else if (!InstallerMojosInformation.environmentName_default.equals(environmentName)) {
				getLog().info("");
				getLog().warn("The provided environment '" + environmentName + "' is not found.");
				getLog().info("");
			}
		}

		String version = "LATEST";
		if (!"standalone-pom".equals(this.project.getArtifactId())) {
			for (Plugin plugin : this.project.getBuildPlugins()) {
				if (getArtifactId().equals(plugin.getArtifactId())) {
					version = plugin.getVersion();
				}
			} 
		}

		List<ArtifactResult> artifactResults = getPlugin(getGroupId() + ":" + getArtifactId(), version);

		List<File> files = new ArrayList<File>();

		String resolvedVersion = null;
		for (ArtifactResult artifactResult : artifactResults) {
			if (getArtifactId().equals(artifactResult.getArtifact().getArtifactId())) {
				resolvedVersion = artifactResult.getArtifact().getVersion();
			}
			files.add(artifactResult.getArtifact().getFile());
        }

        if (!files.isEmpty() && resolvedVersion != null) {
			Properties profileProperties = new Properties();

			getLog().info("Profile for '" + getArtifactId() + "' plugin version '" + resolvedVersion + "' " + (version == "LATEST" ? "(latest version)" : "(defined in current project)"));
			if (currentEnvironment != null && validateProfileProperties(currentEnvironment)) {
				profileProperties = getProfileProperties(currentEnvironment);
				getLog().info("based on TIBCO environment '" + currentEnvironment.getName() + "=" + currentEnvironment.getLocation() + "'");
			}

			GenerateGlobalParametersDocMojo standaloneGenerator = GenerateGlobalParametersDocMojo.standaloneGenerator(session.getCurrentProject(), getBootstrapClass(), files);

			getLog().info("\n" + standaloneGenerator.getFullSampleProfileForCommandLine(getArtifactId(), profileProperties));

			File settingsXml = getBestSettings();

			boolean profileMustBeCompleted = currentEnvironment == null || !validateProfileProperties(currentEnvironment);

			if (settingsXml != null && !profileMustBeCompleted) {
				getLog().info("");

				if (writeToSettings) {
					SettingsXpp3Reader reader = new SettingsXpp3Reader();
					try {
						Settings settings = reader.read(new FileInputStream(settingsXml));
						org.apache.maven.settings.Profile p = standaloneGenerator.getFullSampleProfile(getArtifactId(), profileProperties);
						settings.getProfiles().add(p);

						SettingsXpp3Writer writer = new SettingsXpp3Writer();
						writer.write(new FileOutputStream(settingsXml), settings);
					} catch (IOException | XmlPullParserException e) {
						// nothing
					}
					getLog().info("Adding sample profile in the 'settings.xml' file of current");
					getLog().info("Maven installation:");
				} else {
					getLog().info("This sample profile must be added in the 'settings.xml' file of current");
					getLog().info("Maven installation:");
				}
				getLog().info("  " + settingsXml.getAbsolutePath());

				if (!writeToSettings) {
					getLog().info("");
					getLog().info("To persist the sample profile in 'settings.xml',");
					getLog().info("set '" + InstallerMojosInformation.writeToSettings + "' to 'true'.");
				}
			}

			if (profileMustBeCompleted) { // tell the user to provide an environment to use (and how to find it)
				getLog().info("");
				getLog().info("[...] must be replaced by actual values from the TIBCO environment to use.");
				getLog().info("");
				getLog().info("You can run again this command with option");
				getLog().info("  '-D" + InstallerMojosInformation.environmentName + "=<EnvironmentName>'");
				getLog().info("to fill values automatically using an existing environment.");

				String availableEnvironments = StringUtils.join(Lists.newArrayList(Iterables.transform(CommonInstaller.getAllEnvironments(), new Function<TIBCOEnvironment.Environment, String>() {
					@Override
					public String apply(final TIBCOEnvironment.Environment env) {
						return env.getName();
					}
				})), ", ");

				getLog().info("");
				getLog().info("Available environments on this system are:");
				getLog().info("  " + availableEnvironments);
				getLog().info("");
				getLog().info("Run 'mvn toe:envinfo-list' for environments details.");
				getLog().info("Run 'mvn " + getProductInstallationGoal() + "' to install " + getProductName() + ".");
			}
        }
	}

	protected File getBestSettings() {
		if (!session.getRequest().getUserSettingsFile().exists() && !session.getRequest().getGlobalSettingsFile().exists()) {
			return null;
		}

		if (session.getRequest().getUserSettingsFile().exists()) {
			try {
				return new File(session.getRequest().getUserSettingsFile().getCanonicalPath());
			} catch (IOException e) {
				return null;
			}
		} else {
			try {
				return new File(session.getRequest().getGlobalSettingsFile().getCanonicalPath());
			} catch (IOException e) {
				return null;
			}
		}
	}

	protected List<ArtifactResult> getPlugin(String pluginKey, String version) {
        Artifact artifact = new DefaultArtifact(pluginKey + ":" + version);

        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE );

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency( artifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(project.getRemotePluginRepositories());

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);

        List<ArtifactResult> artifactResults = null;
		try {
			artifactResults = system.resolveDependencies(systemSession, dependencyRequest).getArtifactResults();
		} catch (DependencyResolutionException e) {
			//
		}

		return artifactResults;
	}
}
