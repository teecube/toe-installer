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
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentToInstall extends Environment {
	
	private boolean toBeDeleted;

	public EnvironmentToInstall(Environment environment) {
		this.setEnvironmentName(environment.getEnvironmentName());
		this.setIfExists(environment.getIfExists());
		this.setPackagesDirectory(environment.getPackagesDirectory());
		this.setProducts(environment.getProducts());
		this.setTibcoRoot(environment.getTibcoRoot());

		this.toBeDeleted = false;
	}

	public static List<EnvironmentToInstall> getEnvironmentsToInstall(List<Environment> environments) {
		List<EnvironmentToInstall> environmentsToInstall = new ArrayList<EnvironmentToInstall>();
		for (Environment environment : environments) {
			environmentsToInstall.add(new EnvironmentToInstall(environment));
		}
		return environmentsToInstall;
	}

	public boolean isToBeDeleted() {
		return toBeDeleted;
	}

	public void setToBeDeleted(boolean toBeDeleted) {
		this.toBeDeleted = toBeDeleted;
	}

	public List<TIBCOProduct> getTIBCOProducts() {
		return FluentIterable.from(this.getProducts().getTibcoProductOrCustomProduct())
							 .filter(TIBCOProduct.class)
							 .toList();
	}
 }
