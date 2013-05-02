#!/bin/bash

java -cp target/xbib-search-tools-1.0-SNAPSHOT-content.jar \
   org.xbib.tools.convert.aleph.AlephSeq2MarcXML \
   --path /Users/joerg/Projects/hbz/MARC21_BVB \
   --pattern "m0*.marc" \
   --output "marcxml" \
   --basename "google" \
   --threads 8

