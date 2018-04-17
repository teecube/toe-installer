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
 * <p>Java class for CustomProduct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CustomProduct"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://teecu.be/toe-installer/environments}Product"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice maxOccurs="unbounded"&gt;
 *           &lt;element name="antCommand" type="{http://teecu.be/toe-installer/environments}AntCommand"/&gt;
 *           &lt;element name="mavenCommand" type="{http://teecu.be/toe-installer/environments}MavenCommand"/&gt;
 *           &lt;element name="systemCommand" type="{http://teecu.be/toe-installer/environments}SystemCommand"/&gt;
 *           &lt;element name="uncompressCommand" type="{http://teecu.be/toe-installer/environments}UncompressCommand"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustomProduct", propOrder = {
    "antCommandOrMavenCommandOrSystemCommand"
})
public class CustomProduct
    extends Product
{

    @XmlElements({
        @XmlElement(name = "antCommand", type = AntCommand.class),
        @XmlElement(name = "mavenCommand", type = MavenCommand.class),
        @XmlElement(name = "systemCommand", type = SystemCommand.class),
        @XmlElement(name = "uncompressCommand", type = UncompressCommand.class)
    })
    protected List<AbstractCommand> antCommandOrMavenCommandOrSystemCommand;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the antCommandOrMavenCommandOrSystemCommand property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the antCommandOrMavenCommandOrSystemCommand property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAntCommandOrMavenCommandOrSystemCommand().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AntCommand }
     * {@link MavenCommand }
     * {@link SystemCommand }
     * {@link UncompressCommand }
     * 
     * 
     */
    public List<AbstractCommand> getAntCommandOrMavenCommandOrSystemCommand() {
        if (antCommandOrMavenCommandOrSystemCommand == null) {
            antCommandOrMavenCommandOrSystemCommand = new ArrayList<AbstractCommand>();
        }
        return this.antCommandOrMavenCommandOrSystemCommand;
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
        return name;
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

}
