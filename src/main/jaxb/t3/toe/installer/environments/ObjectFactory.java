//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.09.29 at 12:10:30 PM CEST 
//


package t3.toe.installer.environments;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the t3.toe.installer.environments package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: t3.toe.installer.environments
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Product }
     * 
     */
    public Product createProduct() {
        return new Product();
    }

    /**
     * Create an instance of {@link Environment }
     * 
     */
    public Environment createEnvironment() {
        return new Environment();
    }

    /**
     * Create an instance of {@link Environments }
     * 
     */
    public Environments createEnvironments() {
        return new Environments();
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link LocalPackage }
     * 
     */
    public LocalPackage createLocalPackage() {
        return new LocalPackage();
    }

    /**
     * Create an instance of {@link RemotePackage }
     * 
     */
    public RemotePackage createRemotePackage() {
        return new RemotePackage();
    }

    /**
     * Create an instance of {@link Product.Hotfixes }
     * 
     */
    public Product.Hotfixes createProductHotfixes() {
        return new Product.Hotfixes();
    }

    /**
     * Create an instance of {@link Product.Package }
     * 
     */
    public Product.Package createProductPackage() {
        return new Product.Package();
    }

    /**
     * Create an instance of {@link Product.Properties }
     * 
     */
    public Product.Properties createProductProperties() {
        return new Product.Properties();
    }

    /**
     * Create an instance of {@link Environment.Products }
     * 
     */
    public Environment.Products createEnvironmentProducts() {
        return new Environment.Products();
    }

}
