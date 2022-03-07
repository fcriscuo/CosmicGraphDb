//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.02.01 at 08:07:24 PM PST 
//


package org.uniprot.uniprot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Describes the subcellular location and optionally the topology and orientation of a molecule.
 * 
 * <p>Java class for subcellularLocationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="subcellularLocationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="location" type="{http://uniprot.org/uniprot}evidencedStringType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="topology" type="{http://uniprot.org/uniprot}evidencedStringType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="orientation" type="{http://uniprot.org/uniprot}evidencedStringType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "subcellularLocationType", propOrder = {
    "locations",
    "topologies",
    "orientations"
})
public class SubcellularLocationType
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "location", required = true)
    protected List<EvidencedStringType> locations;
    @XmlElement(name = "topology")
    protected List<EvidencedStringType> topologies;
    @XmlElement(name = "orientation")
    protected List<EvidencedStringType> orientations;

    /**
     * Gets the value of the locations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the locations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EvidencedStringType }
     * 
     * 
     */
    public List<EvidencedStringType> getLocations() {
        if (locations == null) {
            locations = new ArrayList<EvidencedStringType>();
        }
        return this.locations;
    }

    /**
     * Gets the value of the topologies property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the topologies property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTopologies().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EvidencedStringType }
     * 
     * 
     */
    public List<EvidencedStringType> getTopologies() {
        if (topologies == null) {
            topologies = new ArrayList<EvidencedStringType>();
        }
        return this.topologies;
    }

    /**
     * Gets the value of the orientations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the orientations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrientations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EvidencedStringType }
     * 
     * 
     */
    public List<EvidencedStringType> getOrientations() {
        if (orientations == null) {
            orientations = new ArrayList<EvidencedStringType>();
        }
        return this.orientations;
    }

}
