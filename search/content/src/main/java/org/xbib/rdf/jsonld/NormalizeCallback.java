package org.xbib.rdf.jsonld;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xbib.rdf.jsonld.utils.UniqueNamer;

public class NormalizeCallback implements CallbackWrapper {

    private List<Map<String, Object>> statements = new ArrayList();
    private Map<String, Map<String, Object>> bnodes = new HashMap();
    private UniqueNamer namer = new UniqueNamer("_:c14n");

    public void callback(Map<String, Object> statement) {
        if (statement == null) {
            finalise();
        }

        for (Map<String, Object> s : statements) {
            if (JSONLDProcessorImpl.compareRdfStatements(s, statement)) {
                return;
            }
        }

        statements.add(statement);

        for (String n : new String[]{"subject", "object"}) {
            Map<String, Object> node = (Map<String, Object>) statement.get(n);
            String id = (String) node.get("nominalValue");
            if ("BlankNode".equals(node.get("interfaceName"))) {
                List<Object> stmts;
                if (bnodes.containsKey(id)) {
                    stmts = (List<Object>) bnodes.get(id).get("statements");
                } else {
                    stmts = new ArrayList<Object>();
                    Map<String, Object> tmp = new HashMap<String, Object>();
                    tmp.put("statements", stmts);
                    bnodes.put(id, tmp);
                }
                stmts.add(statement);
            }
        }
    }

    public void hashBlankNodes(List<String> unnamed) {
        // generate unique and duplicate hashes for bnodes
        List<String> nextUnnamed = new ArrayList<String>();
        Map<String, List<String>> duplicates = new HashMap<String, List<String>>();
        Map<String, String> unique = new HashMap<String, String>();

        for (String bnode : unnamed) {

            String hash = hashStatements(bnode, bnodes, namer);

            // store hash as unique or a duplicate
            if (duplicates.containsKey(hash)) {
                duplicates.get(hash).add(bnode);
                nextUnnamed.add(bnode);
            } else if (unique.containsKey(hash)) {
                List<String> tmp = new ArrayList<String>();
                tmp.add(unique.get(hash));
                tmp.add(bnode);
                duplicates.put(hash, tmp);
                unique.remove(hash);
            } else {
                unique.put(hash, bnode);
            }
        }

        // name blank nodes
        nameBlankNodes(unique, duplicates, nextUnnamed);
    }

    private void nameBlankNodes(Map<String, String> unique,
            Map<String, List<String>> duplicates, List<String> unnamed) {
        boolean named = false;
        List<String> hashes = new ArrayList<String>(unique.keySet());
        Collections.sort(hashes);
        for (String hash : hashes) {
            String bnode = unique.get(hash);
            namer.getName(bnode);
            named = true;
        }

        if (named) {
            hashBlankNodes(unnamed);
        } else {
            nameDuplicates(duplicates);
        }

    }

    private void nameDuplicates(Map<String, List<String>> duplicates) {
        List<String> hashes = new ArrayList<String>(duplicates.keySet());
        Collections.sort(hashes);

        // process each group
        for (String hash : hashes) {
            // name each group member
            List<String> group = duplicates.get(hash);
            List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

            for (String bnode : group) {
                // skip already-named bnodes
                if (namer.isNamed(bnode)) {
                    continue;
                }

                // hash bnode paths
                UniqueNamer pathNamer = new UniqueNamer("_:t");
                pathNamer.getName(bnode);
                try {
                    // create SHA-1 digest
                    MessageDigest md = MessageDigest.getInstance("SHA-1");

                    // group adjacent bnodes by hash, keep properties and references separate
                    Map<String, List<String>> groups = new HashMap<String, List<String>>();
                    List<Object> statements = (List<Object>) bnodes.get(bnode).get("statements");

                    for (Object statement : statements) {
                        // get adjacent bnode
                        String adjbnode =
                                ("BlankNode".equals(((Map<String, Object>) ((Map<String, Object>) statement).get("subject")).get("interfaceName"))
                                && !bnode.equals(((Map<String, Object>) ((Map<String, Object>) statement).get("subject")).get("nominalValue")))
                                ? (String) ((Map<String, Object>) ((Map<String, Object>) statement).get("subject")).get("nominalValue") : null;
                        String direction = null;
                        if (adjbnode != null) {
                            direction = "p";
                        } else {
                            adjbnode =
                                    ("BlankNode".equals(((Map<String, Object>) ((Map<String, Object>) statement).get("object")).get("interfaceName"))
                                    && !bnode.equals(((Map<String, Object>) ((Map<String, Object>) statement).get("object")).get("nominalValue")))
                                    ? (String) ((Map<String, Object>) ((Map<String, Object>) statement).get("object")).get("nominalValue") : null;
                            if (adjbnode != null) {
                                direction = "r";
                            }
                        }

                        if (adjbnode != null) {
                            // get bnode name (try canonical, path, then hash
                            String name;
                            if (namer.isNamed(adjbnode)) {
                                name = namer.getName(adjbnode);
                            } else if (pathNamer.isNamed(adjbnode)) {
                                name = pathNamer.getName(adjbnode);
                            } else {
                                // TODO: _hashStatements(adjbnode, bnodes, namer);
                                name = "";
                            }

                            MessageDigest md1 = MessageDigest.getInstance("SHA-1");
                            md1.update(direction.getBytes());
                            md1.update(((String) ((Map<String, Object>) ((Map<String, Object>) statement).get("property")).get("nominalValue")).getBytes());
                            md1.update(name.getBytes());
                            String groupHash = new String(md1.digest());

                            if (groups.containsKey(groupHash)) {
                                groups.get(groupHash).add(adjbnode);
                            } else {
                                List<String> tmp = new ArrayList<String>();
                                tmp.add(adjbnode);
                                groups.put(adjbnode, tmp);
                            }
                        }
                    }

                    // hashGroup: hashes a group of adjacent bnodes
                    List<String> groupHashes = new ArrayList<String>(groups.keySet());
                    Collections.sort(groupHashes);
                    for (String groupHash : groupHashes) {
                        // digest group hash
                        md.update(groupHash.getBytes());

                        // choose a path and namer from the permutations
                        // TODO: impt Permutator
                        // TODO: continue impl @ L2341 of jsonld.js
                    }

                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }

            // name bnodes in hash order
            Collections.sort(results, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1,
                        Map<String, Object> o2) {
                    String a = (String) o1.get("hash");
                    String b = (String) o2.get("hash");
                    return a.compareTo(b);
                }
            });
            for (Map<String, Object> r : results) {
                // name all bnodes in path namer in key-entry order
                // Note: key-order is preserved in javascript
                for (String key : ((UniqueNamer) r.get("pathNamer")).existing.keySet()) {
                    namer.getName(key);
                }
            }
        }

