#!/bin/bash

java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.tools.indexer.elasticsearch.HBZ \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --mock true \
    --detect true \
    --threads 1 \
    --shards 1 \
    --maxbulkactions 1 \
    --maxconcurrentbulkrequests 1 \
    --pipelines 1 \
    --index "hbzvk" \
    --type "title" \
    --path "$HOME/import/hbz/aleph/" \
    --pattern "clobs.hbz.metadata.mab.alephxml-clob-dump*.tar.bz2" \
    --elements "mab/hbz/dialect"
