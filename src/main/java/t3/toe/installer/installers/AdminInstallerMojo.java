/**
 * (C) Copyright 2016-2019 teecube
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
package t3.toe.installer.installers;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.ProductType;
import t3.toe.installer.environments.products.TIBCOProductToInstall;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
* <p>
* This goal installs the TIBCO Administrator product from an official archive to
* a target environment.
* </p>
*
* <p>
* If the target environment already contains the dependencies products
* (RendezVous, TRA), only TIBCO Administrator is installed, otherwise, the
* plugin will look for the dependencies to install and call corresponding
* goals (see <a href="install-rv-mojo.html">install-rv goal</a> and
* <a href="./install-tra-mojo.html">install-tra goal</a>).
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "install-admin", requiresProject = false)
public class AdminInstallerMojo extends CommonInstaller {

	@Parameter(property = InstallerMojosInformation.Administrator.installationPackage, defaultValue = "${" + InstallerMojosInformation.installationPackageDirectory + "}/${tibco.admin.installation.package.regex}")
	private File installationPackage;

	@Parameter(property = InstallerMojosInformation.Administrator.installationPackageRegex, defaultValue = InstallerMojosInformation.Administrator.installationPackageRegex_default)
	private String installationPackageRegex;

	@Parameter(property = InstallerMojosInformation.Administrator.installationPackageRegexArchGroupIndex, defaultValue = InstallerMojosInformation.Administrator.installationPackageRegexArchGroupIndex_default)
	private Integer installationPackageRegexArchGroupIndex;

	@Parameter(property = InstallerMojosInformation.Administrator.installationPackageRegexOsGroupIndex, defaultValue = InstallerMojosInformation.Administrator.installationPackageRegexOsGroupIndex_default)
	private Integer installationPackageRegexOsGroupIndex;

	@Parameter(property = InstallerMojosInformation.Administrator.installationPackageRegexVersionGroupIndex, defaultValue = InstallerMojosInformation.Administrator.installationPackageRegexVersionGroupIndex_default)
	private Integer installationPackageRegexVersionGroupIndex;

	@Parameter(property = InstallerMojosInformation.Administrator.installationPackageVersion, defaultValue = "")
	private String installationPackageVersion;

	@Parameter(property = InstallerMojosInformation.Administrator.installationPackageVersionMajorMinor, defaultValue = "")
	private String installationPackageVersionMajorMinor;

	@Parameter(property = InstallerMojosInformation.Administrator.remoteInstallationPackageGroupId, defaultValue = InstallerMojosInformation.Administrator.remoteInstallationPackageGroupId_default, description = InstallerMojosInformation.Administrator.remoteInstallationPackageGroupId_description)
	protected String remoteInstallationPackageGroupId;

	@Parameter(property = InstallerMojosInformation.Administrator.remoteInstallationPackageArtifactId, defaultValue = InstallerMojosInformation.Administrator.remoteInstallationPackageArtifactId_default, description = InstallerMojosInformation.Administrator.remoteInstallationPackageArtifactId_description)
	protected String remoteInstallationPackageArtifactId;

	@Parameter(property = InstallerMojosInformation.Administrator.remoteInstallationPackageVersion, defaultValue = "", description = InstallerMojosInformation.Administrator.remoteInstallationPackageVersion_description)
	protected String remoteInstallationPackageVersion;

	@Parameter(property = InstallerMojosInformation.Administrator.remoteInstallationPackagePackaging, defaultValue = InstallerMojosInformation.Administrator.remoteInstallationPackagePackaging_default, description = InstallerMojosInformation.Administrator.remoteInstallationPackagePackaging_description)
	protected String remoteInstallationPackagePackaging;

	@Parameter(property = InstallerMojosInformation.Administrator.remoteInstallationPackageClassifier, defaultValue = "", description = InstallerMojosInformation.Administrator.remoteInstallationPackageClassifier_description)
	protected String remoteInstallationPackageClassifier;

	@Override
	public List<String> getDependenciesGoals() {
		List<String> dependenciesGoals = new ArrayList<String>();

		dependenciesGoals.add(InstallerMojosInformation.pluginPrefix + "install-rv");
		dependenciesGoals.add(InstallerMojosInformation.pluginPrefix + "install-tra");
		return dependenciesGoals;
	}

	@Override
	public void configureBuild(TIBCOProductToInstall tibcoProductToInstall, File standaloneLocalRepository) {

	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (getCreateNewEnvironment()) {
			_isDependency = true;

			// need to find dependencies
			installRV();
			this.createNewEnvironment = false;
			_createNewEnvironment = createNewEnvironment; // forward to next dependency
			_installationRoot = getInstallationRoot();
			if (_environmentName != null && this.environmentName == null) {
				this.environmentName = _environmentName;
			}
			installTRA();
		}
		_isDependency = false;

		super.execute();
	}

	@Override
	public File getInstallationPackage(boolean resolve) throws MojoExecutionException {
		if (resolve && (installationPackage == null || !installationPackage.exists())) {
			installationPackage = findInstallationPackage();
		}
		return installationPackage;
	}

	@Override
	public String getInstallationPackageRegex() {
		return installationPackageRegex;
	}

	@Override
	public Integer getInstallationPackageArchGroupIndex() {
		return installationPackageRegexArchGroupIndex;
	}

	@Override
	public Integer getInstallationPackageOsGroupIndex() {
		return installationPackageRegexOsGroupIndex;
	}

	@Override
	public Integer getInstallationPackageVersionGroupIndex() {
		return installationPackageRegexVersionGroupIndex;
	}

	@Override
	public String getInstallationPackagePropertyName() {
		return InstallerMojosInformation.Administrator.installationPackage;
	}

	@Override
	public String getInstallationPackageVersionPropertyName() {
		return InstallerMojosInformation.Administrator.installationPackageVersion;
	}

	@Override
	public String getInstallationPackageVersionMajorMinorPropertyName() {
		return InstallerMojosInformation.Administrator.installationPackageVersionMajorMinor;
	}

	@Override
	public String getInstallationPackageArchPropertyName() {
		return InstallerMojosInformation.BW6.installationPackageArch;
	}

	@Override
	public String getInstallationPackageOsPropertyName() {
		return InstallerMojosInformation.BW6.installationPackageOs;
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
	public String getRemoteInstallationPackageGroupId() {
		return remoteInstallationPackageGroupId;
	}

	@Override
	public String getRemoteInstallationPackageArtifactId() {
		return remoteInstallationPackageArtifactId;
	}

	@Override
	public String getRemoteInstallationPackageVersion() {
		return remoteInstallationPackageVersion;
	}

	@Override
	public String getRemoteInstallationPackagePackaging() {
		return remoteInstallationPackagePackaging;
	}

	@Override
	public String getRemoteInstallationPackageClassifier() {
		return remoteInstallationPackageClassifier;
	}

	@Override
	public void setRemoteInstallationPackageGroupId(String remoteInstallationPackageGroupId) {
		this.remoteInstallationPackageGroupId = remoteInstallationPackageGroupId;
	}

	@Override
	public void setRemoteInstallationPackageArtifactId(String remoteInstallationPackageArtifactId) {
		this.remoteInstallationPackageArtifactId = remoteInstallationPackageArtifactId;
	}

	@Override
	public void setInstallationPackageVersion(String installationPackageVersion) {
		this.installationPackageVersion = installationPackageVersion;
	}

	@Override
	public void setRemoteInstallationPackageVersion(String remoteInstallationPackageVersion) {
		this.remoteInstallationPackageVersion = remoteInstallationPackageVersion;
	}

	@Override
	public void setRemoteInstallationPackageClassifier(String remoteInstallationPackageClassifier) {
		this.remoteInstallationPackageClassifier = remoteInstallationPackageClassifier;
	}

	@Override
	public void setInstallationPackage(File installationPackage) {
		this.installationPackage = installationPackage;
	}

	@Override
	public void setInstallationPackageRegex(String installationPackageRegex) {
		this.installationPackageRegex = installationPackageRegex;
	}

	@Override
	public void setInstallationPackageRegexVersionGroupIndex(Integer installationPackageRegexVersionGroupIndex) {
		this.installationPackageRegexVersionGroupIndex = installationPackageRegexVersionGroupIndex;
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
		return getInstallationRoot() != null && getInstallationRoot().exists() && new File(getInstallationRoot(), "administrator/" + getInstallationPackageVersionMajorMinor()).exists();
	}

	@Override
	public void setProperties(Properties props) {
		if (props == null) {
			return;
		}
	}

	@Override
	public String getProductName() {
		return InstallerMojosInformation.Administrator.category;
	}

	@Override
	public ProductType getProductType() {
		return ProductType.ADMIN;
	}


}
