//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.10.29 at 02:19:57 PM PDT 
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
 * Describes the names for the protein and parts thereof.
 *             Equivalent to the flat file DE-line.
 * 
 * <p>Java class for proteinType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="proteinType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://uniprot.org/uniprot}proteinNameGroup"/&gt;
 *         &lt;element name="domain" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;group ref="{http://uniprot.org/uniprot}proteinNameGroup"/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="component" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;group ref="{http://uniprot.org/uniprot}proteinNameGroup"/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "proteinType", propOrder = {
    "recommendedName",
    "alternativeNames",
    "submittedNames",
    "allergenName",
    "biotechName",
    "cdAntigenNames",
    "innNames",
    "domains",
    "components"
})
public class ProteinType
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    protected ProteinType.RecommendedName recommendedName;
    @XmlElement(name = "alternativeName")
    protected List<ProteinType.AlternativeName> alternativeNames;
    @XmlElement(name = "submittedName")
    protected List<ProteinType.SubmittedName> submittedNames;
    protected EvidencedStringType allergenName;
    protected EvidencedStringType biotechName;
    @XmlElement(name = "cdAntigenName")
    protected List<EvidencedStringType> cdAntigenNames;
    @XmlElement(name = "innName")
    protected List<EvidencedStringType> innNames;
    @XmlElement(name = "domain")
    protected List<ProteinType.Domain> domains;
    @XmlElement(name = "component")
    protected List<ProteinType.Component> components;

    /**
     * Gets the value of the recommendedName property.
     * 
     * @return
     *     possible object is
     *     {@link ProteinType.RecommendedName }
     *     
     */
    public ProteinType.RecommendedName getRecommendedName() {
        return recommendedName;
    }

    /**
     * Sets the value of the recommendedName property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProteinType.RecommendedName }
     *     
     */
    public void setRecommendedName(ProteinType.RecommendedName value) {
        this.recommendedName = value;
    }

    /**
     * Gets the value of the alternativeNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the alternativeNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAlternativeNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProteinType.AlternativeName }
     * 
     * 
     */
    public List<ProteinType.AlternativeName> getAlternativeNames() {
        if (alternativeNames == null) {
            alternativeNames = new ArrayList<ProteinType.AlternativeName>();
        }
        return this.alternativeNames;
    }

    /**
     * Gets the value of the submittedNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the submittedNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubmittedNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProteinType.SubmittedName }
     * 
     * 
     */
    public List<ProteinType.SubmittedName> getSubmittedNames() {
        if (submittedNames == null) {
            submittedNames = new ArrayList<ProteinType.SubmittedName>();
        }
        return this.submittedNames;
    }

    /**
     * Gets the value of the allergenName property.
     * 
     * @return
     *     possible object is
     *     {@link EvidencedStringType }
     *     
     */
    public EvidencedStringType getAllergenName() {
        return allergenName;
    }

    /**
     * Sets the value of the allergenName property.
     * 
     * @param value
     *     allowed object is
     *     {@link EvidencedStringType }
     *     
     */
    public void setAllergenName(EvidencedStringType value) {
        this.allergenName = value;
    }

    /**
     * Gets the value of the biotechName property.
     * 
     * @return
     *     possible object is
     *     {@link EvidencedStringType }
     *     
     */
    public EvidencedStringType getBiotechName() {
        return biotechName;
    }

    /**
     * Sets the value of the biotechName property.
     * 
     * @param value
     *     allowed object is
     *     {@link EvidencedStringType }
     *     
     */
    public void setBiotechName(EvidencedStringType value) {
        this.biotechName = value;
    }

    /**
     * Gets the value of the cdAntigenNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cdAntigenNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCdAntigenNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EvidencedStringType }
     * 
     * 
     */
    public List<EvidencedStringType> getCdAntigenNames() {
        if (cdAntigenNames == null) {
            cdAntigenNames = new ArrayList<EvidencedStringType>();
        }
        return this.cdAntigenNames;
    }

    /**
     * Gets the value of the innNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the innNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInnNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EvidencedStringType }
     * 
     * 
     */
    public List<EvidencedStringType> getInnNames() {
        if (innNames == null) {
            innNames = new ArrayList<EvidencedStringType>();
        }
        return this.innNames;
    }

    /**
     * Gets the value of the domains property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the domains property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDomains().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProteinType.Domain }
     * 
     * 
     */
    public List<ProteinType.Domain> getDomains() {
        if (domains == null) {
            domains = new ArrayList<ProteinType.Domain>();
        }
        return this.domains;
    }

    /**
     * Gets the value of the components property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the components property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComponents().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProteinType.Component }
     * 
     * 
     */
    public List<ProteinType.Component> getComponents() {
        if (components == null) {
            components = new ArrayList<ProteinType.Component>();
        }
        return this.components;
    }


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
     *         &lt;element name="fullName" type="{http://uniprot.org/uniprot}evidencedStringType" minOccurs="0"/&gt;
     *         &lt;element name="shortName" type="{http://uniprot.org/uniprot}evidencedStringType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *         &lt;element name="ecNumber" type="{http://uniprot.org/uniprot}evidencedStringType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "fullName",
        "shortNames",
        "ecNumbers"
    })
    public static class AlternativeName
        implements Serializable
    {

        private final static long serialVersionUID = -1L;
        protected EvidencedStringType fullName;
        @XmlElement(name = "shortName")
        protected List<EvidencedStringType> shortNames;
        @XmlElement(name = "ecNumber")
        protected List<EvidencedStringType> ecNumbers;

        /**
         * Gets the value of the fullName property.
         * 
         * @return
         *     possible object is
         *     {@link EvidencedStringType }
         *     
         */
        public EvidencedStringType getFullName() {
            return fullName;
        }

        /**
         * Sets the value of the fullName property.
         * 
         * @param value
         *     allowed object is
         *     {@link EvidencedStringType }
         *     
         */
        public void setFullName(EvidencedStringType value) {
            this.fullName = value;
        }

        /**
         * Gets the value of the shortNames property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the shortNames property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getShortNames().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EvidencedStringType }
         * 
         * 
         */
        public List<EvidencedStringType> getShortNames() {
            if (shortNames == null) {
                shortNames = new ArrayList<EvidencedStringType>();
            }
            return this.shortNames;
        }

        /**
         * Gets the value of the ecNumbers property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the ecNumbers property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEcNumbers().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EvidencedStringType }
         * 
         * 
         */
        public List<EvidencedStringType> getEcNumbers() {
            if (ecNumbers == null) {
                ecNumbers = new ArrayList<EvidencedStringType>();
            }
            return this.ecNumbers;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;group ref="{http://uniprot.org/uniprot}proteinNameGroup"/&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "recommendedName",
        "alternativeNames",
        "submittedNames",
        "allergenName",
        "biotechName",
        "cdAntigenNames",
        "innNames"
    })
    public static class Component
        implements Serializable
    {

        private final static long serialVersionUID = -1L;
        protected ProteinType.RecommendedName recommendedName;
        @XmlElement(name = "alternativeName")
        protected List<ProteinType.AlternativeName> alternativeNames;
        @XmlElement(name = "submittedName")
        protected List<ProteinType.SubmittedName> submittedNames;
        protected EvidencedStringType allergenName;
        protected EvidencedStringType biotechName;
        @XmlElement(name = "cdAntigenName")
        protected List<EvidencedStringType> cdAntigenNames;
        @XmlElement(name = "innName")
        protected List<EvidencedStringType> innNames;

        /**
         * Gets the value of the recommendedName property.
         * 
         * @return
         *     possible object is
         *     {@link ProteinType.RecommendedName }
         *     
         */
        public ProteinType.RecommendedName getRecommendedName() {
            return recommendedName;
        }

        /**
         * Sets the value of the recommendedName property.
         * 
         * @param value
         *     allowed object is
         *     {@link ProteinType.RecommendedName }
         *     
         */
        public void setRecommendedName(ProteinType.RecommendedName value) {
            this.recommendedName = value;
        }

        /**
         * Gets the value of the alternativeNames property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the alternativeNames property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAlternativeNames().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ProteinType.AlternativeName }
         * 
         * 
         */
        public List<ProteinType.AlternativeName> getAlternativeNames() {
            if (alternativeNames == null) {
                alternativeNames = new ArrayList<ProteinType.AlternativeName>();
            }
            return this.alternativeNames;
        }

        /**
         * Gets the value of the submittedNames property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the submittedNames property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSubmittedNames().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ProteinType.SubmittedName }
         * 
         * 
         */
        public List<ProteinType.SubmittedName> getSubmittedNames() {
            if (submittedNames == null) {
                submittedNames = new ArrayList<ProteinType.SubmittedName>();
            }
            return this.submittedNames;
        }

        /**
         * Gets the value of the allergenName property.
         * 
         * @return
         *     possible object is
         *     {@link EvidencedStringType }
         *     
         */
        public EvidencedStringType getAllergenName() {
            return allergenName;
        }

        /**
         * Sets the value of the allergenName property.
         * 
         * @param value
         *     allowed object is
         *     {@link EvidencedStringType }
         *     
         */
        public void setAllergenName(EvidencedStringType value) {
            this.allergenName = value;
        }

        /**
         * Gets the value of the biotechName property.
         * 
         * @return
         *     possible object is
         *     {@link EvidencedStringType }
         *     
         */
        public EvidencedStringType getBiotechName() {
            return biotechName;
        }

        /**
         * Sets the value of the biotechName property.
         * 
         * @param value
         *     allowed object is
         *     {@link EvidencedStringType }
         *     
         */
        public void setBiotechName(EvidencedStringType value) {
            this.biotechName = value;
        }

        /**
         * Gets the value of the cdAntigenNames property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cdAntigenNames property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCdAntigenNames().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EvidencedStringType }
         * 
         * 
         */
        public List<EvidencedStringType> getCdAntigenNames() {
            if (cdAntigenNames == null) {
                cdAntigenNames = new ArrayList<EvidencedStringType>();
            }
            return this.cdAntigenNames;
        }

        /**
         * Gets the value of the innNames property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the innNames property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getInnNames().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EvidencedStringType }
         * 
         * 
         */
        public List<EvidencedStringType> getInnNames() {
            if (innNames == null) {
                innNames = new ArrayList<EvidencedStringType>();
            }
            return this.innNames;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;group ref="{http://uniprot.org/uniprot}proteinNameGroup"/&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "recommendedName",
        "alternativeNames",
        "submittedNames",
        "allergenName",
        "biotechName",
        "cdAntigenNames",
        "innNames"
    })
    public static class Domain
        implements Serializable
    {

        private final static long serialVersionUID = -1L;
        protected ProteinType.RecommendedName recommendedName;
        @XmlElement(name = "alternativeName")
        protected List<ProteinType.AlternativeName> alternativeNames;
        @XmlElement(name = "submittedName")
        protected List<ProteinType.SubmittedName> submittedNames;
        protected EvidencedStringType allergenName;
        protected EvidencedStringType biotechName;
        @XmlElement(name = "cdAntigenName")
        protected List<EvidencedStringType> cdAntigenNames;
        @XmlElement(name = "innName")
        protected List<EvidencedStringType> innNames;

        /**
         * Gets the value of the recommendedName property.
         * 
         * @return
         *     possible object is
         *     {@link ProteinType.RecommendedName }
         *     
         */
        public ProteinType.RecommendedName getRecommendedName() {
            return recommendedName;
        }

        /**
         * Sets the value of the recommendedName property.
         * 
         * @param value
         *     allowed object is
         *     {@link ProteinType.RecommendedName }
         *     
         */
        public void setRecommendedName(ProteinType.RecommendedName value) {
            this.recommendedName = value;
        }

        /**
         * Gets the value of the alternativeNames property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the alternativeNames property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAlternativeNames().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ProteinType.AlternativeName }
         * 
         * 
         */
        public List<ProteinType.AlternativeName> getAlternativeNames() {
            if (alternativeNames == null) {
                alternativeNames = new ArrayList<ProteinType.AlternativeName>();
            }
            return this.alternativeNames;
        }

        /**
         * Gets the value of the submittedNames property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the submittedNames property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSubmittedNames().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ProteinType.SubmittedName }
         * 
         * 
         */
        public List<ProteinType.SubmittedName> getSubmittedNames() {
            if (submittedNames == null) {
                submittedNames = new ArrayList<ProteinType.SubmittedName>();
            }
            return this.submittedNames;
        }

        /**
         * Gets the value of the allergenName property.
         * 
         * @return
         *     possible object is
         *     {@link EvidencedStringType }
         *     
         */
        public EvidencedStringType getAllergenName() {
            return allergenName;
        }

        /**
         * Sets the value of the allergenName property.
         * 
         * @param value
         *     allowed object is
         *     {@link EvidencedStringType }
         *     
         */
        public void setAllergenName(EvidencedStringType value) {
            this.allergenName = value;
        }

        /**
         * Gets the value of the biotechName property.
         * 
         * @return
         *     possible object is
         *     {@link EvidencedStringType }
         *     
         */
        public EvidencedStringType getBiotechName() {
            return biotechName;
        }

        /**
         * Sets the value of the biotechName property.
         * 
         * @param value
         *     allowed object is
         *     {@link EvidencedStringType }
         *     
         */
        public void setBiotechName(EvidencedStringType value) {
            this.biotechName = value;
        }

        /**
         * Gets the value of the cdAntigenNames property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cdAntigenNames property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCdAntigenNames().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EvidencedStringType }
         * 
         * 
         */
        public List<EvidencedStringType> getCdAntigenNames() {
            if (cdAntigenNames == null) {
                cdAntigenNames = new ArrayList<EvidencedStringType>();
            }
            return this.cdAntigenNames;
        }

        /**
         * Gets the value of the innNames property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the innNames property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getInnNames().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EvidencedStringType }
         * 
         * 
         */
        public List<EvidencedStringType> getInnNames() {
            if (innNames == null) {
                innNames = new ArrayList<EvidencedStringType>();
            }
            return this.innNames;
        }

    }


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
     *         &lt;element name="fullName" type="{http://uniprot.org/uniprot}evidencedStringType"/&gt;
     *         &lt;element name="shortName" type="{http://uniprot.org/uniprot}evidencedStringType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *         &lt;element name="ecNumber" type="{http://uniprot.org/uniprot}evidencedStringType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "fullName",
        "shortNames",
        "ecNumbers"
    })
    public static class RecommendedName
        implements Serializable
    {

        private final static long serialVersionUID = -1L;
        @XmlElement(required = true)
        protected EvidencedStringType fullName;
        @XmlElement(name = "shortName")
        protected List<EvidencedStringType> shortNames;
        @XmlElement(name = "ecNumber")
        protected List<EvidencedStringType> ecNumbers;

        /**
         * Gets the value of the fullName property.
         * 
         * @return
         *     possible object is
         *     {@link EvidencedStringType }
         *     
         */
        public EvidencedStringType getFullName() {
            return fullName;
        }

        /**
         * Sets the value of the fullName property.
         * 
         * @param value
         *     allowed object is
         *     {@link EvidencedStringType }
         *     
         */
        public void setFullName(EvidencedStringType value) {
            this.fullName = value;
        }

        /**
         * Gets the value of the shortNames property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the shortNames property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getShortNames().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EvidencedStringType }
         * 
         * 
         */
        public List<EvidencedStringType> getShortNames() {
            if (shortNames == null) {
                shortNames = new ArrayList<EvidencedStringType>();
            }
            return this.shortNames;
        }

        /**
         * Gets the value of the ecNumbers property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the ecNumbers property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEcNumbers().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EvidencedStringType }
         * 
         * 
         */
        public List<EvidencedStringType> getEcNumbers() {
            if (ecNumbers == null) {
                ecNumbers = new ArrayList<EvidencedStringType>();
            }
            return this.ecNumbers;
        }

    }


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
     *         &lt;element name="fullName" type="{http://uniprot.org/uniprot}evidencedStringType"/&gt;
     *         &lt;element name="ecNumber" type="{http://uniprot.org/uniprot}evidencedStringType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "fullName",
        "ecNumbers"
    })
    public static class SubmittedName
        implements Serializable
    {

        private final static long serialVersionUID = -1L;
        @XmlElement(required = true)
        protected EvidencedStringType fullName;
        @XmlElement(name = "ecNumber")
        protected List<EvidencedStringType> ecNumbers;

        /**
         * Gets the value of the fullName property.
         * 
         * @return
         *     possible object is
         *     {@link EvidencedStringType }
         *     
         */
        public EvidencedStringType getFullName() {
            return fullName;
        }

        /**
         * Sets the value of the fullName property.
         * 
         * @param value
         *     allowed object is
         *     {@link EvidencedStringType }
         *     
         */
        public void setFullName(EvidencedStringType value) {
            this.fullName = value;
        }

        /**
         * Gets the value of the ecNumbers property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the ecNumbers property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEcNumbers().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EvidencedStringType }
         * 
         * 
         */
        public List<EvidencedStringType> getEcNumbers() {
            if (ecNumbers == null) {
                ecNumbers = new ArrayList<EvidencedStringType>();
            }
            return this.ecNumbers;
        }

    }

}
