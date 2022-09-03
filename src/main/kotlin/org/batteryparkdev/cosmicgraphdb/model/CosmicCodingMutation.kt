package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.YNtoBoolean
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.neo4j.driver.Value

/*
Represents the data in the CosmicMutantExport or CosmicMutantExportCensus files
Key: mutationId
 */
data class CosmicCodingMutation(
    val geneSymbol: String, val sampleId: Int,
    val genomicMutationId: String, val geneCDSLength: Int,
    val hgncId: Int, val legacyMutationId: String,
    val mutationId: Int, val mutationCds: String,
    val mutationAA: String, val mutationDescription: String, val mutationZygosity: String,
    val LOH: String, val GRCh: String, val mutationGenomePosition: String,
    val mutationStrand: String, val resistanceMutation: String,
    val mutationSomaticStatus: String,
    val pubmedId: Int, val genomeWideScreen: Boolean,
    val hgvsp: String, val hgvsc: String, val hgvsg: String, val tier: String
) : CosmicModel {

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier(
            "CosmicCodingMutation", "mutation_id",
            mutationId.toString()
        )

    override fun isValid(): Boolean = geneSymbol.isNotEmpty().and(sampleId>0)
    override fun getPubMedId(): Int  = pubmedId

    override fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
        .plus(generateGeneMutationCollectionRelationshipCypher(geneSymbol, nodename))
        .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
        .plus(" RETURN $nodename")

    private fun generateMergeCypher(): String = mergeNewNodeCypher

    private val mergeNewNodeCypher = " CALL apoc.merge.node( [\"CosmicCodingMutation\"], " +
            " {mutation_id: $mutationId}, " + // key
            " { legacy_mutation_id: ${legacyMutationId.formatNeo4jPropertyValue()} ," +
            " gene_symbol: ${geneSymbol.formatNeo4jPropertyValue()}, " +
            "  gene_cds_length: $geneCDSLength, " +
            " genomic_mutation_id: ${genomicMutationId.formatNeo4jPropertyValue()} ,"+
            " mutation_cds: ${mutationCds.formatNeo4jPropertyValue()}," +
            " mutation_aa: ${mutationAA.formatNeo4jPropertyValue()}, " +
            " description: ${mutationDescription.formatNeo4jPropertyValue()}," +
            " zygosity: ${mutationZygosity.formatNeo4jPropertyValue()}, " +
            " loh: ${LOH.formatNeo4jPropertyValue()}, " +
            " grch: ${GRCh.formatNeo4jPropertyValue()}, " +
            " genome_position: ${mutationGenomePosition.formatNeo4jPropertyValue()}, " +
            " strand: ${mutationStrand.formatNeo4jPropertyValue()}, " +
            " resistance_mutation: ${resistanceMutation.formatNeo4jPropertyValue()}, " +
            " somatic_status: ${mutationSomaticStatus.formatNeo4jPropertyValue()}, " +
            " pubmed_id: $pubmedId, " +
            " genome_wide_screen: $genomeWideScreen, " +
            " hgvsp: ${hgvsp.formatNeo4jPropertyValue()}, " +
            " hgvsc: ${hgvsc.formatNeo4jPropertyValue()}, " +
            " hgvsq: ${hgvsg.formatNeo4jPropertyValue()}, " +
            " tier: ${tier.formatNeo4jPropertyValue()}, " +
            "  created: datetime()},{}) YIELD node as $nodename \n"

    // Cypher to complete an existing placeholder node
    private val mergeExistingNodeCypher = " CALL apoc.merge.node( [\"CosmicCodingMutation\"], " +
            " {mutation_id: $mutationId}, {}," +
            " { legacy_mutation_id: ${legacyMutationId.formatNeo4jPropertyValue()}, " +
            " gene_symbol: ${geneSymbol.formatNeo4jPropertyValue()}, " +
            " genomic_mutation_id: ${genomicMutationId.formatNeo4jPropertyValue()}, " +
            "  gene_cds_length: $geneCDSLength, " +
            " mutation_cds: ${mutationCds.formatNeo4jPropertyValue()}," +
            " mutation_aa: ${mutationAA.formatNeo4jPropertyValue()}, " +
            " description: ${mutationDescription.formatNeo4jPropertyValue()}," +
            " zygosity: ${mutationZygosity.formatNeo4jPropertyValue()}, " +
            " loh: ${LOH.formatNeo4jPropertyValue()}, " +
            " grch: ${GRCh.formatNeo4jPropertyValue()}, " +
            " genome_position: ${mutationGenomePosition.formatNeo4jPropertyValue()}, " +
            " strand: ${mutationStrand.formatNeo4jPropertyValue()}, " +
            " resistance_mutation: ${resistanceMutation.formatNeo4jPropertyValue()}, " +
            " somatic_status: ${mutationSomaticStatus.formatNeo4jPropertyValue()}, " +
            " pubmed_id: $pubmedId, " +
            " genome_wide_screen: $genomeWideScreen, " +
            " hgvsp: ${hgvsp.formatNeo4jPropertyValue()}, " +
            " hgvsc: ${hgvsc.formatNeo4jPropertyValue()}, " +
            " hgvsq: ${hgvsg.formatNeo4jPropertyValue()}, " +
            " tier: ${tier.formatNeo4jPropertyValue()}, " +
            "  created: datetime()}) YIELD node as $nodename \n"

    companion object : CoreModel {
        const val nodename = "coding_mutation"

        /*
        Private function to match or create a CosmicCodingMutationNode based on the specified
        mutation id
         */
        private fun resolveCodingMutationCypher(mutationId: Int): String =
            "CALL apoc.merge.node( [\"CosmicCodingMutation\"], " +
                    " {mutation_id: $mutationId}," +
                    "  {created: datetime()},{}) " +
                    " YIELD node AS $nodename\n "

        fun generateChildRelationshipCypher(mutationId: Int, childLabel: String): String {
            val relationship = "HAS_".plus(childLabel.uppercase())
            val relname = "rel_mutation"
            return resolveCodingMutationCypher(mutationId).plus(
                "CALL apoc.merge.relationship($nodename, '$relationship', " +
                        " {}, {created: datetime()}, ${childLabel.lowercase()},{} )" +
                        " YIELD rel as $relname \n"
            )
        }

          /*
          Method to parse a CsvRecord into a CosmicCodingMutation
          CosmicMutantExportCensus file is too large to use an APOC method
           */
          fun parseCSVRecord(record:CSVRecord): CosmicCodingMutation =
              CosmicCodingMutation(
                  record.get("Gene name"), // actually HGNC approved symbol
                  record.get("ID_sample").toInt(),
                  record.get("GENOMIC_MUTATION_ID"),
                  record.get("Gene CDS length").parseValidInteger(),
                  record.get("HGNC ID").parseValidInteger(),
                  record.get("LEGACY_MUTATION_ID"),
                  record.get("MUTATION_ID").toInt(),
                  record.get("Mutation CDS"),
                  record.get("Mutation AA"),
                  record.get("Mutation Description"),
                  record.get("Mutation zygosity") ?: "",
                  record.get("LOH") ?: "",
                  record.get("GRCh") ?: "38",
                  record.get("Mutation genome position"),
                  record.get("Mutation strand"),
                  record.get("Resistance Mutation"),
                  record.get("Mutation somatic status"),
                  record.get("Pubmed_PMID").parseValidInteger(),
                  record.get("Genome-wide screen").YNtoBoolean(),
                  record.get("HGVSP"),
                  record.get("HGVSC"),
                  record.get("HGVSG"),
                  record.get("Tier") ?: ""
              )

        /*
               Not all mutation files have a Tier column
          */
        private fun resolveTier(value: Value): String =
            when (value.keys().contains("Tier")) {
                true -> value["Tier"].asString()
                false -> ""
            }

        override fun generateLoadModelCypher(): String {
            TODO("Not yet implemented")
        }

        override fun getModelGeneSymbol(): String {
            TODO("Not yet implemented")
        }

        override fun getModelSampleId(): String {
            TODO("Not yet implemented")
        }

        override fun getNodeIdentifier(): NodeIdentifier {
            TODO("Not yet implemented")
        }

        override fun getPubMedIds(): List<Int> {
            TODO("Not yet implemented")
        }

        override fun isValid(): Boolean {
            TODO("Not yet implemented")
        }

    }
}