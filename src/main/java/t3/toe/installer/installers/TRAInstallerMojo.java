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
package t3.toe.installer.installers;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.ProductType;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
* goal (see <a href="install-rv-mojo.html">install-rv goal</a>).
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "install-tra", requiresProject = false)
public class TRAInstallerMojo extends CommonInstaller {

	@Parameter(property = InstallerMojosInformation.TRA.installationPackage, defaultValue = "${" + InstallerMojosInformation.installationPackageDirectory + "}/${" + InstallerMojosInformation.TRA.installationPackageRegex + "}")
	private File installationPackage;

	@Parameter(property = InstallerMojosInformation.TRA.installationPackageRegex, defaultValue = InstallerMojosInformation.TRA.installationPackageRegex_default)
	private String installationPackageRegex;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegexArchGroupIndex, defaultValue = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegexArchGroupIndex_default)
	private Integer installationPackageRegexArchGroupIndex;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegexOsGroupIndex, defaultValue = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegexOsGroupIndex_default)
	private Integer installationPackageRegexOsGroupIndex;

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

	@Parameter(property = InstallerMojosInformation.TRA.remoteInstallationPackagePackaging, defaultValue = InstallerMojosInformation.TRA.remoteInstallationPackagePackaging_default, description = InstallerMojosInformation.TRA.remoteInstallationPackagePackaging_description)
	protected String remoteInstallationPackagePackaging;

	@Parameter(property = InstallerMojosInformation.TRA.remoteInstallationPackageClassifier, defaultValue = "", description = InstallerMojosInformation.TRA.remoteInstallationPackageClassifier_description)
	protected String remoteInstallationPackageClassifier;

	@Parameter(property = InstallerMojosInformation.TRA.configDirectory, defaultValue = InstallerMojosInformation.TRA.configDirectory_default)
	private File configDirectory;

	@Override
	public void initDefaultParameters() throws MojoExecutionException {
		super.initDefaultParameters();

		if (configDirectory == null) {
			File defaultConfigDirectoryRoot = new File(System.getProperty("user.home"), "tibco_cfg");
			if (defaultConfigDirectoryRoot.exists() && getCreateNewEnvironment()) {
				throw new MojoExecutionException("Configuration directory '" + defaultConfigDirectoryRoot.getAbsolutePath() + "' already exists. Please choose another one by setting '" + InstallerMojosInformation.TRA.configDirectory + "' property.", new FileAlreadyExistsException(defaultConfigDirectoryRoot.getAbsolutePath()));
			} else {
				configDirectory = defaultConfigDirectoryRoot;
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
		props.setProperty("configDirectoryRoot", configDirectory.toString());
	}

	@Override
	public String getProductName() {
		return "TIBCO Runtime Agent (TRA)";
	}

	@Override
	public ProductType getProductType() {
		return ProductType.TRA;
	}

}
