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
import t3.CommonMojo;
import t3.toe.installer.environments.CustomProduct;
import t3.toe.installer.environments.SystemCommand;
import t3.toe.installer.environments.products.CustomProductToInstall;
import t3.toe.installer.environments.products.ProductToInstall;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.regex.Matcher;

public class SystemCommandToExecute extends CommandToExecute<SystemCommand> {

    public SystemCommandToExecute(SystemCommand command, CommonMojo commonMojo, int commandIndex, CommandType commandType) {
        super(command, commonMojo, commandIndex, commandType);
    }

    public SystemCommandToExecute(SystemCommand command, CommonMojo commonMojo, int commandIndex, CommandType commandType, ProductToInstall productToInstall) {
        super(command, commonMojo, commandIndex, commandType, productToInstall);
    }

    public SystemCommandToExecute(SystemCommand command, CommonMojo commonMojo, int commandIndex, CommandType commandType, CustomProductToInstall customProductToInstall) {
        super(command, commonMojo, commandIndex, commandType, customProductToInstall);
    }

    @Override
    public void doExecuteCommand(String commandPrefix, String commandCaption) throws MojoExecutionException {
        String commandLine = null;
        try {
            commandLine = getCommandLine(this.command);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        File workingDirectory = getWorkingDirectory();
        CollectingLogOutputStream commandOutputStream = null;
        try {
            CommonMojo.commandOutputStream = new CollectingLogOutputStream(getLog(), commandPrefix, false);
            if (executeBinary(commandLine, workingDirectory, "The command '" + commandCaption + "' failed.") != 0) {
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

    private String getCommandLine(SystemCommand command) throws IOException {
        if (command.getShell() != null) {
            File shellScript = null;
            shellScript = File.createTempFile("shell", ".sh");
            try (PrintWriter out = new PrintWriter(shellScript)) {
                String shellScriptContent = command.getShell().trim();
                shellScriptContent = shellScriptContent.replaceAll("(?m)^[ \t]*\r?\n", ""); // remove all blank lines
                logger.debug("Shell script content is:");
                logger.debug(shellScriptContent);
                out.println("#!/bin/sh");
                out.print(shellScriptContent);
            }
            return "sh -c " + shellScript.getAbsolutePath().replace("\\", "/");
        }
        return "";
    }

    private File getWorkingDirectory() {
        File workingDirectory = new File(session.getRequest().getBaseDirectory());

        String commandWorkingDirectory = command.getWorkingDirectory();
        if (StringUtils.isNotEmpty(commandWorkingDirectory)) {
            Matcher m = mavenPropertyPattern.matcher(commandWorkingDirectory);

            StringBuffer sb = new StringBuffer();

            while (m.find()) {
                String propertyKey = m.group(1);
                String propertyValue = getCommandPropertyValue(propertyKey);
                if (propertyValue != null) {
                    m.appendReplacement(sb, Matcher.quoteReplacement(propertyValue));
                }
            }
            m.appendTail(sb);
            commandWorkingDirectory = sb.toString();

            workingDirectory = new File(commandWorkingDirectory);
        }

        return workingDirectory;
    }

    @Override
    public String getCommandTypeCaption() {
        return "System command";
    }

}
