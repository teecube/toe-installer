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
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This goal lists all environments installed on the current machine as found in
* <em>'_envInfo.xml'</em> files.
* </p>
* <p>
* To a remove an environment see
* <a href="./envinfo-remove-mojo.html">envinfo-remove goal</a>.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "envinfo-list", requiresProject = false)
public class ListEnvInfoMojo extends EnvInfo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		displayEnvironments();
		
		getLog().info("");
		getLog().info("To remove an environment, use: 'mvn " + InstallerMojosInformation.pluginPrefix + "envinfo-remove -D" + InstallerMojosInformation.environmentName + "=<EnvironmentName>'");
	}

}
