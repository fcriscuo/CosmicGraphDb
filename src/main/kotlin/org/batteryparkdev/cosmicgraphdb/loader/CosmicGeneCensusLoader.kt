package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.cosmicgraphdb.service.TumorTypeService
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import java.nio.file.Paths

/*
Responsible for creating/merging  CosmicGeneCensus nodes and associated annotation nodes
 */
object CosmicGeneCensusLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicGeneCensusFile(cosmicGeneCensusFile: String) =
        produce<CosmicGeneCensus> {
            val path = Paths.get(cosmicGeneCensusFile)
            ApocFileReader.processDelimitedFile(cosmicGeneCensusFile)
                .map { record -> record.get("map") }
                .map { CosmicGeneCensus.parseValueMap(it) }
                .forEach {
                    send(it)
                    delay(20L)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicGeneCensusData( genes: ReceiveChannel<CosmicGeneCensus>) =
    produce<CosmicGeneCensus> {
        for (gene in genes) {
            loadCosmicGeneNode(gene)
            send(gene)
            delay(20)
        }
    }

    private fun loadCosmicGeneNode(gene: CosmicGeneCensus):String =
        Neo4jConnectionService.executeCypherCommand(gene.generateCosmicGeneCypher())

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadSomaticTumors(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus>{
            for (gene in genes) {
                loadTumorList(gene.geneSymbol, gene.somaticTumorTypeList, "Somatic")
                send(gene)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadGermlineTumors(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus>{
            for (gene in genes) {
                loadTumorList(gene.geneSymbol, gene.germlineTumorTypeList, "GermLine")
                send(gene)
                delay(20)
            }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processSynonymAnnotations(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus> {
            for (gene in genes){
                loadSynonymAnnotations(gene.geneSymbol, gene.synonymList)
                send(gene)
                delay(20)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processRoleInCancerAnnotations(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus> {
            for (gene in genes) {
                loadRoleInCancerAnnotations(gene.geneSymbol, gene.roleInCancerList)
                send(gene)
                delay(20)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processMutationTypeAnnotations(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus> {
            for (gene in genes) {
                loadMutationTypeAnnotations(gene.geneSymbol, gene.mutationTypeList)
                send(gene)
                delay(20)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processTissueTypeAnnotations(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus> {
            for (gene in genes) {
                loadTissueTypeAnnotations(gene.geneSymbol, gene.tissueTypeList)
                send(gene)
                delay(20)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processOtherSyndromeAnnotations(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus> {
            for (gene in genes) {
                loadOtherSyndromeAnnotations(gene.geneSymbol, gene.otherSyndromeList)
                send(gene)
                delay(20)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processTranslocPartnerList(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<String> {
            for (gene in genes) {
                loadTranslocPartnerList(gene.geneSymbol,gene.translocationPartnerList)
                send(gene.geneSymbol)
                delay(20)
            }
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
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.addCensusLabel( geneSymbols: ReceiveChannel<String>) =
        produce<String> {
            for (geneSymbol in geneSymbols) {
                addGeneCensusLabel(geneSymbol)
                send(geneSymbol)
                delay(10)
            }
        }
    private fun addGeneCensusLabel(geneSymbol: String): String {
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
Public function load CosmicGeneCensus nodes and associated annotations
 */
    fun loadCosmicGeneCensusData(filename: String) =  runBlocking {
        logger.atInfo().log("Loading CosmicGeneCensus data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val geneSymbols = addCensusLabel(
            processTranslocPartnerList(
            processOtherSyndromeAnnotations(
                processTissueTypeAnnotations(
                    processMutationTypeAnnotations(
                        processRoleInCancerAnnotations(
                            (
                                loadGermlineTumors(
                                    loadSomaticTumors(
                                       loadCosmicGeneCensusData(
                                           parseCosmicGeneCensusFile(filename)
                                       ))))))))))
        for (symbol in geneSymbols) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicGeneCensus data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )

    }
}
