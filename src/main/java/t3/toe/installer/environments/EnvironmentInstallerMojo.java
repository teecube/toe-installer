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

package t3.toe.installer.environments;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.ResolutionScope;
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
import t3.toe.installer.environments.Environment.Products;

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

	public enum TIBCOProduct {
		ADMIN ("admin-install", "Administrator", 30),
		BW5 ("bw5-install", "BusinessWorks 5", 30),
		BW6 ("bw6-install", "BusinessWorks 6", 00),
		EMS ("ems-install", "EMS", 30),
		RV ("rv-install", "RendezVous", 10),
		TEA ("tea-install", "TEA", 00),
		TRA ("tra-install", "TRA", 20);

	    private final String goal;
	    private final String name;
	    private final Integer priority;

	    TIBCOProduct(String goal, String name, Integer priority) {
	        this.goal = goal;
	        this.name = name;
	        this.priority = priority;
	    }

	    protected String goal() { return goal; }
	    protected String productName() { return "TIBCO " + name; }
	    protected Integer priority() { return priority; }
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

		CommonInstaller.firstGoal = false;

		List<EnvironmentToInstall> environmentsToInstall = new ArrayList<EnvironmentToInstall>();
		for (Environment environment : environmentsMarshaller.getObject().getEnvironment()) {
			environmentsToInstall.add(new EnvironmentToInstall(environment));
		}
		for (EnvironmentToInstall environment : environmentsToInstall) {
			// first check if environment exists and if we can continue according to current strategy (keep, fail, delete)
			com.tibco.envinfo.TIBCOEnvironment.Environment localEnvironment = CommonInstaller.getCurrentEnvironment(environment.getEnvironmentName());
			if (CommonInstaller.environmentExists(localEnvironment)) {
				getLog().info("The environment already exists.");

				environment.setToBeDeleted(deleteEnvironment(environment));
			}
			
			Products products = environment.getProducts();
			List<ProductToInstall> productsToInstall = new ArrayList<ProductToInstall>();
			for (Product product : products.getProduct()) {
				productsToInstall.add(new ProductToInstall(product));
			}
			checkAndSortProducts(productsToInstall);

			int i = 1;
			List<List<Element>> configurations = new ArrayList<List<Element>>();
			for (ProductToInstall product : productsToInstall) {
				configurations.add(initInstaller(environment, product, i));
				i++;

				if (productIsRemote(product)) {
					getLog().info(Messages.MESSAGE_SPACE);
				}
			}

			getLog().info("Environment name to install is : " + environment.getEnvironmentName());
			getLog().info("TIBCO root is                  : " + environment.getTibcoRoot());
			getLog().info("");

			i = 1;
			for (ProductToInstall product : productsToInstall) {
				String skipped = (product.isSkip() ? " (skipped)" : "");
				String alreadyInstalled = (!product.isSkip() && product.isAlreadyInstalled() ? " (already installed"
																							  + (environment.isToBeDeleted() ? ", will be deleted then reinstalled in new environment" : "")
																							  + (!environment.isToBeDeleted() && IfProductExistsBehaviour.DELETE.equals(product.getIfExists()) ? ", will be deleted then reinstalled in current environment" : "")
																							  + (!environment.isToBeDeleted() && IfProductExistsBehaviour.KEEP.equals(product.getIfExists()) ? ", will not be reinstalled" : "")
																							  + ")" : "");

				String prefix;
				if (i == 1) {
					prefix = "Products to install            : " + i + ". ";
				} else {
					prefix = "                                 " + i + ". ";
				}
				getLog().info(prefix + product.fullProductName() + alreadyInstalled + skipped);
				i++;
			}

			i = 1;
			for (ProductToInstall product : productsToInstall) {
				installDependency(environment, product, i, configurations.get(i-1));
				i++;
			}
		}
	}

	private boolean deleteEnvironment(EnvironmentToInstall environment) throws MojoExecutionException, MojoFailureException {
		switch (environment.getIfExists()) {
		case DELETE:
			getLog().info("Environment '" + environment.environmentName + "' will be deleted and reinstalled (as specified in topology).");
			getLog().info(Messages.MESSAGE_SPACE);
			return true;
		case FAIL:
			getLog().info(Messages.MESSAGE_SPACE);
			getLog().error("Environment '" + environment.environmentName + "' already exists and cannot be updated nor deleted (as specified in topology).");
			getLog().info(Messages.MESSAGE_SPACE);
			throw new MojoExecutionException("Environment '" + environment.environmentName + "' already exists and cannot be updated nor deleted.");
		case UPDATE:
			getLog().info("Updating environment '" + environment.environmentName + "' (as specified in topology).");
			getLog().info(Messages.MESSAGE_SPACE);
			return false;
		}
		return true; // dead code anyway
	}

	private boolean productIsLocal(ProductToInstall product) {
		return product != null && product.getPackage() != null && product.getPackage().getLocal() != null;
	}

	private boolean productIsRemote(ProductToInstall product) {
		return product != null && product.getPackage() != null && product.getPackage().getRemote() != null;
	}

	private boolean productExists(TIBCOProduct tibcoProduct, List<ProductToInstall> productsToInstall) {
		boolean exists = false;

		for (ProductToInstall product : productsToInstall) {
			if (product.getTibcoProduct().name.equals(tibcoProduct.name)) {
				exists = true;
			}
		}

		return exists;
	}

	private void checkAndSortProducts(List<ProductToInstall> productsToInstall) throws MojoExecutionException {
		// check that priorities are OK
		boolean prioritiesOK = true;
		for (ProductToInstall product : productsToInstall) {
			Integer productPriority = product.getPriority();
			if (productPriority == null) { // if not set in XML, take default one
				productPriority = product.getTibcoProduct().priority();
			}

			if (product.getType().equals(ProductType.ADMIN) || product.getType().equals(ProductType.BW_5)) { // Administator and BW5 need RV and TRA before being installed
				boolean rvExists = productExists(TIBCOProduct.RV, productsToInstall);
				boolean traExists = productExists(TIBCOProduct.TRA, productsToInstall);
				if (!rvExists || !traExists) {
					getLog().info("");
					getLog().error("The product '" + product.fullProductName() + "' has unresolved dependencies in the current topology.");
					if (rvExists) {
						getLog().error("-> The product '" + TIBCOProduct.RV.productName() + "' is required but is not defined.");
					}
					if (traExists) {
						getLog().error("-> The product '" + TIBCOProduct.TRA.productName() + "' is required but is not defined.");
					}

					throw new MojoExecutionException("There are unresolved dependencies in the current topology '" + environmentsTopology.getAbsolutePath() + "'.");
				}
			}
			for (ProductToInstall productToCompare : productsToInstall) {
				if (productToCompare.equals(product)) {
					continue;
				}

				Integer productToComparePriority = productToCompare.getPriority();
				if (productToComparePriority == null) { // if not set in XML, take default one
					productToComparePriority = productToCompare.getTibcoProduct().priority();
				}

				if (product.getType().equals(ProductType.ADMIN) || product.getType().equals(ProductType.BW_5)) { // Administator and BW5 need RV and TRA before being installed
					if (productToCompare.getType().equals(ProductType.RV) || productToCompare.getType().equals(ProductType.TRA)) {
						if (productToComparePriority >= productPriority) {
							getLog().error("The product '" + productToCompare.fullProductName() + "' (priority " + productToComparePriority + ") cannot be installed after product '" + product.fullProductName() + "' (priority " + productPriority + ").");
							prioritiesOK = false;
						}						
					}
				}
			}
		}

		if (!prioritiesOK) {
			throw new MojoExecutionException("The topology has errors in products priorities.");
		}

		// sort products by priorities
		Collections.sort(productsToInstall, new ProductComparator());
	}

	public class ProductComparator implements Comparator<ProductToInstall> {
	    @Override
	    public int compare(ProductToInstall p1, ProductToInstall p2) {
	    	if (p1 == null || p2 == null) return 0;

	    	Integer priority1 = p1.getPriority();
	    	Integer priority2 = p2.getPriority();
	    	if (priority1 == null) {
	    		priority1 = p1.getTibcoProduct().priority();
	    	}
	    	if (priority2 == null) {
	    		priority2 = p2.getTibcoProduct().priority();
	    	}

			return priority1.compareTo(priority2);
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

	private ArrayList<Element> initInstaller(EnvironmentToInstall environment, ProductToInstall product, int productIndex) throws MojoExecutionException {
		String goal = product.getTibcoProduct().goal();

		CommonInstaller mojo = InstallerMojosFactory.getInstallerMojo("toe:" + goal);
		String mojoClassName = mojo.getClass().getCanonicalName();

		ArrayList<Element> configuration = new ArrayList<Element>();
		ArrayList<Element> ignoredParameters = new ArrayList<Element>();

		if (!firstDependency) {
			addProperty(configuration, ignoredParameters, "createNewEnvironment", "false", CommonInstaller.class);
		} else {
			if (environment.isToBeDeleted()) {
				addProperty(configuration, ignoredParameters, "removeExistingEnvironment", "true", CommonInstaller.class);
			}
			firstDependency = false;
		}
		
		addProperty(configuration, ignoredParameters, "ignoreDependencies", "true", CommonInstaller.class); // disable resolution of dependencies in the product goal since dependency are managed here
		mojo.setIgnoreDependencies(true);
		addProperty(configuration, ignoredParameters, "environmentName", environment.getEnvironmentName(), CommonInstaller.class);
		mojo.setEnvironmentName(environment.getEnvironmentName());
		addProperty(configuration, ignoredParameters, "installationRoot", environment.getTibcoRoot(), CommonInstaller.class);
		mojo.setInstallationRoot(new File(environment.getTibcoRoot()));
		if (product.getPackage() != null) {
			if (product.getPackage().getRemote() != null) { // use remote package
				RemotePackage remotePackage = product.getPackage().getRemote();
	
				// version and classifier are mandatory
				addProperty(configuration, ignoredParameters, "remoteInstallationPackageVersion", remotePackage.getVersion(), mojo.getClass());
				mojo.setRemoteInstallationPackageVersion(remotePackage.getVersion());
				mojo.setInstallationPackageVersion(remotePackage.getVersion());
				addProperty(configuration, ignoredParameters, "remoteInstallationPackageClassifier", remotePackage.getClassifier(), mojo.getClass());
				mojo.setRemoteInstallationPackageClassifier(remotePackage.getClassifier());
				if (StringUtils.isNotBlank(remotePackage.getGroupId())) {
					addProperty(configuration, ignoredParameters, "remoteInstallationPackageGroupId", remotePackage.getGroupId(), mojo.getClass());
					mojo.setRemoteInstallationPackageGroupId(remotePackage.getGroupId());
				}
				if (StringUtils.isNotBlank(remotePackage.getArtifactId())) {
					addProperty(configuration, ignoredParameters, "remoteInstallationPackageArtifactId", remotePackage.getArtifactId(), mojo.getClass());
					mojo.setRemoteInstallationPackageArtifactId(remotePackage.getArtifactId());
				}
			} else if (product.getPackage().getLocal() != null) { // use local package
				LocalPackage localPackage = product.getPackage().getLocal();
				if (localPackage.getDirectoryWithPattern() != null) {
					if (StringUtils.isNotBlank(localPackage.getDirectoryWithPattern().getDirectory())) {
						addProperty(configuration, ignoredParameters, "installationPackageDirectory", localPackage.getDirectoryWithPattern().getDirectory(), CommonInstaller.class);
						mojo.setInstallationPackageDirectory(new File(localPackage.getDirectoryWithPattern().getDirectory()));					
					}
					if (StringUtils.isNotBlank(localPackage.getDirectoryWithPattern().getPattern())) {
						addProperty(configuration, ignoredParameters, "installationPackageRegex", localPackage.getDirectoryWithPattern().getPattern(), mojo.getClass());
						mojo.setInstallationPackageRegex(localPackage.getDirectoryWithPattern().getPattern());
					}
					if (localPackage.getDirectoryWithPattern().getVersionGroupIndex() != null) {
						addProperty(configuration, ignoredParameters, "installationPackageRegexVersionGroupIndex", localPackage.getDirectoryWithPattern().getVersionGroupIndex().toString(), mojo.getClass());
						mojo.setInstallationPackageRegexVersionGroupIndex(localPackage.getDirectoryWithPattern().getVersionGroupIndex());
					}
				} else if (localPackage.getFileWithVersion() != null) {
					addProperty(configuration, ignoredParameters, "installationPackage", localPackage.getFileWithVersion().getFile(), mojo.getClass());
					mojo.setInstallationPackage(new File(localPackage.getFileWithVersion().getFile()));

					addProperty(configuration, ignoredParameters, "installationPackageVersion", localPackage.getFileWithVersion().getVersion(), mojo.getClass());
					mojo.setInstallationPackageVersion(localPackage.getFileWithVersion().getVersion());
				}
			}
		} else { // no remote or local package defined -> create a local package with default values
			String installationPackagesDirectory = getInstallationPackagesDirectory(environment, product);
			File installationPackagesDirectoryFile = null;
			if (installationPackagesDirectory != null) {
				installationPackagesDirectoryFile = new File(installationPackagesDirectory);
			}
			if (installationPackagesDirectoryFile == null || !installationPackagesDirectoryFile.exists() || !installationPackagesDirectoryFile.isDirectory()) {
				throw new MojoExecutionException("The product '" + product.fullProductName() + "' has no package directory set in this topology");
			}
			addProperty(configuration, ignoredParameters, "installationPackageDirectory", installationPackagesDirectory, CommonInstaller.class);
			mojo.setInstallationPackageDirectory(installationPackagesDirectoryFile);
		}

		if (product.getProperties() != null && product.getProperties().getProperty() != null) {
			for (Property property : product.getProperties().getProperty()) {
				configuration.add(element(property.getKey(), property.getValue()));
				ignoredParameters.add(element(property.getKey(), mojoClassName));
			}
		}
		configuration.add(element("ignoredParameters", ignoredParameters.toArray(new Element[0])));

		// init the Mojo to check if installation already exists
		mojo.setLog(this.getLog());
		mojo.setLogger(this.logger);
		mojo.setPluginManager(pluginManager);
		mojo.setSession(session);
		Map<String, String> mojoIgnoredParameters = new HashMap<String, String>();
		for (Element ignoredParameter : ignoredParameters) {
			mojoIgnoredParameters.put(ignoredParameter.toDom().getName(), ignoredParameter.toDom().getValue());
		}
		mojo.setIgnoredParameters(mojoIgnoredParameters);
		mojo.initStandalonePOM();
		if (mojo.installationExists()) {
			product.setAlreadyInstalled(true);
		}

		if (product.isAlreadyInstalled() && !environment.isToBeDeleted()) {
			switch (product.getIfExists()) {
			case DELETE:
				product.setToBeDeleted(true);

				throw new MojoExecutionException("Unable to delete product '" + product.fullProductName() + "'", new UnsupportedOperationException()); // TODO : implement uninstall of products

//				break;
			case FAIL:
				getLog().error("Product '" + product.fullProductName() + "' already exists and cannot be kept nor deleted (as specified in topology).");
				getLog().info(Messages.MESSAGE_SPACE);

				throw new MojoExecutionException("Product '" + product.fullProductName()  + "' already exists and cannot be deleted.");
			case KEEP:
				product.setToBeDeleted(false);

				break;
			}
		}

		return configuration;
	}

	private void installDependency(EnvironmentToInstall environment, ProductToInstall product, int productIndex, List<Element> configuration) throws MojoExecutionException {
		getLog().info("");

		String goal = product.getTibcoProduct().goal();

		if (product.isSkip()) {
			getLog().info(productIndex + ". Skipping '" + product.fullProductName() + "'");
			return;
		} else if (product.isAlreadyInstalled() && !environment.isToBeDeleted()) {
			getLog().info(productIndex + ". Skipping '" + product.fullProductName() + "' (already installed)");
			return;
		} else {
			getLog().info(productIndex + ". Installing '" + product.fullProductName() + "'");
		}
		getLog().info("");
		getLog().info(">>> " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " >>>");

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

	/**
	 * Retrieves a directory where to look for the installation package.
	 *
	 * @param environment
	 * @param product
	 * @return
	 */
	private String getInstallationPackagesDirectory(EnvironmentToInstall environment, ProductToInstall product) {
		if (productIsLocal(product) && product.getPackage().getLocal().getDirectoryWithPattern() != null && product.getPackage().getLocal().getDirectoryWithPattern().getDirectory() != null) {
			return product.getPackage().getLocal().getDirectoryWithPattern().getDirectory();
		} else if (environment != null && environment.getPackagesDirectory() != null) {
			return environment.getPackagesDirectory();
		}
		return null;
	}

}
