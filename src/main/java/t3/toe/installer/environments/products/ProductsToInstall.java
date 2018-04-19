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

import com.google.common.collect.FluentIterable;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import t3.CommonMojo;
import t3.toe.installer.environments.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductsToInstall extends ArrayList<ProductToInstall<?>> {

    private final EnvironmentToInstall environment;
    private final CommonMojo commonMojo;
    private final Log logger;
    private File environmentsTopology;
    private final int maxFullProductNameLength;
    private boolean atLeastOneMavenArtifactResolved;

    public int getMaxFullProductNameLength() {
        return maxFullProductNameLength;
    }

    public boolean isAtLeastOneMavenArtifactResolved() {
        return atLeastOneMavenArtifactResolved;
    }

    public ProductsToInstall(EnvironmentToInstall environment, CommonMojo commonMojo) throws MojoExecutionException {
        super();

        this.atLeastOneMavenArtifactResolved = false;
        this.commonMojo = commonMojo;
        this.environment = environment;
        this.logger = commonMojo.getLog();

        init(environment.getProducts());

        // compute the max length of full product name of all products (for display purpose)
        ProductToInstall productWithLongestFullProductName = Collections.max(this, new Comparator<ProductToInstall>() {
            @Override
            public int compare(ProductToInstall p1, ProductToInstall p2) {
            return p1.fullProductName().length() - p2.fullProductName().length();
            }
        });
        maxFullProductNameLength = productWithLongestFullProductName.fullProductName().length();
    }

    private void init(Environment.Products products) throws MojoExecutionException {
        for (Product product : products.getTibcoProductOrCustomProduct()) {
            if (product instanceof t3.toe.installer.environments.TIBCOProduct) {
                this.add(new TIBCOProductToInstall(((t3.toe.installer.environments.TIBCOProduct) product), environment, commonMojo));
            } else if (product instanceof CustomProduct) {
                this.add(new CustomProductToInstall(((CustomProduct) product), environment, commonMojo));
            }
        }

        checkAndSortProducts();

        int i = 1;
        List<List<MojoExecutor.Element>> configurations = new ArrayList<List<MojoExecutor.Element>>();
        for (ProductToInstall product : this) {
            product.init(i);

            if (!atLeastOneMavenArtifactResolved && product.getPackage() != null &&
                (product.getPackage().getMavenRemoteTIBCO() != null || product.getPackage().getMavenRemote() != null)) {
                atLeastOneMavenArtifactResolved = true;
            }
            i++;
        }

    }

    private class ProductComparator implements Comparator<ProductToInstall> {
        @Override
        public int compare(ProductToInstall p1, ProductToInstall p2) {
            if (p1 == null || p2 == null) return 0;

            Integer priority1 = p1.getPriority();
            Integer priority2 = p2.getPriority();
            if (priority1 == null) {
                if (p1 instanceof TIBCOProductToInstall) {
                    priority1 = ((TIBCOProductToInstall) p1).getTibcoProductGoalAndPriority().priority();
                } else {
                    return 0;
                }
            }
            if (priority2 == null) {
                if (p2 instanceof TIBCOProductToInstall) {
                    priority2 = ((TIBCOProductToInstall) p2).getTibcoProductGoalAndPriority().priority();
                }
            }

            return priority1.compareTo(priority2);
        }
    }

    /**
     * Sort products according to their priority, including built-in TIBCO products priorities.
     *
     * @throws MojoExecutionException
     */
    private void checkAndSortProducts() throws MojoExecutionException {
        // check that priorities are OK
        boolean prioritiesOK = true;
        List<TIBCOProductToInstall> tibcoProductsToInstall = FluentIterable.from(this)
                .filter(TIBCOProductToInstall.class)
                .toList();

        for (ProductToInstall product : this) {
            if (product instanceof TIBCOProductToInstall) {
                TIBCOProductToInstall tibcoProduct = (TIBCOProductToInstall) product;
                Integer productPriority = tibcoProduct.getPriority();
                if (productPriority == null) { // if not set in XML, take default one
                    productPriority = tibcoProduct.getTibcoProductGoalAndPriority().priority();
                }

                if (tibcoProduct.getType().equals(ProductType.ADMIN) || tibcoProduct.getType().equals(ProductType.BW_5)) { // Administator and BW5 need RV and TRA before being installed
                    boolean rvExists = productExists(TIBCOProductToInstall.TIBCOProductGoalAndPriority.RV, tibcoProductsToInstall);
                    boolean traExists = productExists(TIBCOProductToInstall.TIBCOProductGoalAndPriority.TRA, tibcoProductsToInstall);
                    if (!rvExists || !traExists) {
                        logger.info("");
                        logger.error("The product '" + product.fullProductName() + "' has unresolved dependencies in the current topology.");
                        if (rvExists) {
                            logger.error("-> The product '" + TIBCOProductToInstall.TIBCOProductGoalAndPriority.RV.productName() + "' is required but is not defined.");
                        }
                        if (traExists) {
                            logger.error("-> The product '" + TIBCOProductToInstall.TIBCOProductGoalAndPriority.TRA.productName() + "' is required but is not defined.");
                        }

                        throw new MojoExecutionException("There are unresolved dependencies in the current topology '" + environmentsTopology.getAbsolutePath() + "'.");
                    }
                }
                for (TIBCOProductToInstall productToCompare : tibcoProductsToInstall) {
                    if (productToCompare.equals(product)) {
                        continue;
                    }

                    Integer productToComparePriority = productToCompare.getPriority();
                    if (productToComparePriority == null) { // if not set in XML, take default one
                        productToComparePriority = productToCompare.getTibcoProductGoalAndPriority().priority();
                    }

                    if (tibcoProduct.getType().equals(ProductType.ADMIN) || tibcoProduct.getType().equals(ProductType.BW_5)) { // Administator and BW5 need RV and TRA before being installed
                        if (productToCompare.getType().equals(ProductType.RV) || productToCompare.getType().equals(ProductType.TRA)) {
                            if (productToComparePriority >= productPriority) {
                                logger.error("The product '" + productToCompare.fullProductName() + "' (priority " + productToComparePriority + ") cannot be installed after product '" + product.fullProductName() + "' (priority " + productPriority + ").");
                                prioritiesOK = false;
                            }
                        }
                    }
                }
            }
        }

        if (!prioritiesOK) {
            throw new MojoExecutionException("The topology has errors in products priorities.");
        }

        // sort products by priorities
        Collections.sort(this, new ProductComparator());
    }

    private boolean productExists(TIBCOProductToInstall.TIBCOProductGoalAndPriority tibcoProductGoalAndPriority, List<TIBCOProductToInstall> productsToInstall) {
        boolean exists = false;

        for (TIBCOProductToInstall product : productsToInstall) {
            if (product.getTibcoProductGoalAndPriority().getName().equals(tibcoProductGoalAndPriority.getName())) {
                exists = true;
            }
        }

        return exists;
    }

}
