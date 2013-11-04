#!/bin/bash

java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.elasticsearch.tools.elasticsearch.SpringerCitations \
    --elasticsearch "es://tyan:9300?es.cluster.name=joerg" \
    --mock false \
    --threads 1 \
    --maxbulkactions 100 \
    --maxconcurrentbulkrequests 1 \
    --index "springer" \
    --type "articles" \
    --path "${HOME}/import/springer" \
    --pattern "*.txt"
