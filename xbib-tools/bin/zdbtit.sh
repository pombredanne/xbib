#!/bin/bash

java \
    -cp lib/xbib-tools-1.0-SNAPSHOT-elasticsearch-zdb.jar \
    org.xbib.tools.indexer.elasticsearch.ZDB \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --threads 1 \
    --maxbulkactions 100 \
    --index "zdb" \
    --type "title" \
    --path "/Users/joerg/Daten/zdb/" \
    --pattern "1208zdbtitutf8.mrc" \
    --elements "marc"
