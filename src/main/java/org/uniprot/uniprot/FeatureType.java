//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.06.13 at 02:25:34 PM PDT 
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
 * Describes different types of sequence annotations.
 *             Equivalent to the flat file FT-line.
 * 
 * <p>Java class for featureType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="featureType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="original" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="variation" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="location" type="{http://uniprot.org/uniprot}locationType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="type" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="active site"/&gt;
 *             &lt;enumeration value="binding site"/&gt;
 *             &lt;enumeration value="calcium-binding region"/&gt;
 *             &lt;enumeration value="chain"/&gt;
 *             &lt;enumeration value="coiled-coil region"/&gt;
 *             &lt;enumeration value="compositionally biased region"/&gt;
 *             &lt;enumeration value="cross-link"/&gt;
 *             &lt;enumeration value="disulfide bond"/&gt;
 *             &lt;enumeration value="DNA-binding region"/&gt;
 *             &lt;enumeration value="domain"/&gt;
 *             &lt;enumeration value="glycosylation site"/&gt;
 *             &lt;enumeration value="helix"/&gt;
 *             &lt;enumeration value="initiator methionine"/&gt;
 *             &lt;enumeration value="lipid moiety-binding region"/&gt;
 *             &lt;enumeration value="metal ion-binding site"/&gt;
 *             &lt;enumeration value="modified residue"/&gt;
 *             &lt;enumeration value="mutagenesis site"/&gt;
 *             &lt;enumeration value="non-consecutive residues"/&gt;
 *             &lt;enumeration value="non-terminal residue"/&gt;
 *             &lt;enumeration value="nucleotide phosphate-binding region"/&gt;
 *             &lt;enumeration value="peptide"/&gt;
 *             &lt;enumeration value="propeptide"/&gt;
 *             &lt;enumeration value="region of interest"/&gt;
 *             &lt;enumeration value="repeat"/&gt;
 *             &lt;enumeration value="non-standard amino acid"/&gt;
 *             &lt;enumeration value="sequence conflict"/&gt;
 *             &lt;enumeration value="sequence variant"/&gt;
 *             &lt;enumeration value="short sequence motif"/&gt;
 *             &lt;enumeration value="signal peptide"/&gt;
 *             &lt;enumeration value="site"/&gt;
 *             &lt;enumeration value="splice variant"/&gt;
 *             &lt;enumeration value="strand"/&gt;
 *             &lt;enumeration value="topological domain"/&gt;
 *             &lt;enumeration value="transit peptide"/&gt;
 *             &lt;enumeration value="transmembrane region"/&gt;
 *             &lt;enumeration value="turn"/&gt;
 *             &lt;enumeration value="unsure residue"/&gt;
 *             &lt;enumeration value="zinc finger region"/&gt;
 *             &lt;enumeration value="intramembrane region"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="evidence" type="{http://uniprot.org/uniprot}intListType" /&gt;
 *       &lt;attribute name="ref" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "featureType", propOrder = {
    "original",
    "variations",
    "location"
})
public class FeatureType
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    protected String original;
    @XmlElement(name = "variation")
    protected List<String> variations;
    @XmlElement(required = true)
    protected LocationType location;
    @XmlAttribute(name = "type", required = true)
    protected String type;
    @XmlAttribute(name = "id")
    protected String id;
    @XmlAttribute(name = "description")
    protected String description;
    @XmlAttribute(name = "evidence")
    protected List<Integer> evidences;
    @XmlAttribute(name = "ref")
    protected String ref;

    /**
     * Gets the value of the original property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginal() {
        return original;
    }

    /**
     * Sets the value of the original property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginal(String value) {
        this.original = value;
    }

    /**
     * Gets the value of the variations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the variations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVariations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getVariations() {
        if (variations == null) {
            variations = new ArrayList<String>();
        }
        return this.variations;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link LocationType }
     *     
     */
    public LocationType getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationType }
     *     
     */
    public void setLocation(LocationType value) {
        this.location = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

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
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
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
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRef(String value) {
        this.ref = value;
    }

}
