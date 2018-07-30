//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.30 at 05:04:39 PM CEST 
//


package t3.toe.installer.environments;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TIBCOProduct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TIBCOProduct"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://teecu.be/toe-installer/environments}Product"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="hotfixes" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="hotfix" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="@type" /&gt;
 *       &lt;attribute name="type" use="required" type="{http://teecu.be/toe-installer/environments}ProductType" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TIBCOProduct", propOrder = {
    "hotfixes"
})
public class TIBCOProduct
    extends Product
{

    protected TIBCOProduct.Hotfixes hotfixes;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "type", required = true)
    protected ProductType type;

    /**
     * Gets the value of the hotfixes property.
     * 
     * @return
     *     possible object is
     *     {@link TIBCOProduct.Hotfixes }
     *     
     */
    public TIBCOProduct.Hotfixes getHotfixes() {
        return hotfixes;
    }

    /**
     * Sets the value of the hotfixes property.
     * 
     * @param value
     *     allowed object is
     *     {@link TIBCOProduct.Hotfixes }
     *     
     */
    public void setHotfixes(TIBCOProduct.Hotfixes value) {
        this.hotfixes = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        if (name == null) {
            return "@type";
        } else {
            return name;
        }
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link ProductType }
     *     
     */
    public ProductType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProductType }
     *     
     */
    public void setType(ProductType value) {
        this.type = value;
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
     *         &lt;element name="hotfix" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
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
        "hotfix"
    })
    public static class Hotfixes {

        @XmlElement(required = true)
        protected String hotfix;

        /**
         * Gets the value of the hotfix property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getHotfix() {
            return hotfix;
        }

        /**
         * Sets the value of the hotfix property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setHotfix(String value) {
            this.hotfix = value;
        }

    }

}