        // done, create JSON-LD array
        createArray();

    }

    private void createArray() {
    }

    public void finalise() {
        hashBlankNodes(new ArrayList<String>(bnodes.keySet()));
    }

    @Override
    public void processIgnored(Object parent, String parentId, String prop, Object object) {
    }

    /**
     * Hashes all of the statements about a blank node.
     *
     * @param id the ID of the bnode to hash statements for.
     * @param bnodes the mapping of bnodes to statements.
     * @param namer the canonical bnode namer.
     *
     * @return the new hash.
     */
    private String hashStatements(String id, Map<String, Map<String, Object>> bnodes, UniqueNamer namer) {
        if (bnodes.get(id).containsKey("hash")) {
            return (String) bnodes.get("id").get("hash");
        }

        List<Map<String, Object>> statements = (List<Map<String, Object>>) bnodes.get(id).get("statements");
        List<String> nquads = new ArrayList<String>();
        for (Map<String, Object> statement : statements) {
            // TODO: this is _toNQuad, and some of the code is pointless in this 
            // case and some is duplicated code.
            Map<String, Object> s = (Map<String, Object>) statement.get("subject");
            Map<String, Object> p = (Map<String, Object>) statement.get("property");
            Map<String, Object> o = (Map<String, Object>) statement.get("object");
            Map<String, Object> g = (Map<String, Object>) statement.get("name");

            String quad = "";

            if ("IRI".equals(s.get("interfaceName"))) {
                quad += "<" + s.get("nominalValue") + ">";
            } else if (id != null) {
                quad += (id.equals(s.get("nominalValue")) ? "_:a" : "_:z");
            } else {
                quad += s.get("nominalValue");
            }

            quad += " <" + p.get("nominalValue") + "> ";

            if ("IRI".equals(o.get("interfaceName"))) {
                quad += "<" + o.get("nominalValue") + ">";
            } else if ("BlankNode".equals(o.get("interfaceName"))) {
                if (id != null) {
                    quad += (id.equals(o.get("nominalValue")) ? "_:a" : "_:z");
                } else {
                    quad += o.get("nominalValue");
                }
            } else {
                String escaped = ((String) o.get("nominalValue"))
                        .replaceAll("\\\\", "\\\\\\\\")
                        .replaceAll("\\t", "\\\\t")
                        .replaceAll("\\n", "\\\\n")
                        .replaceAll("\\r", "\\\\r")
                        .replaceAll("\\\"", "\\\\\"");
                quad += "\"" + escaped + "\"";
                if (o.containsKey("datatype") && !JsonLd.XSD_STRING.equals(((Map<String, Object>) o.get("datatype")).get("nominalValue"))) {
                    quad += "^^<" + ((Map<String, Object>) o.get("datatype")).get("nominalValue") + ">";
                } else if (o.containsKey("language")) {
                    quad += "@" + o.get("language");
                }
            }

            if (g != null) {
                if ("IRI".equals(g.get("interfaceName"))) {
                    quad += " <" + g.get("nominalValue") + ">";
                } else if (id != null) {
                    quad += "_:g";
                } else {
                    quad += " " + g.get("nominalValue");
                }
            }

            quad += " .";
            // END OF _toNQuad

            nquads.add(quad);
        }

        Collections.sort(nquads);
        String hash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (String nquad : nquads) {
                md.update(nquad.getBytes());
            }
            hash = new String(md.digest());
        } catch (NoSuchAlgorithmException e) {
            // TODO: i don't expect that SHA-1 is even NOT going to be available?
            // look into this further
            throw new RuntimeException(e);
        }
        return hash;
    }
}
