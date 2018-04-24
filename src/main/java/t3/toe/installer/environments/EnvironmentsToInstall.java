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
package t3.toe.installer.environments;

import com.google.common.collect.FluentIterable;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import t3.CommonMojo;
import t3.toe.installer.environments.products.CustomProductToInstall;
import t3.toe.installer.environments.products.ProductToInstall;
import t3.toe.installer.environments.products.TIBCOProductToInstall;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EnvironmentsToInstall extends ArrayList<EnvironmentToInstall> {

    public EnvironmentsToInstall(EnvironmentToInstall environment, CommonMojo commonMojo) throws MojoExecutionException {
        super();
    }

    public EnvironmentsToInstall(List<Environment> environments, File topology) {
        super();

        for (Environment environment : environments) {
            this.add(new EnvironmentToInstall(environment, topology));
        }
    }

    public boolean environmentExists(String environmentName) {
        for (EnvironmentToInstall environmentToInstall : this) {
            if (environmentToInstall.getName().equals(environmentName)) {
                return true;
            }
        }
        return false;
    }

    public EnvironmentToInstall getEnvironmentByName(String environmentName) {
        for (EnvironmentToInstall environmentToInstall : this) {
            if (environmentToInstall.getName().equals(environmentName)) {
                return environmentToInstall;
            }
        }
        return null;
    }

}
