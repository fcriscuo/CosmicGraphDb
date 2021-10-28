package org.batteryparkdev.cosmicgraphdb.io

import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicHallmark
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordChannel.produceTSVRecords
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicGeneDao
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Paths

object TsvRecordChannel {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
     fun CoroutineScope.produceTSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            TsvRecordSequenceSupplier(path).get()
                .forEach {
                    send(it)
                    delay(20)
                }
        }
fun produceTSVRecordFlow (filename: String):Flow<CSVRecord> = flow{
    val path = Paths.get(filename)
    TsvRecordSequenceSupplier(path).get()
        .forEach {
            emit(it)
        }
}.flowOn(Dispatchers.IO)

    fun displayRecords(filename: String) = runBlocking {
        val records = produceTSVRecords(filename)
        for (record in records) {
            println(record)
        }
    }
}


fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/CosmicHGNC.tsv"
    TsvRecordChannel.displayRecords(filename)
}