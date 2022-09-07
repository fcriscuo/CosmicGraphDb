package org.batteryparkdev.cosmicgraphdb.dao

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.model.CosmicType
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipProperty
import org.neo4j.driver.Value

fun resolveMutationType(value: Value): CosmicType =
    CosmicType(
        "Mutation", value["MUT_TYPE"].asString()
    )

fun resolveMutationType(record: CSVRecord): CosmicType =
    CosmicType(
        "Mutation", record.get("MUT_TYPE"))

/*
All Mutation nodes have a relationship to a SampleMutationCollection node and a
GeneMutationCollection node
 */
fun completeRelationshipToSampleMutationCollection(model: CoreModel) {
    val sample = NodeIdentifier("SampleMutationCollection", "sample_id",
        model.getModelSampleId())
    NodeIdentifierDao.defineRelationship(
        RelationshipDefinition( sample, model.getNodeIdentifier(),
        "HAS_MUTATION", RelationshipProperty("type",
                model.getNodeIdentifier().primaryLabel)
        )
    )
}

fun completeRelationshipToGeneMutationCollection(model: CoreModel) {
    val gene = NodeIdentifier("GeneMutationCollection", "gene_symbol",
        model.getModelGeneSymbol())
    NodeIdentifierDao.defineRelationship(
        RelationshipDefinition( gene, model.getNodeIdentifier(),
            "HAS_MUTATION", RelationshipProperty("type",
                model.getNodeIdentifier().primaryLabel)
        )
    )

}