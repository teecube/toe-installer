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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.invoker.InvokerLogger;
import org.apache.maven.shared.invoker.PrintStreamLogger;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import t3.CommonMojo;
import t3.log.PrefixedLogger;
import t3.toe.installer.environments.CustomProduct;
import t3.toe.installer.environments.MavenCommand;
import t3.toe.installer.environments.products.ProductToInstall;
import t3.utils.MavenRunner;
import t3.utils.PrefixPrintStream;
import t3.utils.SettingsManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MavenCommandToExecute extends CommandToExecute<MavenCommand> {

    private final MavenCommand mavenCommand;

    public MavenCommandToExecute(MavenCommand command, CommonMojo commonMojo, int commandIndex, CommandType commandType) {
        this(command, commonMojo, commandIndex, commandType, null);
    }

    public MavenCommandToExecute(MavenCommand command, CommonMojo commonMojo, int commandIndex, CommandType commandType, ProductToInstall productToInstall) {
        super(command, commonMojo, commandIndex, commandType, productToInstall);

        this.mavenCommand = command;
    }

    @Override
    public String commandFailureMessagge() {
        return "The Maven command failed.";
    }

    @Override
    public boolean doExecuteCommand(String commandCaption) throws MojoExecutionException {
        getLog().info("");
        getLog(">").info("mvn " + StringUtils.join(this.command.getGoals().getGoal(), " "));
        getLog().info("");

        File settingsFile;
        File pomFile;
        try {
            // save current Settings object to a temporary file
            settingsFile = SettingsManager.saveSettingsToTempFile(this.session.getSettings());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        File workingDirectory = getWorkingDirectory();

        File pomFileInWorkingDirectory = new File(workingDirectory, "pom.xml");
        Properties properties = new Properties();
        if (this.mavenCommand.getProperties() != null) {
            for (MavenCommand.Properties.Property property : this.mavenCommand.getProperties().getProperty()) {
                properties.put(property.getKey(), property.getValue());
            }
        }
        BuiltProject result;

        List<String> profiles = this.mavenCommand.getProfiles() != null ? this.mavenCommand.getProfiles().getProfile() : new ArrayList<String>();

        MavenRunner mavenRunner = new MavenRunner();
        mavenRunner.setGlobalSettingsFile(settingsFile);
        mavenRunner.setUserSettingsFile(settingsFile);
        mavenRunner.setLocalRepositoryDirectory(new File(this.session.getSettings().getLocalRepository()));
        mavenRunner.setMavenVersion("3.3.9");
        mavenRunner.setGoals(this.mavenCommand.getGoals().getGoal());
        PrintStream outPrintStream = new PrefixPrintStream(System.out,"MONPREFIXE ");
        PrintStreamLogger printStreamLogger = new PrintStreamLogger(outPrintStream, InvokerLogger.INFO);
        mavenRunner.setPrintStreamLogger(printStreamLogger);

        if (pomFileInWorkingDirectory.exists()) {
            mavenRunner.setPomFile(pomFileInWorkingDirectory);
        }

        result = mavenRunner.run();

        /*
        String[] lines = result.getMavenLog().split("\\r?\\n");
        for (String line : lines) {
            getLog().info(line);
        }
        */

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
