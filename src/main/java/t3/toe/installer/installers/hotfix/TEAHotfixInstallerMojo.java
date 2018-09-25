/**
 * (C) Copyright 2016-2018 teecube
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
package t3.toe.installer.installers.hotfix;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.products.TIBCOProductToInstall;

import java.io.File;

/**
* <p>
* This goal installs the TIBCO Enterprise Administrator 2.x product hotfix from an official archive to a target
 * environment.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "install-tea-hotfix", requiresProject = false)
public class TEAHotfixInstallerMojo extends CommonHotfixInstaller {

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.Hotfix.installationPackage, defaultValue = "")
	private File installationPackage;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.Hotfix.installationPackageVersion, defaultValue = "")
	private String installationPackageVersion;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// empty but will be modified at compile-time
		super.execute();
	}

	@Override
	public void configureBuild(TIBCOProductToInstall tibcoProductToInstall, File standaloneLocalRepository) {

	}

	@Override
	public boolean dependenciesExist() throws MojoExecutionException {
//		return getInstallationRoot() != null && getInstallationRoot().exists() && new File(getInstallationRoot(), "tea/" + getInstallationPackageVersionMajorMinor()).exists();
		return getInstallationRoot() != null && getInstallationRoot().exists() && new File(getInstallationRoot(), "tea").exists();
	}

	@Override
	public File getInstallationPackage() {
		return installationPackage;
	}

	@Override
	public String getProductName() {
		return "TIBCO Enterprise Administrator (TEA) hotfix";
	}

	@Override
	public String getRemoteInstallationPackageVersion() {
		return installationPackageVersion;
	}

	@Override
	public void setInstallationPackage(File installationPackage) {
		this.installationPackage = installationPackage;
	}

}
