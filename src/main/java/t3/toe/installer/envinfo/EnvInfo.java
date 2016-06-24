/**
 * (C) Copyright 2014-2016 teecube
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
package t3.toe.installer.envinfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.tibco.envinfo.TIBCOEnvironment.Environment;

import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.plugin.PropertiesEnforcer;
import t3.toe.installer.InstallerLifecycleParticipant;

/**
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public class EnvInfo extends CommonMojo {

	protected static String installShieldPath = "InstallShield/Universal/TIBCO/Gen1/_vpddb/_envInfo.xml";
	protected static String userPath = ".TIBCOEnvInfo/_envInfo.xml";

	protected List<File> envInfosFiles;
	private Map<String, Environment> environments;
	private static boolean filesDisplayed = false;

	@Override
	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() throws MojoExecutionException {
		return new InstallerLifecycleParticipant();
	}

	@Override
	protected <T> void initStandalonePOM() throws MojoExecutionException {
		super.initStandalonePOM();

		try {
			if (pluginManager != null) {
				PropertiesEnforcer.enforceProperties(session, pluginManager, logger, new ArrayList<String>(), InstallerLifecycleParticipant.class); // check that all mandatory properties are correct
			}
		} catch (MavenExecutionException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

	private List<File> getEnvInfos() {
		List<File> result = new ArrayList<File>();

		if (SystemUtils.IS_OS_WINDOWS) {
			String commonProgramFiles = System.getenv("CommonProgramFiles");
			if (commonProgramFiles != null) {
				File f = new File(commonProgramFiles, installShieldPath);
				if (f.exists()) {
					getLog().debug("found envInfo: " + f.getAbsolutePath());
					result.add(f);
				}
			}
			String commonProgramFiles32 = System.getenv("CommonProgramFiles(x86)");
			if (commonProgramFiles32 != null) {
				File f = new File(commonProgramFiles32, installShieldPath);
				if (f.exists()) {
					getLog().debug("found envInfo: " + f.getAbsolutePath());
					result.add(f);
				}
			}
		}

		String userHome = System.getProperty("user.home");
		if (userHome != null) {
			File f = new File(userHome, installShieldPath);
			if (f.exists()) {
				getLog().debug("found envInfo: " + f.getAbsolutePath());
				result.add(f);
			}
			f = new File(userHome, userPath);
			if (f.exists()) {
				getLog().debug("found envInfo: " + f.getAbsolutePath());
				result.add(f);
			}
		}
		return result;
	}

	private Map<String, Environment> getEnvironments(List<File> files) throws MojoExecutionException {
		Map<String, Environment> result = new HashMap<String, Environment>();

		if (files == null) return result;

		for (File file : files) {
			TIBCOEnvironmentMarshaller marshaller;
			try {
				marshaller = new TIBCOEnvironmentMarshaller(file);
				for (Environment env : marshaller.getObject().getEnvironment()) {
					if (result.get(env.getName()) == null) {
						result.put(env.getName(), env);
					}
				}
			} catch (JAXBException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}

		return result;
	}

	protected void saveEnvironments(Map<String, Environment> environments, List<File> files) throws MojoExecutionException {
		if (environments == null || files == null) return;

		for (File file : files) {
			TIBCOEnvironmentMarshaller marshaller;
			try {
				marshaller = new TIBCOEnvironmentMarshaller(file);
				marshaller.getObject().getEnvironment().clear();
				marshaller.getObject().getEnvironment().addAll(environments.values());
				marshaller.save();
			} catch (JAXBException | UnsupportedEncodingException | FileNotFoundException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}

	}

	protected void displayEnvironments() {
		int i = 0;
		for (Environment env : getEnvironments().values()) {
			getLog().info("");
			getLog().info("-- Environment #" + ++i);
			getLog().info("Name             : " + env.getName());
			getLog().info("Location         : " + env.getLocation());
			if (env.getConfigDir() != null)  {
				getLog().info("Config directory : " + env.getConfigDir());
			}
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		envInfosFiles = getEnvInfos();
		if (!filesDisplayed) {
			filesDisplayed = true;
			getLog().info("Environment information files on this system are:");
			for (File file : envInfosFiles) {
				getLog().info("  " + file.getAbsolutePath());
			}
		}
		environments = getEnvironments(envInfosFiles);
	}

	public Map<String, Environment> getEnvironments() {
		return environments;
	}

}
