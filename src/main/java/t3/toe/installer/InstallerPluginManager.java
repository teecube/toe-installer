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

import java.lang.reflect.Field;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginConfigurationException;
import org.apache.maven.plugin.PluginContainerException;
import org.apache.maven.plugin.internal.DefaultMavenPluginManager;
import org.apache.maven.project.MavenProject;

import com.google.inject.spi.TypeListener;

import t3.MojosFactory;
import t3.plugin.PluginManager;

/**
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public class InstallerPluginManager extends PluginManager {

	public InstallerPluginManager(DefaultMavenPluginManager defaultMavenPluginManager, MojosFactory mojosFactory) {
		super(defaultMavenPluginManager, mojosFactory);
	}

	public static void registerCustomPluginManager(BuildPluginManager pluginManager, MojosFactory mojosFactory) {
		try {
			Field f = pluginManager.getClass().getDeclaredField("mavenPluginManager");
			f.setAccessible(true);
			DefaultMavenPluginManager oldMavenPluginManager = (DefaultMavenPluginManager) f.get(pluginManager);
			InstallerPluginManager mavenPluginManager = new InstallerPluginManager(oldMavenPluginManager, mojosFactory);
			f.set(pluginManager, mavenPluginManager);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			// no trace
		}
	}

	protected <T extends CommonInstaller> TypeListener getInstallerListener(T configuredMojo, MavenProject currentProject, MavenSession session) {
		return new InstallerListener<T>(configuredMojo, session.getCurrentProject(), session);
	}

	@Override
	protected <T> TypeListener getListener(T configuredMojo, MavenProject currentProject, MavenSession session) {
		return getInstallerListener((CommonInstaller) configuredMojo, session.getCurrentProject(), session);
	}

	@Override
	public <T> T getConfiguredMojo(Class<T> mojoInterface, final MavenSession session, MojoExecution mojoExecution) throws PluginConfigurationException, PluginContainerException {
		return super.getConfiguredMojo(mojoInterface, session, mojoExecution);
	}

}
