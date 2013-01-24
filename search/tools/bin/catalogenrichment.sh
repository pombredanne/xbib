#!/bin/bash

java \
    -cp target/xbib-tools-1.0-SNAPSHOT-elasticsearch-ce.jar \
    org.xbib.tools.indexer.elasticsearch.CE
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --index "hbz" \
    --type "ce" \
    --path "/Users/joerg/Daten/hbz/ce_export" \
    --pattern "*.txt" \
    --threads 4
    
