package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicPatientDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.YNtoBoolean
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

data class CosmicPatient(
    val sampleId: Int,
    val patientId: Int,
    val tumorId: Int,
    val age: Int,
    val ethnicity: String,
    val environmental_variables: String,
    val therapy: String,
    val family: String,
    val gender: String,
    val individual_remark: String,
    val normal_tissue_tested: Boolean
): CoreModel {

    override fun generateLoadModelCypher(): String  = CosmicPatientDao(this).generateLoadCosmicModelCypher()

    override fun getModelGeneSymbol(): String= ""

    override fun getModelSampleId(): String  = sampleId.toString()

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicPatient", "patient_id", patientId.toString())

    override fun getPubMedIds(): List<Int> = emptyList()

    override fun isValid(): Boolean = patientId > 0 && tumorId > 0

    companion object : CoreModelCreator {
        const val nodename = "patient"

        fun parseCsvRecord(record: CSVRecord): CosmicPatient =
            CosmicPatient(
                record.get("sample_id").parseValidInteger(),
                record.get("id_individual").parseValidInteger(),
                record.get("id_tumour").parseValidInteger(),
                record.get("age").parseValidInteger(),
                record.get("ethnicity"),
                record.get("environmental_variables"),
                record.get("therapy"), record.get("family"),
                record.get("gender"),
                record.get("individual_remark"),
                record.get("normal_tissue_tested").YNtoBoolean()
            )

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
                = ::parseCsvRecord
    }

}
