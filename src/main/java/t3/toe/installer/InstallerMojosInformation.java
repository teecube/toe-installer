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

	public final static String pluginPrefix = "toe:";

	public static final String installationRoot = "tibco.installation.root";
	public static final String installationRoot_description = "tibco.installation.root";
	public static final String installationRoot_default = "";

	public static final String environmentName = "tibco.installation.environmentName";
	public static final String environmentName_default = "TIBCO-HOME";
	public static final String environmentName_description = "tibco.installation.environmentName";

	public static final String overwriteExistingProfile = "tibco.configuration.overwriteExistingProfile";
	public static final String overwriteExistingProfile_default = "true";
	public static final String overwriteExistingProfile_description = "tibco.configuration.overwriteExistingProfile";

	public static final String useGlobalSettings = "tibco.configuration.useGlobalSettings";
	public static final String useGlobalSettings_default = "false";
	public static final String useGlobalSettings_description = "tibco.configuration.useGlobalSettings";
	
	public static final String writeToSettings = "tibco.configuration.writeToSettings";
	public static final String writeToSettings_default = "false";
	public static final String writeToSettings_description = "tibco.configuration.writeToSettings";

	public static final String createNewEnvironment = "tibco.installation.createNew";
	public static final String createNewEnvironment_default = "true";
	public static final String createNewEnvironment_description = "tibco.installation.createNew";

	public static final String removeExistingEnvironment = "tibco.installation.removeExisting";
	public static final String removeExistingEnvironment_default = "false";
	public static final String removeExistingEnvironment_description = "tibco.installation.removeExisting";

	public static final String ignoreDependencies = "tibco.installation.ignoreDependencies";
	public static final String ignoreDependencies_default = "false";
	public static final String ignoreDependencies_description = "tibco.installation.ignoreDependencies";
	
	public static final String installationPackageDirectory = "tibco.installation.packages.directory";
	public static final String installationPackageDirectory_default = "${basedir}";
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
		public static final String installationPackageArch = "tibco.rv.installation.package.arch";
		public static final String installationPackageOs = "tibco.rv.installation.package.os";

		public static final String remoteInstallationPackageGroupId = "tibco.rv.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_description = "tibco.rv.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_default = "com.tibco.rv";

		public static final String remoteInstallationPackageArtifactId = "tibco.rv.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_description = "tibco.rv.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_default = "rv-installer";

		public static final String remoteInstallationPackageVersion = "tibco.rv.installation.remotePackage.version";
		public static final String remoteInstallationPackageVersion_description = "tibco.rv.installation.remotePackage.version";

		public static final String remoteInstallationPackageClassifier = "tibco.rv.installation.remotePackage.classifier";
		public static final String remoteInstallationPackageClassifier_description = "tibco.rv.installation.remotePackage.classifier";

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
		public static final String installationPackageArch = "tibco.tra.installation.package.arch";
		public static final String installationPackageOs = "tibco.tra.installation.package.os";

		public static final String remoteInstallationPackageGroupId = "tibco.tra.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_description = "tibco.tra.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_default = "com.tibco.tra";

		public static final String remoteInstallationPackageArtifactId = "tibco.tra.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_description = "tibco.tra.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_default = "tra-installer";

		public static final String remoteInstallationPackageVersion = "tibco.tra.installation.remotePackage.version";
		public static final String remoteInstallationPackageVersion_description = "tibco.tra.installation.remotePackage.version";

		public static final String remoteInstallationPackageClassifier = "tibco.tra.installation.remotePackage.classifier";
		public static final String remoteInstallationPackageClassifier_description = "tibco.tra.installation.remotePackage.classifier";

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
		public static final String installationPackageArch = "tibco.ems.installation.package.arch";
		public static final String installationPackageOs = "tibco.ems.installation.package.os";

		public static final String remoteInstallationPackageGroupId = "tibco.ems.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_description = "tibco.ems.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_default = "com.tibco.ems";

		public static final String remoteInstallationPackageArtifactId = "tibco.ems.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_description = "tibco.ems.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_default = "ems-installer";

		public static final String remoteInstallationPackageVersion = "tibco.ems.installation.remotePackage.version";
		public static final String remoteInstallationPackageVersion_description = "tibco.ems.installation.remotePackage.version";

		public static final String remoteInstallationPackageClassifier = "tibco.ems.installation.remotePackage.classifier";
		public static final String remoteInstallationPackageClassifier_description = "tibco.ems.installation.remotePackage.classifier";
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
		public static final String installationPackageArch = "tibco.bw5.installation.package.arch";
		public static final String installationPackageOs = "tibco.bw5.installation.package.os";

		public static final String remoteInstallationPackageGroupId = "tibco.bw5.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_description = "tibco.bw5.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_default = "com.tibco.bw5";

		public static final String remoteInstallationPackageArtifactId = "tibco.bw5.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_description = "tibco.bw5.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_default = "bw5-installer";

		public static final String remoteInstallationPackageVersion = "tibco.bw5.installation.remotePackage.version";
		public static final String remoteInstallationPackageVersion_description = "tibco.bw5.installation.remotePackage.version";

		public static final String remoteInstallationPackageClassifier = "tibco.bw5.installation.remotePackage.classifier";
		public static final String remoteInstallationPackageClassifier_description = "tibco.bw5.installation.remotePackage.classifier";

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
		public static final String installationPackageArch = "tibco.bw6.installation.package.arch";
		public static final String installationPackageOs = "tibco.bw6.installation.package.os";

		public static final String remoteInstallationPackageGroupId = "tibco.bw6.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_description = "tibco.bw6.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_default = "com.tibco.bw6";

		public static final String remoteInstallationPackageArtifactId = "tibco.bw6.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_description = "tibco.bw6.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_default = "bw6-installer";
		
		public static final String remoteInstallationPackageVersion = "tibco.bw6.installation.remotePackage.version";
		public static final String remoteInstallationPackageVersion_description = "tibco.bw6.installation.remotePackage.version";

		public static final String remoteInstallationPackageClassifier = "tibco.bw6.installation.remotePackage.classifier";
		public static final String remoteInstallationPackageClassifier_description = "tibco.bw6.installation.remotePackage.classifier";

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
		public static final String installationPackageRegex_default = "TIB_(tibcoa|TIBCOA)dmin_(5.\\d+.\\d+)_([^_]*)_(.*).zip";

		public static final String installationPackageRegexVersionGroupIndex = "tibco.admin.installation.package.regex.versionGroupIndex";
		public static final String installationPackageRegexVersionGroupIndex_default = "2";

		public static final String installationPackageRegexOsGroupIndex = "tibco.admin.installation.package.regex.osGroupIndex";
		public static final String installationPackageRegexOsGroupIndex_default = "3";

		public static final String installationPackageRegexArchGroupIndex = "tibco.admin.installation.package.regex.archGroupIndex";
		public static final String installationPackageRegexArchGroupIndex_default = "4";

		public static final String installationPackageVersion = "tibco.admin.installation.package.version";
		public static final String installationPackageVersionMajorMinor = "tibco.admin.installation.package.versionMajorMinor";
		public static final String installationPackageArch = "tibco.admin.installation.package.arch";
		public static final String installationPackageOs = "tibco.admin.installation.package.os";

		public static final String remoteInstallationPackageGroupId = "tibco.admin.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_description = "tibco.admin.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_default = "com.tibco.admin";

		public static final String remoteInstallationPackageArtifactId = "tibco.admin.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_description = "tibco.admin.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_default = "admin-installer";

		public static final String remoteInstallationPackageVersion = "tibco.admin.installation.remotePackage.version";
		public static final String remoteInstallationPackageVersion_description = "tibco.admin.installation.remotePackage.version";

		public static final String remoteInstallationPackageClassifier = "tibco.admin.installation.remotePackage.classifier";
		public static final String remoteInstallationPackageClassifier_description = "tibco.admin.installation.remotePackage.classifier";
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
		public static final String installationPackageArch = "tibco.tea.installation.package.arch";
		public static final String installationPackageOs = "tibco.tea.installation.package.os";

		public static final String remoteInstallationPackageGroupId = "tibco.tea.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_description = "tibco.tea.installation.remotePackage.groupId";
		public static final String remoteInstallationPackageGroupId_default = "com.tibco.tea";

		public static final String remoteInstallationPackageArtifactId = "tibco.tea.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_description = "tibco.tea.installation.remotePackage.artifactId";
		public static final String remoteInstallationPackageArtifactId_default = "tea-installer";

		public static final String remoteInstallationPackageVersion = "tibco.tea.installation.remotePackage.version";
		public static final String remoteInstallationPackageVersion_description = "tibco.tea.installation.remotePackage.version";

		public static final String remoteInstallationPackageClassifier = "tibco.tea.installation.remotePackage.classifier";
		public static final String remoteInstallationPackageClassifier_description = "tibco.tea.installation.remotePackage.classifier";

		public static final String javaHomeDirectory = "tibco.tea.installation.javaHomeDirectory";
		public static final String teaWindowsServiceType = "tibco.tea.installation.teaWindowsServiceType";
		public static final String configDirectoryRoot = "tibco.tea.installation.configDirectoryRoot";
	}

	public static class Packages {
		public static class Install {
			public static final String localRepositoryPath = "tibco.packages.install.localRepositoryPath";
			public static final String localRepositoryPath_default = "${localRepository}";			
		}

		public static class Deploy {
			public static final String remoteRepositoryId = "tibco.packages.deploy.remoteRepositoryId";
			public static final String remoteRepositoryId_default = "";

			public static final String remoteRepositoryURL = "tibco.packages.deploy.remoteRepositoryURL";
			public static final String remoteRepositoryURL_default = "";
		}

		public static class Standalone {
			public static final String directory = "tibco.packages.standalone.directory";
			public static final String directory_default = "${project.build.directory}/standalone";

			public static final String localRepository = "tibco.packages.standalone.localRepository";
			public static final String localRepository_default = "${tibco.packages.standalone.directory}/repository";

			public static final String generateSettings = "tibco.packages.standalone.generateSettings";
			public static final String generateSettings_default = "true";

			public static final String includeTIBCOInstallationPackages = "tibco.packages.standalone.includeTIBCOInstallationPackages";
			public static final String includeTIBCOInstallationPackages_default = "true";

			public static final String topologyGenerate = "tibco.packages.standalone.topology.generate";
			public static final String topologyGenerate_default = "true";

			public static final String topologyGeneratedFile = "tibco.packages.standalone.topology.generatedFile";
			public static final String topologyGeneratedFile_default = "${tibco.packages.standalone.directory}/tibco-environments.xml";

			public static class Archive {
				public static final String archive = "tibco.packages.standalone.archive";
				public static final String archive_default = "${project.build.directory}/t3-standalone.zip";

				public static final String generate = "tibco.packages.standalone.archive.generate";
				public static final String generate_default = "true";				

			}

			public static class Plugins {
				public static final String include = "tibco.packages.standalone.plugins.include";
				public static final String include_default = "true";

				public static final String list = "tibco.packages.standalone.plugins.list";
				public static final String list_default = "TOE_DOMAINS,TOE_INSTALLER,TIC_BW5,TIC_BW6,TAC_ARCHETYPES";

				public static final String toeDomainsVersion = "tibco.packages.standalone.plugins.toeDomainsVersion";
				public static final String toeDomainsVersion_default = "";

				public static final String toeInstallerVersion = "tibco.packages.standalone.plugins.toeInstallerVersion";
				public static final String toeInstallerVersion_default = "";

				public static final String ticBW5Version = "tibco.packages.standalone.plugins.ticBW5Version";
				public static final String ticBW5Version_default = "";

				public static final String ticBW6Version = "tibco.packages.standalone.plugins.ticBW6Version";
				public static final String ticBW6Version_default = "";

				public static final String tacArchetypesVersion = "tibco.packages.standalone.plugins.tacArchetypesVersion";
				public static final String tacArchetypesVersion_default = "";

				public static final String tacArchetypesArtifactsId = "tibco.packages.standalone.plugins.tacArchetypesArtifactsId";
				public static final String tacArchetypesArtifactsId_default = "default-bw5-ear,default-bw6-app-module,default-bw6-application";
			}

		}
	}

	public static class FullEnvironment {
		public static final String topologyFile = "tibco.environments.topology";
		public static final String topologyFile_default = "${basedir}/tibco-environments.xml";

		public static final String topologyGenerate = "tibco.environments.topology.generate";
		public static final String topologyGenerate_default = "false";

		public static final String topologyGenerateWithTemplate = "tibco.environments.topology.generate.withTemplate";
		public static final String topologyGenerateWithTemplate_default = "true";

		public static final String topologyGeneratedFile = "tibco.environments.topology.generatedFile";
		public static final String topologyGeneratedFile_default = "${project.build.directory}/tibco-environments.xml";

		public static final String topologyType = "tibco.environments.topology.type";
		public static final String topologyType_default = "LOCAL";
	}

}
