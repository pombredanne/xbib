
# Beispielaufruf

java -cp target/xbib-tools-1.0-SNAPSHOT-alephseq2xml.jar \
   org.xbib.tools.aleph.AlephSeq2MarcXML \
   --path /Users/joerg/Projects/hbz/MARC21_BVB \
   --pattern "m0*.marc" \
   --output "marcxml" \
   --basename "google" \
   --threads 8

