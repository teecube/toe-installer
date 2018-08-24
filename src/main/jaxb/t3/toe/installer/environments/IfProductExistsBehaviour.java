//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.08.24 at 02:17:45 PM CEST 
//


package t3.toe.installer.environments;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IfProductExistsBehaviour.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="IfProductExistsBehaviour"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="delete"/&gt;
 *     &lt;enumeration value="fail"/&gt;
 *     &lt;enumeration value="keep"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "IfProductExistsBehaviour")
@XmlEnum
public enum IfProductExistsBehaviour {

    @XmlEnumValue("delete")
    DELETE("delete"),
    @XmlEnumValue("fail")
    FAIL("fail"),
    @XmlEnumValue("keep")
    KEEP("keep");
    private final String value;

    IfProductExistsBehaviour(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static IfProductExistsBehaviour fromValue(String v) {
        for (IfProductExistsBehaviour c: IfProductExistsBehaviour.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
