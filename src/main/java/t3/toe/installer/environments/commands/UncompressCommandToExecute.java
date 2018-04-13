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
import t3.toe.installer.environments.CustomProduct;
import t3.toe.installer.environments.UncompressCommand;

public class UncompressCommandToExecute extends CommandToExecute<UncompressCommand> {

    private final CustomProduct customProduct;

    public UncompressCommandToExecute(Log logger, MojoExecutor.ExecutionEnvironment executionEnvironment, UncompressCommand command, int commandIndex, CommandType commandType) {
        this(logger, executionEnvironment, command, commandIndex, commandType, null);
    }

    public UncompressCommandToExecute(Log logger, MojoExecutor.ExecutionEnvironment executionEnvironment, UncompressCommand command, int commandIndex, CommandType commandType, CustomProduct customProduct) {
        super(logger, executionEnvironment, command, commandIndex, commandType);

        this.customProduct = customProduct;
    }

    @Override
    public void doExecuteCommand(String commandPrefix, String commandCaption) throws MojoExecutionException {
        getLog().info("Uncompress file");
    }

    @Override
    public String getCommandTypeCaption() {
        return "Uncompress command";
    }

}
