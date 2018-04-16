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

import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import t3.CommonMojo;
import t3.toe.installer.environments.*;
import t3.toe.installer.environments.commands.CommandToExecute;
import t3.toe.installer.environments.commands.SystemCommandToExecute;
import t3.toe.installer.environments.commands.UncompressCommandToExecute;

import java.io.File;
import java.util.List;

public class CustomProductToInstall extends ProductToInstall<CustomProduct> {

    private final CustomProduct customProduct;

    public CustomProductToInstall(CustomProduct customProduct, EnvironmentToInstall environment, CommonMojo commonMojo) {
        super(customProduct, environment, commonMojo);

        this.setName(customProduct.getName());

        this.customProduct = customProduct;
    }

    public String fullProductName() {
        return this.getName();
    }

    public List<AbstractCommand> getSystemCommandOrUncompressCommand() {
        return customProduct.getSystemCommandOrUncompressCommand();
    }

    @Override
    public void init(int productIndex) throws MojoExecutionException {
        if (customProduct.getPackage().getLocal() != null) {
            if (customProduct.getPackage().getLocal().getFileWithVersion() != null) {
                setResolvedInstallationPackage(new File(customProduct.getPackage().getLocal().getFileWithVersion().getFile()));
            } else if (customProduct.getPackage().getLocal().getDirectoryWithPattern() != null) {
                logger.warn("directory with pattern is not supported for custom products");
            }
        } else if (customProduct.getPackage().getHttpRemote() != null) {
            String url = customProduct.getPackage().getHttpRemote().getUrl();
            // TODO : fetch URL
            logger.warn("Fetch URL not supported");
        } else if (customProduct.getPackage().getMavenRemote() != null) {
            String groupId = customProduct.getPackage().getMavenRemote().getGroupId();
            String artifactId = customProduct.getPackage().getMavenRemote().getArtifactId();
            String version = customProduct.getPackage().getMavenRemote().getVersion();
            String classifier = customProduct.getPackage().getMavenRemote().getClassifier();
            try {
                File resolvedDependency = commonMojo.getDependency(groupId, artifactId, version, "zip", classifier, true);
                if (resolvedDependency != null && resolvedDependency.exists()) {
                    this.setResolvedInstallationPackage(resolvedDependency);
                }
            } catch (ArtifactNotFoundException | ArtifactResolutionException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public void doInstall(EnvironmentToInstall environment, int productIndex) throws MojoExecutionException {
        int i = 1;
        for (AbstractCommand command : customProduct.getSystemCommandOrUncompressCommand()) {
            if (command instanceof SystemCommand) {
                new SystemCommandToExecute((SystemCommand) command, commonMojo, i, CommandToExecute.CommandType.CUSTOM_PRODUCT, this).executeCommand();
            } else if (command instanceof UncompressCommand) {
                new UncompressCommandToExecute((UncompressCommand) command, commonMojo, i, CommandToExecute.CommandType.CUSTOM_PRODUCT, this).executeCommand();
            }
            i++;
        }
    }

}
