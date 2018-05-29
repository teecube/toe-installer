//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.05.29 at 01:57:39 PM CEST 
//


package t3.toe.installer.environments;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CommandOnErrorBehaviour.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CommandOnErrorBehaviour"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="fail"/&gt;
 *     &lt;enumeration value="warn"/&gt;
 *     &lt;enumeration value="ignore"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "CommandOnErrorBehaviour")
@XmlEnum
public enum CommandOnErrorBehaviour {

    @XmlEnumValue("fail")
    FAIL("fail"),
    @XmlEnumValue("warn")
    WARN("warn"),
    @XmlEnumValue("ignore")
    IGNORE("ignore");
    private final String value;

    CommandOnErrorBehaviour(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CommandOnErrorBehaviour fromValue(String v) {
        for (CommandOnErrorBehaviour c: CommandOnErrorBehaviour.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
