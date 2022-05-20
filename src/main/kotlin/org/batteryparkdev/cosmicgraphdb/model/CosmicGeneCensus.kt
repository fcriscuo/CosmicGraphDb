package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.service.TumorTypeService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

data class CosmicGeneCensus(

    val geneSymbol: String, val geneName: String, val entrezGeneId: String,
    val genomeLocation: String, val tier: Int = 0, val hallmark: Boolean = false,
    val chromosomeBand: String, val somatic: Boolean = false, val germline: Boolean,
    val somaticTumorTypeList: List<String>, val germlineTumorTypeList: List<String>,
    val cancerSyndrome: String, val tissueTypeList: List<String>, val molecularGenetics: String,
    val roleInCancerList: List<String>, val mutationTypeList: List<String>,
    val translocationPartnerList: List<String>,
    val otherGermlineMut: String, val otherSyndromeList: List<String>,
    val cosmicId: String, val cosmicGeneName: String,
    val synonymList: List<String>
) : CosmicModel {
    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicGene", "gene_symbol", geneSymbol)

    fun generateCosmicGeneCypher(): String =
        generateMergeCypher()
            .plus(generateGeneMutationCollectionNodeCypher())
            .plus(generateGeneAnnotationCollectionCypher())
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    somaticTumorTypeList,
                    "SomaticTumorType", annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    germlineTumorTypeList, "GermlineTumorType",
                    annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    tissueTypeList,
                    "TissueType", annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    roleInCancerList,
                    "RoleInCancer", annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    mutationTypeList, "MutationType",
                    annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    otherSyndromeList, "OtherSyndrome",
                    annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    synonymList, "Synonym",
                    annoCollNodename
                )
            )
            .plus(CosmicAnnotationFunctions.generateTranslocationCypher(geneSymbol,translocationPartnerList))

            .plus(" RETURN $nodename")

    private fun generateGeneMutationCollectionNodeCypher(): String =
        "CALL apoc.merge.node([\"GeneMutationCollection\"], " +
                " {gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}," +
                " {created: datetime()},{}) YIELD node as ${CosmicGeneCensus.mutCollNodename} \n " +
                " CALL apoc.merge.relationship (${CosmicGeneCensus.nodename}, \"HAS_MUTATION_COLLECTION\", " +
                "  {},{created: datetime()}, ${CosmicGeneCensus.mutCollNodename},{} ) YIELD rel AS mut_rel \n "

    private fun generateGeneAnnotationCollectionCypher(): String =
        "CALL apoc.merge.node([\"GeneAnnotationCollection\"], " +
                " {gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}," +
                " {created: datetime()},{}) YIELD node as ${CosmicGeneCensus.annoCollNodename} \n " +
                " CALL apoc.merge.relationship (${CosmicGeneCensus.nodename}, \"HAS_ANNOTATION_COLLECTION\", " +
                "  {},{created: datetime()}, ${CosmicGeneCensus.annoCollNodename},{} ) YIELD rel AS anno_rel \n "

    private fun generateMergeCypher(): String =
        when (Neo4jUtils.nodeExistsPredicate(getNodeIdentifier())) {
            //update an existing CosmicGene node (i.e. placeholder)
            true -> "CALL apoc.merge.node( [\"CosmicGene\",\"CensusGene\"]," +
                    "{  gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}, " +
                    "{}," +
                    " {gene_name: ${Neo4jUtils.formatPropertyValue(geneName)}," +
                    " entrez_gene_id: ${Neo4jUtils.formatPropertyValue(entrezGeneId)}," +
                    " genome_location: ${Neo4jUtils.formatPropertyValue(genomeLocation)}," +
                    " tier: $tier, hallmark: $hallmark, " +
                    " chromosome_band: $chromosomeBand, " +
                    " somatic: $somatic, germline: $germline, " +
                    " cancer_syndrome: ${Neo4jUtils.formatPropertyValue(cancerSyndrome)}," +
                    " molecular_genetics: $molecularGenetics, " +
                    " other_germline_mut: ${Neo4jUtils.formatPropertyValue(otherGermlineMut)}, " +
                    " cosmic_id: ${Neo4jUtils.formatPropertyValue(cosmicId)}, " +
                    " cosmic_gene_name: ${Neo4jUtils.formatPropertyValue(cosmicGeneName)}, " +
                    "  created: datetime()}) YIELD node as $nodename \n"
            // create a new CosmicGene new node
            false -> "CALL apoc.merge.node( [\"CosmicGene\",\"CensusGene\"]," +
                    "{  gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}," +
                    " {gene_name: ${Neo4jUtils.formatPropertyValue(geneName)}," +
                    " entrez_gene_id: ${Neo4jUtils.formatPropertyValue(entrezGeneId)}," +
                    " genome_location: ${Neo4jUtils.formatPropertyValue(genomeLocation)}," +
                    " tier: $tier, hallmark: $hallmark, " +
                    " chromosome_band: $chromosomeBand, " +
                    " somatic: $somatic, germline: $germline, " +
                    " cancer_syndrome: ${Neo4jUtils.formatPropertyValue(cancerSyndrome)}," +
                    " molecular_genetics: $molecularGenetics, " +
                    " other_germline_mut: ${Neo4jUtils.formatPropertyValue(otherGermlineMut)}," +
                    " cosmic_id: ${Neo4jUtils.formatPropertyValue(cosmicId)}, " +
                    "cosmic_gene_name: ${Neo4jUtils.formatPropertyValue(cosmicGeneName)}, " +
                    "  created: datetime()},{}) YIELD node as $nodename \n"
        }


    companion object : AbstractModel {
        const val nodename = "gene"
        const val mutCollNodename = "mut_collection"
        const val annoCollNodename = "anno_collection"
        private fun resolveGeneNodeIdentifier(geneSymbol: String): NodeIdentifier =
            NodeIdentifier("CosmicGene", "gene_symbol", geneSymbol)

        /*
        Private function to MATCH an existing CosmicGene node if it exists or
        create a placeholder node if the gene is novel
         */
        private fun generateMatchCosmicGeneCypher( geneSymbol:String) =
            " CALL apoc.merge.node([\"CosmicGene\"]," +
                    " { gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}, {created: datetime()},{} ) " +
                    " YIELD node as $nodename \n"

        private fun generatePlaceholderCypher(geneSymbol: String): String =
            when (Neo4jUtils.nodeExistsPredicate(resolveGeneNodeIdentifier(geneSymbol))) {
                // Cypher MATCH
                true -> " CALL apoc.merge.node([\"CosmicGene\"]," +
                        " { gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}, {},{} ) " +
                        " YIELD node as $nodename \n"
                // Cypher MERGE
                false -> " CALL apoc.merge.node([\"CosmicGene\"]," +
                        " { gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}, {created: datetime()},{} ) " +
                        " YIELD node as $nodename \n"
            }

        /*
        Function to create a relationship between a CosmicGene and a child node (e.g. CosmicMutation)
         */
        fun generateGeneParentRelationshipCypher(geneSymbol: String, parentNodeName: String): String {
            val relationship = "HAS_".plus(parentNodeName.uppercase())
            val relName = "rel".plus(parentNodeName.lowercase())
            return generateMatchCosmicGeneCypher(geneSymbol).plus(
                " CALL apoc.merge.relationship($nodename, '$relationship' ," +
                        " {}, {created: datetime()}," +
                        " $parentNodeName, {}) YIELD rel AS $relName \n"
            )
        }

        fun parseValueMap(value: Value): CosmicGeneCensus =
            CosmicGeneCensus(
                value["Gene Symbol"].asString(),
                value["Name"].asString(),
                value["Entrez GeneId"].asString(),
                value["Genome Location"].asString(),
                parseValidIntegerFromString(value["Tier"].asString()),
                value["Hallmark"].toString().isNotBlank(),
                value["Chr Band"].toString(),
                value["Somatic"].toString().isNotBlank(),
                value["Germline"].toString().isNotBlank(),
                processTumorTypes(value["Tumour Types(Somatic)"].asString()),
                processTumorTypes(value["Tumour Types(Germline)"].asString()),
                value["Cancer Syndrome"].asString(),
                parseStringOnComma(value["Tissue Type"].asString()),
                value["Molecular Genetics"].toString(),
                parseStringOnComma(value["Role in Cancer"].asString()),
                parseStringOnComma(value["Mutation Types"].asString()),
                parseStringOnComma(value["Translocation Partner"].asString()),
                value["Other Germline Mut"].asString(),
                parseStringOnSemiColon(value["Other Syndrome"].asString()),
                value["COSMIC ID"].asString(),
                value["cosmic gene name"].asString(),
                parseStringOnComma(value["Synonyms"].asString())
            )

        /*
    Function to resolve a tumor type abbreviations
     */
        private fun processTumorTypes(tumorTypes: String): List<String> {
            val tumorTypeList = mutableListOf<String>()
            parseStringOnComma(tumorTypes).forEach {
                tumorTypeList.add(TumorTypeService.resolveTumorType(it))
            }
            return tumorTypeList.toList()  // make List immutable
        }
    }
}

