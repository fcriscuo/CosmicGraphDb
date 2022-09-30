package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicTypeDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils
import java.util.*

data class CosmicType(
    val label: String,
    val primary: String,
    val subtype1: String = "NS",
    val subtype2: String = "NS",
    val subtype3: String = "NS",
    val typeId:Int  = UUID.randomUUID().hashCode()
): CoreModel {
    override val idPropertyValue: String
        get() = typeId.toString()

    override fun createModelRelationships() = CosmicTypeDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String = CosmicTypeDao(this).generateLoadCosmicModelCypher()

    override fun getModelGeneSymbol(): String = ""

    override fun getModelSampleId(): String = ""

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicType", "cosmic_type_id",typeId.toString(),label )

    override fun getPubMedIds(): List<Int>  = emptyList()

    override fun isValid(): Boolean = label.isNotEmpty().and(primary.isNotEmpty())

    companion object {
        val nodename = "cosmic_type_".plus(Neo4jUtils.getUniqueSuffix())
    }


}
