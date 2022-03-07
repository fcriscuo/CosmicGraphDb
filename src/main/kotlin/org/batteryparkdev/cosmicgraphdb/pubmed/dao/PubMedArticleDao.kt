package org.batteryparkdev.cosmicgraphdb.pubmed.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.pubmed.loader.PubMedLoader
import org.batteryparkdev.cosmicgraphdb.pubmed.model.PubMedEntry
import org.batteryparkdev.cosmicgraphdb.pubmed.model.PubMedIdentifier
import java.util.*

object PubMedArticleDao {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()
    private const val mergePubMedArticleTemplate = "MERGE (pma:PubMedArticle { pubmed_id: PMAID}) " +
            "SET  pma.pmc_id = \"PMCID\", pma.doi_id = \"DOIID\", " +
            " pma.journal_name = \"JOURNAL_NAME\", pma.journal_issue = \"JOURNAL_ISSUE\", " +
            " pma.article_title = \"TITLE\", pma.abstract = \"ABSTRACT\", " +
            " pma.author = \"AUTHOR\", pma.reference_count = REFCOUNT, " +
            " pma.cited_by_count = CITED_BY " +
            "  RETURN pma.pubmed_id"

   fun mergePubMedEntry(pubMedEntry: PubMedEntry): String {
        val merge = mergePubMedArticleTemplate.replace("PMAID", pubMedEntry.pubmedId.toString())
            .replace("PMCID", pubMedEntry.pmcId)
            .replace("DOIID", pubMedEntry.doiId)
            .replace("JOURNAL_NAME", pubMedEntry.journalName)
            .replace("JOURNAL_ISSUE", pubMedEntry.journalIssue)
            .replace("TITLE",  modifyInternalQuotes(pubMedEntry.articleTitle))
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
    A set of three functions to create empty (i.e. placeholder) nodes to
    be completed asynchronously
     */
    fun createPlaceholderNode(identifier: PubMedIdentifier) {
        if (pubMedNodeExistsPredicate(identifier.pubmedId).not()) {
            Neo4jConnectionService.executeCypherCommand(
                "MERGE (pma:PubMedArticle { pubmed_id: " +
                        "${identifier.pubmedId}}) SET pma.article_title = \"\", " +
                        " pma.parent_id = ${identifier.parentId}"
            )
        }
        createPlaceholderRelationship(identifier)
        labelPlaceholderNode(identifier)
    }

    private fun createPlaceholderRelationship(identifier: PubMedIdentifier) {
        if(identifier.parentId> 0) {
            Neo4jConnectionService.executeCypherCommand("" +
                    "MATCH (parent:PubMedArticle), (child:PubMedArticle) WHERE " +
                    " parent.pubmed_id = ${identifier.parentId} AND child.pubmed_id " +
                    " = ${identifier.pubmedId} " +
                    " MERGE (parent) - [r:HAS_REFERENCE] -> (child) " +
                    "  ON CREATE SET parent.reference_count = parent.reference_count +1 RETURN r")
        }
    }

    private fun labelPlaceholderNode(identifier: PubMedIdentifier) {
        if (identifier.label.isNotEmpty()) {
            addPubMedLabel(identifier.pubmedId, identifier.label)
        }
    }

    /*
    Create a relationship between the origin node and either a reference node or a citation node
    Increment appropriate count in the origin node
     */
    fun createPubMedRelationshipByEntry(pubMedEntry: PubMedEntry): String {
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
       // logger.atWarning().log("PubMedArticle node $pubmedId  already has label $label")
        return ""
    }
    /*
    Double quotes (i.e. ") inside a text field causes Cypher
    processing errors
     */
    private fun modifyInternalQuotes(text: String): String =
        text.replace("\"", "'")
}