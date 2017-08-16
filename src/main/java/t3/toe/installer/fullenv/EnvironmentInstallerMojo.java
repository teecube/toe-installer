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

package t3.toe.installer.fullenv;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
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
import t3.toe.installer.environments.LocalPackage;
import t3.toe.installer.environments.Product;
import t3.toe.installer.environments.Property;
import t3.toe.installer.environments.RemotePackage;

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

	@Parameter (property = InstallerMojosInformation.FullEnvironment.environmentsTopologyFile, defaultValue = InstallerMojosInformation.FullEnvironment.environmentsTopologyFile_default)
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
			String filename = "/xsd/environments.xsd";
			InputStream configStream = EnvironmentInstallerMojo.class.getResourceAsStream(filename);

			environmentsMarshaller = new EnvironmentsMarshaller(environmentsTopology, configStream);
		} catch (JAXBException | SAXException e) {
			throw new MojoExecutionException("Unable to load topology from file '" + environmentsTopology.getAbsolutePath() + "'", e);
		}
		if (environmentsMarshaller == null) {
			throw new MojoExecutionException("Unable to load topology from file '" + environmentsTopology.getAbsolutePath() + "'");
		}

	}

	private void installDependency(Environment environment, Product product, int productIndex) throws MojoExecutionException {
		getLog().info("");

		TIBCOProduct tibcoProduct = TIBCOProduct.valueOf(product.getType().value().toUpperCase());

		String goal = tibcoProduct.goal();

		getLog().info("Installing '" + tibcoProduct.productName() + "'");
		getLog().info("");
		getLog().info(">>> " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " >>>");

		CommonInstaller mojo = InstallerMojosFactory.getInstallerMojo("toe:" + goal);
		String mojoClassName = mojo.getClass().getCanonicalName();

		ArrayList<Element> configuration = new ArrayList<Element>();
		ArrayList<Element> ignoredParameters = new ArrayList<Element>();

		if (!firstDependency) {
			addProperty(configuration, ignoredParameters, "createNewEnvironment", "false", CommonInstaller.class);
		} else {
			firstDependency = false;
		}

		addProperty(configuration, ignoredParameters, "environmentName", environment.getEnvironmentName(), CommonInstaller.class);
		addProperty(configuration, ignoredParameters, "installationRoot", environment.getTibcoRoot(), CommonInstaller.class);
		if (product.getPackage() != null) {
			if (product.getPackage().getRemote() != null) {
				RemotePackage remotePackage = product.getPackage().getRemote();
	
				// version and classifier are mandatory
				addProperty(configuration, ignoredParameters, "remoteInstallationPackageVersion", remotePackage.getVersion(), mojo.getClass());
				addProperty(configuration, ignoredParameters, "remoteInstallationPackageClassifier", remotePackage.getClassifier(), mojo.getClass());
				if (StringUtils.isNotBlank(remotePackage.getGroupId())) {
					addProperty(configuration, ignoredParameters, "remoteInstallationPackageGroupId", remotePackage.getGroupId(), mojo.getClass());
				}
				if (StringUtils.isNotBlank(remotePackage.getArtifactId())) {
					addProperty(configuration, ignoredParameters, "remoteInstallationPackageArtifactId", remotePackage.getArtifactId(), mojo.getClass());
				}
			} else if (product.getPackage().getLocal() != null) {
				LocalPackage localPackage = product.getPackage().getLocal();
				if (StringUtils.isNotBlank(localPackage.getDirectory())) {
					addProperty(configuration, ignoredParameters, "installationPackageDirectory", localPackage.getDirectory(), CommonInstaller.class);
				}
				if (StringUtils.isNotBlank(localPackage.getFile())) {
					addProperty(configuration, ignoredParameters, "installationPackage", localPackage.getFile(), mojo.getClass());
				}
				if (StringUtils.isNotBlank(localPackage.getPattern())) {
					addProperty(configuration, ignoredParameters, "installationPackageRegex", localPackage.getPattern(), mojo.getClass());
				}
			}
		} else {
			addProperty(configuration, ignoredParameters, "installationPackageDirectory", getInstallationPackageDirectory(environment, product), CommonInstaller.class);
		}

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
