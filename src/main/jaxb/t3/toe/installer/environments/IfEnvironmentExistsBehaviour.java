//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.10.10 at 06:08:44 PM CEST 
//


package t3.toe.installer.environments;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IfEnvironmentExistsBehaviour.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="IfEnvironmentExistsBehaviour"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="delete"/&gt;
 *     &lt;enumeration value="fail"/&gt;
 *     &lt;enumeration value="update"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "IfEnvironmentExistsBehaviour")
@XmlEnum
public enum IfEnvironmentExistsBehaviour {

    @XmlEnumValue("delete")
    DELETE("delete"),
    @XmlEnumValue("fail")
    FAIL("fail"),
    @XmlEnumValue("update")
    UPDATE("update");
    private final String value;

    IfEnvironmentExistsBehaviour(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static IfEnvironmentExistsBehaviour fromValue(String v) {
        for (IfEnvironmentExistsBehaviour c: IfEnvironmentExistsBehaviour.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}