:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (hallmark:CosmicHallmark {hallmark_id:apoc.create.uuid()})
SET hallmark.gene_symbol= line.`GENE_NAME`,
hallmark.cell_type=line.CELL_TYPE,
hallmark.pub_id=line.PUBMED_PMID,
hallmark.hallmark=line.HALLMARK,
hallmark.impact=line.IMPACT,
hallmark.description=line.DESCRIPTION,
hallmark.cell_line=line.CELL_LINE,
hallmark.date_created=datetime()
MERGE (hgc:HallmarkGeneCollection{gene_symbol:hallmark.gene_symbol})
MERGE (hgc)-[r:HAS_HALLMARK]-(hallmark)
} IN TRANSACTIONS OF 1000 ROWS;

MATCH(hallmark:CosmicHallmark) WHERE hallmark.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(hallmark.pub_id)})
SET p.url= genomiccore.resolvePubmedUrl(hallmark.pub_id)
MERGE (hallmark)-[r:HAS_PUBLICATION]->(p)
;
