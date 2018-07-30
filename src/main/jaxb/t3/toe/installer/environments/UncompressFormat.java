//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.30 at 05:04:39 PM CEST 
//


package t3.toe.installer.environments;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UncompressFormat.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="UncompressFormat"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="auto"/&gt;
 *     &lt;enumeration value="zip"/&gt;
 *     &lt;enumeration value="tar"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "UncompressFormat")
@XmlEnum
public enum UncompressFormat {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("zip")
    ZIP("zip"),
    @XmlEnumValue("tar")
    TAR("tar");
    private final String value;

    UncompressFormat(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UncompressFormat fromValue(String v) {
        for (UncompressFormat c: UncompressFormat.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
