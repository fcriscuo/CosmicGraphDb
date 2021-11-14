package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicMutationGene
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import org.batteryparkdev.cosmicgraphdb.neo4j.loader.CosmicGeneCensusLoader
import org.batteryparkdev.cosmicgraphdb.service.TumorTypeService

object CosmicGeneDao {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    private const val cypherLoadTemplate = "MERGE (cg:CosmicGene{gene_symbol: \"GENESYMBOL\" }) " +
            "SET cg.gene_name = \"GENENAME\", cg.entrez_gene_id = \"ENTREZ\", cg.genome_location = \"LOCATION\"," +
            "cg.tier = TIER, cg.hallmark = HALLMARK, cg.chromosome_band = \"CHRBAND\", cg.somatic = SOMATIC, " +
            "cg.germline = GERMLINE, cg.cancer_syndrome = \"CANCERSYN\", cg.molecular_genetics = \"MOLGEN\", " +
            "cg.other_germline_mut = \"OTHERMUT\"  RETURN cg.gene_symbol"

    fun loadCosmicGeneNode(cosmicGene: CosmicGeneCensus): String {
            val merge = cypherLoadTemplate.replace("GENESYMBOL", cosmicGene.geneSymbol)
                .replace("GENENAME", cosmicGene.geneName)
                .replace("ENTREZ", cosmicGene.entrezGeneId)
                .replace("LOCATION", cosmicGene.genomeLocation)
                .replace("TIER", cosmicGene.tier.toString())
                .replace("HALLMARK", cosmicGene.hallmark.toString())
                .replace("CHRBAND", cosmicGene.chromosomeBand)
                .replace("SOMATIC", cosmicGene.somatic.toString())
                .replace("GERMLINE", cosmicGene.germline.toString())
                .replace("CANCERSYN", cosmicGene.cancerSyndrome)
                .replace("MOLGEN", cosmicGene.molecularGenetics)
                .replace("OTHERMUT", cosmicGene.otherGermlineMut)
            return Neo4jConnectionService.executeCypherCommand(merge)
    }

    fun addGeneCensusLabel(geneSymbol: String): String {
        val label = "CensusGene"
        // confirm that label is novel
        val labelExistsQuery = "MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
                "RETURN apoc.label.exists(cg, \"$label\") AS output;"
        val addLabelCypher = "MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
                " CALL apoc.create.addLabels(pma, [\"$label\"] ) yield node return node"
        if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
            return Neo4jConnectionService.executeCypherCommand(addLabelCypher)
        }
        // logger.atWarning().log("CosmicGene $geneSymbol already has label $label")
        return ""
    }

    fun loadTranslocPartnerList(geneSymbol: String, transPartnerList: List<String>) {
        transPartnerList.forEach { trans ->
            run {
                Neo4jConnectionService.executeCypherCommand("MERGE (ca:CosmicAnnotation{annotation_value: \"$trans\"})")
                addAnnotationLabel(trans, "TranslocationPartner")
                completeBasicRelationship(geneSymbol, trans, "HAS_TRANSLOCATION_PARTNER")
            }
        }
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