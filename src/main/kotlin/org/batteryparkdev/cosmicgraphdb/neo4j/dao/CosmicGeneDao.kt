package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicMutationGene
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import org.batteryparkdev.cosmicgraphdb.neo4j.loader.CosmicGeneCensusLoader
import org.batteryparkdev.cosmicgraphdb.pubmed.dao.PubMedArticleDao
import org.batteryparkdev.cosmicgraphdb.service.TumorTypeService
import java.util.*
import org.batteryparkdev.cosmicgraphdb.pubmed.model.PubMedIdentifier as PubMedIdentifier1

object CosmicGeneDao {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    private const val cypherLoadTemplate = "MERGE (cg:CosmicGene{gene_symbol: GENESYMBOL }) " +
            "SET cg += {gene_name: GENENAME, entrez_gene_id: ENTREZ, genome_location: LOCATION," +
            " tier: TIER, hallmark: HALLMARK, chromosome_band: CHRBAND, somatic: SOMATIC, " +
            " germline: GERMLINE, cancer_syndrome: CANCERSYN, molecular_genetics: MOLGEN, " +
            " other_germline_mut: OTHERMUT }" +
            " RETURN cg.gene_symbol"


    fun loadCosmicGeneNode(cosmicGene: CosmicGeneCensus): String {
        val merge = cypherLoadTemplate.replace(
            "GENESYMBOL",
            Neo4jUtils.formatQuotedString(cosmicGene.geneSymbol))
            .replace("GENENAME", Neo4jUtils.formatQuotedString(cosmicGene.geneName))
            .replace("ENTREZ", Neo4jUtils.formatQuotedString(cosmicGene.entrezGeneId))
            .replace("LOCATION", Neo4jUtils.formatQuotedString(cosmicGene.genomeLocation))
            .replace("TIER", cosmicGene.tier.toString())
            .replace("HALLMARK", cosmicGene.hallmark.toString())
            .replace("CHRBAND", Neo4jUtils.formatQuotedString(cosmicGene.chromosomeBand))
            .replace("SOMATIC", cosmicGene.somatic.toString())
            .replace("GERMLINE", cosmicGene.germline.toString())
            .replace("CANCERSYN", Neo4jUtils.formatQuotedString(cosmicGene.cancerSyndrome))
            .replace("MOLGEN", Neo4jUtils.formatQuotedString(cosmicGene.molecularGenetics))
            .replace("OTHERMUT", Neo4jUtils.formatQuotedString(cosmicGene.otherGermlineMut))
        return Neo4jConnectionService.executeCypherCommand(merge)
    }

    fun addGeneCensusLabel(geneSymbol: String): String {
        val label = "CensusGene"
        // add CensusGene label if novel to node
        val labelCypher = "MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
                " WHERE apoc.label.exists(cg,\"$label\")  = false " +
                "    CALL apoc.create.addLabels(cg, [\"$label\"] ) yield node return node"
        return Neo4jConnectionService.executeCypherCommand(labelCypher)
//        val labelExistsQuery = "MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
//                "RETURN apoc.label.exists(cg, \"$label\") AS output;"
//        val addLabelCypher = "MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
//                " CALL apoc.create.addLabels(pma, [\"$label\"] ) yield node return node"
//        if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
//            return Neo4jConnectionService.executeCypherCommand(addLabelCypher)
//        }
        // logger.atWarning().log("CosmicGene $geneSymbol already has label $label")
        //return ""
    }

    /*
    * Function to determine if a CosmicGene has been loaded into the database
    *
    * */
    fun cosmicGeneNodeExistsPredicate(geneSymbol: String): Boolean {
        val cypher = "OPTIONAL MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
                " RETURN cg IS NOT NULL AS Predicate"
        try {
            val predicate = Neo4jConnectionService.executeCypherCommand(cypher)
            when (predicate.lowercase(Locale.getDefault())) {
                "true" -> return true
                "false" -> return false
            }
        } catch (e: Exception) {
            logger.atSevere().log(e.message.toString())
            return false
        }
        return false
    }

    /*
    Function to create a placeholder CosmicGene with just the gene_symbol property
    This is used to establish a translocation partner relationship between two (2)
    CosmicGene nodes before the second one has been loaded form the gene census file
     */

    private fun createCosmicGenePlaceholderNode(geneSymbol: String) {
        Neo4jConnectionService.executeCypherCommand(
            "MERGE (cg:CosmicGene{ gene_symbol: " +
                    "\"$geneSymbol\"}) "
        )
    }

