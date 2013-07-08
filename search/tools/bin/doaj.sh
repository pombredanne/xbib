#!/bin/bash

java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-content.jar \
    org.xbib.tools.harvest.OAI \
    --server "http://www.doaj.org/oai" \
    --fromDate "1995-01-01" \
    --untilDate "2013-07-01" \
    --prefix "oai_dc" \
    --output "doaj.ttl"
