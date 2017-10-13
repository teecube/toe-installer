/**
 * (C) Copyright 2016-2017 teecube
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
package t3.toe.installer;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;

import t3.CommonMojo;
import t3.MojosFactory;
import t3.toe.installer.envinfo.ListEnvInfoMojo;
import t3.toe.installer.envinfo.RemoveEnvInfoMojo;
import t3.toe.installer.environments.EnvironmentInstallerMojo;
import t3.toe.installer.installers.AdminInstallerMojo;
import t3.toe.installer.installers.BW5InstallerMojo;
import t3.toe.installer.installers.BW6InstallerMojo;
import t3.toe.installer.installers.EMSInstallerMojo;
import t3.toe.installer.installers.RVInstallerMojo;
import t3.toe.installer.installers.TEAInstallerMojo;
import t3.toe.installer.installers.TRAInstallerMojo;
import t3.toe.installer.packages.DisplayPackages;
import t3.toe.installer.packages.InstallPackages;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class InstallerMojosFactory extends MojosFactory {
	@SuppressWarnings("unchecked")
	public <T extends AbstractMojo> T getMojo(Class<T> type) {
		if (type == null) {
			return null;
		}

		String typeName = type.getSimpleName();

		switch (typeName) {
		case "BW5InstallerMojo":
			return (T) new BW5InstallerMojo();
		case "BW6InstallerMojo":
			return (T) new BW6InstallerMojo();
		case "EMSInstallerMojo":
			return (T) new EMSInstallerMojo();
		case "RVInstallerMojo":
			return (T) new RVInstallerMojo();
		case "TEAInstallerMojo":
			return (T) new TEAInstallerMojo();
		case "TRAInstallerMojo":
			return (T) new TRAInstallerMojo();

		case "ListEnvInfoMojo":
			return (T) new ListEnvInfoMojo();
		case "RemoveEnvInfoMojo":
			return (T) new RemoveEnvInfoMojo();

		case "EnvironmentInstallerMojo":
			return (T) new EnvironmentInstallerMojo();

		case "DisplayPackages":
			return (T) new DisplayPackages();
		default:
			return super.getMojo(type);
		}

	}

	@SuppressWarnings("unchecked")
	public static <T extends CommonMojo> T getInstallerMojo(String goal) {
		if (goal == null) {
			return null;
		}

		switch (goal) {
		case InstallerMojosInformation.pluginPrefix + "install-admin":
			return (T) new AdminInstallerMojo();
		case InstallerMojosInformation.pluginPrefix + "install-bw5":
			return (T) new BW5InstallerMojo();
		case InstallerMojosInformation.pluginPrefix + "install-bw6":
			return (T) new BW6InstallerMojo();
		case InstallerMojosInformation.pluginPrefix + "install-ems":
			return (T) new EMSInstallerMojo();
		case InstallerMojosInformation.pluginPrefix + "install-rv":
			return (T) new RVInstallerMojo();
		case InstallerMojosInformation.pluginPrefix + "install-tea":
			return (T) new TEAInstallerMojo();
		case InstallerMojosInformation.pluginPrefix + "install-tra":
			return (T) new TRAInstallerMojo();
		case InstallerMojosInformation.pluginPrefix + "env-install":
			return (T) new EnvironmentInstallerMojo();
		case InstallerMojosInformation.pluginPrefix + "packages-display":
			return (T) new DisplayPackages();
		case InstallerMojosInformation.pluginPrefix + "packages-install":
			return (T) new InstallPackages();

		default:
			return null;
		}
	}

	public static List<Class<? extends CommonInstaller>> getInstallersClasses() {
		List<Class<? extends CommonInstaller>> installersClasses = new ArrayList<>();
		installersClasses.add(AdminInstallerMojo.class);
		installersClasses.add(BW5InstallerMojo.class);
		installersClasses.add(BW6InstallerMojo.class);
		installersClasses.add(EMSInstallerMojo.class);
		installersClasses.add(RVInstallerMojo.class);
		installersClasses.add(TEAInstallerMojo.class);
		installersClasses.add(TRAInstallerMojo.class);

		return installersClasses;
	}
}
