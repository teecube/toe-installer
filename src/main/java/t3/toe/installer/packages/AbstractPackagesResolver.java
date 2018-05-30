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
package t3.toe.installer.packages;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.kohsuke.randname.RandomNameGenerator;
import org.xml.sax.SAXException;
import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.log.NoOpLogger;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.CommonInstaller;
import t3.toe.installer.InstallerLifecycleParticipant;
import t3.toe.installer.InstallerMojosFactory;
import t3.toe.installer.InstallerMojosInformation;
import t3.toe.installer.environments.*;
import t3.toe.installer.environments.Environment.Products;
import t3.toe.installer.environments.products.TIBCOProductToInstall;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.*;

/**
* <p>
* This class resolves all TIBCO installation packages found in a given directory.
* It can be subclassed to display found packages or to install/deploy them in Maven or to include them in a standalone
* package.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public abstract class AbstractPackagesResolver extends CommonMojo {

	@Parameter(property = InstallerMojosInformation.installationPackageDirectory, defaultValue = InstallerMojosInformation.installationPackageDirectory_default)
	protected File installationPackageDirectory;

	@org.apache.maven.plugins.annotations.Parameter(property = InstallerMojosInformation.FullEnvironment.topologyType, defaultValue = InstallerMojosInformation.FullEnvironment.topologyType_default)
	protected TopologyType topologyType;

	@Parameter(property = InstallerMojosInformation.FullEnvironment.topologyGenerateWithTemplate, defaultValue = InstallerMojosInformation.FullEnvironment.topologyGenerateWithTemplate_default)
	protected Boolean topologyGenerateWithTemplate;

	@Parameter (property = InstallerMojosInformation.FullEnvironment.topologyFile, defaultValue = InstallerMojosInformation.FullEnvironment.topologyFile_default)
	protected File topologyTemplateFile;

	protected List<CommonInstaller> installers;

	protected abstract void doExecute() throws MojoExecutionException;
	protected abstract Boolean getGenerateTopology() throws MojoExecutionException;
	protected abstract File getTopologyGeneratedFile() throws MojoExecutionException;

	@Override
	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() {
		return new InstallerLifecycleParticipant();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		installers = new ArrayList<CommonInstaller>();
		List<File> ignoredInstallationPackages = new ArrayList<File>();
		for (Class<? extends CommonInstaller> installerClass : InstallerMojosFactory.getInstallersClasses()) {
			File installationPackage = null;
			do { // repeat to find all packages for the current installer
				CommonInstaller installer;
				try {
					installer = installerClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new MojoExecutionException(e.getLocalizedMessage(), e);
				}
				installer.setSession(session);
				installer.setInstallationPackageDirectory(this.installationPackageDirectory);
				installer.initStandalonePOMNoDefaultParameters();

				installer.setInstallationPackage(null);
				installer.setIgnoredInstallationPackages(ignoredInstallationPackages);

				installationPackage = installer.getInstallationPackage();
				if (installationPackage != null && installationPackage.exists()) {
					installer.initVersionArchOs();
					ignoredInstallationPackages.add(installationPackage);
					installers.add(installer);
				}
			} while (installationPackage != null && installationPackage.exists());
		}

		getLog().info("");
		if (installers.size() > 0) {
			getLog().info("Found " + installers.size() + " TIBCO installation packages:");
			for (CommonInstaller installer : installers) {
				getLog().info("-> " + installer.getProductName() + " version " + installer.getInstallationPackageVersion() + " @ " + installer.getInstallationPackage());
			}

			if (getGenerateTopology()) {
				generateTopology();
			}

			getLog().info("");
			doExecute();
		} else {
			getLog().info("No TIBCO installation package was found.");
		}
	}

	private boolean useTopologyTemplate() {
		return topologyGenerateWithTemplate && topologyTemplateFile != null && topologyTemplateFile.exists();
	}

	protected void generateTopology() throws MojoExecutionException {
		getLog().info("");

		File topologyGeneratedFile = getTopologyGeneratedFile();

		try {
			if (useTopologyTemplate()) {
				FileUtils.copyFile(topologyTemplateFile, topologyGeneratedFile);
				getLog().info("Generating topology file using topology template '" + topologyTemplateFile + "'");
			} else {
				getLog().info("Generating topology file for the resolved TIBCO installation packages...");
				// copy an empty topology to target file
				InputStream emptyEnvironments = EnvironmentInstallerMojo.class.getResourceAsStream("/xml/environments.xml");
				FileUtils.copyInputStreamToFile(emptyEnvironments, topologyGeneratedFile);
			}

			doGenerateStandaloneTopology(topologyGeneratedFile, topologyType, false);
		} catch (JAXBException | SAXException | IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

		getLog().info("Topology generated in: " + topologyGeneratedFile.getAbsolutePath());
	}

	protected void doGenerateStandaloneTopology(File topologyGeneratedFile, TopologyType topologyType, boolean mixLocalAndTopologyPackages) throws JAXBException, SAXException, MojoExecutionException, UnsupportedEncodingException, FileNotFoundException {
		// parse the topology
		EnvironmentsMarshaller environmentMarshaller = new EnvironmentsMarshaller(topologyGeneratedFile);
		RandomNameGenerator randomNameGenerator = new RandomNameGenerator(new Random().nextInt());

		EnvironmentsToInstall environments = new EnvironmentsToInstall(environmentMarshaller.getObject().getEnvironment(), topologyGeneratedFile);

		// determine how many environments must be created according to installers OS or use the ones provided by the template
		Map<String, Pair<EnvironmentToInstall, List<TIBCOProductToInstall>>> environmentsToCreate = new HashMap<String, Pair<EnvironmentToInstall, List<TIBCOProductToInstall>>>();

		if (!environments.isEmpty()) { // a template was provided
			for (EnvironmentToInstall environment : environments) {
				List<TIBCOProductToInstall> resolvedProducts = new ArrayList<TIBCOProductToInstall>();

				environmentsToCreate.put(platformOs + "-" + environment.getName(), new MutablePair<EnvironmentToInstall, List<TIBCOProductToInstall>>(environment, resolvedProducts));
				if (!environment.getTIBCOProducts().isEmpty()) { // replace installers with actual needed TIBCO installation packages
					List<CommonInstaller> resolvedInstallers = new ArrayList<CommonInstaller>();
					for (TIBCOProduct tibcoProduct : environment.getTIBCOProducts()) {
						String goal = new TIBCOProductToInstall(tibcoProduct, environment, this).getTibcoProductGoalAndPriority().goal();
						CommonInstaller installer = InstallerMojosFactory.getInstallerMojo("toe:" + goal);

						TIBCOProductToInstall tibcoProductToInstall = new TIBCOProductToInstall(tibcoProduct, environment, this);
						tibcoProductToInstall.setLog(new NoOpLogger());
						tibcoProductToInstall.init(-1);

						resolvedInstallers.add(tibcoProductToInstall.getInstaller());
						resolvedProducts.add(tibcoProductToInstall);
					}
				}

				if (mixLocalAndTopologyPackages) { // no product is provided or mix is allowed, add locally resolved packages
					if (!installers.isEmpty()) {
						getLog().info("");
						getLog().info("Found " + installers.size() + " TIBCO installation packages in the topology:");
						for (CommonInstaller installer : installers) {
							getLog().info("-> " + installer.getProductName() + " version " + installer.getInstallationPackageVersion() + " @ " + installer.getInstallationPackage());
							TIBCOProduct tibcoProduct = new TIBCOProduct();
							tibcoProduct.setType(installer.getProductType());
							Product.Package tibcoProductPackage = new Product.Package();
							LocalPackage localPackage = new LocalPackage();
							LocalFileWithVersion localFileWithVersion = new LocalFileWithVersion();
							localFileWithVersion.setFile(installer.getInstallationPackage().getAbsolutePath());
							localFileWithVersion.setVersion(installer.getInstallationPackageVersion());
							localPackage.setFileWithVersion(localFileWithVersion);
							tibcoProductPackage.setLocal(localPackage);
							tibcoProduct.setPackage(tibcoProductPackage);
							TIBCOProductToInstall tibcoProductToInstall = new TIBCOProductToInstall(tibcoProduct, environment, this);

							tibcoProductToInstall.setLog(new NoOpLogger());
							tibcoProductToInstall.setInstaller(installer);

							resolvedProducts.add(tibcoProductToInstall);
						}
						getLog().info("");
					}
				}
			}
		} else if (installers != null) { // the topology is empty, use resolved packages to guess environments to create
			for (CommonInstaller installer : installers) {
				String os = installer.getInstallationPackageOs(true);
				if (!environmentsToCreate.containsKey(os)) {
					String environmentName = randomNameGenerator.next();
					Environment environment = new Environment();
					environment.setName(environmentName);
					environment.setIfExists(environment.getIfExists()); // explicitly set default value
					if ("windows".equals(os)) {
						environment.setTibcoRoot("C:/tibco/" + environmentName);
					} else {
						environment.setTibcoRoot("/opt/tibco/" + environmentName);
					}

					environmentsToCreate.put(os, new MutablePair<EnvironmentToInstall, List<TIBCOProductToInstall>>(new EnvironmentToInstall(environment, topologyGeneratedFile), new ArrayList<TIBCOProductToInstall>()));
				}
			}
		}

		for (String osEnvironment : environmentsToCreate.keySet()) {
			EnvironmentToInstall environment = environmentsToCreate.get(osEnvironment).getLeft();
			List<TIBCOProductToInstall> resolvedProducts = environmentsToCreate.get(osEnvironment).getRight();
			environment.clearTIBCOProducts();
            Products products = environment.getProducts();

			if (environments.environmentExists(environment.getName())) {
				environment = environments.getEnvironmentByName(environment.getName());
			}

			for (TIBCOProductToInstall tibcoProduct : resolvedProducts) {
				CommonInstaller installer = tibcoProduct.getInstaller();
				if (!osEnvironment.startsWith(installer.getInstallationPackageOs(true))) continue;

				if (topologyType.equals(TopologyType.REMOTE) && tibcoProduct.getPackage().getLocal() != null) {
					// translate LocalPackage to MavenArtifactPackage
					MavenArtifactPackage mavenArtifactPackage = new MavenArtifactPackage();
					mavenArtifactPackage.setGroupId(installer.getRemoteInstallationPackageGroupId());
					mavenArtifactPackage.setArtifactId(installer.getRemoteInstallationPackageArtifactId());
					mavenArtifactPackage.setVersion(installer.getInstallationPackageVersion());
					mavenArtifactPackage.setPackaging(installer.getRemoteInstallationPackagePackaging());
					String classifier = getInstallerClassifier(installer);
					if (classifier != null) {
						mavenArtifactPackage.setClassifier(classifier);
					}

					tibcoProduct.getPackage().setMavenArtifact(mavenArtifactPackage);
					tibcoProduct.getPackage().setLocal(null);
				}
				/*
				Product.Package productPackage = new Product.Package();
				switch (topologyType) {
				case LOCAL:
					LocalPackage localPackage = new LocalPackage();
					LocalFileWithVersion fileWithVersion = new LocalFileWithVersion();
					fileWithVersion.setFile(installer.getInstallationPackage().getAbsolutePath());
					fileWithVersion.setVersion(installer.getInstallationPackageVersion());
					localPackage.setFileWithVersion(fileWithVersion);
					productPackage.setLocal(localPackage);

					break;
				case REMOTE:
					MavenArtifactPackage mavenRemotePackage = new MavenArtifactPackage();
					mavenRemotePackage.setGroupId(installer.getRemoteInstallationPackageGroupId());
					mavenRemotePackage.setArtifactId(installer.getRemoteInstallationPackageArtifactId());
					mavenRemotePackage.setVersion(installer.getInstallationPackageVersion());
					mavenRemotePackage.setPackaging(installer.getRemoteInstallationPackagePackaging());
					String classifier = getInstallerClassifier(installer);
					if (classifier != null) {
						mavenRemotePackage.setClassifier(classifier);
					}

					productPackage.setMavenRemote(mavenRemotePackage);

					break;
				}
				tibcoProduct.setPackage(productPackage);
				*/
				products.getTibcoProductOrCustomProduct().add(tibcoProduct.getProduct());
			}

            environment.setProducts(products);

			if (!environments.environmentExists(environment.getName())) {
				environments.add(environment);
			}
		}

		environmentMarshaller.getObject().getEnvironment().clear();
		environmentMarshaller.getObject().getEnvironment().addAll(environments);

		environmentMarshaller.save();
	}

	protected boolean installPackagesToLocalRepository(File localRepositoryPath) throws MojoExecutionException {
		if (installers.isEmpty()) {
			return false;
		}

		getLog().info("Installing " + installers.size() + " TIBCO installation packages...");
		getLog().info("");
		getLog().info("Using local repository: " + localRepositoryPath.getAbsolutePath());

		getLog().info("");

		for (CommonInstaller installer : installers) {
			String groupId = installer.getRemoteInstallationPackageGroupId();
			String artifactId = installer.getRemoteInstallationPackageArtifactId();
			String version = installer.getInstallationPackageVersion();
			String packaging = installer.getRemoteInstallationPackagePackaging();
			String classifier = getInstallerClassifier(installer);
			getLog().info("Installing '" + installer.getInstallationPackage().getAbsolutePath() + "' to '" + localRepositoryPath.getAbsolutePath().replace("\\", "/") + "/" + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "'");
			File installedFile = this.installDependency(groupId, artifactId, version, packaging, classifier, installer.getInstallationPackage(), localRepositoryPath, true);
			installer.setInstallationPackage(installedFile);
		}

		return true;
	}

	protected String getInstallerClassifier(CommonInstaller installer) throws MojoExecutionException {
		String classifier = null;

		if (StringUtils.isNotEmpty(installer.getInstallationPackageArch(false)) && StringUtils.isNotEmpty(installer.getInstallationPackageOs(false))) {
			classifier = installer.getInstallationPackageOs(false) + "_" + installer.getInstallationPackageArch(false);
		}

		return classifier;
	}
}
