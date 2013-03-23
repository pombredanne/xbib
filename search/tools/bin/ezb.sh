#!/bin/bash

java \
    -cp bin:lib/xbib-search-tools-1.0-SNAPSHOT-elasticsearch-ezb.jar \
    org.xbib.tools.indexer.elasticsearch.EZB \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --threads 4 \
    --maxbulkactions 1000 \
    --index "ezb" \
    --type "ezb" \
    --path "/Users/joerg/Daten/EZB/" \
    --pattern "*.xml"

