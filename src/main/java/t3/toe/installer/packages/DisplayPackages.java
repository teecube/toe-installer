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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import t3.plugin.annotations.Mojo;

/**
* <p>
* This goal displays resolved TIBCO installation packages found in a given directory.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "packages-display", requiresProject = false)
public class DisplayPackages extends AbstractPackagesResolver {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Resolving local TIBCO installation packages in directory: " + installationPackageDirectory.getAbsolutePath());
		
		super.execute();

		if (installers.size() > 0) {
			getLog().info("");
			getLog().info("These TIBCO installation packages can be automatically:");
			getLog().info("  installed to the local Maven repository by running 'mvn toe:packages-install'");
			getLog().info("  deployed to a remote Maven repository by running 'mvn toe:packages-deploy -Dtibco.remote=<repo>'");
		}

	}

}
