//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.06.23 at 04:27:42 PM CEST 
//


package t3.toe.installer.environments;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProductType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ProductType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="admin"/&gt;
 *     &lt;enumeration value="bw5"/&gt;
 *     &lt;enumeration value="bw6"/&gt;
 *     &lt;enumeration value="ems"/&gt;
 *     &lt;enumeration value="rv"/&gt;
 *     &lt;enumeration value="tea"/&gt;
 *     &lt;enumeration value="tra"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ProductType")
@XmlEnum
public enum ProductType {

    @XmlEnumValue("admin")
    ADMIN("admin"),
    @XmlEnumValue("bw5")
    BW_5("bw5"),
    @XmlEnumValue("bw6")
    BW_6("bw6"),
    @XmlEnumValue("ems")
    EMS("ems"),
    @XmlEnumValue("rv")
    RV("rv"),
    @XmlEnumValue("tea")
    TEA("tea"),
    @XmlEnumValue("tra")
    TRA("tra");
    private final String value;

    ProductType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProductType fromValue(String v) {
        for (ProductType c: ProductType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}