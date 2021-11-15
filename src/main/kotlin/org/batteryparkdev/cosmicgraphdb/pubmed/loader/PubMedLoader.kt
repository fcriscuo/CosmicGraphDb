package org.batteryparkdev.cosmicgraphdb.pubmed.loader

import arrow.core.Either
import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.pubmed.dao.PubMedArticleDao
import org.batteryparkdev.cosmicgraphdb.pubmed.model.PubMedEntry
import org.batteryparkdev.cosmicgraphdb.pubmed.service.PubMedRetrievalService

import java.util.*

object PubMedLoader {

    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    fun loadPubMedEntryById(pubmedId: Int, label: String = "CosmicArticle", parentId: Int = 0):PubMedEntry? {
        return when (val retEither = PubMedRetrievalService.retrievePubMedArticle(pubmedId)) {
            is Either.Right -> {
                val pubmedArticle = retEither.value
                val pubmedEntry = PubMedEntry.parsePubMedArticle(pubmedArticle, label, parentId)
                loadPubMedEntry(pubmedEntry)
                loadReferenceNodes(pubmedEntry)
                // skip loading Citations
               // loadCitationNodes(pubmedEntry)
                pubmedEntry
            }
            is Either.Left -> {
                logger.atSevere().log(retEither.value.message)
                null
            }
        }
    }

    fun loadCitationNodes(pubMedEntry: PubMedEntry){
        logger.atInfo().log("Processing Citations for PubMed Id ${pubMedEntry.pubmedId}")
        val parentId = pubMedEntry.pubmedId
        val label = "Citation"
        pubMedEntry.citationSet.stream().forEach { id ->
            run {
                logger.atFine().log("  Citation id: $id")
                /*
                Only fetch the PubMed data from NCBI if the database does not
                contain a PubMedReference node for this citation id
                 */
                if (!PubMedArticleDao.pubMedNodeExistsPredicate(id)) {
                    logger.atFine().log("  Fetching citation  id: $id from NCBI")
                    val citEntry = loadPubMedEntryById(id, label, parentId)
                } else {
                    PubMedArticleDao.createPubMedRelationshipByEntry(label, parentId, id)
                    PubMedArticleDao.addPubMedLabel(id,label)
                }
            }
        }
    }

    fun loadReferenceNodes(pubMedEntry: PubMedEntry) {
        logger.atInfo().log("Processing References for PubMed Id ${pubMedEntry.pubmedId}")
        val parentId = pubMedEntry.pubmedId  // id of origin node
        val label = "Reference"
        pubMedEntry.referenceSet.stream().forEach { id ->
            run {
                logger.atFine().log("  Reference id: $id")
                if (!PubMedArticleDao.pubMedNodeExistsPredicate(id)) {
                    logger.atFine().log("  Fetching reference id: $id from NCBI")
                    val refEntry = loadPubMedEntryById(id, label, parentId)
                } else {
                    PubMedArticleDao.createPubMedRelationshipByEntry(label, parentId, id)
                    PubMedArticleDao.addPubMedLabel(id, label)
                }
            }
        }
    }

    fun loadPubMedEntry(pubMedEntry: PubMedEntry) {
        /*
        Test if this PubMed Id is already represented
        If so, don't attempt to create another one,
        but the existing node may have a new relationship and
        an additional label
         */
        if (!PubMedArticleDao.pubMedNodeExistsPredicate(pubMedEntry.pubmedId)) {
            val newPubMedId = PubMedArticleDao.mergePubMedEntry(pubMedEntry)
            logger.atFine().log("PubMed Id $newPubMedId  loaded into Neo4j")
        } else {
            logger.atFine().log("PubMed Id ${pubMedEntry.pubmedId}  already loaded into Neo4j")
        }
        if (pubMedEntry.parentPubMedId > 0) {
            val r = PubMedArticleDao.createPubMedRelationshipByEntry(pubMedEntry)
            logger.atFine().log(
                "${pubMedEntry.label} relationship between ids ${pubMedEntry.parentPubMedId} " +
                        " and ${pubMedEntry.pubmedId} created"
            )
            val l = PubMedArticleDao.addPubMedLabel(pubMedEntry.pubmedId, pubMedEntry.label)
            if (l.isNotEmpty()) {
                logger.atFine()
                    .log("Added label: ${pubMedEntry.label} to PubMedArticle node for ${pubMedEntry.pubmedId}")
            }
        }
    }


}