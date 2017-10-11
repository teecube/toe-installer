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
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

import t3.plugin.annotations.Parameter;
import t3.toe.installer.InstallerMojosInformation;

/**
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public abstract class AbstractPackagesAction extends AbstractPackagesResolver {

	@Parameter (property = InstallerMojosInformation.Packages.offlineArchiveLocalRepository, defaultValue = InstallerMojosInformation.Packages.offlineArchiveLocalRepository_default)
	protected File offlineArchiveLocalRepository; 

	@Parameter (property = InstallerMojosInformation.Packages.generateArchive, defaultValue = InstallerMojosInformation.Packages.generateArchive_default)
	protected Boolean generateArchive; 

	@Parameter (property = InstallerMojosInformation.Packages.generateArchiveInstallInLocalRepositoryToo, defaultValue = InstallerMojosInformation.Packages.generateArchiveInstallInLocalRepositoryToo_default)
	protected Boolean generateArchiveInstallInLocalRepositoryToo; 

	@Parameter (property = InstallerMojosInformation.Packages.includePluginsInArchive, defaultValue = InstallerMojosInformation.Packages.includePluginsInArchive_default)
	protected Boolean includePluginsInArchive; 

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute(); // populate installers object

		getLog().info("");

		if (installers.size() > 0) {
			if (generateArchive) {
				if (includePluginsInArchive) {
					goOfflinePlugins(offlineArchiveLocalRepository); // install to target/offline/repository
				}
			}

			doExecute(offlineArchiveLocalRepository);
		} else {
			getLog().info("No TIBCO installation package was found.");
		}
	}

	protected abstract void doExecute(File packagesLocalRepositoryPath) throws MojoExecutionException;
	
	protected void goOfflinePlugins(File localRepositoryPath) throws MojoExecutionException {
		String goal = "go-offline-plugins";

		getLog().info(">>> " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " >>>");

		ArrayList<Element> configuration = new ArrayList<Element>();
		configuration.add(element("offlineArchiveLocalRepository", localRepositoryPath.getAbsolutePath()));

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
