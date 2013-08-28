#!/bin/bash

mkdir bibdatzdb

y=`date +%Y`

for d in `seq 2000 $y`; do
  e=`expr ${d} + 1`
  java \
    -cp bin:lib/tools-1.0-SNAPSHOT-content.jar \
    org.xbib.tools.harvest.DNB \
    --server "http://services.dnb.de/oai/repository" \
    --prefix "PicaPlus-xml" \
    --set "bib" \
    --fromDate "${d}-01-01" \
    --untilDate "${e}-01-01" \
    --output "bibdatzdb/${d}-${e}"
done
