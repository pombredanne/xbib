#!/bin/bash

java \
    -cp bin:lib/xbib-search-tools-1.0-SNAPSHOT-elasticsearch-zdb.jar \
    org.xbib.tools.indexer.elasticsearch.ZDB \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --mock false \
    --threads 1 \
    --maxbulkactions 1000 \
    --index "zdb" \
    --type "title" \
    --path "$HOME/Daten/zdb/" \
    --pattern "1208zdbtitutf8.mrc" \
    --elements "marc"
