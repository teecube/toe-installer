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
import org.apache.maven.plugin.logging.Log;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import t3.CommonMojo;
import t3.log.PrefixedLogger;
import t3.toe.installer.environments.AbstractCommand;
import t3.toe.installer.environments.CustomProduct;
import t3.toe.installer.environments.products.CustomProductToInstall;
import t3.toe.installer.environments.products.ProductToInstall;
import t3.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CommandToExecute<Command extends AbstractCommand> extends CommonMojo {

    private String prefix;
    private String prefixMinusOne;
    private String prefixMinusTwo;
    private String prefixMinusThree;

    private Map<String, Log> loggers;
    protected File workingDirectory;

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
    protected final CommonMojo commonMojo;
    protected final CommandType commandType;
    protected final ProductToInstall<CustomProduct> productToInstall;

    public CommandToExecute(Command command, CommonMojo commonMojo, int commandIndex, CommandType commandType) {
        this(command, commonMojo, commandIndex, commandType, null);
    }

    public CommandToExecute(Command command, CommonMojo commonMojo, int commandIndex, CommandType commandType, ProductToInstall<CustomProduct> productToInstall) {
        this.command = command;
        this.commandIndex = commandIndex;
        this.commonMojo = commonMojo;
        this.commandType = commandType;
        this.productToInstall = productToInstall;

        this.setLog(commonMojo.getLog());
        this.logger = PrefixedLogger.getLoggerFromLog(getLog());
        this.pluginDescriptor = commonMojo.getPluginDescriptor();
        this.project = commonMojo.getProject();
        this.session = commonMojo.getSession();
        this.executionEnvironment = new MojoExecutor.ExecutionEnvironment(this.project, this.session, commonMojo.getPluginManager());

        loggers = new HashMap<String, Log>();
    }

    public abstract boolean doExecuteCommand(String commandCaption) throws MojoExecutionException;
    public abstract String commandFailureMessagge();
    public abstract String getCommandTypeCaption();

    public void executeCommand() throws MojoExecutionException {
        if (this.command.isSkip()) {
            getLog().info("Skipping command '" + this.command.getName() + "'");
            return;
        }

        getLog().info("");

        String name = this.command.getName();
        String commandCaption = (StringUtils.isNotEmpty(name) ? name : getCommandTypeCaption()) + (this.command.getId() != null ? " (id: " + this.command.getId() + ")" : "");

        String commandDisplay;
        prefix = "";
        switch (commandType) {
            case PRODUCT_PRE:
            case PRODUCT_POST:
            case CUSTOM_PRODUCT:
                prefix = "   " + Utils.toAlphabetic(commandIndex - 1) + ". ";
                break;
            default:
                prefix = commandIndex + ". ";
        }
        commandDisplay = prefix + commandCaption;
        this.prefix = StringUtils.repeat(" ", prefix.length() - 2);
        this.prefixMinusOne = StringUtils.repeat(" ", prefix.length() - 1);
        this.prefixMinusTwo = StringUtils.repeat(" ", prefix.length() - 2);
        this.prefixMinusThree = StringUtils.repeat(" ", prefix.length() - 3);

        getLog().info(commandDisplay);

        this.setLog(new PrefixedLogger(logger, prefixMinusOne + "| ", prefix + "| ", prefixMinusThree + "! ", prefixMinusTwo + "!! "));

        if (StringUtils.isNotBlank(this.command.getDescription())) {
            getLog().info("   Description: " + this.command.getDescription());
        }

        boolean commandSucceeded = true;
        try {
            workingDirectory = getWorkingDirectory();

            commandSucceeded = doExecuteCommand(commandCaption);
        } catch (MojoExecutionException e) {
            failedCommand(command);
        }

        if (!commandSucceeded) {
            failedCommand(command);
        }
    }

    protected Log getLog(String customPrefix) {
        if (loggers.containsKey(customPrefix)) {
            return loggers.get(customPrefix);
        }

        PrefixedLogger logger = new PrefixedLogger(this.logger, prefixMinusOne + customPrefix + " ", prefix + customPrefix + " ", prefixMinusThree + customPrefix + " ", prefixMinusTwo + customPrefix + " ");
        loggers.put(customPrefix, logger);
        return logger;
    }

    protected void failedCommand(AbstractCommand command) throws MojoExecutionException {
        switch (command.getOnError()) {
            case FAIL:
                getLog().info("");
                getLog().error(commandFailureMessagge());
                throw new MojoExecutionException(commandFailureMessagge());
            case IGNORE:
                getLog().info("");
                break;
            case WARN:
                getLog().info("");
                getLog().warn(commandFailureMessagge());
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
            if (selector.equals("packageDirectory")) {
                o = "./packages/" + productToInstall.getName();
            } else if (selector.equals("package")) {
                o = productToInstall;
            } else if (selector.equals("uncompressCommand")) {
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
            } else if (lastSelector.equals("package") && selector.equals("version")) {
                o = ((ProductToInstall) o).getVersion();
            }

            lastSelector = selector;
        }

        return o.toString();
    }

    protected String getValueWithReplacedProperties(String string) {
        Matcher m = mavenPropertyPattern.matcher(string);

        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String propertyKey = m.group(1);
            String propertyValue = getCommandPropertyValue(propertyKey);
            if (propertyValue != null) {
                m.appendReplacement(sb, Matcher.quoteReplacement(propertyValue));
            }
        }
        m.appendTail(sb);
        string = sb.toString();

        return string;
    }

    private File getWorkingDirectory() {
        File workingDirectory = null;

        String commandWorkingDirectory = command.getWorkingDirectory();
        if (StringUtils.isNotEmpty(commandWorkingDirectory)) {
            commandWorkingDirectory = getValueWithReplacedProperties(commandWorkingDirectory);

            workingDirectory = new File(commandWorkingDirectory);
            try {
                workingDirectory = new File(workingDirectory.getCanonicalPath());
            } catch (IOException e) {
                workingDirectory = new File(commandWorkingDirectory);
            }
        }

        if (workingDirectory == null || !workingDirectory.exists()) {
            workingDirectory = new File(session.getRequest().getBaseDirectory());
        }

        return workingDirectory;
    }

}
