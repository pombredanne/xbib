#!/bin/bash

# Testdaten Bielefeld/Köln: HBZ_update_dump201250001.xml.gz
# Lieferung NRW  HBZ_update_dump201325001.gz

# Format-Dokumenation
# http://www.zeitschriftendatenbank.de/fileadmin/user_upload/ZDB/pdf/services/Datenlieferdienst_ZDB_EZB_Lizenzdatenformat.pdf

java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.elasticsearch.tools.elasticsearch.EZB \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --threads 1 \
    --maxbulkactions 1000 \
    --index "ezb" \
    --type "licenses" \
    --path "$HOME/import/ezb/" \
    --pattern "HBZ_update_dump*.gz"

