package org.batteryparkdev.cosmicgraphdb.model

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
    fun generateDiffMethylationCypher():String =
        generateMergeCypher()
            .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
            .plus(generateGeneRelationshipCypher())
            .plus(" RETURN  $nodename")

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
    }

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicDiffMethylation", "key", key)

}
