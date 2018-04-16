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
package t3.toe.installer.environments.products;

import org.apache.commons.lang.StringUtils;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import t3.CommonMojo;
import t3.Messages;
import t3.log.PrefixedLogger;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerMojosFactory;
import t3.toe.installer.environments.*;
import t3.toe.installer.environments.commands.CommandToExecute;
import t3.toe.installer.environments.commands.SystemCommandToExecute;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;

public class TIBCOProductToInstall extends ProductToInstall<TIBCOProduct> {

	public static boolean firstDependency = true;

	public enum TIBCOProductGoalAndPriority {
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
		TIBCOProductGoalAndPriority(String goal, String name, Integer priority) {
			this.goal = goal;
			this.name = name;
			this.priority = priority;
		}

		public String goal() { return goal; }

		public String getName() { return name; }
		public String productName() { return "TIBCO " + name; }
		public Integer priority() { return priority; }
	}

	private ArrayList<Element> configuration;
	private TIBCOProduct.Hotfixes hotfixes;
	private ProductType type;

	private TIBCOProductGoalAndPriority tibcoProductGoalAndPriority;
	private String version;

	public TIBCOProductToInstall(TIBCOProduct tibcoProduct, EnvironmentToInstall environment, CommonMojo commonMojo) {
		super(tibcoProduct, environment, commonMojo);

		this.setHotfixes(tibcoProduct.getHotfixes());
		this.setName(tibcoProduct.getName());
		this.setType(tibcoProduct.getType());

		this.setTibcoProductGoalAndPriority(TIBCOProductGoalAndPriority.valueOf(tibcoProduct.getType().value().toUpperCase()));
	}

	public void setHotfixes(TIBCOProduct.Hotfixes hotfixes) {
		this.hotfixes = hotfixes;
	}

	public TIBCOProduct.Hotfixes getHotfixes() {
		return hotfixes;
	}

	public void setType(ProductType type) {
		this.type = type;
	}

	public ProductType getType() {
		return type;
	}

	public TIBCOProductGoalAndPriority getTibcoProductGoalAndPriority() {
		return tibcoProductGoalAndPriority;
	}

	public void setTibcoProductGoalAndPriority(TIBCOProductGoalAndPriority tibcoProductGoalAndPriority) {
		this.tibcoProductGoalAndPriority = tibcoProductGoalAndPriority;
	}

