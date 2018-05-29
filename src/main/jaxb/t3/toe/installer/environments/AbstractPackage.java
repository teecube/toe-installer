//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.05.29 at 01:57:39 PM CEST 
//


package t3.toe.installer.environments;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AbstractPackage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractPackage"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="local" type="{http://teecu.be/toe-installer/environments}LocalPackage"/&gt;
 *           &lt;element name="remote" type="{http://teecu.be/toe-installer/environments}AbstractRemotePackage"/&gt;
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
@XmlType(name = "AbstractPackage", propOrder = {
    "local",
    "remote"
})
public abstract class AbstractPackage {

    protected LocalPackage local;
    protected AbstractRemotePackage remote;

    /**
     * Gets the value of the local property.
     * 
     * @return
     *     possible object is
     *     {@link LocalPackage }
     *     
     */
    public LocalPackage getLocal() {
        return local;
    }

    /**
     * Sets the value of the local property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocalPackage }
     *     
     */
    public void setLocal(LocalPackage value) {
        this.local = value;
    }

    /**
     * Gets the value of the remote property.
     * 
     * @return
     *     possible object is
     *     {@link AbstractRemotePackage }
     *     
     */
    public AbstractRemotePackage getRemote() {
        return remote;
    }

    /**
     * Sets the value of the remote property.
     * 
     * @param value
     *     allowed object is
     *     {@link AbstractRemotePackage }
     *     
     */
    public void setRemote(AbstractRemotePackage value) {
        this.remote = value;
    }

}
