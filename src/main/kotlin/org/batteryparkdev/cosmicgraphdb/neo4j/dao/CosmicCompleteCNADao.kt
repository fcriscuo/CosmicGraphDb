package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicCompleteCNA
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicGeneDao.createCosmicGeneNode

private val logger: FluentLogger = FluentLogger.forEnclosingClass()

fun loadCosmicCompleteCNA(cna: CosmicCompleteCNA): String =
    Neo4jConnectionService.executeCypherCommand(
        "MERGE (cna:CosmicCompleteCNA{cnv_id: ${cna.cnvId}}) " +
                " SET cna +={ sample_name: \"${cna.sampleName}\", total_cn: ${cna.totalCn}, " +
                " minor_allele: \"${cna.minorAllele}\", mutation_type: \"${cna.mutationType}\"," +
                " study_id: ${cna.studyId}, grch: \"${cna.grch}\", " +
                " chromosome_start_stop: \"${cna.chromosomeStartStop}\" }" +
                " RETURN cna.cnv_id"
    )

fun createRelationshipToGene(cnvId: Int, geneSymbol: String) {
    // create basic CosmicGene node if necessary
    createCosmicGeneNode(geneSymbol)
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cna:CosmicCompleteCNA), (cg:CosmicGene) WHERE" +
                " cna.cnvId = $cnvId AND cg.gene_symbol = \"$geneSymbol\" " +
                " MERGE (cna) -[r:HAS_GENE]->(cg)"
    )
}

fun createRelationshipFromTumor(cnvId: Int, tumorId: Int) {
    createCosmicTumorNode(tumorId)
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cna:CosmicCompleteCNA), (ct:CosmicTumor) WHERE " +
                " cna.cnvId = $cnvId AND ct.tumor_id = $tumorId " +
                " MERGE (ct) -[r:HAS_CNA] -> (cna)"
    )
}

fun createRelationshipFromSample(cnvId: Int, sampleId: Int) {
    createCosmicSampleNode(sampleId)
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cna:CosmicCompleteCNA), (cs:CosmicSample) WHERE " +
                " cna.cnvId = $cnvId AND cs.sample_id = $sampleId " +
                " MERGE (cs) -[r:HAS_CNA] -> (cna)"
    )
}


