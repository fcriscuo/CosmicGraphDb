package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.io.CsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import org.batteryparkdev.cosmicgraphdb.service.TumorTypeService
import java.nio.file.Paths

/*
Responsible for creating/merging a CosmicGene node and associated annotation nodes
 */
object CosmicGeneLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();
    private const val cypherLoadTemplate = "MERGE (cg:CosmicGene{gene_symbol: \"GENESYMBOL\" }) " +
            "SET cg.gene_name = \"GENENAME\", cg.entrez_gene_id = \"ENTREZ\", cg.genome_location = \"LOCATION\"," +
            "cg.tier = TIER, cg.hallmark = HALLMARK, cg.chromosome_band = \"CHRBAND\", cg.somatic = SOMATIC, " +
            "cg.germline = GERMLINE, cg.cancer_syndrome = \"CANCERSYN\", cg.molecular_genetics = \"MOLGEN\", " +
            "cg.other_germline_mut = \"OTHERMUT\"  RETURN cg.gene_symbol"

    fun processCosmicGeneNode(cosmicGene: CosmicGeneCensus) {
        loadCosmicGeneNode(cosmicGene)
        loadTumorList(cosmicGene.geneSymbol, cosmicGene.somaticTumorTypeList, "Somatic")
        loadTumorList(cosmicGene.geneSymbol, cosmicGene.germlineTumorTypeList, "Germline")
        loadSynonymAnnotations(cosmicGene.geneSymbol, cosmicGene.synonymList)
        loadRoleInCancerAnnotations(cosmicGene.geneSymbol, cosmicGene.roleInCancerList)
        loadMutationTypeAnnotations(cosmicGene.geneSymbol, cosmicGene.mutationTypeList)
        loadTissueTypeAnnotations(cosmicGene.geneSymbol, cosmicGene.tissueTypeList)
        loadOtherSyndromeAnnotations(cosmicGene.geneSymbol, cosmicGene.otherSyndromeList)
        loadTranslocPartnerList(cosmicGene.geneSymbol, cosmicGene.translocationPartnerList)
        logger.atInfo().log("Completed loading CosmicGene: ${cosmicGene.geneSymbol} into Neo4j database")
    }

    private fun loadTranslocPartnerList(geneSymbol: String, transPartnerList: List<String>) {
        transPartnerList.forEach { trans ->
            run {
                Neo4jConnectionService.executeCypherCommand("MERGE (ca:CosmicAnnotation{annotation_value: \"$trans\"})")
                addAnnotationLabel(trans, "TranslocationPartner")
                completeBasicRelationship(geneSymbol, trans, "HAS_TRANSLOCATION_PARTNER")
            }
        }
    }

    private fun loadOtherSyndromeAnnotations(geneSymbol: String, otherSyndromeList: List<String>) {
        otherSyndromeList.forEach { syn ->
            run {
                Neo4jConnectionService.executeCypherCommand("MERGE (ca:CosmicAnnotation{annotation_value: \"$syn\"})")
                addAnnotationLabel(syn, "OtherSyndrome")
                completeBasicRelationship(geneSymbol, syn, "HAS_OTHER_SYNDROME")
            }
        }
    }

    private fun loadTissueTypeAnnotations(geneSymbol: String, tissueTypeList: List<String>) {
        tissueTypeList.forEach { tissue ->
            run {
                Neo4jConnectionService.executeCypherCommand("MERGE (ca:CosmicAnnotation{annotation_value: \"$tissue\"})")
                addAnnotationLabel(tissue, "TissueType")
                completeBasicRelationship(geneSymbol, tissue, "HAS_TISSUE_TYPE")
            }
        }
    }

    private fun loadMutationTypeAnnotations(geneSymbol: String, mutationTypeList: List<String>) {
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

    private fun loadRoleInCancerAnnotations(geneSymbol: String, roleList: List<String>) {
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

    private fun completeBasicRelationship(
        geneSymbol: String, annotationValue: String,
        relationshipName: String
    ) {
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (cg:CosmicGene), (ca:CosmicAnnotation) WHERE cg.gene_symbol = \"$geneSymbol\" " +
                    " AND ca.annotation_value = \"$annotationValue\" MERGE (cg) -" +
                    "[r: ${relationshipName.uppercase()}] ->(ca) "
        )
    }

    private fun addAnnotationLabel(value: String, label: String) {
        val labelExistsQuery = "MERGE (ca:CosmicAnnotation{annotation_value:\"$value\"}) " +
                "RETURN apoc.label.exists(ca, \"$label\") AS output;"
        if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
            Neo4jConnectionService.executeCypherCommand(
                "MATCH (ca:CosmicAnnotation{annotation_value:\"$value\"}) " +
                        "CALL apoc.create.addLabels(ca,[\"$label\"]) YIELD node RETURN node"
            )
        }
    }

    private fun loadSynonymAnnotations(geneSymbol: String, synonymList: List<String>) {
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

    private fun loadCosmicGeneNode(cosmicGene: CosmicGeneCensus): String {
        if (!cancerGeneLoaded(cosmicGene.geneSymbol)) {
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
        return "CosmicGene: ${cosmicGene.cancerSyndrome} has already been loaded"
    }

    private fun loadTumorList(geneSymbol: String, tumorList: List<String>, tumorType: String) {
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
    Function to determine if a CancerGene node for a specified gene
    has already been loaded
     */
    fun cancerGeneLoaded(geneSymbol: String): Boolean =
        Neo4jUtils.nodeLoadedPredicate(
            "OPTIONAL MATCH (cg:CosmicGene{gene_symbol: \"$geneSymbol\" }) " +
                    " RETURN cg IS NOT NULL AS PREDICATE"
        )
}
/*
Test loading sample CosmicCensusGene file
n.b. This is a destructive test: all existing CosmicGene and CosmicAnnotation nodes & relationships
     are deleted
 */
fun main() {
    val path = Paths.get("./data/cancer_gene_census.csv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    CsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicGeneCensus.parseCsvRecord(it) }
                .forEach {
                    CosmicGeneLoader.processCosmicGeneNode(it)
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}