#!/bin/bash

java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-content.jar \
    org.xbib.tools.convert.SerialsDB \
    --input "${HOME}/crossref" \
    --pattern "titleFile.csv" \
    --output "serials.ttl"
