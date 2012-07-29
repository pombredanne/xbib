
java \
    -Djava.util.logging.config.file=logging.properties \
    -cp target/xbib-tools-1.0-SNAPSHOT-elasticsearchmedline.jar \
    org.xbib.tools.medline.ElasticsearchMedlineIndexer \
    --es "es://interfaces:9300?es.cluster.name=joerg" \
    --index "medline" \
    --type "medline" \
    --path "/Users/joerg/Daten/medline" \
    --pattern "medline*xml.gz" \
    --threads 4
    
 
