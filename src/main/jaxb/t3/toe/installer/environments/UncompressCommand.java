//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.08.24 at 02:17:45 PM CEST 
//


package t3.toe.installer.environments;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UncompressCommand complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UncompressCommand"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://teecu.be/toe-installer/environments}AbstractCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="destination"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="directory" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="tempDirectory" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
 *                 &lt;/choice&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="format" type="{http://teecu.be/toe-installer/environments}UncompressFormat" default="auto" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UncompressCommand", propOrder = {
    "destination"
})
public class UncompressCommand
    extends AbstractCommand
{

    @XmlElement(required = true)
    protected UncompressCommand.Destination destination;
    @XmlAttribute(name = "format")
    protected UncompressFormat format;

    /**
     * Gets the value of the destination property.
     * 
     * @return
     *     possible object is
     *     {@link UncompressCommand.Destination }
     *     
     */
    public UncompressCommand.Destination getDestination() {
        return destination;
    }

    /**
     * Sets the value of the destination property.
     * 
     * @param value
     *     allowed object is
     *     {@link UncompressCommand.Destination }
     *     
     */
    public void setDestination(UncompressCommand.Destination value) {
        this.destination = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link UncompressFormat }
     *     
     */
    public UncompressFormat getFormat() {
        if (format == null) {
            return UncompressFormat.AUTO;
        } else {
            return format;
        }
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link UncompressFormat }
     *     
     */
    public void setFormat(UncompressFormat value) {
        this.format = value;
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
     *       &lt;choice&gt;
     *         &lt;element name="directory" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="tempDirectory" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
     *       &lt;/choice&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "directory",
        "tempDirectory"
    })
    public static class Destination {

        protected String directory;
        protected Object tempDirectory;

        /**
         * Gets the value of the directory property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDirectory() {
            return directory;
        }

        /**
         * Sets the value of the directory property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDirectory(String value) {
            this.directory = value;
        }

        /**
         * Gets the value of the tempDirectory property.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getTempDirectory() {
            return tempDirectory;
        }

        /**
         * Sets the value of the tempDirectory property.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setTempDirectory(Object value) {
            this.tempDirectory = value;
        }

    }

}
