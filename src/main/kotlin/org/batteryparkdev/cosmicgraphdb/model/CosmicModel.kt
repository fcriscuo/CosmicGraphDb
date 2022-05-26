package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.batteryparkdev.placeholder.loader.PubMedPlaceholderNodeLoader

interface CosmicModel {
    abstract fun getNodeIdentifier(): NodeIdentifier
    /*
    Generic function to create a HAS_PUBLICATION relationship between a node and a
    Publication/PubMed node
     */
    fun createPubMedRelationship( pubId:Int) {
        if (pubId > 0) {
            val nodeId = getNodeIdentifier()
            val pub = PubMedPlaceholderNodeLoader(pubId.toString(), nodeId.idValue,
                nodeId.primaryLabel, nodeId.idProperty
            )
            pub.registerPubMedPublication()
        }
    }
    /*
Function to generate Cypher commands to create a
GeneMutationCollection - [HAS_MUTATION] -> specific Mutation relationship
*/
    fun generateGeneMutationCollectionRelationshipCypher(geneSymbol: String, nodename:String): String {
        val relationship = "HAS_".plus(nodename.uppercase()).plus("_MUTATION")
        return "CALL apoc.merge.node([\"GeneMutationCollection\"], " +
                " {gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}, " +
                "{},{} ) YIELD node AS  gene_mut_coll \n " +
                " CALL apoc.merge.relationship( gene_mut_coll, '$relationship', " +
                " {}, {}, $nodename) YIELD rel AS gene_mut_rel \n"
    }

     fun generateSampleMutationCollectionRelationshipCypher(sampleId: Int, nodename:String): String {
        val relationship = "HAS_".plus(nodename.uppercase()).plus("_MUTATION")
       return  "CALL apoc.merge.node([\"SampleMutationCollection\"], " +
                " {sample_id: $sampleId}, " +
                "{},{} ) YIELD node AS  sample_mut_coll \n " +
                " CALL apoc.merge.relationship( sample_mut_coll, '$relationship', " +
                " {}, {}, $nodename) YIELD rel AS sample__mut_rel \n"
    }

}