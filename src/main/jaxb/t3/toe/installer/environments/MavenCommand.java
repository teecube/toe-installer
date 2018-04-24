//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.04.25 at 12:01:05 AM CEST 
//


package t3.toe.installer.environments;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MavenCommand complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MavenCommand"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://teecu.be/toe-installer/environments}AbstractCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="goals" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="arguments" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="argument" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="profiles" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="profile" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enableDebugOutput" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="nonRecursive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MavenCommand", propOrder = {
    "goals",
    "arguments",
    "profiles"
})
public class MavenCommand
    extends AbstractCommand
{

    @XmlElement(required = true)
    protected String goals;
    protected MavenCommand.Arguments arguments;
    protected MavenCommand.Profiles profiles;
    @XmlAttribute(name = "enableDebugOutput")
    protected Boolean enableDebugOutput;
    @XmlAttribute(name = "nonRecursive")
    protected Boolean nonRecursive;

    /**
     * Gets the value of the goals property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGoals() {
        return goals;
    }

    /**
     * Sets the value of the goals property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGoals(String value) {
        this.goals = value;
    }

    /**
     * Gets the value of the arguments property.
     * 
     * @return
     *     possible object is
     *     {@link MavenCommand.Arguments }
     *     
     */
    public MavenCommand.Arguments getArguments() {
        return arguments;
    }

    /**
     * Sets the value of the arguments property.
     * 
     * @param value
     *     allowed object is
     *     {@link MavenCommand.Arguments }
     *     
     */
    public void setArguments(MavenCommand.Arguments value) {
        this.arguments = value;
    }

    /**
     * Gets the value of the profiles property.
     * 
     * @return
     *     possible object is
     *     {@link MavenCommand.Profiles }
     *     
     */
    public MavenCommand.Profiles getProfiles() {
        return profiles;
    }

    /**
     * Sets the value of the profiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link MavenCommand.Profiles }
     *     
     */
    public void setProfiles(MavenCommand.Profiles value) {
        this.profiles = value;
    }

    /**
     * Gets the value of the enableDebugOutput property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnableDebugOutput() {
        if (enableDebugOutput == null) {
            return false;
        } else {
            return enableDebugOutput;
        }
    }

    /**
     * Sets the value of the enableDebugOutput property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableDebugOutput(Boolean value) {
        this.enableDebugOutput = value;
    }

    /**
     * Gets the value of the nonRecursive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNonRecursive() {
        if (nonRecursive == null) {
            return false;
        } else {
            return nonRecursive;
        }
    }

    /**
     * Sets the value of the nonRecursive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNonRecursive(Boolean value) {
        this.nonRecursive = value;
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
     *         &lt;element name="argument" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "argument"
    })
    public static class Arguments {

        protected List<String> argument;

        /**
         * Gets the value of the argument property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the argument property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getArgument().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getArgument() {
            if (argument == null) {
                argument = new ArrayList<String>();
            }
            return this.argument;
        }

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
     *         &lt;element name="profile" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "profile"
    })
    public static class Profiles {

        protected List<String> profile;

        /**
         * Gets the value of the profile property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the profile property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getProfile().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getProfile() {
            if (profile == null) {
                profile = new ArrayList<String>();
            }
            return this.profile;
        }

    }

}
