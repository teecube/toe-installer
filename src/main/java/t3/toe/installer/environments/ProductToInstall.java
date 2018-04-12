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

public abstract class ProductToInstall<P extends Product> {

    private String id;
    private IfProductExistsBehaviour ifExists;
    private String name;
    private Product.Package _package;
    private Commands postInstallCommands;
    private Commands preInstallCommands;
    private Product.Properties properties;
    private Integer priority;
    private boolean skip;

    private boolean alreadyInstalled;
    private boolean toBeDeleted;
    private String version;

    public ProductToInstall(P product) {
        this.setId(product.getId());
        this.setIfExists(product.getIfExists());
        this.setName(product.getName());
        this.setPackage(product.getPackage());
        this.setPostInstallCommands(product.getPostInstallCommands());
        this.setPreInstallCommands(product.getPreInstallCommands());
        this.setPriority(product.getPriority());
        this.setProperties(product.getProperties());
        this.setSkip(product.isSkip());
    }

    public abstract String fullProductName();

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setIfExists(IfProductExistsBehaviour ifExists) {
        this.ifExists = ifExists;
    }

    public IfProductExistsBehaviour getIfExists() {
        return ifExists;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPackage(Product.Package _package) {
        this._package = _package;
    }

    public Product.Package getPackage() {
        return _package;
    }

    public void setPostInstallCommands(Commands postInstallCommands) {
        this.postInstallCommands = postInstallCommands;
    }

    public Commands getPostInstallCommands() {
        return postInstallCommands;
    }

    public void setPreInstallCommands(Commands preInstallCommands) {
        this.preInstallCommands = preInstallCommands;
    }

    public Commands getPreInstallCommands() {
        return preInstallCommands;
    }

    public void setProperties(Product.Properties properties) {
        this.properties = properties;
    }

    public Product.Properties getProperties() {
        return properties;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isSkip() {
        return skip;
    }

    public boolean isAlreadyInstalled() {
        return alreadyInstalled;
    }

    public void setAlreadyInstalled(boolean alreadyInstalled) {
        this.alreadyInstalled = alreadyInstalled;
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
