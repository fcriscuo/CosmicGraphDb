package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.neo4j.driver.Value

// n.b The GENE_NAME column really contains the gene symbol
data class CosmicCompleteGeneExpression(
    val sampleId: Int,
    val geneSymbol: String,
    val regulation: String,
    val zScore: Float,
    val studyId: Int,
    val key:Int
    )
{
    val nodename = "expression"

    fun generateCosmicSampleCypher(): String =
        generateMergeCypher().plus(generateGeneRelationshipCypher())
            .plus(generateSampleRelationshipCypher())
            .plus(" RETURN $nodename")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CompleteGeneExpression\"], " +
            "  {key: $key, regulation: ${Neo4jUtils.formatPropertyValue(regulation)}," +
            " z_score: $zScore, study_id: $studyId, created: datetime} " +
            " { last_mod: datetime()}) YIELD node AS $nodename \n"

    private fun generateGeneRelationshipCypher() =
        CosmicGeneCensus.generateHasGeneRelationshipCypher(geneSymbol,nodename)

    private fun generateSampleRelationshipCypher() =
        CosmicSample.generateChildRelationshipCypher(sampleId, nodename)

    companion object: AbstractModel {

         fun parseValueMap(value: Value): CosmicCompleteGeneExpression =
             CosmicCompleteGeneExpression(value["SAMPLE_ID"].asInt(),
                 value["GENE_NAME"].asString(),
                 value["REGULATION"].asString(),
                 value["Z_SCORE"].asFloat(),
                 value["ID_STUDY"].asInt(),
                 value["GENE_NAME"].asString().plus(value["SAMPLE_ID"].asInt().toString()).hashCode()
             )
         }

}
