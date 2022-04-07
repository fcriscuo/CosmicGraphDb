package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicCompleteGeneExpression
import org.batteryparkdev.neo4j.service.Neo4jConnectionService


/*
Function to load data for a CompleteGeneExpression object into a Neo4j node
 */


fun loadCosmicCompleteGeneExpression(geneExp: CosmicCompleteGeneExpression): String =
    Neo4jConnectionService.executeCypherCommand(
        " MERGE (cge:CosmicGeneExpression{" +
                " key: ${geneExp.key}  }) " +
                " SET cge += {sample_id: ${geneExp.sampleId}, " +
                " gene_symbol: \"${geneExp.geneSymbol}\", regulation: \"${geneExp.regulation}\", " +
                " z_score: ${geneExp.zScore}, study_id: ${geneExp.studyId} }" +
                " RETURN cge.key")

/*
Function to establish a CosmicGeneExpression -[HAS_GENE]->CosmicGene relationship
 */
fun createGeneExpressionToGeneRelationship(geneExp: CosmicCompleteGeneExpression) =
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cge:CosmicGeneExpression), (cg:CosmicGene) WHERE " +
                " cge.key = ${geneExp.key} AND " +
                " cg.gene_symbol = \"${geneExp.geneSymbol}\" " +
                " MERGE (cge) -[r:HAS_GENE] -> (cg)"
    )

/*
Function to create a CosmicSample -[HAS_Expression]-> CosmicGeneExpression relationship
 */
fun createGeneExpressionToSampleRelationship(geneExp: CosmicCompleteGeneExpression) =
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cge:CosmicGeneExpression), (cs:CosmicSample) WHERE " +
                " cge.key = ${geneExp.key} AND " +
                " cs.sample_id = ${geneExp.sampleId} " +
                " MERGE (cs) -[r:HAS_GENE_EXPRESSION] -> (cge)"
    )