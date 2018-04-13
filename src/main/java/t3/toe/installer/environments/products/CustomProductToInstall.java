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
package t3.toe.installer.environments.products;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import t3.toe.installer.environments.*;
import t3.toe.installer.environments.commands.CommandToExecute;
import t3.toe.installer.environments.commands.SystemCommandToExecute;
import t3.toe.installer.environments.commands.UncompressCommandToExecute;

import java.util.List;

public class CustomProductToInstall extends ProductToInstall<CustomProduct> {

    private final CustomProduct customProduct;

    public CustomProductToInstall(CustomProduct customProduct, Log logger, MojoExecutor.ExecutionEnvironment executionEnvironment, PluginDescriptor pluginDescriptor) {
        super(customProduct, logger, executionEnvironment, pluginDescriptor);

        this.setName(customProduct.getName());

        this.customProduct = customProduct;
    }

    public String fullProductName() {
        return this.getName();
    }

    @Override
    public void doInstall(EnvironmentToInstall environment, int productIndex, List<MojoExecutor.Element> configuration) throws MojoExecutionException {
        int i = 1;
        for (AbstractCommand command : customProduct.getInstallCommandOrUncompressCommand()) {
            if (command instanceof SystemCommand) {
                new SystemCommandToExecute(logger, executionEnvironment, (SystemCommand) command, i, CommandToExecute.CommandType.CUSTOM_PRODUCT).executeCommand();
            } else if (command instanceof UncompressCommand) {
                new UncompressCommandToExecute(logger, executionEnvironment, (UncompressCommand) command, i, CommandToExecute.CommandType.CUSTOM_PRODUCT, customProduct).executeCommand();
            }
            i++;
        }
    }

}
