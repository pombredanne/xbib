#!/bin/bash

java \
    -cp bin:lib/xbib-search-tools-1.0-SNAPSHOT-content.jar \
    org.xbib.tools.convert.GND2Turtle \
    --path "${HOME}/Daten/gnd" \
    --pattern "GND.ttl.gz" \
    --output "gnd.ttl"
