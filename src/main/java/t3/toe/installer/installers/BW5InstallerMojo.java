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
package t3.toe.installer.installers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This goal installs the TIBCO BusinessWorks 5.x product from an official
* archive to a target environment.
* </p>
*
* <p>
* If the target environment already contains the dependencies products
* (RendezVous, TRA), only TIBCO BusinessWorks 5.x is installed, otherwise,
* the plugin will look for the dependencies to install and call corresponding
* goals (see <a href="rv-install-mojo.html">rv-install goal</a> and
* <a href="./tra-install-mojo.html">tra-install goal</a>).
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "bw5-install", requiresProject = false)
public class BW5InstallerMojo extends CommonInstaller {

	@Parameter(property = InstallerMojosInformation.BW5.installationPackage, defaultValue = "${" + InstallerMojosInformation.installationPackageDirectory + "}/${tibco.bw5.installation.package.regex}")
	private File installationPackage;

	@Parameter(property = InstallerMojosInformation.BW5.installationPackageRegex, defaultValue = InstallerMojosInformation.BW5.installationPackageRegex_default)
	private String installationPackageRegex;

	@Parameter(property = InstallerMojosInformation.BW5.installationPackageRegexVersionGroupIndex, defaultValue = InstallerMojosInformation.BW5.installationPackageRegexVersionGroupIndex_default)
	private Integer installationPackageRegexVersionGroupIndex;

	@Parameter(property = InstallerMojosInformation.BW5.installationPackageVersion, defaultValue = "")
	private String installationPackageVersion;

	@Parameter(property = InstallerMojosInformation.BW5.installationPackageVersionMajorMinor, defaultValue = "")
	private String installationPackageVersionMajorMinor;

	@Override
	public List<String> getDependenciesGoals() {
		List<String> dependenciesGoals = new ArrayList<String>();

		dependenciesGoals.add("toe:rv-install");
		dependenciesGoals.add("toe:tra-install");
		return dependenciesGoals;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (getCreateNewEnvironment()) {
			_isDependency = true;

			// need to find dependencies
			installRV();
			this.createNewEnvironment = false;
			_createNewEnvironment = createNewEnvironment; // forward to next dependency
			this.removeExistingEnvironment = false;
			_removeExistingEnvironment = removeExistingEnvironment;
			if (_environmentName != null && this.environmentName == null) {
				this.environmentName = _environmentName;
			}
			installTRA();
		}
		_isDependency = false;

		super.execute();
	}

	@Override
	public File getInstallationPackage() {
		if (installationPackage == null || !installationPackage.exists()) {
			installationPackage = findInstallationPackage();
		}
		return installationPackage;
	}

	@Override
	public String getInstallationPackageRegex() {
		return installationPackageRegex;
	}

	@Override
	public Integer getInstallationPackageVersionGroupIndex() {
		return installationPackageRegexVersionGroupIndex;
	}

	@Override
	public String getInstallationPackagePropertyName() {
		return InstallerMojosInformation.BW5.installationPackage;
	}

	@Override
	public String getInstallationPackageVersionPropertyName() {
		return InstallerMojosInformation.BW5.installationPackageVersion;
	}

	@Override
	public String getInstallationPackageVersionMajorMinorPropertyName() {
		return InstallerMojosInformation.BW5.installationPackageVersionMajorMinor;
	}

	@Override
	public String getInstallationPackageVersionMajorMinor() {
		return installationPackageVersionMajorMinor;
	}

	@Override
	public void setInstallationPackageVersionMajorMinor(String version) {
		this.installationPackageVersionMajorMinor = version;
	}

	@Override
	public boolean hasDependencies() {
		return true;
	}

	@Override
	public boolean dependenciesExist() throws MojoExecutionException {
		return getInstallationRoot() != null && getInstallationRoot().exists() && new File(getInstallationRoot(), "tra").exists();
	}

	@Override
	public boolean installationExists() throws MojoExecutionException {
		return getInstallationRoot() != null && getInstallationRoot().exists() && new File(getInstallationRoot(), "bw/" + getInstallationPackageVersionMajorMinor()).exists();
	}

	@Override
	public void setProperties(Properties props) {
		if (props == null) {
			return;
		}
	}

	@Override
	public String getProductName() {
		return "TIBCO BusinessWorks 5.x";
	}

}
