package org.batteryparkdev.cosmicgraphdb.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.model.CosmicCompleteCNA
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneDao.createCosmicGeneNode
import org.batteryparkdev.cosmicgraphdb.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.model.CosmicTumor
import org.batteryparkdev.neo4j.service.Neo4jConnectionService

private val logger: FluentLogger = FluentLogger.forEnclosingClass()

fun loadCosmicCompleteCNA(cna: CosmicCompleteCNA): Int {
    val cypher = cna.generateCompleteCNACypher()
        .plus(cna.site.generateMergeCypher())
        .plus(cna.site.generateParentRelationshipCypher(cna.nodeName))
        .plus(cna.histology.generateMergeCypher())
        .plus(cna.histology.generateParentRelationshipCypher(cna.nodeName))
        .plus(cna.mutationType.generateMergeCypher())
        .plus(cna.mutationType.generateParentRelationshipCypher(cna.nodeName))
        .plus(CosmicGeneCensus.generateHasGeneRelationshipCypher(cna.geneSymbol, cna.nodeName))
        .plus(CosmicTumor.generateChildRelationshipCypher(cna.tumorId, cna.nodeName))
        .plus(" RETURN ${cna.nodeName}")
    println("CNA cypher: $cypher")
    val node = Neo4jConnectionService.executeCypherCommand(cypher)
    return cna.cnvId
}


fun createRelationshipToGene(cnvId: Int, geneSymbol: String) {
    // create basic CosmicGene node if necessary
    createCosmicGeneNode(geneSymbol)
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cna:CosmicCompleteCNA), (cg:CosmicGene) WHERE" +
                " cna.cnvId = $cnvId AND cg.gene_symbol = \"$geneSymbol\" " +
                " MERGE (cna) -[r:HAS_GENE]->(cg)"
    )
}

fun createRelationshipFromTumor(tumorId: Int): String {
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


