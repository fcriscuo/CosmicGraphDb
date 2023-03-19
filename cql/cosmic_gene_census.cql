:// CosmicGeneCensus
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea5TBExt/COSMIC_rel97/cancer_gene_census.csv", { header:true,
mapping:{
     `Tumour Types(Somatic)`: {array:true, arraySep:",", name:"tumor_types_somatic"},
     `Tumour Types(Germline)`: {array:true, arraySep:",", name:"tumor_types_germline"},
     `Translocation Partner`: {array:true, arraySep:",", name:"translocation_partners"},
      `Mutation Types`: {array:true, arraySep:",", name:"mutation_types"},
      `Other Syndrome`: {array:true, arraySep:",", name:"other_syndromes"},
     Synonyms: {array:true, arraySep:",", name:"synonyms"}
   }
}
)
YIELD lineNo, map AS nodeRecord RETURN nodeRecord, lineNo
','
WHERE NOT nodeRecord.`Gene Symbol` IS NULL
MERGE (n: `CosmicGene` { gene_symbol: nodeRecord.`Gene Symbol` })
MERGE (cgmc: `CosmicGeneMutationCollection` {gene_symbol: nodeRecord.`GeneSymbol`})
MERGE (n)-[r:HAS_MUTATION_COLLECTION]->(cgmc)
SET n.`gene_name` = nodeRecord.`Name`
SET n.`entrez_gene_id` = toInteger(trim(nodeRecord.`Entrez GeneId`))
SET n.`genome_location` = nodeRecord.`Genome Location`
SET n.`tier` = toInteger(trim(nodeRecord.`Tier`))
SET n.`hallmark` = nodeRecord.`Hallmark`
SET n.`chr_band` = toFloat(trim(nodeRecord.`Chr Band`))
SET n.`somatic` = nodeRecord.`Somatic`
SET n.`germline` = nodeRecord.`Germline`
SET n.`tumor_types_somatic` = nodeRecord.`tumor_types_somatic`
SET n.`tumor_types_germline` = nodeRecord.`tumor_types_germline`
SET n.`cancer_syndrome` = nodeRecord.`Cancer Syndrome`
SET n.`tissue_type` = nodeRecord.`Tissue Type`
SET n.`molecular_genetics` = nodeRecord.`Molecular Genetics`
SET n.`role_in_cancer` = nodeRecord.`Role in Cancer`
SET n.`mutation_types` = nodeRecord.`mutation_types`
SET n.`translocation_partners` = nodeRecord.`translocation_partners`
SET n.`other_germline_mut` = nodeRecord.`Other Germline Mut`
SET n.`other_syndromes` = nodeRecord.`other_syndromes`
SET n.`cosmic_id` = nodeRecord.`COSMIC ID`
SET n.`cosmic gene name` = nodeRecord.`cosmic gene name`
SET n.cosmic_url= replace("https://cancer.sanger.ac.uk/cosmic/gene/analysis?ln=XXXX", "XXXX",n.gene_symbol)
SET n.`synonyms` = nodeRecord.synonyms;
',
{batchSize:1000, iterateList:true, parallel:true});

// CosmicGene to CosmicGeneHallmarkCollection relationship
MATCH(gene:CosmicGene) WHERE gene.is_hallmark = 'yes'
MERGE (hgc:HallmarkGeneCollection{gene_symbol:hallmark.gene_symbol})
MERGE (gene)-[r:HAS_HALLMARK_COLLECTION]-(hgc);

// Link CosmicGene translocation partners
MATCH (gene:CosmicGene) WHERE  gene.translocation_partners IS NOT NULL
UNWIND gene.translocation_partners AS translocation
MATCH(partner:CosmicGene) WHERE partner.gene_symbol=translocation
MERGE (gene)-[r:HAS_TRANSLOCATION_PARTNER]-(partner)
;

// Add URL to Entrez Gene page if Entrez ID is available
MATCH (cg:CosmicGene) WHERE cg.entrez_id is not null
SET cg.entrez_gene_url= replace("https://www.ncbi.nlm.nih.gov/gene/?term=XXXX","XXXX",cg.entrez_id)
MATCH (eg:EntrezGene) WHERE eg.gene_id = cg.entrez_id
MERGE (cg)-[r1:HAS_ENTREZ_GENE]-(eg)
;

// create a (gene)-[r:HAS_UNIPROT_ENTRY]-(uniprot) relationship
MATCH (cg:CosmicGene)
MATCH (uni:UniProtEntry) WHERE cg.gene_symbol = uni.gene_symbol
MERGE (cg)-[r1:HAS_UNIPROT_ENTRY]->(uni)
;

:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/CosmicHGNC.tsv ' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MATCH (cg:CosmicGene) where cg.gene_symbol = line.COSMIC_GENE_NAME
SET cg.hgnc_id = apoc.text.join(["HGNC",line.HGNC_ID],':')
WITH cg
MATCH (h:Hgnc) WHERE h.hgnc_id = cg.hgnc_id
MERGE (cg)-[r1:HAS_HGNC]->(h)

} IN TRANSACTIONS OF 1000 ROWS;


