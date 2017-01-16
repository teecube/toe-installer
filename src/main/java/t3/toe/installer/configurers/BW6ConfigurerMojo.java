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
package t3.toe.installer.configurers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.tibco.envinfo.TIBCOEnvironment.Environment;

import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This goal generates a ready-to-use Maven profile to include in settings.xml
* file to configure the use of a TIBCO BusinessWorks 6.x installation.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "bw6-configure", requiresProject = false)
public class BW6ConfigurerMojo extends CommonConfigurer {

	@Parameter(property = InstallerMojosInformation.BW6.pluginGroupId, defaultValue = InstallerMojosInformation.BW6.pluginGroupId_default)
	protected String groupId;

	@Parameter(property = InstallerMojosInformation.BW6.pluginArtifactId, defaultValue = InstallerMojosInformation.BW6.pluginArtifactId_default)
	protected String artifactId;

	@Parameter(property = InstallerMojosInformation.BW6.bootstrapClass, defaultValue = InstallerMojosInformation.BW6.bootstrapClass_default)
	protected String bootstrapClass;

	private Properties profileProperties = null;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
	}

	@Override
	public String getGroupId() {
		return groupId;
	}

	@Override
	public String getArtifactId() {
		return artifactId;
	}

	@Override
	public String getBootstrapClass() {
		return bootstrapClass;
	}

	@Override
	protected Properties getProfileProperties(Environment currentEnvironment) {
		if (profileProperties != null) {
			return profileProperties;
		}

		profileProperties = new Properties();

		File tibcoHome = new File(currentEnvironment.getLocation());
		if (tibcoHome == null || !tibcoHome.exists() || !tibcoHome.isDirectory()) {
			return profileProperties;
		}
		profileProperties.put("tibco.home", tibcoHome.getAbsolutePath());

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("6.");
			}
		};

		File[] bwDirectory = new File(tibcoHome, "bw").listFiles(filter);
		if (bwDirectory.length > 0) {
			profileProperties.put("tibco.bw6.version", bwDirectory[0].getName());
		}

		return profileProperties;
	}

	@Override
	protected boolean validateProfileProperties(Environment environment) {
		return getProfileProperties(environment).keySet().size() == 2;
	}

	@Override
	protected String getProductInstallationGoal() {
		return "toe:bw6-install";
	}

	@Override
	public String getProductName() {
		return "TIBCO BusinessWorks 6.x";
	}

}
