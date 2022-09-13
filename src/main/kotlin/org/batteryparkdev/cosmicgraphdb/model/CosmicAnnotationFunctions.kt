package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils.nodeExistsPredicate
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

object CosmicAnnotationFunctions {

    fun generateAnnotationCypher(
        annotationList: List<String>, secondaryLabel: String,
        parentNodeName: String
    ): String {
        var cypher: String = ""
        var index = 0
        val relationship = "HAS_".plus(secondaryLabel.uppercase())
        annotationList.forEach { annon ->
            run {
                index += 1
                val annonName = secondaryLabel.plus(index.toString()).lowercase()
                val relName = "rel_".plus(annonName)
                cypher = cypher.plus(
                    " CALL apoc.merge.node( [\"CosmicAnnotation\"," +
                            " ${secondaryLabel.formatNeo4jPropertyValue()}]," +
                            " {annotation_value: ${annon.formatNeo4jPropertyValue()}}," +
                            " {created: datetime()}) YIELD node as $annonName \n" +
                            " CALL apoc.merge.relationship( $parentNodeName, '$relationship', " +
                            " {}, {created: datetime()}, " +
                            " $annonName, {} ) YIELD rel AS $relName \n"
                )
            }
        }
        return cypher
    }

    /*
    Private function to determine if two genes have already been
    paired as translocation partners
     */
    private fun translocationExists(gene1: String, gene2: String): Boolean
         = Neo4jConnectionService.executeCypherCommand(
            "RETURN EXISTS ((:CosmicGene{gene_symbol:${gene1.formatNeo4jPropertyValue()}}) " +
                    " -[:HAS_TRANSLOCATION_PARTNER]-> " +
                    " (:CosmicGene{gene_symbol:${gene2.formatNeo4jPropertyValue()}}) )"
        ).toBoolean()

    fun generateTranslocationCypher(geneSymbol: String, transPartnerList: List<String>): String {
        var index = 0
        val relationship = "HAS_TRANSLOCATION_PARTNER"
        var cypher: String = ""
        transPartnerList.filter { translocationExists(it,geneSymbol ).not() }
            .forEach { trans ->
            run {
                index += 1
                val relname = "translocation".plus(index.toString())
                val transName = "trans".plus(index.toString())
                val mergeCypher = when (nodeExistsPredicate(NodeIdentifier
                    ("CosmicGene", "gene_symbol", trans))){
                    true -> " CALL apoc.merge.node([\"CosmicGene\", \"CensusGene\"], " +
                            " {  gene_symbol: ${trans.formatNeo4jPropertyValue()}}, " +
                            " {},{}) YIELD node" +
                            " as $transName \n"
                    false -> " CALL apoc.merge.node([\"CosmicGene\", \"CensusGene\"], " +
                            " {  gene_symbol: ${trans.formatNeo4jPropertyValue()}}, " +
                            " {created: datetime()}) YIELD node" +
                            " as $transName \n"
                }

                cypher = cypher.plus(mergeCypher)
                    .plus(
                        " CALL apoc.merge.relationship(${CosmicGeneCensus.nodename}, " +
                                "'$relationship', {},  {created: datetime()}, " +
                                " $transName, {} ) YIELD rel as $relname \n"
                    )
            }
        }
        return cypher
    }
}