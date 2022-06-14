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

class TestCosmicNCV {
    private val LIMIT = Long.MAX_VALUE
    var nodeCount = 0
    fun parseCosmicNCVFile(filename: String): Int {
        // limit the number of records processed

        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicNCV.parseValueMap(it) }
            .forEach { ncv ->
                nodeCount += 1
                when (ncv.isValid()) {
                    true -> println(
                        "CosmicNCV: ${ncv.sampleId} " +
                                "  mutation: ${ncv.mutSeq}"
                    )

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
            val ncv = CosmicNCV.parseCSVRecord(record)
            when (ncv.isValid()) {
                true -> println(
                    "Sample Id: ${ncv.sampleId}  Genomic Mutation Id: ${ncv.genomicMutationId} " +
                            " Mut Seq: ${ncv.mutSeq}"
                )
                false -> println("Row $nodeCount is invalid")
            }
        }
    }
}

fun main() {
    val cosmicNCVFile = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicNCV.tsv")
    val test = TestCosmicNCV()
    test.processCSVRecords(cosmicNCVFile)
    println("Loaded COSMIC NCV file: $cosmicNCVFile  record count = ${test.nodeCount}")
}