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

import org.apache.commons.lang.StringUtils;

import t3.toe.installer.environments.ProductType;
import t3.toe.installer.environments.TIBCOProduct;

public class TIBCOProductToInstall extends ProductToInstall<TIBCOProduct> {

	public enum TIBCOProductGoalAndPriority {
		ADMIN ("install-admin", "Administrator", 30),
		BW5 ("install-bw5", "BusinessWorks 5", 30),
		BW6 ("install-bw6", "BusinessWorks 6", 00),
		EMS ("install-ems", "EMS", 30),
		RV ("install-rv", "RendezVous", 10),
		TEA ("install-tea", "TEA", 00),
		TRA ("install-tra", "TRA", 20);

		private final String goal;
		private final String name;
		private final Integer priority;

		TIBCOProductGoalAndPriority(String goal, String name, Integer priority) {
			this.goal = goal;
			this.name = name;
			this.priority = priority;
		}

		public String goal() { return goal; }
		public String getName() { return name; }
		public String productName() { return "TIBCO " + name; }
		public Integer priority() { return priority; }
	}

	private TIBCOProduct.Hotfixes hotfixes;
	private ProductType type;

	private TIBCOProductGoalAndPriority tibcoProductGoalAndPriority;
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

	public TIBCOProductGoalAndPriority getTibcoProductGoalAndPriority() {
		return tibcoProductGoalAndPriority;
	}

	public void setTibcoProductGoalAndPriority(TIBCOProductGoalAndPriority tibcoProductGoalAndPriority) {
		this.tibcoProductGoalAndPriority = tibcoProductGoalAndPriority;
	}

	public String fullProductName() {
		return tibcoProductGoalAndPriority.productName() + (StringUtils.isNotEmpty(this.getId()) ? " (id: " + this.getId() + ")" : "");
	}

}
