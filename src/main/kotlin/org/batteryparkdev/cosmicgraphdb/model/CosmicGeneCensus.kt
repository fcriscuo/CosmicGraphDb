package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.service.TumorTypeService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier

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

    override fun isValid(): Boolean = geneSymbol.isNotEmpty()
    override fun getPubMedId(): Int = 0

    override fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
            .plus(generateGeneMutationCollectionNodeCypher())
            .plus(generateGeneAnnotationCollectionCypher())
            .plus(generateHgncRelationshipCypher())
            .plus(generateEntrezRelationshipCypher())
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
   /*
   Create a relationship to an Hgnc node loaded into the database as part of the GenomicGraphCore
    */
    private fun generateHgncRelationshipCypher(): String =
        " MATCH (cg:CosmicGene), (h:Hgnc)  WHERE " +
                " cg.gene_symbol = \"${geneSymbol}\" AND " +
                " h.ggene_symbol = \"${geneSymbol}\" " +
                " CREATE (cg) -[r: HAS_HGNC] -> (h) YIELD r AS hgnc_rel \n"

    /*
 Create a relationship to an Entrez node loaded into the database as part of the GenomicGraphCore
  */
    private fun generateEntrezRelationshipCypher(): String =
        when (entrezGeneId.toInt() > 0) {
            true -> "MERGE (e:Entrez{ entrez_id: ${entrezGeneId}} )  " +
                    " WITH e " +
                    " MATCH (cg:CosmicGene{gene_symbol: \"${geneSymbol}\"} ) " +
                    " CREATE (cg) -[r: HAS_ENTREZ] -> (e) YIELD r AS entrez_rel \n"
            false -> " "
        }

    private fun generateGeneMutationCollectionNodeCypher(): String =
        "CALL apoc.merge.node([\"GeneMutationCollection\"], " +
                " {gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}," +
                " {created: datetime()},{}) YIELD node as ${CosmicGeneCensus.mutCollNodename} \n " +
                " CALL apoc.merge.relationship ($nodename, \"HAS_MUTATION_COLLECTION\", " +
                "  {},{created: datetime()}, ${CosmicGeneCensus.mutCollNodename},{} ) YIELD rel AS mut_rel \n "

    private fun generateGeneAnnotationCollectionCypher(): String =
        "CALL apoc.merge.node([\"GeneAnnotationCollection\"], " +
                " {gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}," +
                " {created: datetime()},{}) YIELD node as $annoCollNodename \n " +
                " CALL apoc.merge.relationship ($nodename, \"HAS_ANNOTATION_COLLECTION\", " +
                "  {},{created: datetime()}, $annoCollNodename,{} ) YIELD rel AS anno_rel \n "


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
                    " molecular_genetics: ${Neo4jUtils.formatPropertyValue(molecularGenetics)}, " +
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
                    " molecular_genetics: ${Neo4jUtils.formatPropertyValue(molecularGenetics)}, " +
                    " other_germline_mut: ${Neo4jUtils.formatPropertyValue(otherGermlineMut)}," +
                    " cosmic_id: ${Neo4jUtils.formatPropertyValue(cosmicId)}, " +
                    "cosmic_gene_name: ${Neo4jUtils.formatPropertyValue(cosmicGeneName)}, " +
                    "  created: datetime()},{}) YIELD node as $nodename \n"
        }

    companion object : AbstractModel {

        const val nodename = "gene"
        const val mutCollNodename = "gene_mut_collection"
        const val annoCollNodename = "gene_anno_collection"

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

        fun parseCSVRecord(record: CSVRecord): CosmicGeneCensus =
            CosmicGeneCensus(
                record.get("Gene Symbol"),
                record.get("Name"),
                record.get("Entrez GeneId"),
                record.get("Genome Location"),
                parseValidIntegerFromString(record.get("Tier")),
                record.get("Hallmark").isNotBlank(),
                record.get("Chr Band"),
                record.get("Somatic").isNotBlank(),
                record.get("Germline").isNotBlank(),
                processTumorTypes(record.get("Tumour Types(Somatic)")),
                processTumorTypes(record.get("Tumour Types(Germline)")),
                record.get("Cancer Syndrome"),
                parseStringOnComma(record.get("Tissue Type")),
                record.get("Molecular Genetics"),
                parseStringOnComma(record.get("Role in Cancer")),
                parseStringOnComma(record.get("Mutation Types")),
                parseStringOnComma(record.get("Translocation Partner")),
                record.get("Other Germline Mut"),
                parseStringOnSemiColon(record.get("Other Syndrome")),
                record.get("COSMIC ID"),
                record.get("cosmic gene name"),
                parseStringOnComma(record.get("Synonyms"))
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

