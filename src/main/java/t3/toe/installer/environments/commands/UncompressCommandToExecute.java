/**
 * (C) Copyright 2016-2019 teecube
 * (https://teecu.be) and others.
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
import t3.toe.installer.environments.products.ProductToInstall;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class UncompressCommandToExecute extends CommandToExecute<UncompressCommand> {

    private final UncompressCommand uncompressCommand;
    private File destinationDirectory;

    public UncompressCommandToExecute(UncompressCommand command, CommonMojo commonMojo, int commandIndex, CommandType commandType) {
        this(command, commonMojo, commandIndex, commandType, null);
    }

    public UncompressCommandToExecute(UncompressCommand command, CommonMojo commonMojo, int commandIndex, CommandType commandType, ProductToInstall customProductToInstall) {
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
    public String commandFailureMessagge() {
        return "The uncompress command failed.";
    }

    @Override
    public boolean doExecuteCommand(String commandCaption) throws MojoExecutionException {
        if (productToInstall == null || productToInstall.getResolvedInstallationPackage() == null || !productToInstall.getResolvedInstallationPackage().exists()) {
            getLog().info("");
            getLog().error("The package file for this custom product was not resolved.");
            throw new MojoExecutionException("The package file for this custom product was not resolved.", new FileNotFoundException());
        }

        File resolvedInstallationPackage = productToInstall.getResolvedInstallationPackage();
        File destinationDirectory = getDestinationDirectory();

        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
            if (!destinationDirectory.exists()) {
                throw new MojoExecutionException("Unable to create destination directory '" + destinationDirectory.getAbsolutePath() + "'");
            }
        }

        getLog().info("");
        getLog().info("Uncompressing file '" + productToInstall.getResolvedInstallationPackage().getAbsolutePath() + "' to '" + destinationDirectory.getAbsolutePath() + "'");

        switch (this.uncompressCommand.getFormat()) {
            case TAR:
                untarPackage(resolvedInstallationPackage, destinationDirectory);
                break;
            case ZIP:
                unzipPackage(resolvedInstallationPackage, destinationDirectory);
                break;
            case AUTO:
                switch (FilenameUtils.getExtension(resolvedInstallationPackage.getAbsolutePath()).toLowerCase()) {
                    case "tar":
                        untarPackage(resolvedInstallationPackage, destinationDirectory);
                        break;
                    case "zip":
                        unzipPackage(resolvedInstallationPackage, destinationDirectory);
                        break;
                    default:
                        getLog().error("Unsupported compression format.");
                }
                break;
        }

        getLog().info("");

        return true;
    }

    @Override
    public String getCommandTypeCaption() {
        return "Uncompress command";
    }

    public File getDestinationDirectory() throws MojoExecutionException {
        File result = null;
        if (this.destinationDirectory != null) {
            return this.destinationDirectory;
        }

        if (uncompressCommand.getDestination().getTempDirectory() != null) {
            try {
                result = Files.createTempDirectory("uncompress").toFile();
            } catch (IOException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
        } else {
            if (StringUtils.isEmpty(uncompressCommand.getDestination().getDirectory())) {
                throw new MojoExecutionException("No destination directory was specified.");
            }
            result = new File(uncompressCommand.getDestination().getDirectory());
        }

        this.destinationDirectory = result;
        return result;
    }

}
