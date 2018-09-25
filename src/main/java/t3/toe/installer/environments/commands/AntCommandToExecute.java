/**
 * (C) Copyright 2016-2018 teecube
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
package t3.toe.installer.environments.commands;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import t3.CommonMojo;
import t3.toe.installer.environments.AntCommand;
import t3.toe.installer.environments.products.ProductToInstall;

import java.io.File;
import java.io.PrintStream;

public class AntCommandToExecute extends CommandToExecute<AntCommand> {

    private final AntCommand antCommand;

    public AntCommandToExecute(AntCommand command, CommonMojo commonMojo, int commandIndex, CommandType commandType) {
        this(command, commonMojo, commandIndex, commandType, null);
    }

    public AntCommandToExecute(AntCommand command, CommonMojo commonMojo, int commandIndex, CommandType commandType, ProductToInstall productToInstall) {
        super(command, commonMojo, commandIndex, commandType, productToInstall);

        this.antCommand = command;
    }

    @Override
    public String commandFailureMessagge() {
        return "The Ant command failed.";
    }

    @Override
    public boolean doExecuteCommand(String commandCaption) throws MojoExecutionException {
        boolean success = false;

        String target = this.antCommand.getTarget();

        DefaultLogger consoleLogger = new DefaultLogger();
        PrintStream printStream = new PrintStream(new CollectingLogOutputStream(getLog(), "", false));
        consoleLogger.setErrorPrintStream(printStream);
        consoleLogger.setOutputPrintStream(printStream);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);

        Project project = new Project();
        File buildFile = new File(this.antCommand.getBuildFile());
        if (!buildFile.exists()) {
            buildFile = new File(workingDirectory, this.antCommand.getBuildFile());
        }
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        project.addBuildListener(consoleLogger);

        try {
            project.fireBuildStarted();
            project.init();
            ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
            project.addReference("ant.projectHelper", projectHelper);
            for (AntCommand.Properties.Property property : this.antCommand.getProperties().getProperty()) {
                project.setUserProperty(property.getKey(), property.getValue());
            }

            projectHelper.parse(project, buildFile);

            // If no target specified then default target will be executed.
            String targetToExecute = (target != null && target.trim().length() > 0) ? target.trim() : project.getDefaultTarget();

            getLog().info("");
            String commandLine = "ant -buildfile " + buildFile.getAbsolutePath();
            if (this.command.getProperties() != null && !this.command.getProperties().getProperty().isEmpty()) {
                for (AntCommand.Properties.Property property : this.command.getProperties().getProperty()) {
                    commandLine += " -D" + property.getKey() + "=" + property.getValue();
                }
            }
            commandLine += " " + targetToExecute;
            getLog(">").info("Working directory : " + workingDirectory.getAbsolutePath());
            getLog(">").info("Command           : " + commandLine);
            getLog().info("");

            project.executeTarget(targetToExecute);
            project.fireBuildFinished(null);
            success = true;
        } catch (BuildException e) {
            project.fireBuildFinished(e);
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        return success;
    }

    @Override
    public String getCommandTypeCaption() {
        return "Ant command";
    }
}
