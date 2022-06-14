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

class TestCosmicStruct {

    var nodeCount = 0

    fun parseCosmicStructFile(filename: String): Int {
        ApocFileReader.processDelimitedFile(filename)
            .map { record -> record.get("map") }
            .map { CosmicStruct.parseValueMap(it) }
            .forEach { struct ->
                nodeCount += 1
                when (struct.isValid()) {
                    true -> println("CosmicStruct: ${struct.mutationId} " +
                            "  description: ${struct.description}" )
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
                .filter { it.size() > 1 }
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    fun processCSVRecords(filename: String) = runBlocking {
        val records = produceCSVRecords(filename)
        for (record in records) {
            nodeCount += 1
            val struct = CosmicStruct.parseCSVRecord(record)
            when (struct.isValid()) {
                true -> println(
                    "Sample Id: ${struct.sampleId}  Mutation Id: ${struct.mutationId} " +
                            " Description: ${struct.description}"
                )
                false -> println("Row $nodeCount is invalid")
            }
        }
    }
}

fun main() {
    println("Using Neo4j server at ${System.getenv("NEO4J_URI")}")
    val cosmicStructFile = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicStructExport.tsv")
    println("Loading data from file: $cosmicStructFile")
    val test =  TestCosmicStruct()
        test.processCSVRecords(cosmicStructFile)
    println("Struct record count = ${test.nodeCount}")
}