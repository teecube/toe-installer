//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.10.12 at 04:55:36 PM CEST 
//


package t3.toe.installer.environments;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CommandExecuteOrder.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CommandExecuteOrder"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="afterProducts"/&gt;
 *     &lt;enumeration value="beforeProducts"/&gt;
 *     &lt;enumeration value="withProducts"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "CommandExecuteOrder")
@XmlEnum
public enum CommandExecuteOrder {

    @XmlEnumValue("afterProducts")
    AFTER_PRODUCTS("afterProducts"),
    @XmlEnumValue("beforeProducts")
    BEFORE_PRODUCTS("beforeProducts"),
    @XmlEnumValue("withProducts")
    WITH_PRODUCTS("withProducts");
    private final String value;

    CommandExecuteOrder(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CommandExecuteOrder fromValue(String v) {
        for (CommandExecuteOrder c: CommandExecuteOrder.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
