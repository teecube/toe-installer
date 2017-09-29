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

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This goal displays resolved TIBCO installation packages found in a given directory.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "install-local-packages", requiresProject = false)
public class InstallPackages extends AbstractPackagesResolver {

	@org.apache.maven.plugins.annotations.Parameter (property = InstallerMojosInformation.Packages.localRepositoryPath, defaultValue = InstallerMojosInformation.Packages.localRepositoryPath_default, readonly = true, required = true)
	protected ArtifactRepository localRepository;

	@Parameter (property = InstallerMojosInformation.Packages.archiveLocalRepositoryPath, defaultValue = InstallerMojosInformation.Packages.archiveLocalRepositoryPath_default)
	protected File goOfflineLocalRepository; 

	@Parameter (property = InstallerMojosInformation.Packages.generateArchive, defaultValue = InstallerMojosInformation.Packages.generateArchive_default)
	protected Boolean generateArchive; 

	@Parameter (property = InstallerMojosInformation.Packages.includePluginsInArchive, defaultValue = InstallerMojosInformation.Packages.includePluginsInArchive_default)
	protected Boolean includePluginsInArchive; 

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Installing local TIBCO installation packages found in directory: " + installationPackageDirectory.getAbsolutePath());

		super.execute();

		getLog().info("");

		if (installers.size() > 0) {
			File localRepositoryPath = new File(localRepository.getBasedir());
			if (generateArchive) {
				localRepositoryPath = new File(this.directory, "local-packages");
				if (includePluginsInArchive) {
					goOfflinePlugins(localRepositoryPath);
				}
			}

			getLog().info("Installing " + installers.size() + " TIBCO installation packages...");
			getLog().info("");

			for (CommonInstaller installer : installers) {
				String classifier = null;
				if (StringUtils.isNotEmpty(installer.getInstallationPackageArch(false)) && StringUtils.isNotEmpty(installer.getInstallationPackageOs(false))) {
					classifier = installer.getInstallationPackageOs(false) + "_" + installer.getInstallationPackageArch(false);
				}
				this.installDependency(installer.getRemoteInstallationPackageGroupId(), installer.getRemoteInstallationPackageArtifactId(), installer.getInstallationPackageVersion(), "zip", classifier, installer.getInstallationPackage(), localRepositoryPath);
			}
		} else {
			getLog().info("No TIBCO installation package was found.");
		}
	}

	private void goOfflinePlugins(File localRepositoryPath) throws MojoExecutionException {
		String goal = "go-offline-plugins";

		getLog().info(">>> " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " >>>");

		ArrayList<Element> configuration = new ArrayList<Element>();
		configuration.add(element("goOfflineLocalRepository", localRepositoryPath.getAbsolutePath()));

		executeMojo(
			plugin(
				groupId(pluginDescriptor.getGroupId()),
				artifactId(pluginDescriptor.getArtifactId()),
				version(pluginDescriptor.getVersion())
			),
			goal(goal),
			configuration(
				configuration.toArray(new Element[0])
			),
			getEnvironment(pluginManager)
		);

		getLog().info("<<< " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " <<<");
		getLog().info("");
	}

}
