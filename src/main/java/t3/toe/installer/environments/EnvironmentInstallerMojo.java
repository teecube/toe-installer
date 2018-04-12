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

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.logging.Logger;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.xml.sax.SAXException;

import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.Messages;
import t3.Utils;
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
		ADMIN ("install-admin", "Administrator", 30),
		BW5 ("install-bw5", "BusinessWorks 5", 30),
		BW6 ("install-bw6", "BusinessWorks 6", 00),
		EMS ("install-ems", "EMS", 30),
		RV ("install-rv", "RendezVous", 10),
		TEA ("install-tea", "TEA", 00),
		TRA ("install-tra", "TRA", 20);

	    private final String goal;
	    private final String name;
	    private final Integer priority;

	    TIBCOProduct(String goal, String name, Integer priority) {
	        this.goal = goal;
	        this.name = name;
	        this.priority = priority;
	    }

	    public String goal() { return goal; }
	    protected String productName() { return "TIBCO " + name; }
	    protected Integer priority() { return priority; }
	}

	@Parameter (property = InstallerMojosInformation.FullEnvironment.topologyFile, defaultValue = InstallerMojosInformation.FullEnvironment.topologyFile_default)
	protected File environmentsTopology;

	protected EnvironmentsMarshaller environmentsMarshaller;
	private static boolean firstDependency = true;

	@Override
	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() {
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
		int environmentsCount = environmentsToInstall.size();
		int environmentsIndex = 1;
		for (EnvironmentToInstall environment : environmentsToInstall) {
			if (environmentsCount > 1) { // multiple environments to install
				getLog().info("=== " + StringUtils.leftPad(Utils.toRoman(environmentsIndex), 3, " ") + ". Environment: " + environment.getEnvironmentName() + " ===");
				getLog().info(Messages.MESSAGE_SPACE);

                firstDependency = true;
			}
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
			boolean noRemoteProductYet = true;
			List<List<Element>> configurations = new ArrayList<List<Element>>();
			for (ProductToInstall product : productsToInstall) {
				configurations.add(initInstaller(environment, product, i, pluginManager, session, logger, getLog()));

				if (productIsRemote(product)) {
					if (!noRemoteProductYet || i == productsToInstall.size()) {
						getLog().info(Messages.MESSAGE_SPACE);
					} else {
						noRemoteProductYet = false;
					}
				}

				i++;
			}

			getLog().info("Environment name to install is : " + environment.getEnvironmentName());
			getLog().info("TIBCO root is                  : " + environment.getTibcoRoot());
			getLog().info("");

			ProductToInstall productWithLongestFullProductName = Collections.max(productsToInstall, new Comparator<ProductToInstall>() {
				@Override
				public int compare(ProductToInstall p1, ProductToInstall p2) {
					return p1.fullProductName().length() - p2.fullProductName().length();
				}
			});
			int maxFullProductNameLength = productWithLongestFullProductName.fullProductName().length();
			i = 1;
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
				getLog().info(prefix + StringUtils.rightPad(product.fullProductName(), maxFullProductNameLength, " ") + version + alreadyInstalled + skipped);
				i++;
			}

			// execute pre-products-install commands
			i = 1;
			if (products.getPreInstallCommands() != null && !products.getPreInstallCommands().getCommand().isEmpty()) {
				getLog().info("Executing pre-install commands");
				for (Command command : products.getPreInstallCommands().getCommand()) {
					executeCommand(command, i, "Pre-install command");
					i++;
				}
			}

			i = 1;
			for (ProductToInstall product : productsToInstall) {
				installProduct(environment, product, i, configurations.get(i-1));
				i++;
			}

			getLog().info("");
			getLog().info("End of products installation.");

			// execute post-products-install commands
			i = 1;
			if (products.getPostInstallCommands() != null && !products.getPostInstallCommands().getCommand().isEmpty()) {
				getLog().info("");
				getLog().info("Executing post-install commands");
				for (Command command : products.getPostInstallCommands().getCommand()) {
					executeCommand(command, i, "Post-install command");
					i++;
				}
			}

			if (environmentsCount > 1 && environmentsIndex < environmentsCount) { // add space between environments
				getLog().info("");
				environmentsIndex++;
			}
		}
	}

	private void executeCommand(Command command, int commandIndex, String commandType) throws MojoExecutionException {
		if (command.isSkip()) {
			getLog().info("Skipping command '" + command.getName() + "'");
			return;
		}

		getLog().info("");

		String commandLine = command.getValue().trim();
		String commandCaption = command.getName() + (command.getId() != null ? " (id: " + command.getId() + ")" : "");
		String commandPrefix =  "[" + commandType + " #" + commandIndex + "]" + (command.getId() != null ? " [" + command.getId() + "]": "") + " ";

		getLog().info(commandIndex + ". Name: " + commandCaption);
		if (StringUtils.isNotBlank(command.getDescription())) {
			getLog().info("   Description: " + command.getDescription());
		}
		getLog().info("");

		try {
			CommonMojo.commandOutputStream = new CollectingLogOutputStream(getLog(), commandPrefix);
			if (executeBinary(commandLine, new File(session.getRequest().getBaseDirectory()), "The command '" + commandCaption + "' failed.") != 0) {
				failedCommand(command);
			}
		} catch (MojoExecutionException | IOException e) {
			failedCommand(command);
		} finally {
			CommonMojo.commandOutputStream = null;
		}
	}

	private void failedCommand(Command command) throws MojoExecutionException {
		switch (command.getOnError()) {
		case FAIL:
			getLog().info("");
			getLog().error("The command failed.");
			throw new MojoExecutionException("The command failed.");
		case IGNORE:
			getLog().info("");
			break;
		case WARN:
			getLog().info("");
			getLog().warn("The command failed.");
			break;
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

	private static boolean productIsLocal(ProductToInstall product) {
		return product != null && product.getPackage() != null && product.getPackage().getLocal() != null;
	}

	private static boolean productIsRemote(ProductToInstall product) {
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

		if (!environmentsTopology.exists()) {
			getLog().error("Topology not found");
			getLog().error("Set '" + InstallerMojosInformation.FullEnvironment.topologyFile + "' parameter with an existing topology file");
			throw new MojoExecutionException("Topology not found", new FileNotFoundException(environmentsTopology.getAbsolutePath()));
		}

		environmentsMarshaller = EnvironmentsMarshaller.getEnvironmentMarshaller(environmentsTopology);
	}

	public static ArrayList<Element> initInstaller(EnvironmentToInstall environment, ProductToInstall product, int productIndex, BuildPluginManager pluginManager, MavenSession session, Logger logger, Log log) throws MojoExecutionException {
		String goal = product.getTibcoProduct().goal();
		CommonInstaller installer = InstallerMojosFactory.getInstallerMojo("toe:" + goal);

		return initInstaller(environment, product, productIndex, pluginManager, session, logger, log, installer);
	}

	public static ArrayList<Element> initInstaller(EnvironmentToInstall environment, ProductToInstall product, int productIndex, BuildPluginManager pluginManager, MavenSession session, Logger logger, Log log, CommonInstaller installer) throws MojoExecutionException {
		String mojoClassName = installer.getClass().getCanonicalName();

		ArrayList<Element> configuration = new ArrayList<Element>();
		ArrayList<Element> ignoredParameters = new ArrayList<Element>();

		if (!firstDependency) {
			addProperty(configuration, ignoredParameters, "createNewEnvironment", "false", CommonInstaller.class);
		} else {
			if (environment.isToBeDeleted()) {
				addProperty(configuration, ignoredParameters, "removeExistingEnvironment", "true", CommonInstaller.class);
			} else {
				addProperty(configuration, ignoredParameters, "createNewEnvironment", "false", CommonInstaller.class);
			}
			firstDependency = false;
		}
		
		addProperty(configuration, ignoredParameters, "ignoreDependencies", "true", CommonInstaller.class); // disable resolution of dependencies in the product goal since dependency are managed here
		installer.setIgnoreDependencies(true);
		addProperty(configuration, ignoredParameters, "environmentName", environment.getEnvironmentName(), CommonInstaller.class);
		installer.setEnvironmentName(environment.getEnvironmentName());
		addProperty(configuration, ignoredParameters, "installationRoot", environment.getTibcoRoot(), CommonInstaller.class);
		installer.setInstallationRoot(new File(environment.getTibcoRoot()));
		if (product.getPackage() != null) {
			if (product.getPackage().getRemote() != null) { // use remote package
				RemotePackage remotePackage = product.getPackage().getRemote();
	
				// version and classifier are mandatory
				addProperty(configuration, ignoredParameters, "remoteInstallationPackageVersion", remotePackage.getVersion(), installer.getClass());
				installer.setRemoteInstallationPackageVersion(remotePackage.getVersion());
				installer.setInstallationPackageVersion(remotePackage.getVersion());
				addProperty(configuration, ignoredParameters, "remoteInstallationPackageClassifier", remotePackage.getClassifier(), installer.getClass());
				installer.setRemoteInstallationPackageClassifier(remotePackage.getClassifier());
				if (StringUtils.isNotBlank(remotePackage.getGroupId())) {
					addProperty(configuration, ignoredParameters, "remoteInstallationPackageGroupId", remotePackage.getGroupId(), installer.getClass());
					installer.setRemoteInstallationPackageGroupId(remotePackage.getGroupId());
				}
				if (StringUtils.isNotBlank(remotePackage.getArtifactId())) {
					addProperty(configuration, ignoredParameters, "remoteInstallationPackageArtifactId", remotePackage.getArtifactId(), installer.getClass());
					installer.setRemoteInstallationPackageArtifactId(remotePackage.getArtifactId());
				}
			} else if (product.getPackage().getLocal() != null) { // use local package
				LocalPackage localPackage = product.getPackage().getLocal();
				if (localPackage.getDirectoryWithPattern() != null) {
					if (StringUtils.isNotBlank(localPackage.getDirectoryWithPattern().getDirectory())) {
						addProperty(configuration, ignoredParameters, "installationPackageDirectory", localPackage.getDirectoryWithPattern().getDirectory(), CommonInstaller.class);
						installer.setInstallationPackageDirectory(new File(localPackage.getDirectoryWithPattern().getDirectory()));					
					}
					if (StringUtils.isNotBlank(localPackage.getDirectoryWithPattern().getPattern())) {
						addProperty(configuration, ignoredParameters, "installationPackageRegex", localPackage.getDirectoryWithPattern().getPattern(), installer.getClass());
						installer.setInstallationPackageRegex(localPackage.getDirectoryWithPattern().getPattern());
					}
					if (localPackage.getDirectoryWithPattern().getVersionGroupIndex() != null) {
						addProperty(configuration, ignoredParameters, "installationPackageRegexVersionGroupIndex", localPackage.getDirectoryWithPattern().getVersionGroupIndex().toString(), installer.getClass());
						installer.setInstallationPackageRegexVersionGroupIndex(localPackage.getDirectoryWithPattern().getVersionGroupIndex());
					}
				} else if (localPackage.getFileWithVersion() != null) {
					addProperty(configuration, ignoredParameters, "installationPackage", localPackage.getFileWithVersion().getFile(), installer.getClass());
					installer.setInstallationPackage(new File(localPackage.getFileWithVersion().getFile()));

					addProperty(configuration, ignoredParameters, "installationPackageVersion", localPackage.getFileWithVersion().getVersion(), installer.getClass());
					installer.setInstallationPackageVersion(localPackage.getFileWithVersion().getVersion());
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
			installer.setInstallationPackageDirectory(installationPackagesDirectoryFile);
		}

		if (product.getProperties() != null && product.getProperties().getProperty() != null) {
			for (Property property : product.getProperties().getProperty()) {
				if (StringUtils.isNotEmpty(property.getKey())) {
					configuration.add(element(property.getKey(), property.getValue()));
					ignoredParameters.add(element(property.getKey(), mojoClassName));
				}
			}
		}
		configuration.add(element("ignoredParameters", ignoredParameters.toArray(new Element[0])));

		// init the Mojo to check if installation already exists
		installer.setLog(log);
		installer.setLogger(logger);
		installer.setPluginManager(pluginManager);
		installer.setSession(session);
		Map<String, String> mojoIgnoredParameters = new HashMap<String, String>();
		for (Element ignoredParameter : ignoredParameters) {
			mojoIgnoredParameters.put(ignoredParameter.toDom().getName(), ignoredParameter.toDom().getValue());
		}
		installer.setIgnoredParameters(mojoIgnoredParameters);
		installer.initStandalonePOM();
		if (!installer.installationPackageWasAlreadyResolved() && productIsRemote(product)) {
		    logger.info(Messages.MESSAGE_SPACE);
        }

		if (installer.installationExists()) {
			product.setAlreadyInstalled(true);
		}
		if (StringUtils.isNotBlank(installer.getInstallationPackageVersion())) {
			product.setVersion(installer.getInstallationPackageVersion());
		}

		if (product.isAlreadyInstalled() && !environment.isToBeDeleted()) {
			switch (product.getIfExists()) {
			case DELETE:
				product.setToBeDeleted(true);

				throw new MojoExecutionException("Unable to delete product '" + product.fullProductName() + "'", new UnsupportedOperationException()); // TODO : implement uninstall of products

//				break;
			case FAIL:
				log.error("Product '" + product.fullProductName() + "' already exists and cannot be kept nor deleted (as specified in topology).");
				log.info(Messages.MESSAGE_SPACE);

				throw new MojoExecutionException("Product '" + product.fullProductName()  + "' already exists and cannot be deleted.");
			case KEEP:
				product.setToBeDeleted(false);

				break;
			}
		}

		return configuration;
	}

	private void installProduct(EnvironmentToInstall environment, ProductToInstall product, int productIndex, List<Element> configuration) throws MojoExecutionException {
		getLog().info("");

		String goal = product.getTibcoProduct().goal();

		if (product.isSkip()) {
			getLog().info(productIndex + ". Skipping '" + product.fullProductName() + "'");
			return;
		} else if (product.isAlreadyInstalled() && !environment.isToBeDeleted()) {
			getLog().info(productIndex + ". Skipping '" + product.fullProductName() + "' (already installed)");
			return;
		} else {
			// execute pre-product-install commands
			if (product.getPreInstallCommands() != null && !product.getPreInstallCommands().getCommand().isEmpty()) {
				getLog().info("Executing pre-install commands for current product");
				int i = 1;
				for (Command command : product.getPreInstallCommands().getCommand()) {
					executeCommand(command, i, "");
					i++;
				}
			}

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

		// execute post-product-install commands
		if (product.getPostInstallCommands() != null && !product.getPostInstallCommands().getCommand().isEmpty()) {
			getLog().info("");
			getLog().info("Executing post-install commands for current product");
			int i = 1;
			for (Command command : product.getPostInstallCommands().getCommand()) {
				executeCommand(command, i, "");
				i++;
			}
		}
	}

	private static void addProperty(ArrayList<Element> configuration, ArrayList<Element> ignoredParameters, String key, String value, Class<?> clazz) {
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
	private static String getInstallationPackagesDirectory(EnvironmentToInstall environment, ProductToInstall product) {
		if (productIsLocal(product) && product.getPackage().getLocal().getDirectoryWithPattern() != null && product.getPackage().getLocal().getDirectoryWithPattern().getDirectory() != null) {
			return product.getPackage().getLocal().getDirectoryWithPattern().getDirectory();
		} else if (environment != null && environment.getPackagesDirectory() != null) {
			return environment.getPackagesDirectory();
		}
		return null;
	}

}
