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
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import t3.CommonMojo;
import t3.Utils;
import t3.log.PrefixedLogger;
import t3.toe.installer.environments.AbstractCommand;

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

    private Log logger;

    protected final Command command;
    protected final int commandIndex;
    protected final CommandType commandType;

    public CommandToExecute(Log logger, MojoExecutor.ExecutionEnvironment executionEnvironment, Command command, int commandIndex, CommandType commandType) {
        this.logger = logger;

        this.command = command;
        this.commandIndex = commandIndex;
        this.commandType = commandType;

        this.setProject(executionEnvironment.getMavenProject());
        this.setSession(executionEnvironment.getMavenSession());
        this.setLog(logger);
    }

    public abstract void doExecuteCommand(String commandPrefix, String commandCaption) throws MojoExecutionException;
    public abstract String getCommandTypeCaption();

    public void executeCommand() throws MojoExecutionException {
        if (this.command.isSkip()) {
            logger.info("Skipping command '" + this.command.getName() + "'");
            return;
        }

        logger.info("");

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
        this.setLog(new PrefixedLogger(PrefixedLogger.getLoggerFromLog(logger), prefix + "| "));

        logger.info(commandDisplay);

        if (StringUtils.isNotBlank(this.command.getDescription())) {
            logger.info("   Description: " + this.command.getDescription());
        }

        doExecuteCommand("", commandCaption);
    }

    protected void failedCommand(AbstractCommand command) throws MojoExecutionException {
        switch (command.getOnError()) {
            case FAIL:
                logger.info("");
                logger.error("The command failed.");
                throw new MojoExecutionException("The command failed.");
            case IGNORE:
                logger.info("");
                break;
            case WARN:
                logger.info("");
                logger.warn("The command failed.");
                break;
        }
    }

}
