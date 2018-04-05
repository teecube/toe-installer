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
package t3.toe.installer.envinfo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This goal removes an existing environment from <em>'_envInfo.xml'</em> files
* on the current machine.<br />
* This will <strong>not</strong> remove any directory on the filesystem
* (such as "TIBCO_HOME" directory).
* </p>
* <p>
* To a see a list of all environments on the current machine see 
* <a href="./envinfo-list-mojo.html">envinfo-list goal</a>.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "envinfo-remove", requiresProject = false)
public class RemoveEnvInfoMojo extends EnvInfo {

	@Parameter(property = InstallerMojosInformation.environmentName, /*description = InstallerMojosInformation.environmentName_description,*/ defaultValue = "")
	public String environmentName;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		getLog().info("");
		if (!getEnvironments().containsKey(environmentName)) {
			getLog().warn("The environment '" + environmentName + "' to remove does not exist: skipping.");
			return;
		}
		getLog().info("Removing environment '" + environmentName + "'");
		getEnvironments().remove(environmentName);

		saveEnvironments(getEnvironments(), envInfosFiles);

		getLog().info("");
		getLog().info("Remaining environments:");
		displayEnvironments();
	}

}
