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
package t3.toe.installer.environments.commands;

import org.apache.maven.plugin.MojoExecutionException;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import t3.CommonMojo;
import t3.toe.installer.environments.CustomProduct;
import t3.toe.installer.environments.MavenCommand;
import t3.toe.installer.environments.products.ProductToInstall;
import t3.utils.SettingsManager;

import java.io.File;
import java.io.IOException;

public class MavenCommandToExecute extends CommandToExecute<MavenCommand> {

    private final MavenCommand mavenCommand;

    public MavenCommandToExecute(MavenCommand command, CommonMojo commonMojo, int commandIndex, CommandType commandType, ProductToInstall<CustomProduct> productToInstall) {
        super(command, commonMojo, commandIndex, commandType, productToInstall);

        this.mavenCommand = command;
    }

    @Override
    public String commandFailureMessagge() {
        return "The Maven command failed.";
    }

    @Override
    public boolean doExecuteCommand(String commandCaption) throws MojoExecutionException {
        File settingsFile;
        File pomFile;
        try {
            // save current Settings object to a temporary file
            settingsFile = SettingsManager.saveSettingsToTempFile(this.session.getSettings());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        BuiltProject result = this.commonMojo.executeGoal(this.mavenCommand.getGoals().getGoal(), settingsFile, settingsFile, new File(this.session.getSettings().getLocalRepository()), "3.3.9");

        String[] lines = result.getMavenLog().split("\\r?\\n");
        for (String line : lines) {
            getLog().info(line);
        }

        if (result.getMavenBuildExitCode() != 0) {
            return false;
        }

        return true;
    }

    @Override
    public String getCommandTypeCaption() {
        return "Maven command";
    }
}
