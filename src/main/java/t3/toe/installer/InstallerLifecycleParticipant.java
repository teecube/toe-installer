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
package t3.toe.installer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import t3.AdvancedMavenLifecycleParticipant;
import t3.CommonMojo;
import t3.plugin.PluginConfigurator;
import t3.plugin.PluginManager;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "TOEInstallerLifecycleParticipant")
public class InstallerLifecycleParticipant extends AbstractMavenLifecycleParticipant implements AdvancedMavenLifecycleParticipant {

    @Requirement
    private PlexusContainer plexus;

	@Requirement
	private Logger logger;

	@Requirement
	private ArtifactRepositoryFactory artifactRepositoryFactory;

	@Requirement
	protected BuildPluginManager pluginManager;

	@Requirement
	protected ProjectBuilder projectBuilder;

	@org.apache.maven.plugins.annotations.Component
	protected PluginDescriptor pluginDescriptor; // plugin descriptor of this plugin

	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		fixStandalonePOM(session.getCurrentProject(), new File(session.getRequest().getBaseDirectory()));

		PluginConfigurator.propertiesManager = CommonMojo.propertiesManager(session, session.getCurrentProject());

		List<MavenProject> projects = prepareProjects(session.getProjects(), session);
		session.setProjects(projects);

		PluginManager.registerCustomPluginManager(pluginManager, new InstallerMojosFactory()); // to inject Global Parameters in Mojos

		super.afterProjectsRead(session);
	}

	/**
	 * <p>
	 *
	 * </p>
	 *
	 * @param session
	 * @param projects
	 * @param projectBuildingRequest
	 * @throws MavenExecutionException
	 */
	private List<MavenProject> prepareProjects(List<MavenProject> projects, MavenSession session) throws MavenExecutionException {
		List<MavenProject> result = new ArrayList<MavenProject>();

		if (projects == null) {
			logger.warn("No projects to prepare.");
			return result;
		}

		for (MavenProject mavenProject : projects) {
			mavenProject.getProperties().put("_d", "$");
			PluginConfigurator.addPluginsParameterInModel(mavenProject, InstallerLifecycleParticipant.class, logger);
			result.add(mavenProject);
		}

		return result;
	}

	private void fixStandalonePOM(MavenProject mavenProject, File requestBaseDirectory) {
		if (mavenProject == null) return;

		if ("standalone-pom".equals(mavenProject.getArtifactId()) && requestBaseDirectory != null) {
			mavenProject.setFile(new File(requestBaseDirectory, "pom.xml"));
		}
	}

	public void setPlexus(PlexusContainer plexus) {
		this.plexus = plexus;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setArtifactRepositoryFactory(ArtifactRepositoryFactory artifactRepositoryFactory) {
		this.artifactRepositoryFactory = artifactRepositoryFactory;
	}

	public void setPluginManager(BuildPluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public void setProjectBuilder(ProjectBuilder projectBuilder) {
		this.projectBuilder = projectBuilder;
	}

	public void setPluginDescriptor(PluginDescriptor pluginDescriptor) {
		this.pluginDescriptor = pluginDescriptor;
	}

}