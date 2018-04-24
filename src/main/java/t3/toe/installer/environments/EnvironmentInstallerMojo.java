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
package t3.toe.installer.environments;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.ResolutionScope;
import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.Messages;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.commands.CommandToExecute;
import t3.toe.installer.environments.commands.SystemCommandToExecute;
import t3.toe.installer.environments.products.ProductToInstall;
import t3.toe.installer.environments.products.ProductsToInstall;
import t3.toe.installer.environments.products.TIBCOProductToInstall;
import t3.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
* <p>
* This goal installs a full TIBCO environment composed of several TIBCO products.<br /><br />
* The configuration is specified in an XML file. By default, this file is located in <em>${basedir}</em> and is  named
* <em>tibco-environments.xml</em>.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "env-install", requiresProject = false, requiresDependencyResolution = ResolutionScope.TEST)
public class EnvironmentInstallerMojo extends CommonMojo {

	@Parameter (property = InstallerMojosInformation.FullEnvironment.topologyFile, defaultValue = InstallerMojosInformation.FullEnvironment.topologyFile_default)
	protected File environmentsTopology;

	protected EnvironmentsMarshaller environmentsMarshaller;

	@Override
	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() {
		return new InstallerLifecycleParticipant();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException  {
		super.execute();

		loadTopology();

		EnvironmentsToInstall environmentsToInstall = new EnvironmentsToInstall(environmentsMarshaller.getObject().getEnvironment(), environmentsTopology);

		int environmentsCount = environmentsToInstall.size();
		int environmentsIndex = 1;
		for (EnvironmentToInstall environment : environmentsToInstall) {
			if (environmentsCount > 1) { // multiple environments to install
				getLog().info("=== " + StringUtils.leftPad(Utils.toRoman(environmentsIndex), 3, " ") + ". Environment: " + environment.getName() + " ===");
				getLog().info(Messages.MESSAGE_SPACE);

                TIBCOProductToInstall.firstDependency = true;
			}

			// first check if environment exists and if we can continue according to current strategy (keep, fail, delete)
			com.tibco.envinfo.TIBCOEnvironment.Environment localEnvironment = CommonInstaller.getCurrentEnvironment(environment.getName());
			if (CommonInstaller.environmentExists(localEnvironment)) {
				getLog().info("The environment already exists.");

				environment.setToBeDeleted(deleteEnvironment(environment)); // check whether environment can be deleted
			}

			getLog().info("Environment name to install is : " + environment.getName());
			getLog().info("TIBCO root is                  : " + environment.getTibcoRoot());
			getLog().info("");

			// populate list of products to install in environment
			ProductsToInstall productsToInstall = new ProductsToInstall(environment, this);

			// display the list of products to be installed
			int i = 1;
			for (ProductToInstall product : productsToInstall) {
				String skipped = (product.isSkip() ? " (skipped)" : "");
				String alreadyInstalled = (!product.isSkip() && product.isAlreadyInstalled() ? " (already installed"
																							  + (environment.isToBeDeleted() ? ", will be deleted then reinstalled in new environment" : "")
																							  + (!environment.isToBeDeleted() && IfProductExistsBehaviour.DELETE.equals(product.getIfExists()) ? ", will be deleted then reinstalled in current environment" : "")
																							  + (!environment.isToBeDeleted() && IfProductExistsBehaviour.KEEP.equals(product.getIfExists()) ? ", will not be reinstalled" : "")
																							  + ")" : "");
				String version = (StringUtils.isNotBlank(product.getVersion()) ? " v" + product.getVersion() : "");

				String prefix;
				if (i == 1) {
					prefix = "Products to install            : " + i + ". ";
				} else {
					prefix = "                                 " + i + ". ";
				}
				getLog().info(prefix + StringUtils.rightPad(product.fullProductName(), productsToInstall.getMaxFullProductNameLength(), " ") + version + alreadyInstalled + skipped);
				i++;
			}

			// execute environment pre-install commands
			i = 1;
			if (environment.getPreInstallCommands() != null && !environment.getPreInstallCommands().getCommand().isEmpty()) {
				getLog().info("Executing pre-install commands");
				for (SystemCommand command : environment.getPreInstallCommands().getCommand()) {
					new SystemCommandToExecute(command, this, i, CommandToExecute.CommandType.GLOBAL_PRE).executeCommand();
					i++;
				}
			}

			// install products
			i = 1;
			for (ProductToInstall product : productsToInstall) {
				product.install(environment, i);
                i++;
			}

			getLog().info("");
			getLog().info("End of products installation.");

			// execute environment post-install commands
			i = 1;
			if (environment.getPostInstallCommands() != null && !environment.getPostInstallCommands().getCommand().isEmpty()) {
				getLog().info("");
				getLog().info("Executing post-install commands");
				for (SystemCommand command : environment.getPostInstallCommands().getCommand()) {
					new SystemCommandToExecute(command, this, i, CommandToExecute.CommandType.GLOBAL_POST).executeCommand();
					i++;
				}
			}

			if (environmentsCount > 1 && environmentsIndex < environmentsCount) { // add space between environments
				getLog().info("");
				environmentsIndex++;
			}
		}
	}

	private boolean deleteEnvironment(EnvironmentToInstall environment) throws MojoExecutionException {
		switch (environment.getIfExists()) {
		case DELETE:
			getLog().info("Environment '" + environment.name + "' will be deleted and reinstalled (as specified in topology).");
			getLog().info(Messages.MESSAGE_SPACE);
			return true;
		case FAIL:
			getLog().info(Messages.MESSAGE_SPACE);
			getLog().error("Environment '" + environment.name + "' already exists and cannot be updated nor deleted (as specified in topology).");
			getLog().info(Messages.MESSAGE_SPACE);
			throw new MojoExecutionException("Environment '" + environment.name + "' already exists and cannot be updated nor deleted.");
		case UPDATE:
			getLog().info("Updating environment '" + environment.name + "' (as specified in topology).");
			getLog().info(Messages.MESSAGE_SPACE);
			return false;
		}
		return true; // dead code anyway
	}

	protected void loadTopology() throws MojoExecutionException {
		getLog().info("Using topology file: " + environmentsTopology.getAbsolutePath());
		getLog().info(Messages.MESSAGE_SPACE);

		if (!environmentsTopology.exists()) {
			getLog().error("Topology not found");
			getLog().error("Set '" + InstallerMojosInformation.FullEnvironment.topologyFile + "' parameter with an existing topology file");
			throw new MojoExecutionException("Topology not found", new FileNotFoundException(environmentsTopology.getAbsolutePath()));
		}

		environmentsMarshaller = EnvironmentsMarshaller.getEnvironmentMarshaller(environmentsTopology);
	}

}
