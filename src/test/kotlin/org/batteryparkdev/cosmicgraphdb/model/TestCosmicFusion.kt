package org.batteryparkdev.cosmicgraphdb.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.io.CSVRecordSupplier
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.nio.file.Paths
import kotlin.streams.asSequence

class TestCosmicFusion {
    private val LIMIT = Long.MAX_VALUE
    fun parseCosmicFusionFile (filename: String): Int{
        var nodeCount = 0
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map {CosmicFusion.parseValueMap(it)}
            .filter{ fusion -> fusion.isValid()}
            .forEach {
                nodeCount += 1
                when (it.isValid()) {
                    true -> println("CosmicFusion: ${it.fusionId} " +
                            "  fusion type: ${it.fusionType}" )
                    false -> println("Row $nodeCount is invalid")
                }
            }
        return nodeCount
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceCSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get().asSequence()
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    fun processCSVRecords(filename: String) = runBlocking {
        val records = produceCSVRecords(filename)
        var nodeCount = 0
        for (record in records) {
            nodeCount += 1
            val fusion = CosmicFusion.parseCSVRecord(record)
            when (fusion.isValid()) {
                true -> println("Sample Id: ${fusion.sampleId}  5' gene ${fusion.five_geneSymbol} " +
                        " 3' gene ${fusion.three_geneSymbol}")
                false -> println("Row $nodeCount is invalid")
            }
        }
    }
}
fun main() {
    val cosmicFusionFile = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicFusionExport.tsv")
    val recordCount =
        TestCosmicFusion().processCSVRecords(cosmicFusionFile)
    println("Processed $cosmicFusionFile record count = $recordCount")
}