#!/bin/bash

java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-content.jar \
    org.xbib.tools.convert.HBZConverter \
    --detect true \
    --threads 1 \
    --pipelines 16 \
    --path "$HOME/import/hbz/aleph/" \
    --pattern "clobs.hbz.metadata.mab.alephxml-clob-dump*.tar.bz2" \
    --elements "mab/hbz/dialect"
