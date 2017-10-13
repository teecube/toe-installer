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
import java.io.IOException;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import t3.plugin.annotations.Mojo;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This goal installs resolved TIBCO installation packages found in a given directory to a Maven (local) repository.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "packages-install", requiresProject = false)
public class InstallPackages extends AbstractPackagesAction {

	@org.apache.maven.plugins.annotations.Parameter (property = InstallerMojosInformation.Packages.Install.localRepositoryPath, defaultValue = InstallerMojosInformation.Packages.Install.localRepositoryPath_default, readonly = true, required = true)
	protected ArtifactRepository localRepository;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Installing local TIBCO installation packages found in directory: " + installationPackageDirectory.getAbsolutePath());

		super.execute();
	}

	@Override
	protected void doExecute(File packagesLocalRepositoryPath) throws MojoExecutionException {
		File localRepositoryPath = new File(localRepository.getBasedir()); // install to current Maven local repository

		getLog().info("Installing " + installers.size() + " TIBCO installation packages...");

//		if (generateArchive) {
//			installPackagesToLocalRepository(packagesLocalRepositoryPath);
//			if (installInLocalRepositoryToo) {
//				installPackagesToLocalRepository(localRepositoryPath);
//			}
//		} else {
			installPackagesToLocalRepository(localRepositoryPath);
//		}
	}

	private void installPackagesToLocalRepository(File localRepositoryPath) throws MojoExecutionException {
		getLog().info("");
		getLog().info("Using local repository: " + localRepositoryPath.getAbsolutePath());
		getLog().info("");

		for (CommonInstaller installer : installers) {
			String classifier = null;
			if (StringUtils.isNotEmpty(installer.getInstallationPackageArch(false)) && StringUtils.isNotEmpty(installer.getInstallationPackageOs(false))) {
				classifier = installer.getInstallationPackageOs(false) + "_" + installer.getInstallationPackageArch(false);
			}
			String groupId = installer.getRemoteInstallationPackageGroupId();
			String artifactId = installer.getRemoteInstallationPackageArtifactId();
			String version = installer.getInstallationPackageVersion();
			getLog().info("Installing '" + installer.getInstallationPackage().getAbsolutePath() + "' to '" + localRepositoryPath.getAbsolutePath().replace("\\", "/") + "/" + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "'");
			this.installDependency(groupId, artifactId, version, "zip", classifier, installer.getInstallationPackage(), localRepositoryPath, true);
		}
		
		if (generateArchive) {
			getLog().info("");
			getLog().info("Updating offline archive '" + offlineArchive.getAbsolutePath() + "' with " + installers.size() + " TIBCO installation packages...");

			try {
				addFilesToZip(offlineDirectory, offlineArchive);
			} catch (IOException | ArchiveException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}
	}

}
