package org.batteryparkdev.cosmicgraphdb.pubmed.loader

import arrow.core.Either
import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.pubmed.model.PubMedEntry
import org.batteryparkdev.cosmicgraphdb.pubmed.service.PubMedRetrievalService

import java.util.*

object PubMedLoader {

    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    fun loadPubMedEntryById(pubmedId: Int, label: String = "Origin", parentId: Int = 0):PubMedEntry? {
        return when (val retEither = PubMedRetrievalService.retrievePubMedArticle(pubmedId)) {
            is Either.Right -> {
                val pubmedArticle = retEither.value
                val pubmedEntry = PubMedEntry.parsePubMedArticle(pubmedArticle, label, parentId)
                loadPubMedEntry(pubmedEntry)
                loadReferenceNodes(pubmedEntry)
                loadCitationNodes(pubmedEntry)
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
                if (!pubMedNodeExistsPredicate(id)) {
                    logger.atFine().log("  Fetching citation  id: $id from NCBI")
                    val citEntry = loadPubMedEntryById(id, label, parentId)
                } else {
                    createPubMedRelationshipByEntry(label, parentId, id)
                    addPubMedLabel(id,label)
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
                if (!pubMedNodeExistsPredicate(id)) {
                    logger.atFine().log("  Fetching reference id: $id from NCBI")
                    val refEntry = loadPubMedEntryById(id, label, parentId)
                } else {
                    createPubMedRelationshipByEntry(label, parentId, id)
                    addPubMedLabel(id, label)
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
        if (!pubMedNodeExistsPredicate(pubMedEntry.pubmedId)) {
            val newPubMedId = mergePubMedEntry(pubMedEntry)
            logger.atFine().log("PubMed Id $newPubMedId  loaded into Neo4j")
        } else {
            logger.atFine().log("PubMed Id ${pubMedEntry.pubmedId}  already loaded into Neo4j")
        }
        if (pubMedEntry.parentPubMedId > 0) {
            val r = createPubMedRelationshipByEntry(pubMedEntry)
            logger.atFine().log(
                "${pubMedEntry.label} relationship between ids ${pubMedEntry.parentPubMedId} " +
                        " and ${pubMedEntry.pubmedId} created"
            )
            val l = addPubMedLabel(pubMedEntry.pubmedId, pubMedEntry.label)
            if (l.isNotEmpty()) {
                logger.atFine()
                    .log("Added label: ${pubMedEntry.label} to PubMedArticle node for ${pubMedEntry.pubmedId}")
            }
        }
    }

    private const val mergePubMedArticleTemplate = "MERGE (pma:PubMedArticle { pubmed_id: PMAID}) " +
            "SET  pma.pmc_id = \"PMCID\", pma.doi_id = \"DOIID\", " +
            " pma.journal_name = \"JOURNAL_NAME\", pma.journal_issue = \"JOURNAL_ISSUE\", " +
            " pma.article_title = \"TITLE\", pma.abstract = \"ABSTRACT\", " +
            " pma.author = \"AUTHOR\", pma.reference_count = REFCOUNT, " +
            " pma.cited_by_count = CITED_BY " +
            "  RETURN pma.pubmed_id"

    private fun mergePubMedEntry(pubMedEntry: PubMedEntry): String {
        val merge = mergePubMedArticleTemplate.replace("PMAID", pubMedEntry.pubmedId.toString())
            .replace("PMCID", pubMedEntry.pmcId)
            .replace("DOIID", pubMedEntry.doiId)
            .replace("JOURNAL_NAME", pubMedEntry.journalName)
            .replace("JOURNAL_ISSUE", pubMedEntry.journalIssue)
            .replace("TITLE", pubMedEntry.articleTitle)
            .replace("ABSTRACT", modifyInternalQuotes(pubMedEntry.abstract))
            .replace("AUTHOR", pubMedEntry.authorCaption)
            .replace("REFCOUNT", pubMedEntry.referenceSet.size.toString())
            .replace("CITED_BY", pubMedEntry.citedByCount.toString())
        return Neo4jConnectionService.executeCypherCommand(merge)
    }

    fun createPubMedRelationshipByEntry(label:String, parentPubMedId:Int, pubmedId: Int): String {
        val command = when (label.uppercase()) {
            "REFERENCE" -> "MATCH (parent:PubMedArticle), (child:PubMedArticle) WHERE " +
                    "parent.pubmed_id = $parentPubMedId AND child.pubmed_id = $pubmedId " +
                    "MERGE (parent) - [r:HAS_REFERENCE] -> (child) " +
                    "ON CREATE SET parent.reference_count = parent.reference_count +1 RETURN r"
            "CITATION" -> "MATCH (parent:PubMedArticle), (child:PubMedArticle) WHERE " +
                    "parent.pubmed_id = $parentPubMedId AND child.pubmed_id = $pubmedId " +
                    "MERGE (parent) - [r:CITED_BY] -> (child) RETURN r"
            else -> ""
        }
        return Neo4jConnectionService.executeCypherCommand(command)
    }

    /*
    Create a relationship between the origin node and either a reference node or a citation node
    Increment appropriate count in the origin node
     */
    private fun createPubMedRelationshipByEntry(pubMedEntry: PubMedEntry): String {
        val command = when (pubMedEntry.label.uppercase()) {
            "REFERENCE" -> "MATCH (parent:PubMedArticle), (child:PubMedArticle) WHERE " +
                    "parent.pubmed_id = ${pubMedEntry.parentPubMedId} AND child.pubmed_id = ${pubMedEntry.pubmedId} " +
                    "MERGE (parent) - [r:HAS_REFERENCE] -> (child) " +
                    "ON CREATE SET parent.reference_count = parent.reference_count +1 RETURN r"
            "CITATION" -> "MATCH (parent:PubMedArticle), (child:PubMedArticle) WHERE " +
                    "parent.pubmed_id = ${pubMedEntry.parentPubMedId} AND child.pubmed_id = ${pubMedEntry.pubmedId} " +
                    "MERGE (parent) - [r:CITED_BY] -> (child) RETURN r"
            else -> ""
        }
        return Neo4jConnectionService.executeCypherCommand(command)
    }

    /*
    Function to determine if the PubMed data is already in the database
     */
   fun pubMedNodeExistsPredicate(pubmedId: Int): Boolean {
        val cypher = "OPTIONAL MATCH (p:PubMedArticle{pubmed_id: $pubmedId }) " +
                " RETURN p IS NOT NULL AS Predicate"
        try {
            val predicate = Neo4jConnectionService.executeCypherCommand(cypher)
            when (predicate.lowercase(Locale.getDefault())) {
                "true" -> return true
                "false" -> return false
            }
        } catch (e: Exception) {
            logger.atSevere().log(e.message.toString())
            return false
        }
        return false
    }

    fun addPubMedLabel(pubmedId: Int, label: String): String {
        // confirm that label is novel
        val labelExistsQuery = "MATCH (pma:PubMedArticle{pubmed_id: $pubmedId }) " +
                "RETURN apoc.label.exists(pma, \"$label\") AS output;"
        val addLabelCypher = "MATCH (pma:PubMedArticle{pubmed_id: $pubmedId }) " +
                " CALL apoc.create.addLabels(pma, [\"$label\"] ) yield node return node"
        if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
            return Neo4jConnectionService.executeCypherCommand(addLabelCypher)
        }
       logger.atWarning().log("PubMedArticle node $pubmedId  already has label $label")
        return ""
    }
    /*
    Double quotes (i.e. ") inside a text field causes Cypher
    processing errors
     */
    private fun modifyInternalQuotes(text: String): String =
        text.replace("\"", "'")
}