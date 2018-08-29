//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.08.29 at 04:42:44 PM CEST 
//


package t3.toe.installer.environments;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Product complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Product"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="preInstallCommands" type="{http://teecu.be/toe-installer/environments}Commands" minOccurs="0"/&gt;
 *         &lt;element name="package" type="{http://teecu.be/toe-installer/environments}Package"/&gt;
 *         &lt;element name="properties" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="property" type="{http://teecu.be/toe-installer/environments}Property" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="postInstallCommands" type="{http://teecu.be/toe-installer/environments}Commands" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="priority" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="skip" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="ifExists" type="{http://teecu.be/toe-installer/environments}IfProductExistsBehaviour" default="keep" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Product", propOrder = {
    "preInstallCommands",
    "_package",
    "properties",
    "postInstallCommands"
})
@XmlSeeAlso({
    TIBCOProduct.class,
    CustomProduct.class
})
public abstract class Product {

    protected Commands preInstallCommands;
    @XmlElement(name = "package", required = true)
    protected Package _package;
    protected Product.Properties properties;
    protected Commands postInstallCommands;
    @XmlAttribute(name = "id")
    protected String id;
    @XmlAttribute(name = "priority")
    protected Integer priority;
    @XmlAttribute(name = "skip")
    protected Boolean skip;
    @XmlAttribute(name = "ifExists")
    protected IfProductExistsBehaviour ifExists;

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
     * Gets the value of the package property.
     * 
     * @return
     *     possible object is
     *     {@link Package }
     *     
     */
    public Package getPackage() {
        return _package;
    }

    /**
     * Sets the value of the package property.
     * 
     * @param value
     *     allowed object is
     *     {@link Package }
     *     
     */
    public void setPackage(Package value) {
        this._package = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link Product.Properties }
     *     
     */
    public Product.Properties getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link Product.Properties }
     *     
     */
    public void setProperties(Product.Properties value) {
        this.properties = value;
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
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPriority(Integer value) {
        this.priority = value;
    }

    /**
     * Gets the value of the skip property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSkip() {
        if (skip == null) {
            return false;
        } else {
            return skip;
        }
    }

    /**
     * Sets the value of the skip property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSkip(Boolean value) {
        this.skip = value;
    }

    /**
     * Gets the value of the ifExists property.
     * 
     * @return
     *     possible object is
     *     {@link IfProductExistsBehaviour }
     *     
     */
    public IfProductExistsBehaviour getIfExists() {
        if (ifExists == null) {
            return IfProductExistsBehaviour.KEEP;
        } else {
            return ifExists;
        }
    }

    /**
     * Sets the value of the ifExists property.
     * 
     * @param value
     *     allowed object is
     *     {@link IfProductExistsBehaviour }
     *     
     */
    public void setIfExists(IfProductExistsBehaviour value) {
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
     *         &lt;element name="property" type="{http://teecu.be/toe-installer/environments}Property" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "property"
    })
    public static class Properties {

        protected List<Property> property;

        /**
         * Gets the value of the property property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the property property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getProperty().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Property }
         * 
         * 
         */
        public List<Property> getProperty() {
            if (property == null) {
                property = new ArrayList<Property>();
            }
            return this.property;
        }

    }

}
