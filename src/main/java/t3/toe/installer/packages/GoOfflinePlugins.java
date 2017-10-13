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
package t3.toe.installer.packages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.DefaultSettingsWriter;

import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.toe.installer.InstallerMojosInformation;

/**
* <p>
* This goal displays resolved TIBCO installation packages found in a given directory.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
@Mojo(name = "go-offline-plugins", requiresProject = false)
public class GoOfflinePlugins extends AbstractPackagesResolver {

	@Parameter(property = InstallerMojosInformation.Packages.Offline.generateSettings, defaultValue = InstallerMojosInformation.Packages.Offline.generateSettings_default)
	protected Boolean generateSettings;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		MavenProject goOfflineProject = generateGoOfflineProject();
		getLog().info("Going offline by creating a standalone Maven repository in '" + offlineArchiveLocalRepository.getAbsolutePath() + "'");

		if (!plugins.isEmpty()) {
			getLog().info("");
			getLog().info("This repository will include following plugins:");
			for (T3Plugins plugin : plugins) {
				getLog().info("-> " + plugin.getProductName());
			}
			getLog().info("");
		}
	
		getLog().info("This might take some minutes...");

		goOffline(goOfflineProject, offlineArchiveLocalRepository, "3.5.0");

		if (generateSettings) {
			generateOfflineSettings();
		}

		if (generateArchive) {
			getLog().info("");
			getLog().info("Adding offline repository '" + offlineDirectory.getAbsolutePath() + "' to archive '" + offlineArchive.getAbsolutePath() + "'");
			try {
				offlineArchive.delete();
				addFilesToZip(offlineDirectory, offlineArchive);
			} catch (IOException | ArchiveException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}

		getLog().info("");
	}

	private void generateOfflineSettings() throws MojoExecutionException {
		getLog().info("");
		getLog().info("Generating offline Maven settings.xml in '" + offlineDirectory.getAbsolutePath() + "'");

		Settings defaultSettings = new Settings();

		defaultSettings.setLocalRepository("./offline/repository"); // use only offline repository
		defaultSettings.setOffline(true); // offline to use only offline repository
		// <pluginGroups> to define T3 plugins' groupIds
		List<String> pluginGroups = new ArrayList<String>();
		pluginGroups.add("io.teecube.tic");
		pluginGroups.add("io.teecube.toe");
		defaultSettings.setPluginGroups(pluginGroups );

		// write the settings.xml in target/offline
		DefaultSettingsWriter settingsWriter = new DefaultSettingsWriter();
		try {
			settingsWriter.write(new File(offlineDirectory, "settings.xml"), null, defaultSettings);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

}
