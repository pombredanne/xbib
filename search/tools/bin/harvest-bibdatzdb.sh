#!/bin/bash

for d in `seq 2000 2013`; do
  e=`expr ${d} + 1`
  java \
    -cp bin:lib/tools-1.0-SNAPSHOT-content.jar \
    org.xbib.tools.harvest.BibdatZDB \
    --server "http://services.dnb.de/oai/repository" \
    --prefix "PicaPlus-xml" \
    --set "bib" \
    --fromDate "${d}-01-01" \
    --untilDate "${e}-01-01" \
    --output "bibdatzdb-${d}-${e}"
done
