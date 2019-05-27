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
* This goal installs the TIBCO Enterprise Administrator 2.x product from an
* official archive to a target environment.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "install-tea", requiresProject = false)
public class TEAInstallerMojo extends CommonInstaller {

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.installationPackage, defaultValue = "${" + InstallerMojosInformation.installationPackageDirectory + "}/${tibco.tea.installation.package.regex}")
	private File installationPackage;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegex, defaultValue = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegex_default)
	private String installationPackageRegex;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegexArchGroupIndex, defaultValue = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegexArchGroupIndex_default)
	private Integer installationPackageRegexArchGroupIndex;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegexOsGroupIndex, defaultValue = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegexOsGroupIndex_default)
	private Integer installationPackageRegexOsGroupIndex;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegexVersionGroupIndex, defaultValue = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegexVersionGroupIndex_default)
	private Integer installationPackageRegexVersionGroupIndex;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.installationPackageVersion, defaultValue = "")
	private String installationPackageVersion;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.installationPackageVersionMajorMinor, defaultValue = "")
	private String installationPackageVersionMajorMinor;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageGroupId, defaultValue = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageGroupId_default, description = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageGroupId_description)
	protected String remoteInstallationPackageGroupId;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageArtifactId, defaultValue = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageArtifactId_default, description = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageArtifactId_description)
	protected String remoteInstallationPackageArtifactId;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageVersion, defaultValue = "", description = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageVersion_description)
	protected String remoteInstallationPackageVersion;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackagePackaging, defaultValue = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackagePackaging_default, description = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackagePackaging_description)
	protected String remoteInstallationPackagePackaging;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageClassifier, defaultValue = "", description = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageClassifier_description)
	protected String remoteInstallationPackageClassifier;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.javaHomeDirectory, defaultValue = "c:\\Program Files\\Java\\jdk1.7.0_71") // default value is the one found in default .silent file
	private File javaHomeDirectory;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.teaWindowsServiceType, defaultValue = "manual")
	private String teaWindowsServiceType;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.configDirectory, defaultValue = InstallerMojosInformation.EnterpriseAdministrator.configDirectory_default)
	private String configDirectory;

	@Override
	public List<String> getDependenciesGoals() {
		return new ArrayList<String>();
	}

	@Override
	public void configureBuild(TIBCOProductToInstall tibcoProductToInstall, File standaloneLocalRepository) {

	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// empty but will be modified at compile-time
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
	public void setInstallationPackageRegex(String installationPackageRegex) {
		this.installationPackageRegex = installationPackageRegex;
	}

	@Override
	public String getInstallationPackagePropertyName() {
		return InstallerMojosInformation.EnterpriseAdministrator.installationPackage;
	}

	@Override
	public String getInstallationPackageVersionPropertyName() {
		return InstallerMojosInformation.EnterpriseAdministrator.installationPackageVersion;
	}

	@Override
	public String getInstallationPackageVersionMajorMinorPropertyName() {
		return InstallerMojosInformation.EnterpriseAdministrator.installationPackageVersionMajorMinor;
	}

	@Override
	public String getInstallationPackageArchPropertyName() {
		return InstallerMojosInformation.EnterpriseAdministrator.installationPackageArch;
	}

	@Override
	public String getInstallationPackageOsPropertyName() {
		return InstallerMojosInformation.EnterpriseAdministrator.installationPackageOs;
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
	public void setInstallationPackageRegexVersionGroupIndex(Integer installationPackageRegexVersionGroupIndex) {
		this.installationPackageRegexVersionGroupIndex = installationPackageRegexVersionGroupIndex;
	}

	@Override
	public boolean dependenciesExist() {
		return true;
	}

	@Override
	public boolean installationExists() throws MojoExecutionException {
		return getInstallationRoot() != null && getInstallationRoot().exists() && new File(getInstallationRoot(), "tea/" + getInstallationPackageVersionMajorMinor()).exists();
	}

	@Override
	public boolean hasDependencies() {
		return false;
	}

	@Override
	public void setProperties(Properties props) {
		if (props == null) {
			return;
		}

		if (!javaHomeDirectory.exists()) {
			String javaHome = System.getProperty("java.home");
			if (new File(javaHome).exists()) {
				javaHomeDirectory = new File(javaHome);
			}
		}

		props.setProperty("configDirectoryRoot", configDirectory);
		props.setProperty("java.home.directory", javaHomeDirectory.getAbsolutePath());
		props.setProperty("teaWindowsServiceType", teaWindowsServiceType);
	}

	@Override
	public String getProductName() {
		return "TIBCO Enterprise Administrator (TEA)";
	}

	@Override
	public ProductType getProductType() {
		return ProductType.TEA;
	}


}
