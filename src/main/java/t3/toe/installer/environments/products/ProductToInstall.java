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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import t3.CommonMojo;
import t3.toe.installer.environments.*;
import t3.toe.installer.environments.commands.*;

import java.io.File;
import java.io.IOException;

public abstract class ProductToInstall<P extends Product> {

    protected final EnvironmentToInstall environment;
    protected final MojoExecutor.ExecutionEnvironment executionEnvironment;
    protected final PluginDescriptor pluginDescriptor;
    protected final MavenProject project;
    protected final MavenSession session;
    protected final CommonMojo commonMojo;

    public void setLog(Log log) {
        this.logger = log;
    }

    protected Log logger;

    private P product;

    private String id;
    private IfProductExistsBehaviour ifExists;
    private String name;
    private Product.Package _package;
    private Commands postInstallCommands;
    private Commands preInstallCommands;
    private Product.Properties properties;
    private Integer priority;
    private boolean skip;

    public File getResolvedInstallationPackage() {
        return resolvedInstallationPackage;
    }

    public void setResolvedInstallationPackage(File resolvedInstallationPackage) throws IOException {
        this.resolvedInstallationPackage = new File(resolvedInstallationPackage.getCanonicalPath());
    }

    private boolean alreadyInstalled;
    private File resolvedInstallationPackage;
    private boolean toBeDeleted;
    private String version;

    public P getProduct() {
        return product;
    }

    public ProductToInstall(P product, EnvironmentToInstall environment, CommonMojo commonMojo) {
        this.product = product;

        this.setId(product.getId());
        this.setIfExists(product.getIfExists());
        this.setPackage(product.getPackage());
        this.setPostInstallCommands(product.getPostInstallCommands());
        this.setPreInstallCommands(product.getPreInstallCommands());
        this.setPriority(product.getPriority());
        this.setProperties(product.getProperties());
        this.setSkip(product.isSkip());

        this.environment = environment;

        this.commonMojo = commonMojo;
        this.logger = commonMojo.getLog();
        this.pluginDescriptor = commonMojo.getPluginDescriptor();
        this.project = commonMojo.getProject();
        this.session = commonMojo.getSession();
        this.executionEnvironment = new MojoExecutor.ExecutionEnvironment(this.project, this.session, this.commonMojo.getPluginManager());
    }

    public abstract void doInstall(EnvironmentToInstall environment, int productIndex) throws MojoExecutionException;
    public abstract String fullProductName();
    public abstract void init(int productIndex) throws MojoExecutionException;

    protected CommandToExecute getCommandToExecute(AbstractCommand command, CommandToExecute.CommandType commandType, int index) {
        CommandToExecute commandToExecute = null;
        if (command instanceof AntCommand) {
            commandToExecute = new AntCommandToExecute((AntCommand) command, commonMojo, index, commandType, this);
        } else if (command instanceof MavenCommand) {
            commandToExecute = new MavenCommandToExecute((MavenCommand) command, commonMojo, index, commandType, this);
        } else if (command instanceof SystemCommand) {
            commandToExecute = new SystemCommandToExecute((SystemCommand) command, commonMojo, index, commandType, this);
        } else if (command instanceof UncompressCommand) {
            commandToExecute = new UncompressCommandToExecute((UncompressCommand) command, commonMojo, index, commandType, this);
        }

        return commandToExecute;
    }

    public void install(EnvironmentToInstall environment, int productIndex) throws MojoExecutionException {
        logger.info("");

        if (this.isSkip()) {
            logger.info(productIndex + ". Skipping '" + this.fullProductName() + "'");
            return;
        } else if (this.isAlreadyInstalled() && !environment.isToBeDeleted()) {
            logger.info(productIndex + ". Skipping '" + this.fullProductName() + "' (already installed)");
            return;
        } else {
            logger.info(productIndex + ". Installing '" + this.fullProductName() + "'");

            // execute pre-product-install commands
            if (this.getPreInstallCommands() != null && !this.getPreInstallCommands().getAntCommandOrMavenCommandOrSystemCommand().isEmpty()) {
                logger.info("");
                logger.info("   Executing pre-install commands for current product");
                int i = 1;
                for (AbstractCommand command : this.getPreInstallCommands().getAntCommandOrMavenCommandOrSystemCommand()) {
                    CommandToExecute commandToExecute = getCommandToExecute(command, CommandToExecute.CommandType.PRODUCT_PRE, i);

                    if (commandToExecute != null) {
                        commandToExecute.executeCommand();
                    }

                    i++;
                }
            }
        }

        doInstall(environment, productIndex);

        // execute post-product-install commands
        if (this.getPostInstallCommands() != null && !this.getPostInstallCommands().getAntCommandOrMavenCommandOrSystemCommand().isEmpty()) {
            logger.info("");
            logger.info("   Executing post-install commands for current product");
            int i = 1;
            for (AbstractCommand command : this.getPostInstallCommands().getAntCommandOrMavenCommandOrSystemCommand()) {
                CommandToExecute commandToExecute = getCommandToExecute(command, CommandToExecute.CommandType.PRODUCT_POST, i);

                if (commandToExecute != null) {
                    commandToExecute.executeCommand();
                }

                i++;
            }
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setIfExists(IfProductExistsBehaviour ifExists) {
        this.ifExists = ifExists;
    }

    public IfProductExistsBehaviour getIfExists() {
        return ifExists;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPackage(Product.Package _package) {
        this._package = _package;
    }

    public Product.Package getPackage() {
        return _package;
    }

    public void setPostInstallCommands(Commands postInstallCommands) {
        this.postInstallCommands = postInstallCommands;
    }

    public Commands getPostInstallCommands() {
        return postInstallCommands;
    }

    public void setPreInstallCommands(Commands preInstallCommands) {
        this.preInstallCommands = preInstallCommands;
    }

    public Commands getPreInstallCommands() {
        return preInstallCommands;
    }

    public void setProperties(Product.Properties properties) {
        this.properties = properties;
    }

    public Product.Properties getProperties() {
        return properties;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isSkip() {
        return skip;
    }

    public boolean isAlreadyInstalled() {
        return alreadyInstalled;
    }

    public void setAlreadyInstalled(boolean alreadyInstalled) {
        this.alreadyInstalled = alreadyInstalled;
    }

    public boolean isToBeDeleted() {
        return toBeDeleted;
    }

    public void setToBeDeleted(boolean toBeDeleted) {
        this.toBeDeleted = toBeDeleted;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof ProductToInstall) {
            File resolvedInstallationPackage = ((ProductToInstall) o).getResolvedInstallationPackage();
            if (resolvedInstallationPackage == null) {
                return this.getResolvedInstallationPackage() == null;
            } else {
                try {
                    return (this.getResolvedInstallationPackage() != null) && resolvedInstallationPackage.getCanonicalPath().equals(this.getResolvedInstallationPackage().getCanonicalPath());
                } catch (IOException e) {
                    return false;
                }
            }
        } else {
            return false;
        }
    }
}