	public String fullProductName() {
		return tibcoProductGoalAndPriority.productName() + (StringUtils.isNotEmpty(this.getId()) ? " (id: " + this.getId() + ")" : "");
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
	private String getInstallationPackagesDirectory(EnvironmentToInstall environment, TIBCOProductToInstall product) {
		if (productIsLocal(product) && product.getPackage().getLocal().getDirectoryWithPattern() != null && product.getPackage().getLocal().getDirectoryWithPattern().getDirectory() != null) {
			return product.getPackage().getLocal().getDirectoryWithPattern().getDirectory();
		} else if (environment != null && environment.getPackagesDirectory() != null) {
			return environment.getPackagesDirectory();
		}
		return null;
	}

	private boolean productIsLocal(TIBCOProductToInstall product) {
		return product != null && product.getPackage() != null && product.getPackage().getLocal() != null;
	}

	private boolean productIsRemote(TIBCOProductToInstall product) {
		return product != null && product.getPackage() != null && (product.getPackage().getHttpRemote() != null || product.getPackage().getMavenRemote() != null);
	}

	@Override
	public void init(int productIndex) throws MojoExecutionException {
		String goal = this.getTibcoProductGoalAndPriority().goal();
		CommonInstaller installer = InstallerMojosFactory.getInstallerMojo("toe:" + goal);

		String mojoClassName = installer.getClass().getCanonicalName();

		configuration = new ArrayList<Element>();
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
		if (this.getPackage() != null) {
			if (this.getPackage().getMavenRemoteTIBCO() != null) { // use remote package
				MavenRemoteTIBCOPackage mavenRemotePackage = this.getPackage().getMavenRemoteTIBCO();

				// version and classifier are mandatory
				addProperty(configuration, ignoredParameters, "remoteInstallationPackageVersion", mavenRemotePackage.getVersion(), installer.getClass());
				installer.setRemoteInstallationPackageVersion(mavenRemotePackage.getVersion());
				installer.setInstallationPackageVersion(mavenRemotePackage.getVersion());
				addProperty(configuration, ignoredParameters, "remoteInstallationPackageClassifier", mavenRemotePackage.getClassifier(), installer.getClass());
				installer.setRemoteInstallationPackageClassifier(mavenRemotePackage.getClassifier());
				if (org.apache.commons.lang3.StringUtils.isNotBlank(mavenRemotePackage.getGroupId())) {
					addProperty(configuration, ignoredParameters, "remoteInstallationPackageGroupId", mavenRemotePackage.getGroupId(), installer.getClass());
					installer.setRemoteInstallationPackageGroupId(mavenRemotePackage.getGroupId());
				}
				if (org.apache.commons.lang3.StringUtils.isNotBlank(mavenRemotePackage.getArtifactId())) {
					addProperty(configuration, ignoredParameters, "remoteInstallationPackageArtifactId", mavenRemotePackage.getArtifactId(), installer.getClass());
					installer.setRemoteInstallationPackageArtifactId(mavenRemotePackage.getArtifactId());
				}
			} else if (this.getPackage().getLocal() != null) { // use local package
				LocalPackage localPackage = this.getPackage().getLocal();
				if (localPackage.getDirectoryWithPattern() != null) {
					if (org.apache.commons.lang3.StringUtils.isNotBlank(localPackage.getDirectoryWithPattern().getDirectory())) {
						addProperty(configuration, ignoredParameters, "installationPackageDirectory", localPackage.getDirectoryWithPattern().getDirectory(), CommonInstaller.class);
						installer.setInstallationPackageDirectory(new File(localPackage.getDirectoryWithPattern().getDirectory()));
					}
					if (org.apache.commons.lang3.StringUtils.isNotBlank(localPackage.getDirectoryWithPattern().getPattern())) {
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
			String installationPackagesDirectory = getInstallationPackagesDirectory(environment, this);
			File installationPackagesDirectoryFile = null;
			if (installationPackagesDirectory != null) {
				installationPackagesDirectoryFile = new File(installationPackagesDirectory);
			}
			if (installationPackagesDirectoryFile == null || !installationPackagesDirectoryFile.exists() || !installationPackagesDirectoryFile.isDirectory()) {
				throw new MojoExecutionException("The product '" + this.fullProductName() + "' has no package directory set in this topology");
			}
			addProperty(configuration, ignoredParameters, "installationPackageDirectory", installationPackagesDirectory, CommonInstaller.class);
			installer.setInstallationPackageDirectory(installationPackagesDirectoryFile);
		}

		if (this.getProperties() != null && this.getProperties().getProperty() != null) {
			for (Property property : this.getProperties().getProperty()) {
				if (org.apache.commons.lang3.StringUtils.isNotEmpty(property.getKey())) {
					configuration.add(element(property.getKey(), property.getValue()));
					ignoredParameters.add(element(property.getKey(), mojoClassName));
				}
			}
		}
		configuration.add(element("ignoredParameters", ignoredParameters.toArray(new Element[0])));

		// init the Mojo to check if installation already exists
		installer.setLog(logger);
		installer.setLogger(PrefixedLogger.getLoggerFromLog(logger));
		installer.setPluginManager(executionEnvironment.getPluginManager());
		installer.setSession(session);
		Map<String, String> mojoIgnoredParameters = new HashMap<String, String>();
		for (Element ignoredParameter : ignoredParameters) {
			mojoIgnoredParameters.put(ignoredParameter.toDom().getName(), ignoredParameter.toDom().getValue());
		}
		installer.setIgnoredParameters(mojoIgnoredParameters);
		installer.initStandalonePOM();

		File resolvedInstallationPackage = installer.getInstallationPackage();
		if (resolvedInstallationPackage != null && resolvedInstallationPackage.exists()) {
            this.setResolvedInstallationPackage(installer.getInstallationPackage());
        } else {
		    logger.error("Unresolved TIBCO installation package!");
        }

		if (!installer.installationPackageWasAlreadyResolved() && productIsRemote(this)) {
			logger.info(Messages.MESSAGE_SPACE);
		}

		if (installer.installationExists()) {
			this.setAlreadyInstalled(true);
		}
		if (org.apache.commons.lang3.StringUtils.isNotBlank(installer.getInstallationPackageVersion())) {
			this.setVersion(installer.getInstallationPackageVersion());
		}

		if (this.isAlreadyInstalled() && !environment.isToBeDeleted()) {
			switch (this.getIfExists()) {
				case DELETE:
					this.setToBeDeleted(true);

					throw new MojoExecutionException("Unable to delete product '" + this.fullProductName() + "'", new UnsupportedOperationException()); // TODO : implement uninstall of products

//				break;
				case FAIL:
					logger.error("Product '" + this.fullProductName() + "' already exists and cannot be kept nor deleted (as specified in topology).");
					logger.info(Messages.MESSAGE_SPACE);

					throw new MojoExecutionException("Product '" + this.fullProductName()  + "' already exists and cannot be deleted.");
				case KEEP:
					this.setToBeDeleted(false);

					break;
			}
		}
	}

	@Override
	public void doInstall(EnvironmentToInstall environment, int productIndex) throws MojoExecutionException {
		String goal = this.getTibcoProductGoalAndPriority().goal();

		logger.info("");
		logger.info(">>> " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " >>>");

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
			executionEnvironment
		);

		logger.info("");
		logger.info("<<< " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " <<<");
	}

}
