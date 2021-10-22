package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicSample
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicTypeDao
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.addCosmicSampleTypeLabel
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createCosmicSampleRelationships
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.loadCosmicSample
import java.nio.file.Paths

object CosmicSampleLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    fun processCosmicSampleNode(cosmicSample: CosmicSample){
        val id = loadCosmicSample(cosmicSample)
        if (cosmicSample.sampleType.isNotEmpty()) {
            addCosmicSampleTypeLabel(id, cosmicSample.sampleName)
        }
        loadCosmicSampleTypes(cosmicSample)
        createCosmicSampleRelationships(cosmicSample)
    }

    private fun loadCosmicSampleTypes(cosmicSample: CosmicSample){
        CosmicTypeDao.processCosmicTypeNode(cosmicSample.site)
        CosmicTypeDao.processCosmicTypeNode(cosmicSample.histology)
    }
}

fun main() {
    val path = Paths.get("./data/sample_CosmicSample.tsv")
    println("Processing tsv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicSample.parseCsvRecord(it) }
                .forEach { sample ->
                    CosmicSampleLoader.processCosmicSampleNode(sample)
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}