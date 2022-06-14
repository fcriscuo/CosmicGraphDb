package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value
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
): CosmicModel
{
    override fun generateLoadCosmicModelCypher():String =
        generateMergeCypher()
            .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
            .plus(generateGeneRelationshipCypher())
            .plus(" RETURN  $nodename")

    override fun isValid(): Boolean = sampleId > 0
    override fun getPubMedId(): Int  = 0

    /*
    The gene symbol parameter is sparsely represented
    Limit relationships to only those methylation entries that specify a gene
     */
    private fun generateGeneRelationshipCypher(): String =
        when(geneName.isNotEmpty()) {
            true -> generateGeneMutationCollectionRelationshipCypher(geneName, nodename)
            false -> " "
        }

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicDiffMethylation\"], " +
            " { key: apoc.create.uuid()}," +
            "{ study_id: $studyId, tumor_id: $tumorId, sample_id: $sampleId, " +
            " fragment_id: ${Neo4jUtils.formatPropertyValue(fragmentId)}, genome_version: " +
            " ${Neo4jUtils.formatPropertyValue(genomeVersion)}, chromosome: $chromosome," +
            " position: $position, strand: ${Neo4jUtils.formatPropertyValue(strand)}," +
            " gene_name: ${Neo4jUtils.formatPropertyValue(geneName)}, " +
            " methylation: ${Neo4jUtils.formatPropertyValue(methylation)}," +
            " avg_beta_value_normal: $avgBetaValueNormal, beta_value: $betaValue," +
            " two_sided_p_value: $twoSidedPValue, created: datetime()}, " +
            " { last_mod: datetime()}) YIELD node AS $nodename \n"

    companion object : AbstractModel {
        val nodename = "methylation"
        fun parseValueMap(value: Value): CosmicDiffMethylation =
            CosmicDiffMethylation(
                UUID.randomUUID().toString(),
                value["STUDY_ID"].asString().toInt(),
                value["ID_SAMPLE"].asString().toInt(),
                value["ID_TUMOUR"].asString().toInt(),
                value["FRAGMENT_ID"].asString(),
                value["GENOME_VERSION"].asString(),
                value["CHROMOSOME"].asString().toInt(),  //Integer is OK here (x=23, y=24)
                value["POSITION"].asString().toInt(),
                when (value["STRAND"].asString().toInt()) {
                    1 -> "+"
                    else ->"-"
                },
                value["GENE_NAME"].asString(),
                value["METHYLATION"].asString(),
                value["AVG_BETA_VALUE_NORMAL"].asString().toFloat(),
                value["BETA_VALUE"].asString().toFloat(),
                value["TWO_SIDED_P_VALUE"].asString().toDouble()
            )

        fun parseCSVRecord(record: CSVRecord): CosmicDiffMethylation =
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
    }

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicDiffMethylation", "key", key)
}
