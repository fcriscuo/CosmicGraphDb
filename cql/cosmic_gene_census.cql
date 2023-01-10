:auto
LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/cancer_gene_census.csv' AS line
CALL {
WITH line
MERGE (gene:CosmicGene:CensusGene:Gene{gene_symbol: line.`Gene Symbol`})
MERGE (gmc:GeneMutationCollection){gene_symbol: line.`Gene Symbol`})
MERGE (gene)-[r:HAS_MUTATION_COLLECTION]->(gmc)
MERGE (gene)-[r2:HAS_ACTIONABILITY_COLLECTION]->(gac)

SET gene.gene_name=line.Name,
gene.entrez_id=line.`Entrez GeneId`,
gene.genome_location=line.`Genome Location`,
gene.tier=line.Tier,
gene.is_hallmark=toLower(line.Hallmark),
gene.chr_band=line.`Chr Band`,
gene.is_somatic=toLower(line.Somatic),
gene.is_germ_line=toLower(line.Germline),
gene.somatic_tumor_types=split(line.`Tumour Types(Somatic)`,','),
gene.germline_tumor_types=split(line.`Tumour Types(Germline)`,','),
gene.cancer_syndrome=line.`Cancer Syndrome`,
gene.tissue_types=split(line.`Tissue Type`,','),
gene.molecular_genetics=line.`Molecular Genetics`,
gene.roles_in_cancer=split(line.`Role in Cancer`,','),
gene.mutation_types=split(line.`Mutation Types`,','),
gene.translocation_partner=line.`Translocation Partner`,
gene.other_germline_mutations=split(line.`Other Germline Mut`,','),
gene.other_syndromes=split(line.`Other Syndrome`,','),
gene.cosmic_id=line.COSMIC_ID,
gene.comic_gene_symbol=line.`cosmic gene name`,
gene.synonyms=split(line.Synonyms,','),
gene.date_created=datetime()
} IN TRANSACTIONS OF 1000 ROWS;

MATCH (gene:CosmicGene) WHERE  gene.translocation_partner <> ""
MATCH(partner:CosmicGene) WHERE partner.gene_symbol=gene.translocation_partner
MERGE (gene)-[r:HAS_TRANSLOCATION_PARTNER]-(partner)
;

MATCH(gene:CosmicGene) WHERE gene.is_hallmark = "yes"
MATCH(hgc:HallmarkGeneCollection) where hgc.gene_symbol = gene.gene_symbol
MERGE (gene)-[r:HAS_HALLMARK_COLLECTION]->(hgc)
;

MATCH (cg:CosmicGene) WHERE cg.entrez_id is not null
SET cg.entrez_gene_url= replace("https://www.ncbi.nlm.nih.gov/gene/XXXX","XXXX",cg.entrez_id)
;
