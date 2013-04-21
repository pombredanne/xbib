#!/bin/bash

java \
    -cp target/xbib-search-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.tools.indexer.elasticsearch.Medline \
    --es "es://interfaces:9300?es.cluster.name=joerg" \
    --index "medline" \
    --type "medline" \
    --path "${HOME}/Daten/medline" \
    --pattern "medline*xml.gz" \
    --threads 4
    
 
