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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This goal displays resolved TIBCO installation packages found in a given directory.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "go-offline-plugins", requiresProject = false)
public class GoOfflinePlugins extends AbstractPackagesResolver {

	@Parameter (property = InstallerMojosInformation.Packages.archiveLocalRepositoryPath, defaultValue = InstallerMojosInformation.Packages.archiveLocalRepositoryPath_default)
	protected File goOfflineLocalRepository; 

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		MavenProject goOfflineProject = generateGoOfflineProject();
		goOffline(goOfflineProject, goOfflineLocalRepository, "3.5.0");
		getLog().info("");
	}

}
