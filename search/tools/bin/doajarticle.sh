#!/bin/bash

for d in `seq 1995 2013`; do
  e=`expr ${d} + 1`
  java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-content.jar \
    org.xbib.tools.harvest.DOAJ \
    --server "http://www.doaj.org/oai.article" \
    --fromDate "${d}-01-01" \
    --untilDate "${e}-01-01" \
    --prefix "doajArticle" \
    --output "doajarticle-${d}.ttl"
done
