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
import java.io.FilenameFilter;
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
* This goal installs the TIBCO BusinessWorks 6.x product from an official
* archive to a target environment.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "bw6-install", requiresProject = false)
public class BW6InstallerMojo extends CommonInstaller {

	@Parameter(property = InstallerMojosInformation.BW6.installationPackage, defaultValue = "${" + InstallerMojosInformation.installationPackageDirectory + "}/${tibco.bw6.installation.package.regex}")
	private File installationPackage;

	@Parameter(property = InstallerMojosInformation.BW6.installationPackageRegex, defaultValue = InstallerMojosInformation.BW6.installationPackageRegex_default)
	private String installationPackageRegex;

	@Parameter(property = InstallerMojosInformation.BW6.installationPackageRegexArchGroupIndex, defaultValue = InstallerMojosInformation.BW6.installationPackageRegexArchGroupIndex_default)
	private Integer installationPackageRegexArchGroupIndex;

	@Parameter(property = InstallerMojosInformation.BW6.installationPackageRegexOsGroupIndex, defaultValue = InstallerMojosInformation.BW6.installationPackageRegexOsGroupIndex_default)
	private Integer installationPackageRegexOsGroupIndex;

	@Parameter(property = InstallerMojosInformation.BW6.installationPackageRegexVersionGroupIndex, defaultValue = InstallerMojosInformation.BW6.installationPackageRegexVersionGroupIndex_default)
	private Integer installationPackageRegexVersionGroupIndex;
	
	@Parameter(property = InstallerMojosInformation.BW6.installationPackageVersion, defaultValue = "")
	private String installationPackageVersion;

	@Parameter(property = InstallerMojosInformation.BW6.installationPackageVersionMajorMinor, defaultValue = "")
	private String installationPackageVersionMajorMinor;

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
		return InstallerMojosInformation.BW6.installationPackage;
	}

	@Override
	public String getInstallationPackageVersionPropertyName() {
		return InstallerMojosInformation.BW6.installationPackageVersion;
	}

	@Override
	public String getInstallationPackageVersionMajorMinorPropertyName() {
		return InstallerMojosInformation.BW6.installationPackageVersionMajorMinor;
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
	public boolean dependenciesExist() {
		return true;
	}

	@Override
	public boolean installationExists() throws MojoExecutionException {
		return getInstallationRoot() != null && getInstallationRoot().exists() && new File(getInstallationRoot(), "bw/" + getInstallationPackageVersionMajorMinor()).exists();
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
		
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return dir.isDirectory() && name.startsWith("product_tibco_sunec_");
			}
		};

		File[] sunec = installationPackageDirectory.listFiles(filter);

		if (sunec.length > 0) {
			props.setProperty("LGPLAssemblyDownload", "false");
			props.setProperty("LGPLAssemblyPath", sunec[0].getParentFile().getAbsolutePath());
		}
	}

	@Override
	public String getProductName() {
		return "TIBCO BusinessWorks 6.x";
	}

}
