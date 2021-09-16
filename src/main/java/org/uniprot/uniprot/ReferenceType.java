//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.09.16 at 03:38:29 PM PDT 
//


package org.uniprot.uniprot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Describes a citation and a summary of its content.
 *             Equivalent to the flat file RN-, RP-, RC-, RX-, RG-, RA-, RT- and RL-lines.
 * 
 * <p>Java class for referenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="referenceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="citation" type="{http://uniprot.org/uniprot}citationType"/&gt;
 *         &lt;group ref="{http://uniprot.org/uniprot}sptrCitationGroup"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="evidence" type="{http://uniprot.org/uniprot}intListType" /&gt;
 *       &lt;attribute name="key" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "referenceType", propOrder = {
    "citation",
    "scopes",
    "source"
})
public class ReferenceType
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(required = true)
    protected CitationType citation;
    @XmlElement(name = "scope", required = true)
    protected List<String> scopes;
    protected SourceDataType source;
    @XmlAttribute(name = "evidence")
    protected List<Integer> evidences;
    @XmlAttribute(name = "key", required = true)
    protected String key;

    /**
     * Gets the value of the citation property.
     * 
     * @return
     *     possible object is
     *     {@link CitationType }
     *     
     */
    public CitationType getCitation() {
        return citation;
    }

    /**
     * Sets the value of the citation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CitationType }
     *     
     */
    public void setCitation(CitationType value) {
        this.citation = value;
    }

    /**
     * Gets the value of the scopes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scopes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScopes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getScopes() {
        if (scopes == null) {
            scopes = new ArrayList<String>();
        }
        return this.scopes;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link SourceDataType }
     *     
     */
    public SourceDataType getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceDataType }
     *     
     */
    public void setSource(SourceDataType value) {
        this.source = value;
    }

    /**
     * Gets the value of the evidences property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the evidences property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEvidences().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getEvidences() {
        if (evidences == null) {
            evidences = new ArrayList<Integer>();
        }
        return this.evidences;
    }

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKey(String value) {
        this.key = value;
    }

}
