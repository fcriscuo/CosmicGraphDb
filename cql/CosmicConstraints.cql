CREATE CONSTRAINT unique_classification_id IF NOT EXISTS FOR (cl: CosmicClassification)  REQUIRE cl.phenotype_id IS UNIQUE;
CREATE CONSTRAINT unique_site_id IF NOT EXISTS FOR (site:CosmicSite)  REQUIRE site.primary_site IS UNIQUE;
CREATE CONSTRAINT unique_histology_id IF NOT EXISTS FOR (histology:Histology)  REQUIRE histology.histology IS UNIQUE;
CREATE CONSTRAINT unique_gene_symbol IF NOT EXISTS FOR (gene:CosmicGene)  REQUIRE gene.gene_symbol IS UNIQUE;
CREATE CONSTRAINT unique_hallmark_id IF NOT EXISTS FOR (hallmark:CosmicHallmark)  REQUIRE hallmark.hallmark_id IS UNIQUE;
CREATE CONSTRAINT unique_hallmark_gene_symbol IF NOT EXISTS FOR (hgc:HallmarkGeneCollection)  REQUIRE hgc.gene_symbol IS UNIQUE;