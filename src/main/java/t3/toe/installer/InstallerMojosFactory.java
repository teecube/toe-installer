/**
 * (C) Copyright 2014-2016 teecube
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

import org.apache.maven.plugin.AbstractMojo;

import t3.MojosFactory;
import t3.toe.installer.envinfo.ListEnvInfoMojo;
import t3.toe.installer.envinfo.RemoveEnvInfoMojo;
import t3.toe.installer.installers.AdminInstallerMojo;
import t3.toe.installer.installers.BW5InstallerMojo;
import t3.toe.installer.installers.BW6InstallerMojo;
import t3.toe.installer.installers.RVInstallerMojo;
import t3.toe.installer.installers.TEAInstallerMojo;
import t3.toe.installer.installers.TRAInstallerMojo;

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
		default:
			return super.getMojo(type);
		}

	}

	@SuppressWarnings("unchecked")
	public static <T extends CommonInstaller> T getInstallerMojo(String goal) {
		if (goal == null) {
			return null;
		}

		switch (goal) {
		case "toe:bw5-install":
			return (T) new BW5InstallerMojo();
		case "toe:bw6-install":
			return (T) new BW6InstallerMojo();
		case "toe:rv-install":
			return (T) new RVInstallerMojo();
		case "toe:tea-install":
			return (T) new TEAInstallerMojo();
		case "toe:tra-install":
			return (T) new TRAInstallerMojo();
		case "toe:admin-install":
			return (T) new AdminInstallerMojo();

		default:
			return null;
		}
	}

}
