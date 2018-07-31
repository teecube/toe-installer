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
import t3.CommonMojo;
import t3.toe.installer.environments.AntCommand;
import t3.toe.installer.environments.CustomProduct;
import t3.toe.installer.environments.EnvironmentToInstall;
import t3.toe.installer.environments.products.ProductToInstall;

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
        return false;
    }

    @Override
    public String getCommandTypeCaption() {
        return "Ant command";
    }
}
