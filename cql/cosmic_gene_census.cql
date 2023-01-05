:auto CALL {
LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/cosmic_gene_census.csv' AS line

MERGE (gene:CosmicGene:CensusGene:Gene{gene_symbol: line.`Gene Symbol`})
SET gene.gene_name=line.Name,
gene.entrez_id=line.`Entrez GeneId`,
gene.genome_location=line.`Genome Location`,
gene.tier=line.Tier,
gene.is_halmark=line.Hallmark,
gene.chr_band=line.`Chr Band`,
gene.somatic=line.Somatic,
gene.germ_line=line.Germline,
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
MERGE (gene)-[r:HAS_TRANSLOCATTION_PARTNER]-(partner)
;