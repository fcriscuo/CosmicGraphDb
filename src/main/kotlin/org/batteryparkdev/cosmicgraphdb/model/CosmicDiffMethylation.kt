package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicDiffMethylationDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import java.util.*

data class CosmicDiffMethylation(
    val key: String,
    val studyId: Int, val sampleId: Int,
    val tumorId: Int, val fragmentId: String,
    val genomeVersion: String, val chromosome: Int, // n.b. numeric values for chromosomes (x=23, y=24)
    val position: Int, val strand: String,
    val geneName: String, val methylation: String,
    val avgBetaValueNormal: Float, val betaValue: Float,
    val twoSidedPValue: Double
): CoreModel
{
    override fun createModelRelationships() = CosmicDiffMethylationDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String  =
        CosmicDiffMethylationDao(this).generateLoadCosmicModelCypher()

    // The methylation file uses non-standard gene names that are not
    // consistent with HGNC gene symbols
    override fun getModelGeneSymbol(): String = ""

    override fun getModelSampleId(): String = sampleId.toString()

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicDiffMethylation", "key", key)

    override fun getPubMedIds(): List<Int>  = emptyList()


    override fun isValid(): Boolean = sampleId > 0


    companion object : CoreModelCreator {
        val nodename = "diff_methylation"

        fun parseCsvRecord(record: CSVRecord): CosmicDiffMethylation =
            CosmicDiffMethylation(
                UUID.randomUUID().toString(),
                record.get("STUDY_ID").toInt(),
                record.get("ID_SAMPLE").toInt(),
                record.get("ID_TUMOUR").toInt(),
                record.get("FRAGMENT_ID"),
                record.get("GENOME_VERSION"),
                record.get("CHROMOSOME").toInt(),  //Integer is OK here (x=23, y=24)
                record.get("POSITION").toInt(),
                when (record.get("STRAND").toInt()) {
                    1 -> "+"
                    else ->"-"
                },
                record.get("GENE_NAME"),
                record.get("METHYLATION"),
                record.get("AVG_BETA_VALUE_NORMAL").toFloat(),
                record.get("BETA_VALUE").toFloat(),
                record.get("TWO_SIDED_P_VALUE").toDouble()
            )

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
            = ::parseCsvRecord
    }

}
