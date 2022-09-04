package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import java.util.*

data class CosmicType(
    val label: String,
    val primary: String,
    val subtype1: String = "NS",
    val subtype2: String = "NS",
    val subtype3: String = "NS",
    val typeId:Int  = UUID.randomUUID().hashCode()
): CoreModel {


    fun generateCosmicTypeCypher(parentNodeName: String) =
        generateMergeCypher()

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node([\"CosmicType\",\"${label}\"], " +
                "{ cosmic_type_id: $typeId}, " +
                "{ primary: ${primary.formatNeo4jPropertyValue()}\", " +
                "  subtype1: ${subtype1.formatNeo4jPropertyValue()}\", " +
                "  subtype2: ${subtype2.formatNeo4jPropertyValue()}\", " +
                "  subtype3: ${subtype3.formatNeo4jPropertyValue()}\", " +
                " created: datetime() }," +
                "{ last_mod: datetime()}) YIELD node AS ${label.lowercase()}\n "

    private fun generateParentRelationshipCypher(parentNodeName: String): String {
        val relationship = "HAS_".plus(label.uppercase()).plus("_TYPE")
        val relName = "rel_".plus(label.lowercase())
        return " CALL apoc.merge.relationship($parentNodeName, '$relationship' ," +
                    " {}, {created: datetime()}," +
                    " ${label.lowercase()}, {}) YIELD rel AS $relName\n"
    }

    override fun generateLoadModelCypher(): String {
        TODO("Not yet implemented")
    }
    override fun getModelGeneSymbol(): String = ""

    override fun getModelSampleId(): String = ""

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicType", "cosmic_type_id",typeId.toString(),label )

    override fun getPubMedIds(): List<Int>  = emptyList()

    override fun isValid(): Boolean = label.isNotEmpty().and(primary.isNotEmpty())
}
