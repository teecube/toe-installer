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
import org.twdata.maven.mojoexecutor.MojoExecutor;
import t3.CommonMojo;
import t3.log.PrefixedLogger;
import t3.toe.installer.environments.AbstractCommand;
import t3.toe.installer.environments.CustomProduct;
import t3.toe.installer.environments.UncompressCommand;
import t3.toe.installer.environments.products.CustomProductToInstall;
import t3.toe.installer.environments.products.ProductToInstall;
import t3.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CommandToExecute<Command extends AbstractCommand> extends CommonMojo {

    public enum CommandType {
        GLOBAL_PRE ("Global pre-install command"),
        GLOBAL_POST ("Global post-install command"),
        PRODUCT_PRE ("Product pre-install command"),
        PRODUCT_POST ("Product post-install command"),
        CUSTOM_PRODUCT ("Custom product command");

        private final String caption;

        CommandType(String caption) {
            this.caption = caption;
        }

        public String getCaption() {
            return caption;
        }
    }

    private final MojoExecutor.ExecutionEnvironment executionEnvironment;

    protected final Command command;
    protected final int commandIndex;
    protected final CommandType commandType;
    protected final ProductToInstall<CustomProduct> productToInstall;

    public CommandToExecute(Command command, CommonMojo commonMojo, int commandIndex, CommandType commandType) {
        this(command, commonMojo, commandIndex, commandType, null);
    }

    public CommandToExecute(Command command, CommonMojo commonMojo, int commandIndex, CommandType commandType, ProductToInstall<CustomProduct> productToInstall) {
        this.command = command;
        this.commandIndex = commandIndex;
        this.commandType = commandType;
        this.productToInstall = productToInstall;

        this.setLog(commonMojo.getLog());
        this.logger = PrefixedLogger.getLoggerFromLog(getLog());
        this.pluginDescriptor = commonMojo.getPluginDescriptor();
        this.project = commonMojo.getProject();
        this.session = commonMojo.getSession();
        this.executionEnvironment = new MojoExecutor.ExecutionEnvironment(this.project, this.session, commonMojo.getPluginManager());
    }

    public abstract void doExecuteCommand(String commandPrefix, String commandCaption) throws MojoExecutionException;
    public abstract String getCommandTypeCaption();

    public void executeCommand() throws MojoExecutionException {
        if (this.command.isSkip()) {
            getLog().info("Skipping command '" + this.command.getName() + "'");
            return;
        }

        getLog().info("");

        String name = this.command.getName();
        String commandCaption = (name != null ? name : getCommandTypeCaption()) + (this.command.getId() != null ? " (id: " + this.command.getId() + ")" : "");

        String commandDisplay;
        String prefix = "";
        switch (commandType) {
            case CUSTOM_PRODUCT:
                prefix = "   " + Utils.toAlphabetic(commandIndex - 1) + ". ";
                break;
            default:
                prefix = commandIndex + ". Name: ";
        }
        commandDisplay = prefix + commandCaption;
        prefix = StringUtils.repeat(" ", prefix.length() - 2);
        String prefixShort = StringUtils.repeat(" ", prefix.length() - 1);

        getLog().info(commandDisplay);

        this.setLog(new PrefixedLogger(logger, prefixShort + "| ", prefix + "| ", prefix + "| ", prefixShort + "| "));

        if (StringUtils.isNotBlank(this.command.getDescription())) {
            getLog().info("   Description: " + this.command.getDescription());
        }

        doExecuteCommand("", commandCaption);
    }

    protected void failedCommand(AbstractCommand command) throws MojoExecutionException {
        switch (command.getOnError()) {
            case FAIL:
                getLog().info("");
                getLog().error("The command failed.");
                throw new MojoExecutionException("The command failed.");
            case IGNORE:
                getLog().info("");
                break;
            case WARN:
                getLog().info("");
                getLog().warn("The command failed.");
                break;
        }
    }

    /**
     * Resolves properties relatively to current product to install.
     *
     * @param property
     * @return
     */
    protected String getCommandPropertyValue(String property) {
        if (!(this.productToInstall instanceof CustomProductToInstall)) {
            return null;
        }

        CustomProductToInstall customProductToInstall = (CustomProductToInstall) productToInstall;

        Pattern p = Pattern.compile("([^\\[\\]\\.]*)(\\[(\\d+)\\])?[\\.]?");
        Matcher m = p.matcher(property);

        Object o = null;

        String lastSelector = "";

        while (m.find()) {
            String selector = m.group(1);
            if (StringUtils.isEmpty(selector)) {
                break;
            }

            int index;
            String _index = m.group(3);
            if (_index == null) {
                index = 1;
            } else {
                index = Integer.parseInt(_index);
            }
            int i = 1; // XPath-like so starts from 1
            if (selector.equals("uncompressCommand")) {
                for (CommandToExecute commandToExecute : customProductToInstall.getCommandsToExecute()) {
                    if (commandToExecute instanceof UncompressCommandToExecute) {
                        if (index == i) {
                            o = (UncompressCommandToExecute) customProductToInstall.getCommandsToExecute().get(i-1);
                            break;
                        }
                        i++;
                    }
                }
            }
            if (lastSelector.equals("uncompressCommand") && selector.equals("destinationDirectory")) {
                try {
                    o = ((UncompressCommandToExecute) o).getDestinationDirectory();
                } catch (MojoExecutionException e) {
                    o = "";
                }
            }
            lastSelector = selector;
        }

        return o.toString();
    }

}
