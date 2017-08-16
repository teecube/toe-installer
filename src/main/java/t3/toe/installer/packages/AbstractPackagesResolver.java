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
package t3.toe.installer.packages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosFactory;
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This class resolves all TIBCO installation packages found in a given directory.
* It can be subclassed to display found packages or to install/deploy them in Maven.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public abstract class AbstractPackagesResolver extends CommonMojo {

	protected List<CommonInstaller> installers;

	@Parameter(property = InstallerMojosInformation.installationPackageDirectory, defaultValue = "${basedir}")
	protected File installationPackageDirectory;

	@Override
	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() throws MojoExecutionException {
		return new InstallerLifecycleParticipant();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// empty but will be modified at compile-time
		super.execute();

		installers = new ArrayList<CommonInstaller>();
		for (Class<? extends CommonInstaller> installerClass : InstallerMojosFactory.getInstallersClasses()) {
			CommonInstaller installer;
			try {
				installer = installerClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
			installer.setSession(session);
			installer.setInstallationPackageDirectory(this.installationPackageDirectory);
			installer.initStandalonePOMNoDefaultParameters();

			File installationPackage = installer.getInstallationPackage();
			if (installationPackage != null && installationPackage.exists()) {
				installers.add(installer);
			}
		}
	}

}
