#!/bin/bash

for d in `seq 2010 2013`; do
  e=`expr ${d} + 1`
  java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-content.jar \
    org.xbib.tools.harvest.NatLizOAI \
    --server "http://dl380-47.gbv.de/oai/natliz/" \
    --prefix "extpp2" \
    --fromDate "${d}-01-01" \
    --untilDate "${e}-01-01" \
    --output "natliz-${d}"
done
