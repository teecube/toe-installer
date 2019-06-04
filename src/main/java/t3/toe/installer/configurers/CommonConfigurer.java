/**
 * (C) Copyright 2016-2019 teecube
 * (https://teecu.be) and others.
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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.tibco.envinfo.TIBCOEnvironment;
import com.tibco.envinfo.TIBCOEnvironment.Environment;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.plugin.annotations.Parameter;
import t3.site.GenerateGlobalParametersDocMojo;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosInformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public abstract class CommonConfigurer extends CommonMojo {

	@org.apache.maven.plugins.annotations.Parameter (defaultValue = "${localRepository}", readonly = true, required = true)
	protected ArtifactRepository localRepository;

	@Parameter(property = InstallerMojosInformation.Installation.environmentName, defaultValue = InstallerMojosInformation.Installation.environmentName_default, description = InstallerMojosInformation.Configuration.environmentName_description)
	protected String environmentName;

	@Parameter(property = InstallerMojosInformation.Configuration.enableProfile, defaultValue = InstallerMojosInformation.Configuration.enableProfile_default, description = InstallerMojosInformation.Configuration.enableProfile_description)
	protected Boolean enableProfile;

	@Parameter(property = InstallerMojosInformation.Configuration.overwriteExistingProfile, defaultValue = InstallerMojosInformation.Configuration.overwriteExistingProfile_default, description = InstallerMojosInformation.Configuration.overwriteExistingProfile_description)
	protected Boolean overwriteExistingProfile;

	@Parameter(property = InstallerMojosInformation.Configuration.useGlobalSettings, defaultValue = InstallerMojosInformation.Configuration.useGlobalSettings_default, description = InstallerMojosInformation.Configuration.useGlobalSettings_description)
	protected Boolean useGlobalSettings;

	@Parameter(property = InstallerMojosInformation.Configuration.overriddenSettingsLocation, defaultValue = InstallerMojosInformation.Configuration.overriddenSettingsLocation_default, description = InstallerMojosInformation.Configuration.overriddenSettingsLocation_description)
	protected File overriddenSettingsLocation;

	@Parameter(property = InstallerMojosInformation.Configuration.writeToSettings, defaultValue = InstallerMojosInformation.Configuration.writeToSettings_default, description = InstallerMojosInformation.Configuration.writeToSettings_description)
	protected Boolean writeToSettings;
	
	protected abstract String getGroupId();
	protected abstract String getArtifactId();
	protected abstract String getBootstrapClass();

	protected abstract String getProductInstallationGoal();
	protected abstract String getProductName();

	protected abstract Properties getProfileProperties(Environment environment);
	protected abstract boolean validateProfileProperties(Environment environment);

	@Override
	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() {
		return new InstallerLifecycleParticipant();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Environment currentEnvironment = null;
		if (environmentName != null && !environmentName.isEmpty()) {
			currentEnvironment = CommonInstaller.getCurrentEnvironment(environmentName);
			if (currentEnvironment == null && !InstallerMojosInformation.Installation.environmentName_default.equals(environmentName)) {
				getLog().info("");
				getLog().warn("The provided environment '" + environmentName + "' is not found.");
				getLog().info("");
			} else if (currentEnvironment != null && !validateProfileProperties(currentEnvironment)) {
				getLog().info("");
				getLog().warn("The provided environment '" + environmentName + "' is not valid.");
				getLog().info("");
			} else if (currentEnvironment != null && validateProfileProperties(currentEnvironment)) {

			} else if (!InstallerMojosInformation.Installation.environmentName_default.equals(environmentName)) {
				getLog().info("");
				getLog().warn("The default environment '" + environmentName + "' is not found.");
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

		if (session.isOffline()) {
			RemoteRepository localAsRemoteRepository = new RemoteRepository.Builder("local", "default", session.getLocalRepository().getUrl().replace("\\", "/")).build();
			project.getRemotePluginRepositories().clear();
			project.getRemotePluginRepositories().add(localAsRemoteRepository);
			localAsRemoteRepository = new RemoteRepository.Builder("central", "default", session.getLocalRepository().getUrl().replace("\\", "/")).build();
			project.getRemotePluginRepositories().add(localAsRemoteRepository);
		}
		List<ArtifactResult> artifactResults = null;
		try {
			artifactResults = getPlugin(getGroupId() + ":" + getArtifactId(), version);
		} catch (ArtifactResolutionException | DependencyResolutionException e) {
			String message = "Unable to resolve plugin '" + getGroupId() + ":" + getArtifactId() + ":" + version + "'";
			getLog().error(message);
			throw new MojoExecutionException(message);
		}

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
				getLog().info("based on following TIBCO environment:");
				getLog().info("       name = " + currentEnvironment.getName());
				getLog().info(" TIBCO_HOME = " + currentEnvironment.getLocation());
			}

			GenerateGlobalParametersDocMojo standaloneGenerator = GenerateGlobalParametersDocMojo.standaloneGenerator(session.getCurrentProject(), getBootstrapClass(), files);

			String profileId = getArtifactId();
			getLog().info("\n" + standaloneGenerator.getFullSampleProfileForCommandLine(profileId, profileProperties));

			File settingsXml = getSettingsXml();

			boolean profileMustBeCompleted = currentEnvironment == null || !validateProfileProperties(currentEnvironment);

			if (settingsXml != null && !profileMustBeCompleted) {
				getLog().info("");

				if (writeToSettings) {
					SettingsXpp3Reader reader = new SettingsXpp3Reader();
					try {
						org.apache.maven.settings.Profile p = standaloneGenerator.getFullSampleProfile(getArtifactId(), profileProperties);

						Boolean existingProfile = false;
						Settings settings = reader.read(new FileInputStream(settingsXml));
						for (Iterator<Profile> iterator = settings.getProfiles().listIterator(); iterator.hasNext();) {
							Profile profile = iterator.next();
							if (p.getId().equals(profile.getId())) {
								existingProfile = true;
								// a profile with same id already exists
								if (overwriteExistingProfile) {
									getLog().info("Profile '" + p.getId() + "' already exists in Maven settings file '" + settingsXml + "'. Overwriting.");
									getLog().info("");
									profile = p;
									iterator.remove();
									settings.addProfile(p);
									break;
								} else {
									getLog().warn("Profile '" + p.getId() + "' already exists in Maven settings file '" + settingsXml + "'. Skipping.");
									return;
								}
							}
						}
						if (!existingProfile) {
							settings.getProfiles().add(p);
						}

						if (!settings.getActiveProfiles().contains(p.getId()) && enableProfile) {
							settings.getActiveProfiles().add(p.getId());
						}

						// try to fix relative directory of local repository
                        if (settings.getLocalRepository().startsWith("./") || settings.getLocalRepository().startsWith(".\\")) {
                            settings.setLocalRepository(new File(settingsXml.getParentFile(), settings.getLocalRepository()).getCanonicalPath());
                        }

						SettingsXpp3Writer writer = new SettingsXpp3Writer();
						writer.write(new FileOutputStream(settingsXml), settings);
					} catch (IOException | XmlPullParserException e) {
						// nothing
					}
					getLog().info("Adding sample profile in the Maven settings.");
					getLog().info("Current Maven settings is:");
				} else {
					getLog().info("This sample profile must be added in the Maven settings.");
					getLog().info("Current Maven settings is:");
				}
				getLog().info("  " + settingsXml.getAbsolutePath());

				if (!writeToSettings) {
					getLog().info("");
					getLog().info("To persist the sample profile in the Maven settings,");
					getLog().info("set '" + InstallerMojosInformation.Configuration.writeToSettings + "' to 'true'.");
				}
			}

			if (profileMustBeCompleted) { // tell the user to provide an environment to use (and how to find it)
				getLog().info("");
				getLog().info("[...] must be replaced by actual values from the TIBCO environment to use.");
				getLog().info("");
				getLog().info("You can run again this command with option");
				getLog().info("  '-D" + InstallerMojosInformation.Installation.environmentName + "=<EnvironmentName>'");
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
				getLog().info("Run 'mvn " + InstallerMojosInformation.pluginPrefix + "envinfo-list' for environments details.");
				getLog().info("Run 'mvn " + getProductInstallationGoal() + "' to install " + getProductName() + ".");
			}
        }
	}

	protected File getSettingsXml() {
		if (overriddenSettingsLocation != null && overriddenSettingsLocation.exists()) {
			return overriddenSettingsLocation;
		}

		if (!session.getRequest().getUserSettingsFile().exists() && !session.getRequest().getGlobalSettingsFile().exists()) {
			return null;
		}

		if (useGlobalSettings && !session.getRequest().getGlobalSettingsFile().exists()) {
			getLog().warn("'" + InstallerMojosInformation.Configuration.useGlobalSettings + "' was set to true but no Maven global settings was found. Defaulting to Maven user settings.");
			useGlobalSettings = false;
		}

		if (session.getRequest().getUserSettingsFile().exists() && !useGlobalSettings) {
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

	protected List<ArtifactResult> getPlugin(String pluginKey, String version) throws ArtifactResolutionException, DependencyResolutionException {
        Artifact artifact = new DefaultArtifact(pluginKey + ":" + version);

        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(project.getRemotePluginRepositories());

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);

        List<ArtifactResult> artifactResults = new ArrayList<ArtifactResult>();
		try {
			ArtifactRequest artifactRequest = new ArtifactRequest(artifact, project.getRemotePluginRepositories(), "");
			artifactResults.add(system.resolveArtifact(systemSession, artifactRequest));
			artifactResults.addAll(system.resolveDependencies(systemSession, dependencyRequest).getArtifactResults());
		} catch (DependencyResolutionException e) {
			// same player shoots again (sometimes two resolutions are required)
			artifactResults.addAll(system.resolveDependencies(systemSession, dependencyRequest).getArtifactResults());
		}

		return artifactResults;
	}
}
