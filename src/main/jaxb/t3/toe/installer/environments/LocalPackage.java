//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.04.12 at 05:42:24 PM CEST 
//


package t3.toe.installer.environments;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LocalPackage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LocalPackage"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="fileWithVersion" type="{http://teecu.be/toe-installer/environments}LocalFileWithVersion"/&gt;
 *           &lt;element name="directoryWithPattern" type="{http://teecu.be/toe-installer/environments}LocalPackagePattern"/&gt;
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
@XmlType(name = "LocalPackage", propOrder = {
    "fileWithVersion",
    "directoryWithPattern"
})
public class LocalPackage {

    protected LocalFileWithVersion fileWithVersion;
    protected LocalPackagePattern directoryWithPattern;

    /**
     * Gets the value of the fileWithVersion property.
     * 
     * @return
     *     possible object is
     *     {@link LocalFileWithVersion }
     *     
     */
    public LocalFileWithVersion getFileWithVersion() {
        return fileWithVersion;
    }

    /**
     * Sets the value of the fileWithVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocalFileWithVersion }
     *     
     */
    public void setFileWithVersion(LocalFileWithVersion value) {
        this.fileWithVersion = value;
    }

    /**
     * Gets the value of the directoryWithPattern property.
     * 
     * @return
     *     possible object is
     *     {@link LocalPackagePattern }
     *     
     */
    public LocalPackagePattern getDirectoryWithPattern() {
        return directoryWithPattern;
    }

    /**
     * Sets the value of the directoryWithPattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocalPackagePattern }
     *     
     */
    public void setDirectoryWithPattern(LocalPackagePattern value) {
        this.directoryWithPattern = value;
    }

}
