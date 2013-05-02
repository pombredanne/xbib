package org.xbib.rdf.jsonld;

public interface JsonLd {

    String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    String XSD_NS = "http://www.w3.org/2001/XMLSchema#";

    String XSD_BOOLEAN = XSD_NS + "boolean";
    String XSD_DOUBLE = XSD_NS + "double";
    String XSD_INTEGER = XSD_NS + "integer";
    String XSD_STRING = XSD_NS + "string";

    String RDF_FIRST = RDF + "first";
    String RDF_REST = RDF + "rest";
    String RDF_NIL = RDF + "nil";
    String RDF_TYPE = RDF + "type";

    String JSONLD_CONTEXT = "@context";
    String JSONLD_CONTAINER = "@container";
    String JSONLD_DEFAULT = "@default";
    String JSONLD_EMBED = "@embed";
    String JSONLD_EXPLICIT = "@explicit";
    String JSONLD_GRAPH = "@graph";
    String JSONLD_ID = "@id";
    String JSONLD_LANGUAGE = "@language";
    String JSONLD_LIST = "@list";
    String JSONLD_OMITDEFAULT = "@omitDefault";
    String JSONLD_PRESERVE = "@preserve";
    String JSONLD_SET = "@set";
    String JSONLD_TYPE = "@type";
    String JSONLD_VALUE = "@value";
    String JSONLD_VOCAB = "@vocab";

}
