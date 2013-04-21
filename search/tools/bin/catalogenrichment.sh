#!/bin/bash

java \
    -cp target/xbib-search-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.tools.indexer.elasticsearch.CE
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --index "hbz" \
    --type "ce" \
    --path "${HOME}/Daten/hbz/ce_export" \
    --pattern "*.txt" \
    --threads 4
