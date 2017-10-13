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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosFactory;
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This class resolves all TIBCO installation packages found in a given directory.
* It can be subclassed to display found packages or to install/deploy them in Maven or to include them in a standalone
* package.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public abstract class AbstractPackagesResolver extends CommonMojo {

	@Parameter(property = InstallerMojosInformation.installationPackageDirectory, defaultValue = InstallerMojosInformation.installationPackageDirectory_default)
	protected File installationPackageDirectory;

	protected List<CommonInstaller> installers;

	@Override
	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() throws MojoExecutionException {
		return new InstallerLifecycleParticipant();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		installers = new ArrayList<CommonInstaller>();
		for (Class<? extends CommonInstaller> installerClass : InstallerMojosFactory.getInstallersClasses()) {
			CommonInstaller installer;
			try {
				installer = installerClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
			installer.setSession(session);
			installer.setInstallationPackageDirectory(this.installationPackageDirectory);
			installer.initStandalonePOMNoDefaultParameters();

			File installationPackage = installer.getInstallationPackage();
			if (installationPackage != null && installationPackage.exists()) {
				installers.add(installer);
			}
		}

		getLog().info("");
		if (installers.size() > 0) {
			getLog().info("Found " + installers.size() + " TIBCO installation packages:");
			for (CommonInstaller installer : installers) {
				getLog().info("-> " + installer.getProductName() + " version " + installer.getInstallationPackageVersion() + " @ " + installer.getInstallationPackage());
			}

			getLog().info("");
			doExecute();
		} else {
			getLog().info("No TIBCO installation package was found.");
		}
	}

	protected abstract void doExecute() throws MojoExecutionException;

	protected void installPackagesToLocalRepository(File localRepositoryPath) throws MojoExecutionException {
		getLog().info("Installing " + installers.size() + " TIBCO installation packages...");
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
		
//		if (generateArchive) {
//			getLog().info("");
//			getLog().info("Updating offline archive '" + offlineArchive.getAbsolutePath() + "' with " + installers.size() + " TIBCO installation packages...");
//
//			try {
//				addFilesToZip(offlineDirectory, offlineArchive);
//			} catch (IOException | ArchiveException e) {
//				throw new MojoExecutionException(e.getLocalizedMessage(), e);
//			}
//		}
	}

}
