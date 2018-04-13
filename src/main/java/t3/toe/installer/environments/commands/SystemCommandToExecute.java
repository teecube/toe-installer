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
import org.apache.maven.plugin.logging.Log;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import t3.CommonMojo;
import t3.log.PrefixedLogger;
import t3.toe.installer.environments.SystemCommand;

import java.io.File;
import java.io.IOException;

public class SystemCommandToExecute extends CommandToExecute<SystemCommand> {

    public SystemCommandToExecute(Log logger, MojoExecutor.ExecutionEnvironment executionEnvironment, SystemCommand command, int commandIndex, CommandType commandType) {
        super(logger, executionEnvironment, command, commandIndex, commandType);
    }

    @Override
    public void doExecuteCommand(String commandPrefix, String commandCaption) throws MojoExecutionException {
        String commandLine = this.command.getCommand().trim();

        CollectingLogOutputStream commandOutputStream = null;
        try {
            CommonMojo.commandOutputStream = new CollectingLogOutputStream(getLog(), commandPrefix, false);
            if (executeBinary(commandLine, new File(session.getRequest().getBaseDirectory()), "The command '" + commandCaption + "' failed.") != 0) {
                failedCommand(command);
            }
        } catch (MojoExecutionException | IOException e) {
            failedCommand(command);
        } finally {
            try {
                if (commandOutputStream != null) {
                    commandOutputStream.close();
                }
            } catch (IOException e) {
            }
        }
    }

    @Override
    public String getCommandTypeCaption() {
        return "System command";
    }

}
