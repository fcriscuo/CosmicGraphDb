//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.01.31 at 05:20:20 PM PST 
//


package org.uniprot.uniprot;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for interactantType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="interactantType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;group ref="{http://uniprot.org/uniprot}interactantGroup" minOccurs="0"/&gt;
 *       &lt;attribute name="intactId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "interactantType", propOrder = {
    "id",
    "label",
    "dbReference"
})
public class InteractantType
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    protected String id;
    protected String label;
    protected DbReferenceType dbReference;
    @XmlAttribute(name = "intactId", required = true)
    protected String intactId;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the dbReference property.
     * 
     * @return
     *     possible object is
     *     {@link DbReferenceType }
     *     
     */
    public DbReferenceType getDbReference() {
        return dbReference;
    }

    /**
     * Sets the value of the dbReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link DbReferenceType }
     *     
     */
    public void setDbReference(DbReferenceType value) {
        this.dbReference = value;
    }

    /**
     * Gets the value of the intactId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIntactId() {
        return intactId;
    }

    /**
     * Sets the value of the intactId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIntactId(String value) {
        this.intactId = value;
    }

}
