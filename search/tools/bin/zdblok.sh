#!/bin/bash

java -Xmx2g \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-elasticsearch-zdb.jar \
    org.xbib.tools.indexer.elasticsearch.ZDB \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --threads 1 \
    --maxbulkactions 1000 \
    --index "zdb" \
    --type "holdings" \
    --path "/Users/joerg/Daten/zdb/" \
    --pattern "1208zdblokutf8.mrc" \
    --elements "marc/holdings"
