#!/bin/bash

java -Xmx1g \
    -cp bin:lib/xbib-search-tools-1.0-SNAPSHOT-elasticsearch-zdb.jar \
    org.xbib.tools.indexer.elasticsearch.ZDB \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --mock false \
    --threads 1 \
    --maxbulkactions 1000 \
    --maxconcurrentbulkrequests 10 \
    --index "zdb" \
    --type "holdings" \
    --path "$HOME/Daten/zdb/" \
    --pattern "1208zdblokutf8.mrc" \
    --elements "marc/holdings"
