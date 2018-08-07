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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.InstallerMojosInformation;

import java.io.File;

/**
* <p>
* This goal displays resolved TIBCO installation packages found in a given directory.
* </p>
* <p>
* To install these TIBCO installation packages, use
* <a href="packages-install-mojo.html">packages-install goal</a> and to deploy them, use
* <a href="packages-deploy-mojo.html">packages-deploy goal</a>.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "packages-display", requiresProject = false)
public class DisplayPackagesMojo extends AbstractPackagesResolver {

	@Parameter(property = InstallerMojosInformation.FullEnvironment.topologyGenerate, defaultValue = InstallerMojosInformation.FullEnvironment.topologyGenerate_default)
	protected Boolean generateTopology;

	@Parameter (property = InstallerMojosInformation.FullEnvironment.topologyGeneratedFile, defaultValue = InstallerMojosInformation.FullEnvironment.topologyGeneratedFile_default)
	protected File topologyGeneratedFile;

	@Override
	protected Boolean getGenerateTopology() throws MojoExecutionException {
		return generateTopology;
	}

	@Override
	protected File getTopologyGeneratedFile() throws MojoExecutionException {
		return topologyGeneratedFile;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Resolving local TIBCO installation packages in directory: " + installationPackageDirectory.getAbsolutePath());
		
		super.execute();
	}

	@Override
	protected void doExecute() throws MojoExecutionException {
		getLog().info("These TIBCO installation packages can be automatically:");
		getLog().info("-> installed to the local Maven repository by running 'mvn toe:packages-install'");
		getLog().info("-> deployed to a remote Maven repository by running 'mvn toe:packages-deploy -D" + InstallerMojosInformation.Packages.Deploy.remoteRepositoryId + "=<repositoryId> -D" + InstallerMojosInformation.Packages.Deploy.remoteRepositoryURL + "=<repositoryURL>'");		
	}

}
