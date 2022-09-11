package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

interface CosmicModel {

    /*
    Generic function to create a HAS_PUBLICATION relationship between a node and a
    Publication/PubMed node
     */
    fun createPubMedRelationship() {
        val pubId = getPubMedId()
        if (pubId > 0) {
            val parentNodeId = getNodeIdentifier()
            val pubNodeId = NodeIdentifier("Publication","pub_id", pubId.toString(),"PubMed")
            NodeIdentifierDao.createPlaceholderNode(pubNodeId)
             RelationshipDefinition(parentNodeId,pubNodeId,"HAS_PUBLICATION").also {
                 NodeIdentifierDao.defineRelationship(it)
             }
        }
    }

}