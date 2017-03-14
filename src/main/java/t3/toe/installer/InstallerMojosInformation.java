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
package t3.toe.installer;

import t3.CommonMojoInformation;
import t3.plugin.annotations.Categories;
import t3.plugin.annotations.Category;

/**
* <p>
* Centralization of all Mojo parameters.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Categories({
	@Category(title = InstallerMojosInformation.BW5.category, description = InstallerMojosInformation.BW5.category_description),
})
public class InstallerMojosInformation extends CommonMojoInformation {

	public static final String installationRoot = "tibco.installation.root";
	public static final String installationRoot_description = "tibco.installation.root";
	public static final String installationRoot_default = "";

	public static final String environmentName = "tibco.installation.environmentName";
	public static final String environmentName_default = "TIBCO-HOME";
	public static final String environmentName_description = "tibco.installation.environmentName";

	public static final String writeToSettings = "tibco.configuration.writeToSettings";
	public static final String writeToSettings_description = "tibco.configuration.writeToSettings";

	public static final String createNewEnvironment = "tibco.installation.createNew";
	public static final String createNewEnvironment_default = "true";
	public static final String createNewEnvironment_description = "tibco.installation.createNew";

	public static final String removeExistingEnvironment = "tibco.installation.removeExisting";
	public static final String removeExistingEnvironment_default = "false";
	public static final String removeExistingEnvironment_description = "tibco.installation.removeExisting";

	public static final String installationPackageDirectory = "tibco.installation.packages.directory";
	public static final String installationPackageDirectory_description = "tibco.installation.packages.directory";

	public static final String dotTIBCOHome = "tibco.dothome.directory";
	public static final String dotTIBCOHome_default = "${user.home}/.TIBCO";
	public static final String dotTIBCOHome_description = "tibco.dothome.directory";

	public static class RV {
		public static final String category = "TIBCO RendezVous";
		public static final String category_description = "Properties concerning " + category + " binaries & environment";

		public static final String installationPackage = "tibco.rv.installation.package";
		public static final String installationPackage_description = "tibco.rv.installation.package";

		public static final String installationPackageRegex = "tibco.rv.installation.package.regex";
		public static final String installationPackageRegex_default = "TIB_RV_(8.\\d+.\\d+)_([^_]*)_(.*).zip";

		public static final String installationPackageRegexVersionGroupIndex = "tibco.rv.installation.package.regex.versionGroupIndex";
		public static final String installationPackageRegexVersionGroupIndex_default = "1";

		public static final String installationPackageRegexOsGroupIndex = "tibco.rv.installation.package.regex.osGroupIndex";
		public static final String installationPackageRegexOsGroupIndex_default = "2";

		public static final String installationPackageRegexArchGroupIndex = "tibco.rv.installation.package.regex.archGroupIndex";
		public static final String installationPackageRegexArchGroupIndex_default = "3";

		public static final String installationPackageVersion = "tibco.rv.installation.package.version";
		public static final String installationPackageVersionMajorMinor = "tibco.rv.installation.package.versionMajorMinor";

		public static final String configDirectoryRoot = "tibco.rv.configDirectoryRoot";
	}

	public static class TRA {
		public static final String category = "TIBCO Runtime Agent (TRA)";
		public static final String category_description = "Properties concerning " + category + " binaries & environment";

		public static final String installationPackage = "tibco.tra.installation.package";
		public static final String installationPackage_description = "tibco.tra.installation.package";

		public static final String installationPackageRegex = "tibco.tra.installation.package.regex";
		public static final String installationPackageRegex_default = "TIB_TRA_(5.\\d+.\\d+)_([^_]*)_(.*).zip";

		public static final String installationPackageRegexVersionGroupIndex = "tibco.tra.installation.package.regex.versionGroupIndex";
		public static final String installationPackageRegexVersionGroupIndex_default = "1";

		public static final String installationPackageRegexOsGroupIndex = "tibco.tra.installation.package.regex.osGroupIndex";
		public static final String installationPackageRegexOsGroupIndex_default = "2";

		public static final String installationPackageRegexArchGroupIndex = "tibco.tra.installation.package.regex.archGroupIndex";
		public static final String installationPackageRegexArchGroupIndex_default = "3";
		
		public static final String installationPackageVersion = "tibco.tra.installation.package.version";
		public static final String installationPackageVersionMajorMinor = "tibco.tra.installation.package.versionMajorMinor";

		public static final String configDirectory = "tibco.tra.configDirectory";
	}

	public static class EMS {
		public static final String category = "TIBCO EMS";
		public static final String category_description = "Properties concerning " + category + " binaries & environment";

		public static final String installationPackage = "tibco.ems.installation.package";
		public static final String installationPackage_description = "tibco.ems.installation.package";

		public static final String installationPackageRegex = "tibco.ems.installation.package.regex";
		public static final String installationPackageRegex_default = "TIB_ems_(\\d+.\\d+.\\d+)_([^_]*)_(.*).zip";

		public static final String installationPackageRegexVersionGroupIndex = "tibco.ems.installation.package.regex.versionGroupIndex";
		public static final String installationPackageRegexVersionGroupIndex_default = "1";

		public static final String installationPackageRegexOsGroupIndex = "tibco.ems.installation.package.regex.osGroupIndex";
		public static final String installationPackageRegexOsGroupIndex_default = "2";

		public static final String installationPackageRegexArchGroupIndex = "tibco.ems.installation.package.regex.archGroupIndex";
		public static final String installationPackageRegexArchGroupIndex_default = "3";

		public static final String installationPackageVersion = "tibco.ems.installation.package.version";
		public static final String installationPackageVersionMajorMinor = "tibco.ems.installation.package.versionMajorMinor";
	}

	public static class BW5 {
		public static final String category = "TIBCO BusinessWorks 5";
		public static final String category_description = "Properties concerning " + category + " binaries & environment";

		public static final String installationPackage = "tibco.bw5.installation.package";
		public static final String installationPackage_description = "tibco.bw5.installation.package";

		public static final String installationPackageRegex = "tibco.bw5.installation.package.regex";
		public static final String installationPackageRegex_default = "TIB_BW_(5.\\d+.\\d+)_([^_]*)_(.*).zip";

		public static final String installationPackageRegexVersionGroupIndex = "tibco.bw5.installation.package.regex.versionGroupIndex";
		public static final String installationPackageRegexVersionGroupIndex_default = "1";

		public static final String installationPackageRegexOsGroupIndex = "tibco.bw5.installation.package.regex.osGroupIndex";
		public static final String installationPackageRegexOsGroupIndex_default = "2";

		public static final String installationPackageRegexArchGroupIndex = "tibco.bw5.installation.package.regex.archGroupIndex";
		public static final String installationPackageRegexArchGroupIndex_default = "3";

		public static final String installationPackageVersion = "tibco.bw5.installation.package.version";
		public static final String installationPackageVersionMajorMinor = "tibco.bw5.installation.package.versionMajorMinor";

		public static final String pluginGroupId = "tibco.bw5.plugin.groupId";
		public static final String pluginGroupId_default = "io.teecube.tic";
		public static final String pluginArtifactId = "tibco.bw5.plugin.artifactId";
		public static final String pluginArtifactId_default = "tic-bw5";
		public static final String bootstrapClass = "tibco.bw5.plugin.bootstrapClass";
		public static final String bootstrapClass_default = "t3.tic.bw5.BW5LifecycleParticipant";
	}

	public static class BW6 {
		public static final String category = "TIBCO BusinessWorks 6";
		public static final String category_description = "Properties concerning " + category + " binaries & environment";

		public static final String installationPackage = "tibco.bw6.installation.package";
		public static final String installationPackage_description = "tibco.bw6.installation.package";

		public static final String installationPackageRegex = "tibco.bw6.installation.package.regex";
		public static final String installationPackageRegex_default = "TIB_BW(-dev)?_(6.\\d+.\\d+)_(?!HF)([^_]*)_(.*).zip";

		public static final String installationPackageRegexArchGroupIndex = "tibco.bw6.installation.package.regex.archGroupIndex";
		public static final String installationPackageRegexArchGroupIndex_default = "4";
		
		public static final String installationPackageRegexOsGroupIndex = "tibco.bw6.installation.package.regex.osGroupIndex";
		public static final String installationPackageRegexOsGroupIndex_default = "3";
		
		public static final String installationPackageRegexVersionGroupIndex = "tibco.bw6.installation.package.regex.versionGroupIndex";
		public static final String installationPackageRegexVersionGroupIndex_default = "2";

		public static final String installationPackageVersion = "tibco.bw6.installation.package.version";
		public static final String installationPackageVersionMajorMinor = "tibco.bw6.installation.package.versionMajorMinor";

		public static final String pluginGroupId = "tibco.bw6.plugin.groupId";
		public static final String pluginGroupId_default = "io.teecube.tic";
		public static final String pluginArtifactId = "tibco.bw6.plugin.artifactId";
		public static final String pluginArtifactId_default = "tic-bw6";
		public static final String bootstrapClass = "tibco.bw6.plugin.bootstrapClass";
		public static final String bootstrapClass_default = "t3.tic.bw6.BW6LifecycleParticipant";
	}

	public static class Administrator {
		public static final String category = "TIBCO Administrator";
		public static final String category_description = "Properties concerning " + category + " binaries & environment";

		public static final String installationPackage = "tibco.admin.installation.package";
		public static final String installationPackage_description = "tibco.admin.installation.package";

		public static final String installationPackageRegex = "tibco.admin.installation.package.regex";
		public static final String installationPackageRegex_default = "TIB_tibcoadmin_(5.\\d+.\\d+)_([^_]*)_(.*).zip";

		public static final String installationPackageRegexVersionGroupIndex = "tibco.admin.installation.package.regex.versionGroupIndex";
		public static final String installationPackageRegexVersionGroupIndex_default = "1";

		public static final String installationPackageRegexOsGroupIndex = "tibco.admin.installation.package.regex.osGroupIndex";
		public static final String installationPackageRegexOsGroupIndex_default = "2";

		public static final String installationPackageRegexArchGroupIndex = "tibco.admin.installation.package.regex.archGroupIndex";
		public static final String installationPackageRegexArchGroupIndex_default = "3";

		public static final String installationPackageVersion = "tibco.admin.installation.package.version";
		public static final String installationPackageVersionMajorMinor = "tibco.admin.installation.package.versionMajorMinor";
	}

	public static class EnterpriseAdministrator {
		public static final String category = "TIBCO Enterprise Administrator (TEA)";
		public static final String category_description = "Properties concerning " + category + " binaries & environment";

		public static final String installationPackage = "tibco.tea.installation.package";
		public static final String installationPackage_description = "tibco.tea.installation.package";

		public static final String installationPackageRegex = "tibco.tea.installation.package.regex";
		public static final String installationPackageRegex_default = "TIB_tea_(2.\\d+.\\d+)_([^_]*)_(.*).zip";

		public static final String installationPackageRegexVersionGroupIndex = "tibco.tea.installation.package.regex.versionGroupIndex";
		public static final String installationPackageRegexVersionGroupIndex_default = "1";

		public static final String installationPackageRegexOsGroupIndex = "tibco.tea.installation.package.regex.osGroupIndex";
		public static final String installationPackageRegexOsGroupIndex_default = "2";

		public static final String installationPackageRegexArchGroupIndex = "tibco.tea.installation.package.regex.archGroupIndex";
		public static final String installationPackageRegexArchGroupIndex_default = "3";

		public static final String installationPackageVersion = "tibco.tea.installation.package.version";
		public static final String installationPackageVersionMajorMinor = "tibco.tea.installation.package.versionMajorMinor";

		public static final String javaHomeDirectory = "tibco.tea.installation.javaHomeDirectory";
		public static final String teaWindowsServiceType = "tibco.tea.installation.teaWindowsServiceType";
		public static final String configDirectoryRoot = "tibco.tea.installation.configDirectoryRoot";
	}

}
