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

import org.apache.commons.lang.StringUtils;

import t3.toe.installer.environments.EnvironmentInstallerMojo.TIBCOProductGoalAndPriority;

public class TIBCOProductToInstall extends ProductToInstall<TIBCOProduct> {

	private TIBCOProduct.Hotfixes hotfixes;
	private ProductType type;

	private boolean alreadyInstalled;
	private TIBCOProductGoalAndPriority tibcoProductGoalAndPriority;
	private boolean toBeDeleted;
	private String version;

	public TIBCOProductToInstall(TIBCOProduct tibcoProduct) {
		super(tibcoProduct);

		this.setHotfixes(tibcoProduct.getHotfixes());
		this.setType(tibcoProduct.getType());

		this.setTibcoProductGoalAndPriority(TIBCOProductGoalAndPriority.valueOf(tibcoProduct.getType().value().toUpperCase()));
	}

	public void setHotfixes(TIBCOProduct.Hotfixes hotfixes) {
		this.hotfixes = hotfixes;
	}

	public TIBCOProduct.Hotfixes getHotfixes() {
		return hotfixes;
	}

	public void setType(ProductType type) {
		this.type = type;
	}

	public ProductType getType() {
		return type;
	}

	public boolean isAlreadyInstalled() {
		return alreadyInstalled;
	}

	public void setAlreadyInstalled(boolean alreadyInstalled) {
		this.alreadyInstalled = alreadyInstalled;
	}

	public TIBCOProductGoalAndPriority getTibcoProductGoalAndPriority() {
		return tibcoProductGoalAndPriority;
	}

	public void setTibcoProductGoalAndPriority(TIBCOProductGoalAndPriority tibcoProductGoalAndPriority) {
		this.tibcoProductGoalAndPriority = tibcoProductGoalAndPriority;
	}

	public String fullProductName() {
		return tibcoProductGoalAndPriority.productName() + (StringUtils.isNotEmpty(this.getId()) ? " (id: " + this.getId() + ")" : "");
	}

	public boolean isToBeDeleted() {
		return toBeDeleted;
	}

	public void setToBeDeleted(boolean toBeDeleted) {
		this.toBeDeleted = toBeDeleted;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
