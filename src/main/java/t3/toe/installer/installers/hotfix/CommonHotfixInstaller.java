/**
 * (C) Copyright 2016-2018 teecube
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
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerMojosInformation;

import java.io.*;
import java.util.Properties;

/**
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public abstract class CommonHotfixInstaller extends AbstractCommonHotfixInstaller {

	public abstract File getInstallationPackage();

	private File silentFile;

	@Component
	protected ArchiverManager archiverManager;

	@Override
	public File getInstallationPackage(boolean resolve) throws MojoExecutionException {
		return getInstallationPackage();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		File universalInstaller = getUniversalInstaller();

		File installationPackage = getInstallationPackage();
		if (installationPackage == null || !installationPackage.exists()) {
			throw new MojoExecutionException("Hotfix installation package was not found.", new FileNotFoundException());
		}

		extractInstallationPackage(installationPackage);

		File tmpDirectory = Files.createTempDir();

		try {
			UnArchiver unArchiver = archiverManager.getUnArchiver(installationPackage);

			unArchiver.setSourceFile(installationPackage);
			unArchiver.setDestDirectory(tmpDirectory);

			getLog().info("-> Package extraction");
			getLog().info("");
			getLog().info("Extracting installation package to : " + tmpDirectory.getAbsolutePath());

			unArchiver.extract();

			silentFile = CommonInstaller.getSilentFile(tmpDirectory);

			Properties props = new Properties();
			props.loadFromXML(new FileInputStream(silentFile));

			props.setProperty("acceptLicense", "true"); // always true
			props.setProperty("environmentName", environmentName.toString());
			props.setProperty("installationRoot", installationRoot.getAbsolutePath());

			props.storeToXML(new FileOutputStream(silentFile), "Automatic install");

			FileUtils.copyFileToDirectory(universalInstaller, tmpDirectory); // copy Universal Installer in temp directory

			getLog().info(tmpDirectory.getAbsolutePath());
		} catch (NoSuchArchiverException | IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

	}

	private File getUniversalInstaller() throws MojoExecutionException {
		File universalInstallerDirectory = new File(installationRoot, "tools/universal_installer");

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("TIBCOUniversalInstaller");
			}
		};

		File[] universalInstallers = universalInstallerDirectory.listFiles(filter);
		File universalInstaller = null;
		if (universalInstallers.length > 0) {
			universalInstaller = universalInstallers[0];
		} else {
			throw new MojoExecutionException("TIBCO universal installer was not found.", new FileNotFoundException());
		}

		getLog().info(universalInstaller.getAbsolutePath());
		return universalInstaller;
	}

}
