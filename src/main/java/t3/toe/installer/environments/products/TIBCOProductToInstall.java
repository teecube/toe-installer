/**
 * (C) Copyright 2016-2019 teecube
 * (https://teecu.be) and others.
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

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import t3.CommonMojo;
import t3.Messages;
import t3.log.PrefixedLogger;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerMojosFactory;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.*;
import t3.toe.installer.environments.Package;
import t3.toe.installer.installers.hotfix.CommonHotfixInstaller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

public class TIBCOProductToInstall extends ProductToInstall<TIBCOProduct> {

	public static boolean firstDependency = true;

	private CommonInstaller installer;
	private ArrayList<File> resolvedDependencies;

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
	private TIBCOProduct.Dependencies dependencies;
	private ProductType type;
	private boolean configure;

	private TIBCOProductGoalAndPriority tibcoProductGoalAndPriority;
	private String version;

	public TIBCOProductToInstall(TIBCOProduct tibcoProduct, EnvironmentToInstall environment, CommonMojo commonMojo) {
		super(tibcoProduct, environment, commonMojo);

		this.setHotfixes(tibcoProduct.getHotfixes());
		this.setDependencies(tibcoProduct.getDependencies());
		this.setName(tibcoProduct.getName());
		this.setType(tibcoProduct.getType());
		this.setConfigure(tibcoProduct.isConfigure());

		this.setTibcoProductGoalAndPriority(TIBCOProductGoalAndPriority.valueOf(tibcoProduct.getType().value().toUpperCase()));
	}

	public void setDependencies(TIBCOProduct.Dependencies dependencies) {
		this.dependencies = dependencies;
		this.getProduct().setDependencies(this.dependencies);
	}

	public TIBCOProduct.Dependencies getDependencies() {
		return dependencies;
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

	public void setConfigure(boolean configure) {
		this.configure = configure;
	}

	public boolean isConfigure() {
		return configure;
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
		if (productIsLocal(product) && product.getPackage().getDirectoryWithPattern() != null && product.getPackage().getDirectoryWithPattern().getDirectory() != null) {
			return product.getPackage().getDirectoryWithPattern().getDirectory();
		} else if (environment != null && environment.getPackagesDirectory() != null) {
			return environment.getPackagesDirectory();
		}
		return null;
	}

	private boolean productIsLocal(TIBCOProductToInstall product) {
		return product != null && product.getPackage() != null && (product.getPackage().getFileWithVersion() != null || product.getPackage().getDirectoryWithPattern() != null);
	}

	private boolean productIsRemote(TIBCOProductToInstall product) {
		return product != null && product.getPackage() != null && (product.getPackage().getHttpRemote() != null || product.getPackage().getMavenArtifact() != null);
	}

	private Package cachedPackage = null;

	@Override
	public Package getPackage() {
		if (cachedPackage != null) {
			return cachedPackage;
		}

		Package superPackage = super.getPackage();

		MavenTIBCOArtifactPackage mavenTIBCOArtifact = superPackage.getMavenTIBCOArtifact();
		if (mavenTIBCOArtifact != null) {
			normalizeMavenTIBCOArtifact(mavenTIBCOArtifact);
			superPackage.setMavenTIBCOArtifact(mavenTIBCOArtifact);
		}

		cachedPackage = superPackage;

		return superPackage;
	}

	private MavenTIBCOArtifactPackage normalizeMavenTIBCOArtifact(MavenTIBCOArtifactPackage mavenTIBCOArtifact) {
		return normalizeMavenTIBCOArtifact(mavenTIBCOArtifact, false);
	}

	public MavenTIBCOArtifactPackage normalizeMavenTIBCOArtifact(MavenTIBCOArtifactPackage mavenTIBCOArtifact, boolean isHotfix) {
		if (StringUtils.isEmpty(mavenTIBCOArtifact.getGroupId())) {
			switch (type) {
				case ADMIN:
					mavenTIBCOArtifact.setGroupId(InstallerMojosInformation.Administrator.remoteInstallationPackageGroupId_default);
					break;
				case BW_5:
					mavenTIBCOArtifact.setGroupId(InstallerMojosInformation.BW5.remoteInstallationPackageGroupId_default);
					break;
				case BW_6:
					mavenTIBCOArtifact.setGroupId(InstallerMojosInformation.BW6.remoteInstallationPackageGroupId_default);
					break;
				case EMS:
					mavenTIBCOArtifact.setGroupId(InstallerMojosInformation.EMS.remoteInstallationPackageGroupId_default);
					break;
				case TEA:
					mavenTIBCOArtifact.setGroupId(InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageGroupId_default);
					break;
				case TRA:
					mavenTIBCOArtifact.setGroupId(InstallerMojosInformation.TRA.remoteInstallationPackageGroupId_default);
					break;
				case RV:
					mavenTIBCOArtifact.setGroupId(InstallerMojosInformation.RV.remoteInstallationPackageGroupId_default);
					break;
			}
		}
		if (StringUtils.isEmpty(mavenTIBCOArtifact.getArtifactId())) {
			String artifactId = "";
			switch (type) {
				case ADMIN:
					artifactId = InstallerMojosInformation.Administrator.remoteInstallationPackageArtifactId_default;
					break;
				case BW_5:
					artifactId = InstallerMojosInformation.BW5.remoteInstallationPackageArtifactId_default;
					break;
				case BW_6:
					artifactId = InstallerMojosInformation.BW6.remoteInstallationPackageArtifactId_default;
					break;
				case EMS:
					artifactId = InstallerMojosInformation.EMS.remoteInstallationPackageArtifactId_default;
					break;
				case TEA:
					artifactId = InstallerMojosInformation.EnterpriseAdministrator.remoteInstallationPackageArtifactId_default;
					break;
				case TRA:
					artifactId = InstallerMojosInformation.TRA.remoteInstallationPackageArtifactId_default;
					break;
				case RV:
					artifactId = InstallerMojosInformation.RV.remoteInstallationPackageArtifactId_default;
					break;
			}
			if (isHotfix) {
				artifactId = artifactId + "-hotfix";
			}
			mavenTIBCOArtifact.setArtifactId(artifactId);
		}
		if (StringUtils.isEmpty(mavenTIBCOArtifact.getPackaging())) {
			mavenTIBCOArtifact.setPackaging("zip");
		}

		return mavenTIBCOArtifact;
	}

    public File resolvePackage(AbstractPackage abstractPackage, CommonMojo commonMojo, boolean isHotfix) throws MojoExecutionException {
        if (abstractPackage instanceof MavenTIBCOArtifactPackage) {
            MavenTIBCOArtifactPackage mavenTIBCOArtifactPackage = (MavenTIBCOArtifactPackage) abstractPackage;
			abstractPackage = normalizeMavenTIBCOArtifact(mavenTIBCOArtifactPackage, isHotfix);
        }

        return super.resolvePackage(abstractPackage, commonMojo);
    }

	@Override
	public void init(int productIndex) throws MojoExecutionException {
		CommonInstaller.firstGoal = false; // to ignore rules enforcement

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
				addProperty(configuration, ignoredParameters, "createNewEnvironment", "true", CommonInstaller.class);
			}
			firstDependency = false;
		}

		addProperty(configuration, ignoredParameters, "ignoreDependencies", "true", CommonInstaller.class); // disable resolution of dependencies in the product goal since dependency are managed here
		installer.setIgnoreDependencies(true);
		addProperty(configuration, ignoredParameters, "environmentName", environment.getName(), CommonInstaller.class);
		installer.setEnvironmentName(environment.getName());
		addProperty(configuration, ignoredParameters, "installationRoot", environment.getTibcoRoot(), CommonInstaller.class);
		installer.setInstallationRoot(new File(environment.getTibcoRoot()));
		if (this.getPackage() != null) {
			if (this.getPackage().getMavenTIBCOArtifact() != null) { // use remote package
				MavenTIBCOArtifactPackage mavenRemotePackage = this.getPackage().getMavenTIBCOArtifact();

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
			} else if (this.getPackage().getMavenArtifact() != null) { // use remote package
				MavenArtifactPackage mavenRemotePackage = this.getPackage().getMavenArtifact();

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
			} else if (this.getPackage().getDirectoryWithPattern() != null) {
				if (org.apache.commons.lang3.StringUtils.isNotBlank(this.getPackage().getDirectoryWithPattern().getDirectory())) {
					addProperty(configuration, ignoredParameters, "installationPackageDirectory", this.getPackage().getDirectoryWithPattern().getDirectory(), CommonInstaller.class);
					installer.setInstallationPackageDirectory(new File(this.getPackage().getDirectoryWithPattern().getDirectory()));
				}
				if (org.apache.commons.lang3.StringUtils.isNotBlank(this.getPackage().getDirectoryWithPattern().getPattern())) {
					addProperty(configuration, ignoredParameters, "installationPackageRegex", this.getPackage().getDirectoryWithPattern().getPattern(), installer.getClass());
					installer.setInstallationPackageRegex(this.getPackage().getDirectoryWithPattern().getPattern());
				}
				if (this.getPackage().getDirectoryWithPattern().getVersionGroupIndex() != null) {
					addProperty(configuration, ignoredParameters, "installationPackageRegexVersionGroupIndex", this.getPackage().getDirectoryWithPattern().getVersionGroupIndex().toString(), installer.getClass());
					installer.setInstallationPackageRegexVersionGroupIndex(this.getPackage().getDirectoryWithPattern().getVersionGroupIndex());
				}
			} else if (this.getPackage().getFileWithVersion() != null) {
				addProperty(configuration, ignoredParameters, "installationPackage", this.getPackage().getFileWithVersion().getFile(), installer.getClass());
				installer.setInstallationPackage(new File(this.getPackage().getFileWithVersion().getFile()));

				addProperty(configuration, ignoredParameters, "installationPackageVersion", this.getPackage().getFileWithVersion().getVersion(), installer.getClass());
				installer.setInstallationPackageVersion(this.getPackage().getFileWithVersion().getVersion());
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

		if (this.getDependencies() != null) {
			resolvedDependencies = new ArrayList<File>();

			for (AbstractPackage abstractPackage : getDependencies().getHttpRemoteOrMavenArtifactOrMavenTIBCOArtifact()) {
			    File resolvedPackage = resolvePackage(abstractPackage, commonMojo, false);
			    if (resolvedPackage != null && resolvedPackage.exists()) {
                    resolvedDependencies.add(resolvedPackage);
                }
			}
			addProperty(configuration, ignoredParameters, "additionalDependencies", StringUtils.join(resolvedDependencies, ","), CommonInstaller.class);
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
			try {
				this.setResolvedInstallationPackage(installer.getInstallationPackage());
			} catch (IOException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
			this.setInstaller(installer);
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

	public ArrayList<File> getResolvedDependencies() {
		if (resolvedDependencies == null) {
			resolvedDependencies = new ArrayList<File>();
		}
		return resolvedDependencies;
	}

	public CommonInstaller getInstaller() {
		return installer;
	}

	public void setInstaller(CommonInstaller installer) {
		this.installer = installer;
	}

	@Override
	public void installMainProduct(EnvironmentToInstall environment, int productIndex) throws MojoExecutionException {
		String goal = this.getTibcoProductGoalAndPriority().goal();

		logger.info("");
		logger.info(">>> " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " >>>");
		logger.info("");

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

	@Override
	public void installProductHotfixes(EnvironmentToInstall environment, int productIndex, boolean mainProductWasSkipped) throws MojoExecutionException {
		if (this.getHotfixes() != null && this.getHotfixes().getMavenArtifactOrMavenTIBCOArtifactOrFileWithVersion() != null) {
			if (mainProductWasSkipped) {
				if (!this.getHotfixes().isInstallWhenProductIsSkipped()) {
					return;
				} else {
					logger.info("");
					logger.info("Product installation was skipped but product hotfixes will be installed nevertheless as specified by 'installWhenProductIsSkipped' attribute.");
				}
			}

			String goal = this.getTibcoProductGoalAndPriority().goal();
			goal = goal + "-hotfix";
			CommonHotfixInstaller installer = InstallerMojosFactory.getInstallerMojo("toe:" + goal);

			for (AbstractPackage hotfix : this.getHotfixes().getMavenArtifactOrMavenTIBCOArtifactOrFileWithVersion()) {
				configuration.clear();

				ArrayList<Element> ignoredParameters = new ArrayList<Element>();

				addProperty(configuration, ignoredParameters, "createNewEnvironment", "false", CommonInstaller.class);
				addProperty(configuration, ignoredParameters, "installationRoot", environment.getTibcoRoot(), CommonInstaller.class);
				addProperty(configuration, ignoredParameters, "environmentName", environment.getName(), CommonInstaller.class);

				File installationPackage = null;
				String version = null;
				installationPackage = resolvePackage(hotfix, commonMojo, true);
				if (hotfix instanceof LocalFileWithVersion) {
					version = ((LocalFileWithVersion) hotfix).getVersion();
				} else if (hotfix instanceof MavenArtifactPackage) {
					MavenArtifactPackage mavenArtifact = (MavenArtifactPackage) hotfix;
					version = mavenArtifact.getVersion();
				} else if (hotfix instanceof MavenTIBCOArtifactPackage) {
					MavenTIBCOArtifactPackage mavenTIBCOArtifactPackage = (MavenTIBCOArtifactPackage) hotfix;
					version = mavenTIBCOArtifactPackage.getVersion();
				}

				if (installationPackage != null && version != null) {
					try {
						addProperty(configuration, ignoredParameters, "installationPackage", installationPackage.getCanonicalPath(), installer.getClass());
					} catch (IOException e) {
						throw new MojoExecutionException(e.getLocalizedMessage(), e);
					}
					addProperty(configuration, ignoredParameters, "installationPackageVersion", version, installer.getClass());
				}

				configuration.add(element("ignoredParameters", ignoredParameters.toArray(new Element[0])));

				logger.info("");
				logger.info(">>> " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " >>>");
				logger.info("");

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
	}

	@Override
	public void configureInstallation() {
		if (this.isConfigure()) {
			List<AbstractCommand> commands = new ArrayList<AbstractCommand>();

			switch (type) {
				case BW_5:
				case BW_6:
					MavenCommand configureCommand = new MavenCommand();

					// set goal
					MavenCommand.Goals mavenGoals = new MavenCommand.Goals();
					switch (type) {
						case BW_5:
							configureCommand.setName("Configuration of " + InstallerMojosInformation.BW5.category + " in Maven settings.xml");
							mavenGoals.getGoal().add("toe:configure-bw5");
							break;
						case BW_6:
							configureCommand.setName("Configuration of " + InstallerMojosInformation.BW6.category + " in Maven settings.xml");
							mavenGoals.getGoal().add("toe:configure-bw6");
							break;
					}
					configureCommand.setGoals(mavenGoals);

					// set arguments ("-D" parameters)
					MavenCommand.Properties mavenArguments = new MavenCommand.Properties();
					MavenCommand.Properties.Property environmentNameProperty = new MavenCommand.Properties.Property();
					environmentNameProperty.setKey("tibco.installation.environmentName");
					environmentNameProperty.setValue(environment.getName());

					MavenCommand.Properties.Property writeToSettingsProperty = new MavenCommand.Properties.Property();
					writeToSettingsProperty.setKey("tibco.configuration.writeToSettings");
					writeToSettingsProperty.setValue("true");

					mavenArguments.getProperty().add(environmentNameProperty);
					mavenArguments.getProperty().add(writeToSettingsProperty);

					File userSettingsFile = session.getRequest().getUserSettingsFile();
					if (userSettingsFile.exists()) {
						MavenCommand.Properties.Property overriddenSettingsLocationProperty = new MavenCommand.Properties.Property();
						overriddenSettingsLocationProperty.setKey("tibco.configuration.overriddenSettingsLocation");
						overriddenSettingsLocationProperty.setValue(userSettingsFile.getAbsolutePath());

						mavenArguments.getProperty().add(overriddenSettingsLocationProperty);
					}

					configureCommand.setProperties(mavenArguments);

					commands.add(configureCommand);

					if (type.equals(ProductType.BW_6)) {
						// mvn bw6:p2maven-install -P tic-bw6
						MavenCommand p2MavenInstallCommand = new MavenCommand();
                        p2MavenInstallCommand.setName("Installation of the p2 repositories");

						MavenCommand.Goals p2MavenInstallGoals = new MavenCommand.Goals();
						p2MavenInstallGoals.getGoal().add("bw6:p2maven-install");
						p2MavenInstallCommand.setGoals(p2MavenInstallGoals);

						MavenCommand.Profiles p2MavenInstallProfiles = new MavenCommand.Profiles();
						p2MavenInstallProfiles.getProfile().add("tic-bw6");
						p2MavenInstallCommand.setProfiles(p2MavenInstallProfiles);

						commands.add(p2MavenInstallCommand);

						// mvn bw6:studio-proxy-install -P tic-bw6
						MavenCommand studioProxyInstallCommand = new MavenCommand();
                        studioProxyInstallCommand.setName("Installation of the Studio proxy");

						MavenCommand.Goals studioProxyInstallGoals = new MavenCommand.Goals();
						studioProxyInstallGoals.getGoal().add("bw6:studio-proxy-install");
						studioProxyInstallCommand.setGoals(studioProxyInstallGoals);

						MavenCommand.Profiles studioProxyInstallProfiles = new MavenCommand.Profiles();
						studioProxyInstallProfiles.getProfile().add("tic-bw6");
						studioProxyInstallCommand.setProfiles(studioProxyInstallProfiles);

						commands.add(studioProxyInstallCommand);
					}
					break;
			}

			if (!commands.isEmpty()) {
				if (this.getPostInstallCommands() == null) {
					Commands postInstallCommands = new Commands();
					this.setPostInstallCommands(postInstallCommands);
				}

				this.getPostInstallCommands().getAntCommandOrMavenCommandOrSystemCommand().addAll(commands);
			}
		}
	}
}