    /*
    Function to create a bidirectional relationship between two CosmicGene nodes
    If a CosmicGene for a target doesn't exist, create a placeholder node
    */
    fun loadTranslocPartnerList(geneSymbol: String, transPartnerList: List<String>) {
        addTranslocatonLabel(geneSymbol)
        transPartnerList.forEach { trans ->
            run {
                // if the target gene hasn't been loaded yet, create
                // a placeholder
                if (cosmicGeneNodeExistsPredicate(trans).not()) {
                    createCosmicGenePlaceholderNode(trans)
                }
//                create bi-directional relationship between CosmicGene nodes
                Neo4jConnectionService.executeCypherCommand(
                    "MATCH (cg1:CosmicGene), (cg2:CosmicGene) " +
                            "WHERE cg1.gene_symbol = \"$geneSymbol\" AND cg2.gene_symbol = \"$trans\" " +
                            " MERGE (cg1) - [r:HAS_TRANSLOCATION_PARTNER] - (cg2) "
                )
//                add translocation label
                addTranslocatonLabel(trans)
            }
        }
    }

    private fun addTranslocatonLabel(geneSymbol: String): String {
        // confirm that label is novel
        val label = "TranslocationPartner"
        val labelExistsQuery = "MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\"}) " +
                "RETURN apoc.label.exists(cg ,\"$label\") AS output;"
        val addLabelCypher = "MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\"}) " +
                " CALL apoc.create.addLabels(cg, [\"$label\"] ) yield node return node"
        if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
            return Neo4jConnectionService.executeCypherCommand(addLabelCypher)
        }
        return ""
    }

    fun loadOtherSyndromeAnnotations(geneSymbol: String, otherSyndromeList: List<String>) {
        otherSyndromeList.forEach { syn ->
            run {
                Neo4jConnectionService.executeCypherCommand("MERGE (ca:CosmicAnnotation{annotation_value: \"$syn\"})")
                addAnnotationLabel(syn, "OtherSyndrome")
                completeBasicRelationship(geneSymbol, syn, "HAS_OTHER_SYNDROME")
            }
        }
    }

    fun loadTissueTypeAnnotations(geneSymbol: String, tissueTypeList: List<String>) {
        tissueTypeList.forEach { tissue ->
            run {
                Neo4jConnectionService.executeCypherCommand("MERGE (ca:CosmicAnnotation{annotation_value: \"$tissue\"})")
                addAnnotationLabel(tissue, "TissueType")
                completeBasicRelationship(geneSymbol, tissue, "HAS_TISSUE_TYPE")
            }
        }
    }

    fun loadMutationTypeAnnotations(geneSymbol: String, mutationTypeList: List<String>) {
        mutationTypeList.forEach { mut ->
            run {
                Neo4jConnectionService.executeCypherCommand("MERGE (ca:CosmicAnnotation{annotation_value: \"$mut\"})")
                // add MutationType label if novel
                addAnnotationLabel(mut, "MutationType")
                // CosmicGene -> CosmicAnnotation
                completeBasicRelationship(geneSymbol, mut, "HAS_MUTATION_TYPE")
            }
        }
    }

    fun loadRoleInCancerAnnotations(geneSymbol: String, roleList: List<String>) {
        roleList.forEach { role ->
            run {
                Neo4jConnectionService.executeCypherCommand("MERGE (ca:CosmicAnnotation{annotation_value: \"$role\"})")
                // add RoleInCancer label if novel
                addAnnotationLabel(role, "RoleInCancer")
                // CosmicGene -> CosmicAnnotation
                completeBasicRelationship(geneSymbol, role, "HAS_ROLE_IN_CANCER")
            }
        }
    }

    fun addAnnotationLabel(value: String, label: String) {
        val labelExistsQuery = "MERGE (ca:CosmicAnnotation{annotation_value:\"$value\"}) " +
                "RETURN apoc.label.exists(ca, \"$label\") AS output;"
        if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
            Neo4jConnectionService.executeCypherCommand(
                "MATCH (ca:CosmicAnnotation{annotation_value:\"$value\"}) " +
                        "CALL apoc.create.addLabels(ca,[\"$label\"]) YIELD node RETURN node"
            )
        }
    }

    fun loadSynonymAnnotations(geneSymbol: String, synonymList: List<String>) {
        synonymList.forEach { syn ->
            run {
                Neo4jConnectionService.executeCypherCommand("MERGE (ca:CosmicAnnotation{annotation_value: \"$syn\"})")
                // add Synonym label if novel
                val labelExistsQuery = "MERGE (ca:CosmicAnnotation{annotation_value:\"$syn\"}) " +
                        "RETURN apoc.label.exists(ca, \"Synonym\") AS output;"
                if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
                    Neo4jConnectionService.executeCypherCommand(
                        "MATCH (ca:CosmicAnnotation{annotation_value:\"$syn\"}) " +
                                " CALL apoc.create.addLabels(ca,[\"Synonym\"]) YIELD node RETURN node"
                    )
                }
                // CosmicGene -> CosmicAnnotation
                Neo4jConnectionService.executeCypherCommand(
                    "MATCH (cg:CosmicGene), (ca:CosmicAnnotation) WHERE cg.gene_symbol = \"$geneSymbol\" " +
                            " AND ca.annotation_value = \"$syn\" MERGE (cg) -" +
                            "[r: HAS_SYNONYM] ->(ca) "
                )
            }
        }
    }

    fun loadTumorList(geneSymbol: String, tumorList: List<String>, tumorType: String) {
        tumorList.map { tt -> TumorTypeService.resolveTumorType(tt) }
            .filter { tt -> tt.isNotEmpty() }
            .forEach { tt ->
                run {
                    Neo4jConnectionService.executeCypherCommand(
                        "MERGE (ca:CosmicAnnotation{annotation_value: \"$tt\"})"
                    )
                    // add TumorType label if novel
                    val labelExistsQuery = "MERGE (ca:CosmicAnnotation{annotation_value:\"$tt\"})" +
                            "RETURN apoc.label.exists(ca, \"TumorType\") AS output;"
                    if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
                        Neo4jConnectionService.executeCypherCommand(
                            "MATCH (ca:CosmicAnnotation{annotation_value:\"$tt\"} )" +
                                    "CALL apoc.create.addLabels(ca,[\"TumorType\"]) YIELD node RETURN node"
                        )
                    }
                    // create CosmicGene -> CosmicAnnotation
                    Neo4jConnectionService.executeCypherCommand(
                        "MATCH (cg:CosmicGene), (ca:CosmicAnnotation) WHERE cg.gene_symbol = \"$geneSymbol\" " +
                                " AND ca.annotation_value = \"$tt\" MERGE (cg) -" +
                                "[r: HAS_TUMOR_TYPE {type: \"$tumorType\"}] ->(ca) "
                    )
                }
            }
    }

    /*
  The CosmicMutantExport & CosmicMutantExportCensus files contain some
  additional gene properties and identifiers
  Use this function to merge these data into CosmicGene nodes
  Also, allowing this function to create new CosmicGene nodes, removes
  the necessity of loading genes before loading mutations
     */
    fun loadCosmicMutationGene(mutGene: CosmicMutationGene) {
        //createCosmicGeneNode(mutGene.geneSymbol)
        Neo4jConnectionService.executeCypherCommand(
            "MERGE (cg:CosmicGene{gene_symbol: \"${mutGene.geneSymbol}\" }) " +
                    "SET cg.accession_number = \"${mutGene.accessionNumber}\", " +
                    " cg.cds_length =${mutGene.cdsLength}, cg.hgnc_id = \"${mutGene.hgncId}\" "
        )
    }

    /*
    Function to create a basic CosmicGene node as a placeholder
    for subsequent completion if necessary
     */
    fun createCosmicGeneNode(geneSymbol: String): String {
        return when (cancerGeneSymbolLoaded(geneSymbol)) {
            true -> geneSymbol
            false -> Neo4jConnectionService.executeCypherCommand(
                "MERGE (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
                        " RETURN  cg.gene_symbol"
            )
        }
    }
}

fun completeBasicRelationship(
    geneSymbol: String, annotationValue: String,
    relationshipName: String
) {
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cg:CosmicGene), (ca:CosmicAnnotation) WHERE cg.gene_symbol = \"$geneSymbol\" " +
                " AND ca.annotation_value = \"$annotationValue\" MERGE (cg) -" +
                "[r: ${relationshipName.uppercase()}] ->(ca) "
    )
}

/*
Function to determine if a CancerGene node for a specified gene name
has already been loaded
*/
fun cancerGeneNameLoaded(geneSymbol: String): Boolean =
    Neo4jUtils.nodeLoadedPredicate(
        "OPTIONAL MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
                " RETURN cg IS NOT NULL AS PREDICATE"
    )

/*
Function to determine if CosmicGene node exists
 */
fun cancerGeneSymbolLoaded(geneSymbol: String): Boolean =
    Neo4jUtils.nodeLoadedPredicate(
        "OPTIONAL MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
                " RETURN cg IS NOT NULL AS PREDICATE"
    )