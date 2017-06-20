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
* This goal installs the TIBCO Enterprise Administrator 2.x product from an
* official archive to a target environment.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "tea-install", requiresProject = false)
public class TEAInstallerMojo extends CommonInstaller {

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.installationPackage, defaultValue = "${" + InstallerMojosInformation.installationPackageDirectory + "}/${tibco.tea.installation.package.regex}")
	private File installationPackage;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegex, defaultValue = InstallerMojosInformation.EnterpriseAdministrator.installationPackageRegex_default)
	private String installationPackageRegex;

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

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageClassifier, defaultValue = "", description = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageClassifier_description)
	protected String remoteInstallationPackageClassifier;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.javaHomeDirectory, defaultValue = "c:\\Program Files\\Java\\jdk1.7.0_71") // default value is the one found in default .silent file
	private File javaHomeDirectory;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.teaWindowsServiceType, defaultValue = "manual")
	private String teaWindowsServiceType;

	@Parameter(property = InstallerMojosInformation.EnterpriseAdministrator.configDirectoryRoot, defaultValue = "C:\\ProgramData\\tibco")
	private String configDirectoryRoot;

	@Override
	public List<String> getDependenciesGoals() {
		return new ArrayList<String>();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// empty but will be modified at compile-time
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
	public String getRemotePackageClassifier() {
		return remoteInstallationPackageClassifier;
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

		props.setProperty("configDirectoryRoot", configDirectoryRoot);
		props.setProperty("java.home.directory", javaHomeDirectory.getAbsolutePath());
		props.setProperty("teaWindowsServiceType", teaWindowsServiceType);
	}

	@Override
	public String getProductName() {
		return "TIBCO Enterprise Administrator (TEA)";
	}

}
