package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

// n.b The GENE_NAME column really contains the gene symbol
data class CosmicCompleteGeneExpression(
    val sampleId: Int,
    val geneSymbol: String,
    val regulation: String,
    val zScore: Float,
    val studyId: Int,
    val key:Int
    ): CosmicModel
{
    val nodename = "expression"

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CompleteGeneExpression", "key",
            key.toString())
    override fun isValid(): Boolean = geneSymbol.isNotEmpty().and(sampleId > 0)
    override fun getPubMedId(): Int = 0

    override fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
            .plus(generateGeneMutationCollectionRelationshipCypher(geneSymbol,nodename))
            .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
            .plus(" RETURN $nodename")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CompleteGeneExpression\"], " +
            "  {key: $key, regulation: ${Neo4jUtils.formatPropertyValue(regulation)}, " +
            "  gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)} ," +
            "  sample_id: $sampleId, " +
            " z_score: $zScore, study_id: $studyId, created: datetime()}, " +
            " { last_mod: datetime()}) YIELD node AS $nodename \n"

    private fun generateGeneRelationshipCypher() =
        CosmicGeneCensus.generateGeneParentRelationshipCypher(geneSymbol,nodename)

    private fun generateSampleRelationshipCypher() =
        CosmicSample.generateChildRelationshipCypher(sampleId, nodename)

    companion object: AbstractModel {

         fun parseValueMap(value: Value): CosmicCompleteGeneExpression =
             CosmicCompleteGeneExpression(value["SAMPLE_ID"].asString().toInt(),
                 value["GENE_NAME"].asString(),
                 value["REGULATION"].asString(),
                 value["Z_SCORE"].asString().toFloat(),
                 value["ID_STUDY"].asString().toInt(),
                 value["GENE_NAME"].asString().plus(value["SAMPLE_ID"].asString()).hashCode()
             )
         }

}
