package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicType
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService

object CosmicTypeLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    fun processCosmicTypeNode(cosmicType: CosmicType) {
        val id = loadCosmicTypeNode(cosmicType)
        addCosmicTypeLabel(id, cosmicType.label)
        logger.atInfo().log("CosmicType: ${cosmicType.label}   primary: ${cosmicType.primary} " +
                " loaded into Neo4j")
    }

    private fun loadCosmicTypeNode(cosmicType: CosmicType): Int {
        val id = Neo4jConnectionService.executeCypherCommand(
            "MERGE " +
                    " (ct:CosmicType{type_id: ${cosmicType.typeId}}) " +
                    " SET ct.primary = \"${cosmicType.primary}\", ct.subtype1 = \"${cosmicType.subtype1}\", " +
                    " ct.subtype2 = \"${cosmicType.subtype2}\"," +
                    "  ct.subtype3 = \"${cosmicType.subtype3}\"  RETURN ct.type_id"
        ).toInt()
        return id
    }

    private fun addCosmicTypeLabel(id: Int, label: String) {
        val labelExistsQuery = "MERGE (ct:CosmicType{type_id:$id}) " +
                "RETURN apoc.label.exists(ct, \"$label\") AS output;"
        if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
            Neo4jConnectionService.executeCypherCommand(
                "MATCH (ct:CosmicType{type_id:$id}) " +
                        "CALL apoc.create.addLabels(ct,[\"$label\"]) YIELD node RETURN node"
            )
        }
    }
}