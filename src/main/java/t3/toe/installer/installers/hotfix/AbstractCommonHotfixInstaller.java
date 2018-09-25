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

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import t3.CommonMojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.ProductType;

import java.io.*;
import java.util.List;
import java.util.Properties;

/**
* <p>
 *     This class is just an intermediate class inheriting from CommonInstaller and implementing all its abstract
 *     methods. It is inherited by CommonHotfixInstaller.
* </p>
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public abstract class AbstractCommonHotfixInstaller extends CommonInstaller {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

	}

	@Override
	public ProductType getProductType() {
		return null;
	}

	@Override
	public String getInstallationPackageRegex() {
		return "";
	}

	@Override
	public void setInstallationPackageRegex(String installationPackageRegex) {

	}

	@Override
	public Integer getInstallationPackageVersionGroupIndex() {
		return null;
	}

	@Override
	public void setInstallationPackageRegexVersionGroupIndex(Integer installationPackageRegexVersionGroupIndex) {

	}

	@Override
	public Integer getInstallationPackageArchGroupIndex() {
		return null;
	}

	@Override
	public String getInstallationPackageArchPropertyName() {
		return null;
	}

	@Override
	public Integer getInstallationPackageOsGroupIndex() {
		return null;
	}

	@Override
	public String getInstallationPackageOsPropertyName() {
		return null;
	}

	@Override
	public String getInstallationPackagePropertyName() {
		return null;
	}

	@Override
	public String getInstallationPackageVersionPropertyName() {
		return null;
	}

	@Override
	public String getInstallationPackageVersionMajorMinorPropertyName() {
		return null;
	}

	@Override
	public String getInstallationPackageVersionMajorMinor() {
		return null;
	}

	@Override
	public String getRemoteInstallationPackageGroupId() {
		return null;
	}

	@Override
	public String getRemoteInstallationPackageArtifactId() {
		return null;
	}

	@Override
	public String getRemoteInstallationPackagePackaging() {
		return null;
	}

	@Override
	public String getRemoteInstallationPackageClassifier() {
		return null;
	}

	@Override
	public void setRemoteInstallationPackageGroupId(String remoteInstallationPackageGroupId) {

	}

	@Override
	public void setRemoteInstallationPackageArtifactId(String remoteInstallationPackageArtifactId) {

	}

	@Override
	public void setRemoteInstallationPackageVersion(String remoteInstallationPackageVersion) {

	}

	@Override
	public void setRemoteInstallationPackageClassifier(String remoteInstallationPackageClassifier) {

	}

	@Override
	public void setInstallationPackageVersionMajorMinor(String version) {

	}

	@Override
	public boolean hasDependencies() {
		return false;
	}

	@Override
	public boolean installationExists() throws MojoExecutionException {
		return false;
	}

	@Override
	public void setProperties(Properties props) {

	}

	@Override
	public List<String> getDependenciesGoals() {
		return null;
	}
}
