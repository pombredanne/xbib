
java \
    -Djava.util.logging.config.file=logging.properties \
    -cp target/xbib-tools-1.0-SNAPSHOT-elasticsearchcatalogenrichment.jar \
    org.xbib.tools.indexer.ElasticsearchCatalogEnrichmentIndexer \
    --elasticsearch "es://interfaces:9300?es.cluster.name=joerg" \
    --index "hbz" \
    --type "ce" \
    --path "/Users/joerg/Daten/hbz/ce_export" \
    --pattern "*.txt" \
    --threads 4
    
