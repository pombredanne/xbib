#!/bin/bash

java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.elasticsearch.tools.aggregate.zdb.MergeWithCitations \
    --source "es://hostname:9300?es.cluster.name=joerg&serialIndex=zdb&serialType=title&citationIndex=works&citationType=articles" \
    --target "es://hostname:9300?es.cluster.name=joerg&index=cit" \
    --size 100 \
    --millis 600000
