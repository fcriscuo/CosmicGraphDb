//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.07.06 at 12:41:56 PM PDT 
//


package org.uniprot.uniprot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="accession" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *         &lt;element name="protein" type="{http://uniprot.org/uniprot}proteinType"/&gt;
 *         &lt;element name="gene" type="{http://uniprot.org/uniprot}geneType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="organism" type="{http://uniprot.org/uniprot}organismType"/&gt;
 *         &lt;element name="organismHost" type="{http://uniprot.org/uniprot}organismType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="geneLocation" type="{http://uniprot.org/uniprot}geneLocationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="reference" type="{http://uniprot.org/uniprot}referenceType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="comment" type="{http://uniprot.org/uniprot}commentType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="dbReference" type="{http://uniprot.org/uniprot}dbReferenceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="proteinExistence" type="{http://uniprot.org/uniprot}proteinExistenceType"/&gt;
 *         &lt;element name="keyword" type="{http://uniprot.org/uniprot}keywordType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="feature" type="{http://uniprot.org/uniprot}featureType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="evidence" type="{http://uniprot.org/uniprot}evidenceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="sequence" type="{http://uniprot.org/uniprot}sequenceType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="dataset" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="Swiss-Prot"/&gt;
 *             &lt;enumeration value="TrEMBL"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="created" use="required" type="{http://www.w3.org/2001/XMLSchema}date" /&gt;
 *       &lt;attribute name="modified" use="required" type="{http://www.w3.org/2001/XMLSchema}date" /&gt;
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "accessions",
    "names",
    "protein",
    "genes",
    "organism",
    "organismHosts",
    "geneLocations",
    "references",
    "comments",
    "dbReferences",
    "proteinExistence",
    "keywords",
    "features",
    "evidences",
    "sequence"
})
@XmlRootElement(name = "entry")
public class Entry
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "accession", required = true)
    protected List<String> accessions;
    @XmlElement(name = "name", required = true)
    protected List<String> names;
    @XmlElement(required = true)
    protected ProteinType protein;
    @XmlElement(name = "gene")
    protected List<GeneType> genes;
    @XmlElement(required = true)
    protected OrganismType organism;
    @XmlElement(name = "organismHost")
    protected List<OrganismType> organismHosts;
    @XmlElement(name = "geneLocation")
    protected List<GeneLocationType> geneLocations;
    @XmlElement(name = "reference", required = true)
    protected List<ReferenceType> references;
    @XmlElement(name = "comment", nillable = true)
    protected List<CommentType> comments;
    @XmlElement(name = "dbReference")
    protected List<DbReferenceType> dbReferences;
    @XmlElement(required = true)
    protected ProteinExistenceType proteinExistence;
    @XmlElement(name = "keyword")
    protected List<KeywordType> keywords;
    @XmlElement(name = "feature")
    protected List<FeatureType> features;
    @XmlElement(name = "evidence")
    protected List<EvidenceType> evidences;
    @XmlElement(required = true)
    protected SequenceType sequence;
    @XmlAttribute(name = "dataset", required = true)
    protected String dataset;
    @XmlAttribute(name = "created", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar created;
    @XmlAttribute(name = "modified", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar modified;
    @XmlAttribute(name = "version", required = true)
    protected int version;

    /**
     * Gets the value of the accessions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accessions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccessions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAccessions() {
        if (accessions == null) {
            accessions = new ArrayList<String>();
        }
        return this.accessions;
    }

    /**
     * Gets the value of the names property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the names property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getNames() {
        if (names == null) {
            names = new ArrayList<String>();
        }
        return this.names;
    }

    /**
     * Gets the value of the protein property.
     * 
     * @return
     *     possible object is
     *     {@link ProteinType }
     *     
     */
    public ProteinType getProtein() {
        return protein;
    }

    /**
     * Sets the value of the protein property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProteinType }
     *     
     */
    public void setProtein(ProteinType value) {
        this.protein = value;
    }

    /**
     * Gets the value of the genes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the genes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGenes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GeneType }
     * 
     * 
     */
    public List<GeneType> getGenes() {
        if (genes == null) {
            genes = new ArrayList<GeneType>();
        }
        return this.genes;
    }

    /**
     * Gets the value of the organism property.
     * 
     * @return
     *     possible object is
     *     {@link OrganismType }
     *     
     */
    public OrganismType getOrganism() {
        return organism;
    }

    /**
     * Sets the value of the organism property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganismType }
     *     
     */
    public void setOrganism(OrganismType value) {
        this.organism = value;
    }

    /**
     * Gets the value of the organismHosts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the organismHosts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrganismHosts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OrganismType }
     * 
     * 
     */
    public List<OrganismType> getOrganismHosts() {
        if (organismHosts == null) {
            organismHosts = new ArrayList<OrganismType>();
        }
        return this.organismHosts;
    }

    /**
     * Gets the value of the geneLocations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the geneLocations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeneLocations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GeneLocationType }
     * 
     * 
     */
    public List<GeneLocationType> getGeneLocations() {
        if (geneLocations == null) {
            geneLocations = new ArrayList<GeneLocationType>();
        }
        return this.geneLocations;
    }

    /**
     * Gets the value of the references property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the references property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferences().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReferenceType }
     * 
     * 
     */
    public List<ReferenceType> getReferences() {
        if (references == null) {
            references = new ArrayList<ReferenceType>();
        }
        return this.references;
    }

    /**
     * Gets the value of the comments property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the comments property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComments().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CommentType }
     * 
     * 
     */
    public List<CommentType> getComments() {
        if (comments == null) {
            comments = new ArrayList<CommentType>();
        }
        return this.comments;
    }

    /**
     * Gets the value of the dbReferences property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dbReferences property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDbReferences().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DbReferenceType }
     * 
     * 
     */
    public List<DbReferenceType> getDbReferences() {
        if (dbReferences == null) {
            dbReferences = new ArrayList<DbReferenceType>();
        }
        return this.dbReferences;
    }

    /**
     * Gets the value of the proteinExistence property.
     * 
     * @return
     *     possible object is
     *     {@link ProteinExistenceType }
     *     
     */
    public ProteinExistenceType getProteinExistence() {
        return proteinExistence;
    }

    /**
     * Sets the value of the proteinExistence property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProteinExistenceType }
     *     
     */
    public void setProteinExistence(ProteinExistenceType value) {
        this.proteinExistence = value;
    }

    /**
     * Gets the value of the keywords property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the keywords property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKeywords().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link KeywordType }
     * 
     * 
     */
    public List<KeywordType> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<KeywordType>();
        }
        return this.keywords;
    }

    /**
     * Gets the value of the features property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the features property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFeatures().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureType }
     * 
     * 
     */
    public List<FeatureType> getFeatures() {
        if (features == null) {
            features = new ArrayList<FeatureType>();
        }
        return this.features;
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
     * {@link EvidenceType }
     * 
     * 
     */
    public List<EvidenceType> getEvidences() {
        if (evidences == null) {
            evidences = new ArrayList<EvidenceType>();
        }
        return this.evidences;
    }

    /**
     * Gets the value of the sequence property.
     * 
     * @return
     *     possible object is
     *     {@link SequenceType }
     *     
     */
    public SequenceType getSequence() {
        return sequence;
    }

    /**
     * Sets the value of the sequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link SequenceType }
     *     
     */
    public void setSequence(SequenceType value) {
        this.sequence = value;
    }

    /**
     * Gets the value of the dataset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataset() {
        return dataset;
    }

    /**
     * Sets the value of the dataset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataset(String value) {
        this.dataset = value;
    }

    /**
     * Gets the value of the created property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreated() {
        return created;
    }

    /**
     * Sets the value of the created property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreated(XMLGregorianCalendar value) {
        this.created = value;
    }

    /**
     * Gets the value of the modified property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getModified() {
        return modified;
    }

    /**
     * Sets the value of the modified property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setModified(XMLGregorianCalendar value) {
        this.modified = value;
    }

    /**
     * Gets the value of the version property.
     * 
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     */
    public void setVersion(int value) {
        this.version = value;
    }

}
