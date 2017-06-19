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
import java.nio.file.FileAlreadyExistsException;
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
* This goal installs the TIBCO Runtime Agent product from an official archive to
* a target environment.
* </p>
*
* <p>
* If the target environment already contains the dependency product
* (RendezVous), only TIBCO Runtime Agent is installed, otherwise,
* the plugin will look for the dependency to install and call corresponding
* goal (see <a href="rv-install-mojo.html">rv-install goal</a>).
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "tra-install", requiresProject = false)
public class TRAInstallerMojo extends CommonInstaller {

	@Parameter(property = InstallerMojosInformation.TRA.installationPackage, defaultValue = "${" + InstallerMojosInformation.installationPackageDirectory + "}/${" + InstallerMojosInformation.TRA.installationPackageRegex + "}")
	private File installationPackage;

	@Parameter(property = InstallerMojosInformation.TRA.installationPackageRegex, defaultValue = InstallerMojosInformation.TRA.installationPackageRegex_default)
	private String installationPackageRegex;

	@Parameter(property = InstallerMojosInformation.TRA.installationPackageRegexVersionGroupIndex, defaultValue = InstallerMojosInformation.TRA.installationPackageRegexVersionGroupIndex_default)
	private Integer installationPackageRegexVersionGroupIndex;

	@Parameter(property = InstallerMojosInformation.TRA.installationPackageVersion, defaultValue = "")
	private String installationPackageVersion;

	@Parameter(property = InstallerMojosInformation.TRA.installationPackageVersionMajorMinor, defaultValue = "")
	private String installationPackageVersionMajorMinor;

	@Parameter(property = InstallerMojosInformation.TRA.remoteInstallationPackageGroupId, defaultValue = InstallerMojosInformation.TRA.remoteInstallationPackageGroupId_default, description = InstallerMojosInformation.TRA.remoteInstallationPackageGroupId_description)
	protected String remoteInstallationPackageGroupId;

	@Parameter(property = InstallerMojosInformation.TRA.remoteInstallationPackageArtifactId, defaultValue = InstallerMojosInformation.TRA.remoteInstallationPackageArtifactId_default, description = InstallerMojosInformation.TRA.remoteInstallationPackageArtifactId_description)
	protected String remoteInstallationPackageArtifactId;

	@Parameter(property = InstallerMojosInformation.TRA.remoteInstallationPackageVersion, defaultValue = "", description = InstallerMojosInformation.TRA.remoteInstallationPackageVersion_description)
	protected String remoteInstallationPackageVersion;

	@Parameter(property = InstallerMojosInformation.TRA.configDirectory, defaultValue = "${user.home}/tibco_cfg")
	private File configDirectoryRoot;

	@Override
	protected void initDefaultParameters() throws MojoExecutionException {
		super.initDefaultParameters();

		if (configDirectoryRoot == null) {
			File defaultConfigDirectoryRoot = new File(System.getProperty("user.home"), "tibco_cfg");
			if (defaultConfigDirectoryRoot.exists() && getCreateNewEnvironment()) {
				throw new MojoExecutionException("Configuration directory '" + defaultConfigDirectoryRoot.getAbsolutePath() + "' already exists. Please choose another one by setting '" + InstallerMojosInformation.TRA.configDirectory + "' property.", new FileAlreadyExistsException(defaultConfigDirectoryRoot.getAbsolutePath()));
			} else {
				configDirectoryRoot = defaultConfigDirectoryRoot;
			}
		}
	}

	@Override
	public List<String> getDependenciesGoals() {
		return new ArrayList<String>();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (getCreateNewEnvironment()) {
			// need to find dependencies
			installRV();
			this.createNewEnvironment = false;
		}

		super.execute();
	}

	@Override
	public File getInstallationPackage() throws MojoExecutionException {
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
		return InstallerMojosInformation.TRA.installationPackage;
	}

	@Override
	public String getInstallationPackageVersionPropertyName() {
		return InstallerMojosInformation.TRA.installationPackageVersion;
	}

	@Override
	public String getInstallationPackageVersionMajorMinorPropertyName() {
		return InstallerMojosInformation.TRA.installationPackageVersionMajorMinor;
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
	public String getRemotePackageGroupId() {
		return remoteInstallationPackageGroupId;
	}

	@Override
	public String getRemotePackageArtifactId() {
		return remoteInstallationPackageArtifactId;
	}

	@Override
	public String getRemotePackageVersion() {
		return remoteInstallationPackageVersion;
	}

	@Override
	public boolean hasDependencies() {
		return true;
	}

	@Override
	public boolean dependenciesExist() throws MojoExecutionException {
		return getInstallationRoot() != null && getInstallationRoot().exists() && new File(getInstallationRoot(), "tibrv").exists();
	}

	@Override
	public boolean installationExists() throws MojoExecutionException {
		return getInstallationRoot() != null && getInstallationRoot().exists() && new File(getInstallationRoot(), "tra/" + getInstallationPackageVersionMajorMinor()).exists();
	}

	@Override
	public void setProperties(Properties props) {
		if (props == null) {
			return;
		}
		props.setProperty("configDirectoryRoot", configDirectoryRoot.toString());
	}

	@Override
	public String getProductName() {
		return "TIBCO Runtime Agent (TRA)";
	}

}
