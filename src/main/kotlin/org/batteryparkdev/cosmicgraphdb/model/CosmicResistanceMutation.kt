package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

data class CosmicResistanceMutation(
    val mutationId: Int,
    val genomicMutationId: String,
    val legacyMutationId: String,
    val aaMutation: String,
    val cdsMutation: String,
    val somaticStatus: String,
    val zygosity: String,
    val genomeCoordinates: String,
    val tier: Int,
    val hgvsp: String,
    val hgvsc: String,
    val hgvsg: String,
    val sampleId: Int,
    val geneSymbol: String,
    val transcript: String,
    val drugName: String,
    val pubmedId: Int
) : CosmicModel {

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier(
            "CosmicResistanceMutation", "resistance_id",
            mutationId.toString()
        )

    override fun generateLoadCosmicModelCypher(): String = generateMergeCypher()
        .plus(generateGeneMutationCollectionRelationshipCypher(geneSymbol, nodename))
        .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
        .plus(generateDrugRelationshipCypher())
        .plus("  RETURN  $nodename \n")

    override fun isValid(): Boolean = geneSymbol.isNotEmpty()
        .and(sampleId > 0).and(mutationId > 0)

    override fun getPubMedId(): Int = pubmedId

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node([\"CosmicResistanceMutation\"], " +
                "  { mutation_id: $mutationId} , " +
                " { gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}, " +
                " genomic_mutation_id: ${Neo4jUtils.formatPropertyValue(genomicMutationId)}, " +
                " legacy_mutation_id: ${Neo4jUtils.formatPropertyValue(legacyMutationId)}, " +
                " aa_mutation: ${Neo4jUtils.formatPropertyValue(aaMutation)}, " +
                " cds_mutation: ${Neo4jUtils.formatPropertyValue(cdsMutation)}," +
                " transcript: ${Neo4jUtils.formatPropertyValue(transcript)}, " +
                " somatic_status: ${Neo4jUtils.formatPropertyValue(somaticStatus)}, " +
                " zygosity: ${Neo4jUtils.formatPropertyValue(zygosity)}, " +
                " genome_coordinates: ${Neo4jUtils.formatPropertyValue(genomeCoordinates)}, " +
                " tier: $tier, " +
                " hgvsp: ${Neo4jUtils.formatPropertyValue(hgvsp)}, " +
                " hgvsc: ${Neo4jUtils.formatPropertyValue(hgvsc)}, " +
                " hgvsg: ${Neo4jUtils.formatPropertyValue(hgvsg)}, " +
                " transcript: ${Neo4jUtils.formatPropertyValue(transcript)}, " +
                " pubmed_id: $pubmedId, " +
                "  created: datetime()}) YIELD node as $nodename \n"

    private fun generateMatchDrugCypher(): String =
        "CALL apoc.merge.node( [\"CosmicDrug\"], " +
                " {drug_name: ${Neo4jUtils.formatPropertyValue(drugName.lowercase())}},  {created: datetime()},{} )" +
                " YIELD node AS drug_node \n"

    private fun generateDrugRelationshipCypher(): String {
        val relationship = "RESISTANT_TO"
        val relname = "rel_drug"
        return generateMatchDrugCypher().plus(
            "CALL apoc.merge.relationship($nodename, '$relationship', " +
                    " {}, {created: datetime()}, drug_node,{} )" +
                    " YIELD rel as $relname \n"
        )
    }

    companion object : AbstractModel {
        const val nodename = "resistance"

        fun parseCSVRecord(record: CSVRecord): CosmicResistanceMutation =
            CosmicResistanceMutation(
                record.get("MUTATION_ID").toInt(),
                record.get("GENOMIC_MUTATION_ID"),
                record.get("LEGACY_MUTATION_ID"),
                record.get("AA Mutation"),
                record.get("CDS Mutation"),
                record.get("Somatic Status"),
                record.get("Zygosity"),
                record.get("Genome Coordinates (GRCh38)"),
                parseValidIntegerFromString(record.get("Tier")),
                record.get("HGVSP"),
                record.get("HGVSC"),
                record.get("HGVSG"),
                record.get("Sample ID").toInt(),
                record.get("Gene Name"),
                record.get("Transcript"),
                record.get("Drug Name"),
                record.get("Pubmed Id").toInt()
            )
    }
}
