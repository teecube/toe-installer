/**
 * (C) Copyright 2014-2016 teecube
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

import java.io.File;
import java.util.Iterator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.tibco.envinfo.TIBCOEnvironment.Environment;

import t3.plugin.annotations.Mojo;

/**
* <p>
* This goal removes all existing environment from <em>'_envInfo.xml'</em> files
* on the current machine whose "TIBCO_HOME" directory does not exist.<br />
* This will <strong>not</strong> remove any directory on the filesystem
* (since "TIBCO_HOME" directory does not exist).
* </p>
* <p>
* To a see a list of all environments on the current machine see
* <a href="./envinfo-list-mojo.html">envinfo-list goal</a>.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "envinfo-remove-empty", requiresProject = false)
public class RemoveEmptyEnvInfoMojo extends EnvInfo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		getLog().info("");
		for (Iterator<Environment> it = getEnvironments().values().iterator(); it.hasNext();) {
			Environment env = (Environment) it.next();

			if (!new File(env.getLocation()).exists()) {
				getLog().info("Removing non existing environment : " + env.getName() + "=" + env.getLocation());
				it.remove();
			}
		}

		saveEnvironments(getEnvironments(), envInfosFiles);

		getLog().info("");
		getLog().info("Remaining environments:");
		displayEnvironments();
	}

}
