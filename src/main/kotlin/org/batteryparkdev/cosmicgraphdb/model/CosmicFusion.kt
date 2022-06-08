package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

/*
Responsible for mapping data from the CosmicFusionExport.tsv file
Only entries that have translocation name value will be processed
Duplicate fusion ids differ only by fusion type and will be
persisted in Neo4j as a single CosmicFusion node
 */
data class CosmicFusion(
    val fusionId: Int, val sampleId: Int, val sampleName: String,
    val translocationName: String,
    val five_chromosome: Int, val five_strand: String,
    val five_geneId: Int, val five_geneSymbol: String, val five_lastObservedExon: Int,
    val five_genomeStartFrom: Int, val five_genomeStartTo: Int,
    val five_genomeStopFrom: Int, val five_genomeStopTo: Int,
    val three_chromosome: Int, val three_strand: String,
    val three_geneId: Int, val three_geneSymbol: String, val three_firstObservedExon: Int,
    val three_genomeStartFrom: Int, val three_genomeStartTo: Int,
    val three_genomeStopFrom: Int, val three_genomeStopTo: Int,
    val fusionType: String, val pubmedId: Int
) : CosmicModel {

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier(
            "CosmicFusion", "fusion_id",
            fusionId.toString()
        )

    //FusionExport file contains a number of incomplete entries
    override fun isValid(): Boolean =
        fusionType.isNotEmpty().and(fusionId > 0).
        and( sampleId > 0).and( translocationName.isNotEmpty())
            .and(five_geneSymbol.isNotEmpty()).and(three_geneSymbol.isNotEmpty())

    override fun getPubMedId(): Int  = 0

    override fun generateLoadCosmicModelCypher(): String {
        val cypher = when (Neo4jUtils.nodeExistsPredicate(getNodeIdentifier())) {
            false -> generateMergeCypher()
                .plus(generateGeneMutationCollectionRelationshipCypher(five_geneSymbol, nodename))
                .plus(generateGeneMutationCollectionRelationshipCypher(three_geneSymbol, nodename))
                .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
                .plus(" RETURN  $nodename \n")
            true -> addSecondFusionTypeLabelCypher()
                .plus(" RETURN  $nodename \n")
        }
        return cypher
    }

    private fun addSecondFusionTypeLabelCypher(): String =
        "CALL apoc.merge.node([\"CosmicFusion\"], {fusion_id: $fusionId}, " +
                "{},{ last_mod: datetime()}) YIELD node AS $nodename  \n"
                    .plus("CALL apoc.create.addLabels($nodename, [${Neo4jUtils.formatPropertyValue(fusionType)}]) " +
                            " YIELD node AS label2 \n")

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node([\"CosmicFusion\", " +
                "${Neo4jUtils.formatPropertyValue(fusionType)}], " +  // secondary label
                "{fusion_id: $fusionId}, " +
                "{translocation_name: ${Neo4jUtils.formatPropertyValue(translocationName)} ," +
                " five_prime_chromosome: $five_chromosome, " +
                " five_prime_strand: ${Neo4jUtils.formatPropertyValue(five_strand)}," +
                " five_prime_gene_id: $five_geneId, " +
                " five_prime_gene_symbol: ${Neo4jUtils.formatPropertyValue(five_geneSymbol)}, " +
                " five_prime_last_observed_exon: $five_lastObservedExon, " +
                " five_prime_genome_start_from: $five_genomeStartFrom, " +
                " five_prime_genome_start_to: $five_genomeStartTo, " +
                " five_prime_genome_stop_from: $five_genomeStopFrom, " +
                " five_prime_genome_stop_to: $five_genomeStopTo, " +
                " three_prime_chromosome: $three_chromosome, " +
                " three_prime_strand: ${Neo4jUtils.formatPropertyValue(three_strand)}," +
                " three_prime_gene_id: $three_geneId, " +
                " three_prime_gene_symbol: ${Neo4jUtils.formatPropertyValue(three_geneSymbol)}, " +
                " three_prime_first_observed_exon: $three_firstObservedExon, " +
                " three_prime_genome_start_from: $three_genomeStartFrom, " +
                " three_prime_genome_start_to: $three_genomeStartTo, " +
                " three_prime_genome_stop_from: $three_genomeStopFrom, " +
                " three_prime_genome_stop_to: $three_genomeStopTo, " +
                " pubmed_id: $pubmedId, created: datetime()}, " +
                "  { last_mod: datetime()}) YIELD node AS $nodename \n"

    companion object : AbstractModel {
        const val nodename = "fusion"
        fun parseValueMap(value: Value): CosmicFusion =
            CosmicFusion(
                parseValidIntegerFromString(value["FUSION_ID"].asString()),
                parseValidIntegerFromString(value["SAMPLE_ID"].asString()),
                value["SAMPLE_NAME"].asString(),
                value["TRANSLOCATION_NAME"].asString(),
                parseValidIntegerFromString(value["5'_CHROMOSOME"].asString()),
                value["5'_STRAND"].asString(),
                parseValidIntegerFromString(value["5'_GENE_ID"].asString()),
                value["5'_GENE_NAME"].asString(),
                parseValidIntegerFromString(value["5'_LAST_OBSERVED_EXON"].asString()),
                parseValidIntegerFromString(value["5'_GENOME_START_FROM"].asString()),
                parseValidIntegerFromString(value["5'_GENOME_START_TO"].asString()),
                parseValidIntegerFromString(value["5'_GENOME_STOP_FROM"].asString()),
                parseValidIntegerFromString(value["5'_GENOME_STOP_TO"].asString()),
                parseValidIntegerFromString(value["3'_CHROMOSOME"].asString()),
                value["3'_STRAND"].asString(),
                parseValidIntegerFromString(value["3'_GENE_ID"].asString()),
                value["3'_GENE_NAME"].asString(),
                parseValidIntegerFromString(value["3'_FIRST_OBSERVED_EXON"].asString()),
                parseValidIntegerFromString(value["3'_GENOME_START_FROM"].asString()),
                parseValidIntegerFromString(value["3'_GENOME_START_TO"].asString()),
                parseValidIntegerFromString(value["3'_GENOME_STOP_FROM"].asString()),
                parseValidIntegerFromString(value["3'_GENOME_STOP_TO"].asString()),
                value["FUSION_TYPE"].asString().filter { !it.isWhitespace() },
                value["PUBMED_PMID"].asString().toInt()
            )
    }

}

