# rdflib is slow...

import rdflib
g = rdflib.Graph()
result = g.parse('bibdat.nt', format='n3')
print len(g)
for stmt in g:
    print stmt
