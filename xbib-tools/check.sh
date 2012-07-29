for i in  target/xml/test_*.xml; do
  xmllint --format $i | grep "tag=\"001\"" >> idlist.txt
done
