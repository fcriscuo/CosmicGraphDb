package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicTumorDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.common.removeInternalQuotes
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

/*
Represents the tumor data in the CosmicMutantExport or CosmicMutantExportCensus files
Key: tumorId
Relationships:
  Patient -[HAS_TUMOR] -> Tumor
  Tumor - [HAS_SAMPLE] -> Sample
 */
data class CosmicTumor(
    val tumorId: Int,
    val sampleId: Int,
    val tumorSource: String,
    val tumorRemark: String,
    val patient: CoreModel
): CoreModel {
    override val idPropertyValue: String
        get() = tumorId.toString()

    override fun createModelRelationships() = CosmicTumorDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String  = CosmicTumorDao(this).generateLoadCosmicModelCypher()

    override fun getModelGeneSymbol(): String  = ""

    override fun getModelSampleId(): String = sampleId.toString()

    override fun getNodeIdentifier(): NodeIdentifier = generateNodeIdentifierByModel(CosmicTumor, this)

    override fun getPubMedIds(): List<Int> = emptyList()

    override fun isValid(): Boolean = patient.isValid().and(tumorId > 0)

    companion object : CoreModelCreator {
        override val nodename = "tumor"

        fun parseCsvRecord(record: CSVRecord): CosmicTumor =
            CosmicTumor(
                record.get("id_tumour").parseValidInteger(),
                record.get("sample_id").parseValidInteger(),
                record.get("tumour_source"),
                record.get("tumour_remark").removeInternalQuotes(),
                CosmicPatient.createCoreModelFunction.invoke(record)
            )

        fun generatePlaceholderCypher(tumorId: Int)  = " CALL apoc.merge.node([\"CosmicTumor\"], " +
                " {tumor_id: $tumorId}, {created: datetime()},{modified: datetime()}) " +
                " YIELD node as $nodename  \n"

        fun generateChildRelationshipCypher (tumorId: Int, childLabel: String ) :String{
            val relationship = "HAS_".plus(childLabel.uppercase())
            val relname = "rel_tumor"
            return  generatePlaceholderCypher(tumorId).plus(
            " CALL apoc.merge.relationship ($nodename, '$relationship', " +
                    " {}, {created: datetime()}, " +
                    " $childLabel, {} ) YIELD rel AS $relname \n")
        }

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
           = ::parseCsvRecord
        override val nodeIdProperty: String
            get() = "tumor_id"
        override val nodelabel: String
            get() = "CosmicTumor"

    }
}


