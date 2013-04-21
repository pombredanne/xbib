#!/bin/bash

java \
    -cp bin:lib/xbib-search-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.tools.indexer.elasticsearch.ZDB \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --mock false \
    --detect true \
    --threads 1 \
    --shards 4 \
    --maxbulkactions 1000 \
    --maxconcurrentbulkrequests 5 \
    --index "zdb" \
    --type "holdings" \
    --path "$HOME/Daten/zdb/" \
    --pattern "1302zdblokalgesamt.mrc.gz" \
    --elements "marc/holdings"
