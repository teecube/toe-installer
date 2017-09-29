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
package t3.toe.installer.environments;

import t3.toe.installer.environments.EnvironmentInstallerMojo.TIBCOProduct;

public class ProductToInstall extends Product {
	
	private boolean alreadyInstalled;
	private TIBCOProduct tibcoProduct;

	public ProductToInstall(Product product) {
		this.setHotfixes(product.getHotfixes());
		this.setName(product.getName());
		this.setPackage(product.getPackage());
		this.setPriority(product.getPriority());
		this.setProperties(product.getProperties());
		this.setSkip(product.isSkip());
		this.setType(product.getType());

		this.setTibcoProduct(TIBCOProduct.valueOf(product.getType().value().toUpperCase()));
	}

	public boolean isAlreadyInstalled() {
		return alreadyInstalled;
	}

	public void setAlreadyInstalled(boolean alreadyInstalled) {
		this.alreadyInstalled = alreadyInstalled;
	}

	public TIBCOProduct getTibcoProduct() {
		return tibcoProduct;
	}

	public void setTibcoProduct(TIBCOProduct tibcoProduct) {
		this.tibcoProduct = tibcoProduct;
	}
	
}
