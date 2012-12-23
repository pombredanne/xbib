
java \
    -Djava.util.logging.config.file=bin/logging.properties \
    -cp lib/xbib-tools-1.0-SNAPSHOT-elasticsearchezb.jar \
    org.xbib.tools.indexer.ElasticsearchEZBIndexer \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --index "ezb" \
    --type "ezb" \
    --path "/Users/joerg/Daten/EZB/" \
    --pattern "HBZ_update_dump201250001.xml" \
    --threads 1
