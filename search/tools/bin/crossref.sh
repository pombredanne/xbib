#!/bin/bash

java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-content.jar \
    org.xbib.tools.convert.ArticleDB \
    --path "${HOME}/crossref" \
    --pattern "*.json" \
    --threads 4
