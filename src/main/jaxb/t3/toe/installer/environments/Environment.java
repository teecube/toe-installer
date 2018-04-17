//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.04.17 at 05:09:17 PM CEST 
//


package t3.toe.installer.environments;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Environment complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Environment"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="environmentName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="tibcoRoot" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="packagesDirectory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="preInstallCommands" type="{http://teecu.be/toe-installer/environments}Commands" minOccurs="0"/&gt;
 *         &lt;element name="products"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;element name="tibcoProduct" type="{http://teecu.be/toe-installer/environments}TIBCOProduct"/&gt;
 *                     &lt;element name="customProduct" type="{http://teecu.be/toe-installer/environments}CustomProduct"/&gt;
 *                   &lt;/choice&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="postInstallCommands" type="{http://teecu.be/toe-installer/environments}Commands" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ifExists" type="{http://teecu.be/toe-installer/environments}IfEnvironmentExistsBehaviour" default="fail" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Environment", propOrder = {
    "environmentName",
    "tibcoRoot",
    "packagesDirectory",
    "preInstallCommands",
    "products",
    "postInstallCommands"
})
public class Environment {

    @XmlElement(required = true)
    protected String environmentName;
    @XmlElement(required = true)
    protected String tibcoRoot;
    protected String packagesDirectory;
    protected Commands preInstallCommands;
    @XmlElement(required = true)
    protected Environment.Products products;
    protected Commands postInstallCommands;
    @XmlAttribute(name = "ifExists")
    protected IfEnvironmentExistsBehaviour ifExists;

    /**
     * Gets the value of the environmentName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnvironmentName() {
        return environmentName;
    }

    /**
     * Sets the value of the environmentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnvironmentName(String value) {
        this.environmentName = value;
    }

    /**
     * Gets the value of the tibcoRoot property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTibcoRoot() {
        return tibcoRoot;
    }

    /**
     * Sets the value of the tibcoRoot property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTibcoRoot(String value) {
        this.tibcoRoot = value;
    }

    /**
     * Gets the value of the packagesDirectory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPackagesDirectory() {
        return packagesDirectory;
    }

    /**
     * Sets the value of the packagesDirectory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPackagesDirectory(String value) {
        this.packagesDirectory = value;
    }

    /**
     * Gets the value of the preInstallCommands property.
     * 
     * @return
     *     possible object is
     *     {@link Commands }
     *     
     */
    public Commands getPreInstallCommands() {
        return preInstallCommands;
    }

    /**
     * Sets the value of the preInstallCommands property.
     * 
     * @param value
     *     allowed object is
     *     {@link Commands }
     *     
     */
    public void setPreInstallCommands(Commands value) {
        this.preInstallCommands = value;
    }

    /**
     * Gets the value of the products property.
     * 
     * @return
     *     possible object is
     *     {@link Environment.Products }
     *     
     */
    public Environment.Products getProducts() {
        return products;
    }

    /**
     * Sets the value of the products property.
     * 
     * @param value
     *     allowed object is
     *     {@link Environment.Products }
     *     
     */
    public void setProducts(Environment.Products value) {
        this.products = value;
    }

    /**
     * Gets the value of the postInstallCommands property.
     * 
     * @return
     *     possible object is
     *     {@link Commands }
     *     
     */
    public Commands getPostInstallCommands() {
        return postInstallCommands;
    }

    /**
     * Sets the value of the postInstallCommands property.
     * 
     * @param value
     *     allowed object is
     *     {@link Commands }
     *     
     */
    public void setPostInstallCommands(Commands value) {
        this.postInstallCommands = value;
    }

    /**
     * Gets the value of the ifExists property.
     * 
     * @return
     *     possible object is
     *     {@link IfEnvironmentExistsBehaviour }
     *     
     */
    public IfEnvironmentExistsBehaviour getIfExists() {
        if (ifExists == null) {
            return IfEnvironmentExistsBehaviour.FAIL;
        } else {
            return ifExists;
        }
    }

    /**
     * Sets the value of the ifExists property.
     * 
     * @param value
     *     allowed object is
     *     {@link IfEnvironmentExistsBehaviour }
     *     
     */
    public void setIfExists(IfEnvironmentExistsBehaviour value) {
        this.ifExists = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;element name="tibcoProduct" type="{http://teecu.be/toe-installer/environments}TIBCOProduct"/&gt;
     *           &lt;element name="customProduct" type="{http://teecu.be/toe-installer/environments}CustomProduct"/&gt;
     *         &lt;/choice&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "tibcoProductOrCustomProduct"
    })
    public static class Products {

        @XmlElements({
            @XmlElement(name = "tibcoProduct", type = TIBCOProduct.class),
            @XmlElement(name = "customProduct", type = CustomProduct.class)
        })
        protected List<Product> tibcoProductOrCustomProduct;

        /**
         * Gets the value of the tibcoProductOrCustomProduct property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the tibcoProductOrCustomProduct property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTibcoProductOrCustomProduct().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link TIBCOProduct }
         * {@link CustomProduct }
         * 
         * 
         */
        public List<Product> getTibcoProductOrCustomProduct() {
            if (tibcoProductOrCustomProduct == null) {
                tibcoProductOrCustomProduct = new ArrayList<Product>();
            }
            return this.tibcoProductOrCustomProduct;
        }

    }

}
