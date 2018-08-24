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
package t3.toe.installer;

import com.google.common.io.Files;
import com.tibco.envinfo.TIBCOEnvironment.Environment;
import org.apache.commons.exec.*;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.twdata.maven.mojoexecutor.MojoExecutor.*;
import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.log.NoOpLogger;
import t3.plugin.PropertiesEnforcer;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.envinfo.EnvInfo;
import t3.toe.installer.envinfo.RemoveEnvInfoMojo;
import t3.toe.installer.environments.ProductType;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public abstract class CommonInstaller extends CommonMojo {

	@Parameter(property = InstallerMojosInformation.installationPackageDirectory, defaultValue = InstallerMojosInformation.installationPackageDirectory_default)
	protected File installationPackageDirectory;

	@Parameter(property = InstallerMojosInformation.installationRoot, defaultValue = "")
	protected File installationRoot;

	@Parameter(property = InstallerMojosInformation.environmentName, defaultValue = InstallerMojosInformation.environmentName_default, required = true)
	protected String environmentName;

	@Parameter(property = InstallerMojosInformation.createNewEnvironment, defaultValue = InstallerMojosInformation.createNewEnvironment_default)
	protected Boolean createNewEnvironment;

	@Parameter(property = InstallerMojosInformation.dotTIBCOHome, defaultValue = InstallerMojosInformation.dotTIBCOHome_default)
	private File dotTIBCOHome;

	@Parameter(property = InstallerMojosInformation.removeExistingEnvironment, defaultValue = InstallerMojosInformation.removeExistingEnvironment_default)
	protected Boolean removeExistingEnvironment;

	@Parameter(property = InstallerMojosInformation.ignoreDependencies, defaultValue = InstallerMojosInformation.ignoreDependencies_default)
	protected Boolean ignoreDependencies;

	private List<File> ignoredInstallationPackages;

	protected String currentGoalName;

	private String installationPackageArch;
	private String installationPackageOs;
	private String installationPackageVersion;

	private Boolean installationRootChecked = false;

	private static Boolean installationRootWasNotSet = false;
	private static EnvInfo envInfo = null;
	private boolean installationPackageWasAlreadyResolved = false;

	public Boolean getCreateNewEnvironment() {
		if (createNewEnvironment != _createNewEnvironment && _createNewEnvironment != null) {
			return _createNewEnvironment;
		} else {
			return createNewEnvironment;
		}
	}

	public Boolean getRemoveExistingEnvironment() {
		if (removeExistingEnvironment != _removeExistingEnvironment && _removeExistingEnvironment != null) {
			return _removeExistingEnvironment;
		} else {
			return removeExistingEnvironment;
		}
	}

	public static Collection<Environment> getAllEnvironments() {
		if (envInfo == null) {
			envInfo = new EnvInfo();

			try {
				envInfo.setLog(new NoOpLogger());
				envInfo.execute();
			} catch (MojoExecutionException | MojoFailureException e) {
				return null;
			}
		}

		Map<String, Environment> environment = envInfo.getEnvironments();
		return environment.values();
	}

	public static boolean environmentExists(Environment environment) {
		return environment != null && environment.getLocation() != null && new File(environment.getLocation()).exists() && new File(environment.getLocation()).isDirectory();
	}

	public static Environment getCurrentEnvironment(String environmentName) {
		if (environmentName != null && !environmentName.isEmpty()) {
			if (envInfo == null) {
				envInfo = new EnvInfo();

				try {
					envInfo.setLog(new NoOpLogger());
					envInfo.execute();
				} catch (MojoExecutionException | MojoFailureException e) {
					return null;
				}
			}

			Environment environment = envInfo.getEnvironments().get(environmentName);
			return environment;
		}

		return null;
	}

	private Environment getCurrentEnvironment() {
		return getCurrentEnvironment(environmentName);
	}

	public File getInstallationRoot() throws MojoExecutionException {
		if (installationRoot == null && _installationRoot == null) {
			Environment environment = getCurrentEnvironment();
			if (environment != null) {
				installationRoot = new File(environment.getLocation());

				getLog().info("There is no installation root directory set with 'tibco.installation.root' property.");
				getLog().info("Using 'TIBCO_HOME' of current environment '" + environmentName+ "': '" + installationRoot + "'.");

				// environment exists so don't create a new one
				this.createNewEnvironment = false;
				_createNewEnvironment = false;
				_installationRoot = installationRoot;

				session.getCurrentProject().getProperties().put(InstallerMojosInformation.installationRoot, installationRoot.getAbsolutePath().replace("\\", "/"));
				return installationRoot;
			}
		} else if (!installationRootChecked) {
			installationRootChecked  = true;

			Environment environment = getCurrentEnvironment();
			if (environment != null) {
				File installationRootSet = getInstallationRoot(); // should not loop
				File installationRootFromEnvironment = new File(environment.getLocation());

				try {
					if (installationRootSet == null ||
						installationRootFromEnvironment == null ||
						(installationRootSet.exists() && !installationRootFromEnvironment.exists()) ||
						(!installationRootSet.exists() && installationRootFromEnvironment.exists()) ||
						(installationRootSet.exists() && installationRootFromEnvironment.exists() && !java.nio.file.Files.isSameFile(installationRootSet.toPath(), installationRootFromEnvironment.toPath())) ||
						(!installationRootSet.exists() && !installationRootFromEnvironment.exists() && !installationRootSet.getAbsolutePath().equals(installationRootFromEnvironment.getAbsolutePath()))
						) {

						getLog().error("Installation root set by '" + InstallerMojosInformation.installationRoot + "' property is not the same as the one found in current environment '" + environment.getName() + "' (set by '" + InstallerMojosInformation.environmentName + "').");
						getLog().error(installationRootSet.getAbsolutePath() + " != " + installationRootFromEnvironment.getAbsolutePath());
						getLog().error("Either remove '" + InstallerMojosInformation.installationRoot + "' property (current installation root directory '" + installationRootFromEnvironment.getAbsolutePath() + "' will be used) or change '" + InstallerMojosInformation.environmentName + "' to create a new environment.");
						throw new MojoExecutionException(installationRootSet.getAbsolutePath() + " != " + installationRootFromEnvironment.getAbsolutePath());
					}
				} catch (IOException e) {
					throw new MojoExecutionException(e.getLocalizedMessage(), e);
				}

			}
		}
		if (installationRoot != _installationRoot && _installationRoot != null) {
			return _installationRoot;
		} else {
			return installationRoot;
		}
	}

	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}

	public void setIgnoreDependencies(Boolean ignoreDependencies) {
		this.ignoreDependencies = ignoreDependencies;
	}
	
	public void setInstallationPackageVersion(String installationPackageVersion) {
		this.installationPackageVersion = installationPackageVersion;
	}

	public void setInstallationRoot(File installationRoot) {
		this.installationRoot = installationRoot;
	}

	protected Boolean reuseExistingEnvironment(File installationRoot) throws IOException {
		if (((createNewEnvironment || _createNewEnvironment) && installationRootWasNotSet) || _isDependency) {
			if (!_isDependency) {
				getLog().info("");
				getLog().info("The environment '" + environmentName + "' exists and the '" + InstallerMojosInformation.installationRoot + "' property was not set.");
				getLog().info("Assuming you want to use existing environment ('" + InstallerMojosInformation.createNewEnvironment + "' property set to 'false').");
			} else {
				if (installationRoot == null ||
					this.installationRoot == null ||
					(installationRoot.exists() && !this.installationRoot.exists()) ||
					(!installationRoot.exists() && this.installationRoot.exists()) ||
					!java.nio.file.Files.isSameFile(installationRoot.toPath(), this.installationRoot.toPath())) {
					return false;
				}
			}
			return true;
		} else {
			if (session.getGoals().subList(1, session.getGoals().size()).contains(InstallerMojosInformation.pluginPrefix + "" + currentGoalName)) { // current goal is after the first one : most likely that the first goal created an environment (tibco.installation.createNew=true) and we want to continue on the same
				return true;
			}

			return false;
		}
	}

	protected static String _environmentName = null;
	protected static File _installationRoot = null;
	protected static Boolean _createNewEnvironment = null;
	protected static Boolean _isDependency = false;
	protected static Boolean _removeExistingEnvironment = null;

	public abstract String getProductName();
	public abstract ProductType getProductType();
	public abstract File getInstallationPackage(boolean resolve) throws MojoExecutionException;
	public abstract String getInstallationPackageRegex();
	public abstract void setInstallationPackageRegex(String installationPackageRegex);
	public abstract Integer getInstallationPackageVersionGroupIndex();
	public abstract void setInstallationPackageRegexVersionGroupIndex(Integer installationPackageRegexVersionGroupIndex);
	public abstract Integer getInstallationPackageArchGroupIndex();
	public abstract String getInstallationPackageArchPropertyName();
	public abstract Integer getInstallationPackageOsGroupIndex();
	public abstract String getInstallationPackageOsPropertyName();
	public abstract String getInstallationPackagePropertyName();
	public abstract String getInstallationPackageVersionPropertyName();
	public abstract String getInstallationPackageVersionMajorMinorPropertyName();
	public abstract String getInstallationPackageVersionMajorMinor();
	public abstract void setInstallationPackage(File installationPackage);

	public abstract String getRemoteInstallationPackageGroupId();
	public abstract String getRemoteInstallationPackageArtifactId();
	public abstract String getRemoteInstallationPackageVersion();
	public abstract String getRemoteInstallationPackagePackaging();
	public abstract String getRemoteInstallationPackageClassifier();
	public abstract void setRemoteInstallationPackageGroupId(String remoteInstallationPackageGroupId);
	public abstract void setRemoteInstallationPackageArtifactId(String remoteInstallationPackageArtifactId);
	public abstract void setRemoteInstallationPackageVersion(String remoteInstallationPackageVersion);
	public abstract void setRemoteInstallationPackageClassifier(String remoteInstallationPackageClassifier);
	public String getRemotePackageCoordinates() {
		String classifier = getRemoteInstallationPackageClassifier();

		return getRemoteInstallationPackageGroupId() + ":" + getRemoteInstallationPackageArtifactId() + ":" + getRemoteInstallationPackageVersion() + ":zip" + (classifier != null ? ":" + classifier : "");
	}

	public abstract void setInstallationPackageVersionMajorMinor(String version);
	public abstract boolean hasDependencies();
	public abstract boolean dependenciesExist() throws MojoExecutionException;
	public abstract boolean installationExists() throws MojoExecutionException;
	public abstract void setProperties(Properties props);
	public abstract List<String> getDependenciesGoals();

	public void buildFailed(File logFile) {
		getLog().info("");

		getLog().info(getProductName() + " installation failed, see log file for details : " + logFile.getAbsolutePath());
	}

	@Component
	protected ArchiverManager archiverManager;

	private File executableFile;
	private File silentFile;
	private List<File> logDirectories;

	private boolean firstDependency = true;
	public static boolean firstGoal = true;

	private File getSilentFile(File directory) {
		if (silentFile != null) {
			return silentFile;
		}

		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".silent");
			}
		};

		File[] silentFiles = directory.listFiles(filter);
		if (silentFiles.length > 1) {
			// TODO: warn
			silentFile = silentFiles[0];
		} else if (silentFiles.length == 1) {
			silentFile = silentFiles[0];
		} else {
			silentFile = null;
		}

		return silentFile;
	}

	protected File getExecutableFile(File directory) throws MojoExecutionException {
		if (executableFile != null) {
			return executableFile;
		}

		final String extension;
		if (SystemUtils.IS_OS_WINDOWS) {
			extension = ".exe";
		} else {
			extension = ".bin";
		}

		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("TIBCOUniversalInstaller") && name.endsWith(extension);
			}
		};

		File[] executableFiles = directory.listFiles(filter);
		if (executableFiles.length > 1) {
			// TODO: warn
			executableFile = executableFiles[0];
		} else if (executableFiles.length == 1) {
			executableFile = executableFiles[0];
		} else {
			executableFile = null;
		}

		return executableFile;
	}

	public File getInstallationPackage() throws MojoExecutionException {
		return getInstallationPackage(true);
	}

	private File findRemoteInstallationPackage() throws MojoExecutionException, ArtifactNotFoundException, ArtifactResolutionException {
		String groupId = getRemoteInstallationPackageGroupId();
		String artifactId = getRemoteInstallationPackageArtifactId();
		String version = getRemoteInstallationPackageVersion();
		String packaging = getRemoteInstallationPackagePackaging();
		String classifier = getRemoteInstallationPackageClassifier();

		return getDependency(groupId, artifactId, version, packaging, classifier, true);
	}

	/**
	 * <p>
	 * This method tries to find the installation package (i.e. the official TIBCO archive of the software to install).
	 * This package can be in the {@code installationPacakgeDirectory} following the regular expression retrieved with
	 * {@code getInstallationPackageRegex()} or "remotely" from a Maven repository (can be also the local repository)
	 * based on {@code getRemotePackageGroupId()}, {@code getRemotePackageArtifactId()} and
	 * {@code getRemotePackageVersion()} methods to retrieve the coordinates of artefact to use as the installation
	 * package. 
	 * </p>
	 *
	 * @return the installation package found, null otherwise
	 *
	 * @throws MojoExecutionException 
	 */
	protected File findInstallationPackage() throws MojoExecutionException {
		String remoteInstallationPackageVersion = getRemoteInstallationPackageVersion();
		String remoteInstallationPackageClassifier = getRemoteInstallationPackageClassifier();
		if (!StringUtils.isEmpty(remoteInstallationPackageVersion) && !StringUtils.isEmpty(remoteInstallationPackageClassifier)) {
			File remoteInstallationPacakge;
			try {
				remoteInstallationPacakge = findRemoteInstallationPackage();

				if (remoteInstallationPacakge == null || !remoteInstallationPacakge.exists()) {
					throw new FileNotFoundException();
				} else {
					installationPackageVersion = remoteInstallationPackageVersion;
					return remoteInstallationPacakge;
				}
			} catch (MojoExecutionException | FileNotFoundException | ArtifactNotFoundException | ArtifactResolutionException e) {
				getLog().info("");
				getLog().error("This goal is configured to retrieve a remote installation package but this package cannot be found.");
				getLog().error("The Maven coordinates configured for the remote installation package are: " + this.getRemotePackageCoordinates());

				throw new MojoExecutionException("Remote installation package not found", e);
			}
		}

		if (installationPackageDirectory == null || !installationPackageDirectory.exists() || !installationPackageDirectory.isDirectory()) {
			return null;
		}

	    final Pattern p = Pattern.compile(getInstallationPackageRegex(), Pattern.CASE_INSENSITIVE);

		File[] result = installationPackageDirectory.listFiles(new FileFilter(){
	        @Override
	        public boolean accept(File file) {
	        return p.matcher(file.getName()).matches();
	        }
	    });

		if (result != null && result.length > 0) {
			if (ignoredInstallationPackages != null) {
				for (File f : result) {
					if (ignoredInstallationPackages.contains(f)) {
						continue;
					}
					return f;
				}
			} else {
				return result[0];
			}
		}

		return null;
	}

	public String getInstallationPackageVersion() throws MojoExecutionException {
		if (installationPackageVersion != null && !installationPackageVersion.isEmpty()) {
			return installationPackageVersion;
		}

		File installationPackage = getInstallationPackage();
		if (installationPackage == null) {
			return null;
		}

		if (installationPackageVersion != null && !installationPackageVersion.isEmpty()) {
			return installationPackageVersion;
		}

		String name = installationPackage.getName();

		Pattern p = Pattern.compile(getInstallationPackageRegex(), Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(name);
		if (m.matches()) {
			installationPackageVersion = m.group(getInstallationPackageVersionGroupIndex());
		} else if (StringUtils.isNotEmpty(getRemoteInstallationPackageVersion())) {
			installationPackageVersion = getRemoteInstallationPackageVersion();
		} else {
			installationPackageVersion = "Version Not Found";
		}

		return installationPackageVersion;
	}

	private String getNormalizedInstallationPackageArch(String packageArch) {
		if ("x86_64".equals(packageArch)) {
			packageArch = "amd64";
		}
		return packageArch;
	}

	public String getInstallationPackageArch(boolean normalized) throws MojoExecutionException {
		if (installationPackageArch != null && !installationPackageArch.isEmpty()) {
			return normalized ? getNormalizedInstallationPackageArch(installationPackageArch) : installationPackageArch;
		}

		File installationPackage = getInstallationPackage();
		if (installationPackage == null) {
			return null;
		}
		
		String name = installationPackage.getName();
		
		Pattern p = Pattern.compile(getInstallationPackageRegex(), Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(name);
		if (m.matches()) {
			installationPackageArch = m.group(getInstallationPackageArchGroupIndex());
		} else if (StringUtils.isNotBlank(getRemoteInstallationPackageClassifier())) {
			name = getRemoteInstallationPackageClassifier();
			p = Pattern.compile("([^_]*)_(.*)");
			m = p.matcher(name);
			if (m.matches()) {
				installationPackageArch = m.group(2);
			}
		} else {
			installationPackageArch = "Arch Not Found";
		}

		return normalized ? getNormalizedInstallationPackageArch(installationPackageArch) : installationPackageArch;
	}

	private String getNormalizedInstallationPackageOs(String packageOs) {
		if ("win".equals(packageOs)) {
			return "windows";
		} else if (packageOs.contains("linux")) {
			return "unix";
		} else if (packageOs.contains("mac")) {
			return "mac";
		}

		return packageOs;
	}

	public String getInstallationPackageOs(boolean normalized) throws MojoExecutionException {
		if (installationPackageOs != null && !installationPackageOs.isEmpty()) {
			return normalized ? getNormalizedInstallationPackageOs(installationPackageOs) : installationPackageOs;
		}
		
		File installationPackage = getInstallationPackage();
		if (installationPackage == null) {
			return null;
		}
		
		String name = installationPackage.getName();
		
		Pattern p = Pattern.compile(getInstallationPackageRegex(), Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(name);
		if (m.matches()) {
			installationPackageOs = m.group(getInstallationPackageOsGroupIndex());
		} else if (StringUtils.isNotBlank(getRemoteInstallationPackageClassifier())) {
			name = getRemoteInstallationPackageClassifier();
			p = Pattern.compile("([^_]*)_(.*)");
			m = p.matcher(name);
			if (m.matches()) {
				installationPackageOs = m.group(1);
			}
		} else {
			installationPackageOs = "OS Not Found";
		}

		return normalized ? getNormalizedInstallationPackageOs(installationPackageOs) : installationPackageOs;
	}

	protected void extractInstallationPackagePreparation(@NotNull File installationPackage) throws MojoExecutionException {
		String version = getInstallationPackageVersion();
		String productName = getProductName();

		getLog().info("-> Product information");
		getLog().info("");
		getLog().info("Product name                       : " + productName);
		getLog().info("Version                            : " + version);
		getLog().info("Package file                       : " + installationPackage.getAbsolutePath());
		getLog().info("");
	}

	protected File extractInstallationPackage(@NotNull File installationPackage) throws MojoExecutionException {
		extractInstallationPackagePreparation(installationPackage);

		File tmpDirectory = Files.createTempDir();
		File silentFile;

		try {
			UnArchiver unArchiver = archiverManager.getUnArchiver(installationPackage);

			unArchiver.setSourceFile(installationPackage);
			unArchiver.setDestDirectory(tmpDirectory);

			getLog().info("-> Package extraction");
			getLog().info("");
			getLog().info("Extracting installation package to : " + tmpDirectory.getAbsolutePath());

			unArchiver.extract();

			silentFile = getSilentFile(tmpDirectory);

			Properties props = new Properties();
			props.loadFromXML(new FileInputStream(silentFile));

			if (environmentName == null) {
				if (_environmentName != null) {
					environmentName = _environmentName;
				} else {
					environmentName = props.getProperty("environmentName");
					_environmentName = environmentName;
				}
			}

			props.setProperty("acceptLicense", "true"); // always true
			props.setProperty("createNewEnvironment", getCreateNewEnvironment().toString());
			props.setProperty("environmentName", environmentName.toString());
			props.setProperty("installationRoot", getInstallationRoot().getAbsolutePath());

			setProperties(props);

			props.storeToXML(new FileOutputStream(silentFile), "Automatic install");
		} catch (NoSuchArchiverException | IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

		return tmpDirectory;
	}

	private void installDependency(String goal) throws MojoExecutionException {
		if (ignoreDependencies) {
			return;
		}

		getLog().info("Detected a dependency. Installing...");
		getLog().info("");
		getLog().info(">>> " + pluginDescriptor.getArtifactId() + ":" + pluginDescriptor.getVersion() + ":" + goal + " (" + "default-cli" + ") @ " + project.getArtifactId() + " >>>");
		getLog().info("");

		ArrayList<Element> configuration = new ArrayList<Element>();
		if (!firstDependency) {
			configuration.add(element("createNewEnvironment", "false"));
		} else {
			firstDependency = false;
		}

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

	protected void installRV() throws MojoExecutionException {
		session.getRequest().getGoals().add(InstallerMojosInformation.pluginPrefix + "install-rv");
		installDependency("install-rv");
	}

	protected void installTRA() throws MojoExecutionException {
		session.getRequest().getGoals().add(InstallerMojosInformation.pluginPrefix + "install-tra");
		installDependency("install-tra");
	}

	@Override
	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() {
		return new InstallerLifecycleParticipant();
	}

	public void initDefaultParameters() throws MojoExecutionException {
        initVersionArchOs();

		if (getInstallationRoot() == null) {
			// set "c:/tibco" if windows, "/opt/tibco" if *nix
			installationRootWasNotSet = true;
			File defaultInstallationRoot;
			if (SystemUtils.IS_OS_WINDOWS) {
				defaultInstallationRoot = new File("C:/tibco");
			} else {
				defaultInstallationRoot = new File("/opt/tibco");
			}
			installationRoot = defaultInstallationRoot;

			if (installationRoot.exists()) {
				getLog().error("There is no installation root directory set and the default directory '" + installationRoot + "' already exists.");
				getLog().error("Set installation root directory explicitly with '" + InstallerMojosInformation.installationRoot + "' property to force installation in this directory or to choose another one.");

				throw new MojoExecutionException("Default installation root directory already exists", new FileExistsException(installationRoot));
			} else {
				getLog().warn("There is no installation root directory set with property '" + InstallerMojosInformation.installationRoot + "'.");
				getLog().warn("The default directory '" + installationRoot + "' will be used because it does not exist.");
				getLog().warn("Set installation root directory explicitly with '" + InstallerMojosInformation.installationRoot + "' property to avoid this warning.");
			}

			session.getCurrentProject().getProperties().put(InstallerMojosInformation.installationRoot, getInstallationRoot().getAbsolutePath().replace("\\", "/"));
		}

		File installationPackage = getInstallationPackage();
		if (installationPackage != null && installationPackage.exists()) {
			session.getCurrentProject().getProperties().put(getInstallationPackagePropertyName(), installationPackage.getAbsolutePath().replace("\\", "/"));
		}
	}

    public void initVersionArchOs() throws MojoExecutionException {
        String packageVersion = getInstallationPackageVersion();
        if (packageVersion != null) {
			this.setInstallationPackageVersion(packageVersion);
			session.getCurrentProject().getProperties().put(getInstallationPackageVersionPropertyName(), packageVersion);
            Pattern p = Pattern.compile("(\\d+.\\d+).*", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(packageVersion);

            String packageVersionMajorMinor = "";
            if (m.matches()) {
                packageVersionMajorMinor = m.group(1);
            }

            this.setInstallationPackageVersionMajorMinor(packageVersionMajorMinor);
            session.getCurrentProject().getProperties().put(getInstallationPackageVersionMajorMinorPropertyName(), packageVersionMajorMinor);
        }

        String packageArch = getInstallationPackageArch(true);
        if (packageArch != null) {
			session.getCurrentProject().getProperties().put(getInstallationPackageArchPropertyName(), packageArch);
        }
        String packageOs = getInstallationPackageOs(true);
        if (packageOs != null) {
            session.getCurrentProject().getProperties().put(getInstallationPackageOsPropertyName(), packageOs);
        }
    }

    public <T> void initStandalonePOMNoDefaultParameters() throws MojoExecutionException {
		super.initStandalonePOM();
	}

	@Override
	public <T> void initStandalonePOM() throws MojoExecutionException {
		super.initStandalonePOM();

		if (getInstallationPackage(false).exists()) {
			installationPackageWasAlreadyResolved = true;
		}

		initDefaultParameters();

		if (firstGoal) {
			firstGoal = false;

			List<String> _goals = new ArrayList<String>();
			_goals.addAll(session.getRequest().getGoals());
			List<String> dependenciesGoals = getDependenciesGoals();
			if (ignoreDependencies) {
				dependenciesGoals.clear();
			}

			try {
				if (_goals.size() > 1) {
					for (String goal : _goals.subList(1, _goals.size())) {
						if (!dependenciesGoals.contains(goal)) {
							dependenciesGoals.add(goal);
						}
					}
				}

				if (dependenciesGoals.size() > 0) {
					for (Iterator<String> it = dependenciesGoals.iterator(); it.hasNext();) {
						String goal = (String) it.next();
						CommonInstaller mojo = InstallerMojosFactory.getInstallerMojo(goal);
						mojo.setSession(session);
						mojo.initStandalonePOM();
						if (!getCreateNewEnvironment() && mojo.installationExists()) {
							it.remove();
						}
					}
				}

				session.getRequest().getGoals().addAll(dependenciesGoals);

				try {
					if (pluginManager != null && !_isDependency) {
						PropertiesEnforcer.enforceProperties(session, pluginManager, logger, new ArrayList<String>(), InstallerLifecycleParticipant.class, InstallerLifecycleParticipant.pluginKey); // check that all mandatory properties are correct
					}
				} catch (MavenExecutionException e) {
					throw new MojoExecutionException(e.getLocalizedMessage(), e);
				}

				session.getRequest().setGoals(_goals);
			} finally {
				session.getRequest().setGoals(_goals);
			}
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Mojo mojoAnnotation = this.getClass().getAnnotation(Mojo.class);
		currentGoalName = mojoAnnotation.name();

		if (getCreateNewEnvironment() || _isDependency) {
			try {
				checkForExistingInstallation();
			} catch (IOException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}

		if (installationExists()) {
			getLog().info(getProductName() + " version " + getInstallationPackageVersionMajorMinor() + " is already installed in the directory '" + getInstallationRoot().getAbsolutePath() + "': skipping installation.");
			return;
		}

		if (getCreateNewEnvironment() && hasDependencies()) { // check if it is possible
			throw new MojoExecutionException("The package to install has dependencies and cannot be created in a new environment");
		}
		if (!getCreateNewEnvironment() && !dependenciesExist()) {
			throw new MojoExecutionException("The package to install has unresolved dependencies in this environment (" + environmentName + "=" + installationRoot + ")");
		}

		File installationPackage = getInstallationPackage();

		if (installationPackage == null || !installationPackage.exists()) {
			throw new MojoExecutionException("Installation package not found.", new FileNotFoundException());
		}

		File extractedInstallationPackage = extractInstallationPackage(installationPackage);

		executeInstallationPackage(extractedInstallationPackage);
	}

	private void checkForExistingInstallation() throws MojoExecutionException, MojoFailureException, IOException {
		EnvInfo envInfo = new EnvInfo();
		try {
			envInfo.setLog(new NoOpLogger());
			envInfo.execute();
		} catch (MojoExecutionException | MojoFailureException e) {
			return;
		}

		getLog().debug(environmentName);
		getLog().debug(getInstallationRoot().getAbsolutePath());

		getLog().info("-> Checking up environment");
		getLog().info("");
		Environment environment = envInfo.getEnvironments().get(environmentName);
		File environmentLocation = null;
		if (environment != null) {
			environmentLocation = new File(environment.getLocation());
		}

		if (environment != null && environmentLocation.exists()) {
			getLog().info("The environment '" + environmentName + "' already exists.");
			if (getRemoveExistingEnvironment()) {
				getLog().info("");
				if (environmentLocation.exists()) {
					getLog().info("Deleting existing installation directory '" + environmentLocation + "'.");
					try {
						FileUtils.deleteDirectory(environmentLocation);
					} catch (IOException e) {
						throw new MojoExecutionException(e.getLocalizedMessage(), e);
					}
				}
				if (envInfo.getEnvironments().containsKey(environmentName)) {
					getLog().info("Removing existing environment information '" + environmentName + "'.");

					RemoveEnvInfoMojo removeEnvInfoMojo = new RemoveEnvInfoMojo();
					removeEnvInfoMojo.setLog(new NoOpLogger());
					removeEnvInfoMojo.setSession(session);
					removeEnvInfoMojo.environmentName = environmentName;
					removeEnvInfoMojo.execute();
				}
				getLog().info("");
			} else if (reuseExistingEnvironment(environmentLocation)) {
				getLog().info("");
				getLog().info("The installation will continue and the environment '" + environmentName + "' will be updated.");
				getLog().info("");
				this.createNewEnvironment = false;
				_createNewEnvironment = false;
				this.installationRoot = environmentLocation;
				_installationRoot = environmentLocation;
			} else {
				getLog().error("Unable to continue. Set '" + InstallerMojosInformation.removeExistingEnvironment + "' to 'true' to force installation removal.");
				getLog().error("!!! Caution: this will remove the 'TIBCO_HOME' directory of the installation: '" + environmentLocation + "' !!!");
				getLog().error("");
				getLog().error("Otherwise, specify another target environment with the two properties 'tibco.installation.root' and 'tibco.installation.environmentName'.");
				getLog().error("If the product to install needs to be installed in an existing environment, set '" + InstallerMojosInformation.createNewEnvironment + "' to 'false'.");
				throw new MojoExecutionException("The installation already exists");
			}
		} else if (environment != null && !environmentLocation.exists()) {
			
		} else {
			getLog().info("Everything is ready to be installed in environment:");
			getLog().info(environmentName + "=" + getInstallationRoot().getAbsolutePath());
			getLog().info("");
		}
	}

	private File getLastLogFile() {
		File result = null;
		if (dotTIBCOHome == null || !dotTIBCOHome.exists() || !dotTIBCOHome.isDirectory()) return result;

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return dir.isDirectory() && name.startsWith("install_");
			}
		};

		File[] logDirectories = dotTIBCOHome.listFiles(filter);

		for (int i = 0; i < logDirectories.length; i++) {
			File logFile = logDirectories[i];

			if (!this.logDirectories.contains(logFile)) {
				File[] logFiles = logFile.listFiles(
					new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.startsWith("tibco_") && name.endsWith(".log");
						}
					}
				);

				if (logFiles.length > 1) {
					// TODO: warn
					result = logFiles[0];
				} else if (logFiles.length == 1) {
					result = logFiles[0];
				} else {
					result = null;
				}

			}
		}

		this.logDirectories.addAll(Arrays.asList(logDirectories));

		return result;
	}

	protected void addCommandLineArguments(CommandLine cmdLine) throws MojoExecutionException {
		cmdLine.addArgument("-silent");
		cmdLine.addArgument("-V");
		cmdLine.addArgument("responseFile=" + silentFile.getAbsolutePath(), false);
	}

	private void executeInstallationPackage(File extractedInstallationPackage) throws MojoExecutionException {
		if (extractedInstallationPackage == null || !extractedInstallationPackage.exists() || !extractedInstallationPackage.isDirectory()) {
			return;
		}

		File executableFile = getExecutableFile(extractedInstallationPackage);

		if (executableFile == null || !executableFile.exists()) {
			throw new MojoExecutionException("TIBCO Universal Installer not found.", new FileNotFoundException());
		}

		getLog().info("");
		getLog().info("-> Installing " + getProductName() + "...");
		getLog().info("");
		if (silentFile != null && silentFile.exists()) {
			getLog().info("Using silent file                  : " + silentFile.getAbsolutePath());
		}
		getLog().info("Creating a new environment         : " + (getCreateNewEnvironment() ? "yes" : "no"));
		getLog().info("Environment name                   : " + environmentName.toString());
		getLog().info("Installation root (TIBCO_HOME)     : " + getInstallationRoot().getAbsolutePath());

		CommandLine cmdLine = new CommandLine(executableFile);
		addCommandLineArguments(cmdLine);

		getLog().debug("install command line : " + cmdLine.toString());
		getLog().debug("working dir : " + extractedInstallationPackage);

		DefaultExecutor executor = new DefaultExecutor();
		executor.setWorkingDirectory(extractedInstallationPackage);

		Integer timeOut = 1800; // 30 minutes
		if (timeOut > 0) {
			ExecuteWatchdog watchdog = new ExecuteWatchdog(timeOut * 1000);
			executor.setWatchdog(watchdog);
		}

		executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

		ByteArrayOutputStream stdOutAndErr = new ByteArrayOutputStream();
		executor.setStreamHandler(new PumpStreamHandler(stdOutAndErr));

		// list old log files
		this.logDirectories = new ArrayList<File>();
		getLastLogFile();

		int result = -1;
		try {
			getLog().debug(cmdLine.toString());
			result = executor.execute(cmdLine);
		} catch (IOException e) {
			if (e.getCause().getMessage().contains("=740")) { // Windows and not admin
				windowsNeedElevation();
				//return;
			}
		}

		// deduce new log file
		File logFile = getLastLogFile();
		if (logFile != null) {
			getLog().debug("Found log file in : " + logFile.getAbsolutePath());
		}

		if (result != 0) {
			if (logFile != null) {
				buildFailed(logFile);
			}
			throw new MojoExecutionException(Messages.INSTALLATION_FAILURE);
		} else { // 0 but could have failed silently
			detectSilentErrors(logFile);
		}

	}

	private void windowsNeedElevation() throws MojoExecutionException {
		getLog().info("");
		getLog().error("Administration privileges are required to install TIBCO products on Windows.");

		throw new MojoExecutionException("Administration privileges are required to install TIBCO products on Windows.");
	}

	private void detectSilentErrors(File logFile) throws MojoExecutionException {
		if (logFile != null) {
			LineIterator it;
			try {
				it = FileUtils.lineIterator(logFile, "UTF-8");
			} catch (IOException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}

			try {
				while (it.hasNext()) {
					String line = it.nextLine();
					if (hasError(line) && !ignoreError(line)) {
						getLog().debug(line);
						buildFailed(logFile);
						throw new MojoExecutionException(Messages.INSTALLATION_FAILURE);
					}
				}
			} finally {
				LineIterator.closeQuietly(it);
			}
		}
	}

	private boolean hasError(String line) {
		return line.matches("(\\(.*\\)), ::ERROR::, (.*)") ||
			   line.matches("(.*)ERROR:(.*)");
	}

	private boolean ignoreError(String line) {
		return line.contains("Unable to install::product_tibco_rv_runtime");
	}

	public void setInstallationPackageDirectory(File installationPackageDirectory) {
		this.installationPackageDirectory = installationPackageDirectory;
	}

	public void setIgnoredInstallationPackages(List<File> ignoredInstallationPackages) {
		this.ignoredInstallationPackages = ignoredInstallationPackages;
	}

	public boolean installationPackageWasAlreadyResolved() {
		return installationPackageWasAlreadyResolved;
	}
}
