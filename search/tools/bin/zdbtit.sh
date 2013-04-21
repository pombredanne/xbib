#!/bin/bash

java \
    -cp bin:lib/xbib-search-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.tools.indexer.elasticsearch.ZDB \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --mock false \
    --detect false \
    --threads 1 \
    --shards 4 \
    --maxbulkactions 1000 \
    --maxconcurrentbulkrequests 30 \
    --index "zdb" \
    --type "title" \
    --path "$HOME/Daten/zdb/" \
    --pattern "1302zdbtitgesamt.mrc.gz" \
    --elements "marc"
