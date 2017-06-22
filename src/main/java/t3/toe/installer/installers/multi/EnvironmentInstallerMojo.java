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

package t3.toe.installer.installers.multi;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.xml.sax.SAXException;

import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.Messages;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosFactory;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.Environment;
import t3.toe.installer.environments.Product;
import t3.toe.installer.environments.Property;

/**
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "env-install", requiresProject = false)
public class EnvironmentInstallerMojo extends CommonMojo {

	public enum TIBCOProduct {
		ADMIN ("admin-install", "Administrator"),
		BW5 ("bw5-install", "BusinessWorks 5"),
		BW6 ("bw6-install", "BusinessWorks 6"),
		EMS ("ems-install", "EMS"),
		RV ("rv-install", "RV"),
		TEA ("tea-install", "TEA"),
		TRA ("tra-install", "TRA");

	    private final String goal;
	    private final String name;

	    TIBCOProduct(String goal, String name) {
	        this.goal = goal;
	        this.name = name;
	    }

	    protected String goal() { return goal; }
	    protected String productName() { return "TIBCO " + name; }
	}

	@Parameter (property = InstallerMojosInformation.MultiInstall.environmentsTopologyFile, defaultValue = InstallerMojosInformation.MultiInstall.environmentsTopologyFile_default)
	protected File environmentsTopology;

	protected EnvironmentsMarshaller environmentsMarshaller;
	private boolean firstDependency = true;

	@Override
	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() throws MojoExecutionException {
		return new InstallerLifecycleParticipant();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException  {
		super.execute();

		loadTopology();

		for (Environment environment : environmentsMarshaller.getObject().getEnvironment()) {
			getLog().info("Environment name to install is : " + environment.getEnvironmentName());
			getLog().info("TIBCO root is                  : " + environment.getTibcoRoot());
			int i = 1;
			for (Product product : environment.getProducts().getProduct()) {
				installDependency(environment, product, i);
				i++;
			}
		}
	}

	protected void loadTopology() throws MojoExecutionException {
		getLog().info("Using topology file: " + environmentsTopology.getAbsolutePath());
		getLog().info(Messages.MESSAGE_SPACE);

		try {
			environmentsMarshaller = new EnvironmentsMarshaller(environmentsTopology);
		} catch (JAXBException | SAXException e) {
			throw new MojoExecutionException("Unable to load topology from file '" + environmentsTopology.getAbsolutePath() + "'");
		}
		if (environmentsMarshaller == null) {
			throw new MojoExecutionException("Unable to load topology from file '" + environmentsTopology.getAbsolutePath() + "'");
		}

	}

	private void installDependency(Environment environment, Product product, int productIndex) throws MojoExecutionException {
//		InstallerPluginManager.registerCustomPluginManager(pluginManager, new InstallerMojosFactory());

		TIBCOProduct tibcoProduct = TIBCOProduct.valueOf(product.getName().value().toUpperCase());

		String goal = tibcoProduct.goal();

		getLog().info("Installing '" + tibcoProduct.productName() + "'");
		getLog().info("");
		getLog().info(">>> " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " >>>");

		ArrayList<Element> configuration = new ArrayList<Element>();
		ArrayList<Element> ignoredParameters = new ArrayList<Element>();

		if (!firstDependency) {
//			configuration.add(element("createNewEnvironment", "false"));
			addProperty(configuration, ignoredParameters, "createNewEnvironment", "false", CommonInstaller.class);
		} else {
			firstDependency = false;
		}

		addProperty(configuration, ignoredParameters, "environmentName", environment.getEnvironmentName(), CommonInstaller.class);
		addProperty(configuration, ignoredParameters, "installationRoot", environment.getTibcoRoot(), CommonInstaller.class);
		addProperty(configuration, ignoredParameters, "installationPackageDirectory", getInstallationPackageDirectory(environment, product), CommonInstaller.class);

//		configuration.add(element("environmentName", environment.getEnvironmentName()));
//		configuration.add(element("installationRoot", environment.getTibcoRoot()));
//		configuration.add(element("installationPackageDirectory", getInstallationPackageDirectory(environment, product)));
//
//		ignoredParameters.add(element("createNewEnvironment", CommonInstaller.class.getCanonicalName()));
//		ignoredParameters.add(element("environmentName", CommonInstaller.class.getCanonicalName()));
//		ignoredParameters.add(element("installationRoot", CommonInstaller.class.getCanonicalName()));
//		ignoredParameters.add(element("installationPackageDirectory", CommonInstaller.class.getCanonicalName()));

		CommonInstaller mojo = InstallerMojosFactory.getInstallerMojo("toe:" + goal);
		String mojoClassName = mojo.getClass().getCanonicalName();

		if (product.getProperties() != null && product.getProperties().getProperty() != null) {
			for (Property property : product.getProperties().getProperty()) {
				configuration.add(element(property.getKey(), property.getValue()));
				ignoredParameters.add(element(property.getKey(), mojoClassName));
			}
		}

		configuration.add(element("ignoredParameters", ignoredParameters.toArray(new Element[0])));

		executeMojo(
			plugin(
				groupId(pluginDescriptor.getGroupId()),
				artifactId(pluginDescriptor.getArtifactId()),
				version(pluginDescriptor.getVersion())
			),
			goal(goal),
			configuration(
				configuration.toArray(new Element[0])
			),
			getEnvironment(pluginManager)
		);

		getLog().info("");
		getLog().info("<<< " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " <<<");
		getLog().info("");
	}

	private void addProperty(ArrayList<Element> configuration, ArrayList<Element> ignoredParameters, String key, String value, Class<?> clazz) {
		configuration.add(element(key, value));
		ignoredParameters.add(element(key, clazz.getCanonicalName()));
	}

	private String getInstallationPackageDirectory(Environment environment, Product product) {
		if (product != null && product.getPackage() != null && product.getPackage().getLocal() != null && product.getPackage().getLocal().getDirectory() != null) {
			return product.getPackage().getLocal().getDirectory();
		} else if (environment != null && environment.getPackagesDirectory() != null) {
			return environment.getPackagesDirectory();
		}
		return null;
	}

}
