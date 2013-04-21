#!/bin/bash

java \
    -cp bin:lib/xbib-search-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.tools.indexer.elasticsearch.BibdatZDB \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --mock false \
    --detect true \
    --threads 1 \
    --maxbulkactions 200 \
    --maxconcurrentbulkrequests 10 \
    --index "bib" \
    --type "zdbadr" \
    --path "$HOME/Daten/zdb/adressen" \
    --pattern "1302Bibdatzdb.pp.xml"
