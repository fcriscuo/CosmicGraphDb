package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.lang3.RandomStringUtils
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.batteryparkdev.placeholder.loader.PubMedPlaceholderNodeLoader

interface CosmicModel {
    abstract fun getNodeIdentifier(): NodeIdentifier

    abstract fun generateLoadCosmicModelCypher(): String

    abstract fun isValid(): Boolean

    abstract fun getPubMedId(): Int


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
n.b. It's possible to establish >1 gene-mutation relationship in the same Cypher transaction,
     so the relationship name must be unique
*/
    fun generateGeneMutationCollectionRelationshipCypher(geneSymbol: String, nodename:String): String {
        val relationship = "HAS_".plus(nodename.uppercase())
        val suffix = RandomStringUtils.randomAlphanumeric(6).lowercase()
        val gene_rel_name =  "gene_mut_rel_".plus(suffix)
        val gene_coll_name = "gene_mut_coll_".plus(suffix)
        return "CALL apoc.merge.node([\"GeneMutationCollection\"], " +
                " {gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}, " +
                "{},{} ) YIELD node AS $gene_coll_name \n " +
                " CALL apoc.merge.relationship( $gene_coll_name, '$relationship', " +
                " {}, {}, $nodename) YIELD rel AS $gene_rel_name \n"
    }

     fun generateSampleMutationCollectionRelationshipCypher(sampleId: Int, nodename:String): String {
        val relationship = "HAS_".plus(nodename.uppercase())
       return  "CALL apoc.merge.node([\"SampleMutationCollection\"], " +
                " {sample_id: $sampleId}, " +
                "{},{} ) YIELD node AS  sample_mut_coll \n " +
                " CALL apoc.merge.relationship( sample_mut_coll, '$relationship', " +
                " {}, {}, $nodename) YIELD rel AS sample_mut_rel \n"
    }

}