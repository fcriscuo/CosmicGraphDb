
// CosmicBreakpoint -> CosmicBreakpoint
MATCH(break:CosmicBreakpoint) WHERE break.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(break.pub_id)})
ON CREATE
SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
p.needs_properties=true, p.needs_references=true
MERGE (break)-[r2:HAS_PUBLICATION]->(p)
;

// CosmicHallmark -> Publication
MATCH(hallmark:CosmicHallmark) WHERE hallmark.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(hallmark.pub_id)})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
MERGE (hallmark)-[r:HAS_PUBLICATION]->(p)
;

// CosmicMutation -> Publication
MATCH (ccm:CosmicMutation) WHERE ccm.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:ccm.pub_id})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
MERGE (ccm)-[HAS_PUBLICATION]->(p)
;

// CosmicNCV -> Publication
MATCH(ncv:CosmicNCV) WHERE ncv.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(ncv.pub_id)})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
MERGE (ncv)-[r:HAS_PUBLICATION]->(p)
;

// CosmicResistanceMutation -> Publication
MATCH(resist:CosmicResistanceMutation) WHERE resist.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(resist.pub_id)})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
MERGE (resist)-[r1:HAS_PUBLICATION]->(p)
;

// CosmicStruct ->Publication
MATCH (struct:CosmicStruct) WHERE struct.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(struct.pub_id)})
  ON CREATE
  SET p.url= genomiccore.resolvePubmedUrl(toString(p.pub_id)),
  p.needs_properties=true, p.needs_references=true
MERGE (struct)-[r:HAS_PUBLICATION]->(p)
;