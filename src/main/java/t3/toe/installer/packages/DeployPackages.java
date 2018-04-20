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

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This goal deploys resolved TIBCO installation packages found in a given directory to a Maven (remote) repository.
* </p>
*
* <p>
* To display the TIBCO installation packages before deploying them, use
* <a href="packages-display-mojo.html">packages-display goal</a>.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "packages-deploy", requiresProject = false)
public class DeployPackages extends AbstractPackagesResolver {

	@Parameter (property = InstallerMojosInformation.Packages.Deploy.remoteRepositoryId, defaultValue = InstallerMojosInformation.Packages.Deploy.remoteRepositoryId_default, required = true)
	protected String remoteRepositoryId; 

	@Parameter (property = InstallerMojosInformation.Packages.Deploy.remoteRepositoryURL, defaultValue = InstallerMojosInformation.Packages.Deploy.remoteRepositoryURL_default, required = true)
	protected String remoteRepositoryURL; 

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
		getLog().info("Deploying local TIBCO installation packages found in directory: " + installationPackageDirectory.getAbsolutePath());

		super.execute();
	}

	@Override
	protected void doExecute() throws MojoExecutionException {
		getLog().info("Deploying " + installers.size() + " TIBCO installation packages...");

		deployPackagesToRemoteRepository(remoteRepositoryId, remoteRepositoryURL);
	}

	private void deployPackagesToRemoteRepository(String remoteRepositoryId, String remoteRepositoryURL) throws MojoExecutionException {
		getLog().info("");
		getLog().info("Using repository:");
		getLog().info("  Id = " + remoteRepositoryId);
		getLog().info("  URL = " + remoteRepositoryURL);
		getLog().info("");

		for (CommonInstaller installer : installers) {
			String classifier = null;
			if (StringUtils.isNotEmpty(installer.getInstallationPackageArch(false)) && StringUtils.isNotEmpty(installer.getInstallationPackageOs(false))) {
				classifier = installer.getInstallationPackageOs(false) + "_" + installer.getInstallationPackageArch(false);
			}
			String groupId = installer.getRemoteInstallationPackageGroupId();
			String artifactId = installer.getRemoteInstallationPackageArtifactId();
			String version = installer.getInstallationPackageVersion();
			String packaging = installer.getRemoteInstallationPackagePackaging();

			getLog().info("Deploying product '" + installer.getProductName() + "'");

			getLog().info("");
			this.deployDependency(groupId, artifactId, version, packaging, classifier, installer.getInstallationPackage(), remoteRepositoryId, remoteRepositoryURL, false);
			getLog().info("");
		}
	}

}
