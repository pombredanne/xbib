#!/bin/bash

java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.elasticsearch.tools.elasticsearch.Medline \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --index "medline" \
    --type "medline" \
    --path "${HOME}/import/medline" \
    --pattern "medline*xml.gz"

 
