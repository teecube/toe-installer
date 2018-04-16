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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.AbstractUnArchiver;
import org.codehaus.plexus.archiver.tar.TarUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import t3.CommonMojo;
import t3.toe.installer.environments.UncompressCommand;
import t3.toe.installer.environments.products.CustomProductToInstall;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class UncompressCommandToExecute extends CommandToExecute<UncompressCommand> {

    private final UncompressCommand uncompressCommand;

    public UncompressCommandToExecute(UncompressCommand command, CommonMojo commonMojo, int commandIndex, CommandType commandType, CustomProductToInstall customProductToInstall) {
        super(command, commonMojo, commandIndex, commandType, customProductToInstall);

        this.uncompressCommand = command;
    }

    private void uncompress(File resolvedPackage, File destinationDirectory, Class<? extends AbstractUnArchiver> unArchiverClass) throws MojoExecutionException {
        try {
            AbstractUnArchiver unArchiver = unArchiverClass.newInstance();
            unArchiver.enableLogging(logger);
            unArchiver.setSourceFile(resolvedPackage);
            unArchiver.setDestDirectory(destinationDirectory);
            unArchiver.extract();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private void untarPackage(File resolvedPackage, File destinationDirectory) throws MojoExecutionException {
        uncompress(resolvedPackage, destinationDirectory, TarUnArchiver.class);
    }

    private void unzipPackage(File resolvedPackage, File destinationDirectory) throws MojoExecutionException {
        uncompress(resolvedPackage, destinationDirectory, ZipUnArchiver.class);
    }

    @Override
    public void doExecuteCommand(String commandPrefix, String commandCaption) throws MojoExecutionException {
        if (productToInstall != null && productToInstall.getResolvedInstallationPackage() != null && productToInstall.getResolvedInstallationPackage().exists()) {
            getLog().info("Uncompressing file '" + productToInstall.getResolvedInstallationPackage().getAbsolutePath() + "'");
        } else {
            getLog().error("The package file for this custom product was not resolved.");
            throw new MojoExecutionException("The package file for this custom product was not resolved.", new FileNotFoundException());
        }

        File resovledPackage = productToInstall.getResolvedInstallationPackage();
        File destinationDirectory;

        if (this.command.isToTempDirectory()) {
            try {
                destinationDirectory = Files.createTempDirectory("uncompress").toFile();
            } catch (IOException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
        } else {
            if (StringUtils.isEmpty(command.getDestinationDirectory())) {
                getLog().error("No destination directory was specified.");
                throw new MojoExecutionException("No destination directory was specified.");
            }
            destinationDirectory = new File(command.getDestinationDirectory());
        }
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
            if (!destinationDirectory.exists()) {
                throw new MojoExecutionException("Unable to create destination directory '" + destinationDirectory.getAbsolutePath() + "'");
            }
        }

        switch (this.uncompressCommand.getFormat()) {
            case TAR:
                untarPackage(resovledPackage, destinationDirectory);
                break;
            case ZIP:
                unzipPackage(resovledPackage, destinationDirectory);
                break;
            case AUTO:
                switch (FilenameUtils.getExtension(resovledPackage.getAbsolutePath()).toLowerCase()) {
                    case "tar":
                        untarPackage(resovledPackage, destinationDirectory);
                        break;
                    case "zip":
                        unzipPackage(resovledPackage, destinationDirectory);
                        break;
                    default:
                        getLog().error("Unsupported compression format.");
                }
                break;
        }
    }

    @Override
    public String getCommandTypeCaption() {
        return "Uncompress command";
    }

}