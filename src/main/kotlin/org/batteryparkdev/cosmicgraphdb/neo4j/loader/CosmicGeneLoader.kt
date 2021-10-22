package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.io.CsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicGeneDao.loadCosmicGeneNode
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicGeneDao.loadMutationTypeAnnotations
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicGeneDao.loadOtherSyndromeAnnotations
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicGeneDao.loadRoleInCancerAnnotations
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicGeneDao.loadSynonymAnnotations
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicGeneDao.loadTissueTypeAnnotations
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicGeneDao.loadTranslocPartnerList
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicGeneDao.loadTumorList
import java.nio.file.Paths

/*
Responsible for creating/merging a CosmicGene node and associated annotation nodes
 */
object CosmicGeneLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

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