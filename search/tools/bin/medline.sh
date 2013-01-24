#!/bin/bash

java \
    -cp target/xbib-tools-1.0-SNAPSHOT-elasticsearch-medline.jar \
    org.xbib.tools.indexer.elasticsearch.Medline \
    --es "es://interfaces:9300?es.cluster.name=joerg" \
    --index "medline" \
    --type "medline" \
    --path "/Users/joerg/Daten/medline" \
    --pattern "medline*xml.gz" \
    --threads 4
    
 
