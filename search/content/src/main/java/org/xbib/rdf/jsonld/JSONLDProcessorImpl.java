package org.xbib.rdf.jsonld;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.xbib.rdf.jsonld.utils.ActiveContext;
import org.xbib.rdf.jsonld.utils.FramingContext;
import org.xbib.rdf.jsonld.utils.JSONLDUtils;
import org.xbib.rdf.jsonld.utils.JSONUtils;
import org.xbib.rdf.jsonld.utils.Obj;
import org.xbib.rdf.jsonld.utils.Options;
import org.xbib.rdf.jsonld.utils.UniqueNamer;

public class JSONLDProcessorImpl implements JsonLd {

    private Options opts;

    public JSONLDProcessorImpl(Options opts) {
        if (opts == null) {
            this.opts = new Options("");
        } else {
            this.opts = opts;
        }
    }

    /**
     * Defines a context mapping during context processing.
     *
     * @param activeCtx the current active context.
     * @param ctx the local context being processed.
     * @param key the key in the local context to define the mapping for.
     * @param base the base IRI.
     * @param defined a map of defining/defined keys to detect cycles and
     * prevent double definitions.
     * @throws org.xbib.rdf.jsonld.JSONLDProcessingError
     */
    private void defineContextMapping(ActiveContext activeCtx, Map<String, Object> ctx, String key, String base, Map<String, Boolean> defined) throws JSONLDProcessingError {
        if (defined.containsKey(key)) {
            // key already defined
            if (defined.get(key) == Boolean.TRUE) {
                return;
            }
            // cycle detected
            throw new JSONLDProcessingError("Cyclical context definition detected")
                    .setDetail("context", ctx)
                    .setDetail("key", key);
        }
        // now defining key
        defined.put(key, Boolean.FALSE);

        // if key has a prefix, define it first
        String prefix = null;
        int colon = key.indexOf(":");
        if (colon != -1) {
            prefix = key.substring(0, colon);
            if (ctx.containsKey(prefix)) {
                // define parent prefix
                defineContextMapping(activeCtx, ctx, prefix, base, defined);
            }
        }

        // get context key value
        Object value = ctx.get(key);

        if (JSONLDUtils.isKeyword(key)) {

            // support @vocab
            if (JSONLD_VOCAB.equals(key)) {
                if (!value.equals(null) && !(value instanceof String)) {
                    throw new JSONLDProcessingError("Invalid JSON-LD syntax; the value of \"@vocab\" in a @context must be a string or null")
                            .setDetail("context", ctx);
                }
                if (((String) value).indexOf(":") == -1) {
                    throw new JSONLDProcessingError("Invalid JSON-LD syntax; the value of \"@vocab\" in a @context must be an absolute IRI")
                            .setDetail("context", ctx);
                }
                if (value == null) {
                    activeCtx.remove(JSONLD_VOCAB);
                } else {
                    activeCtx.put(JSONLD_VOCAB, value);
                }
                defined.put(key, Boolean.TRUE);
                return;
            }

            // only @language is permitted
            if (!JSONLD_LANGUAGE.equals(key)) {
                throw new JSONLDProcessingError("Invalid JSON-LD syntax; keywords cannot be overridden")
                        .setDetail("context", ctx);
            }
            if (!value.equals(null) && !(value instanceof String)) {
                throw new JSONLDProcessingError("Invalid JSON-LD syntax; the value of \"@language\" in a @context must be a string or null")
                        .setDetail("context", ctx);
            }
            if (value == null) {
                activeCtx.remove(JSONLD_LANGUAGE);
            } else {
                activeCtx.put(JSONLD_LANGUAGE, value);
            }
            defined.put(key, Boolean.TRUE);
            return;
        }

        // clear context entry
        if (value == null || (value instanceof Map && ((Map<String, Object>) value).containsKey(JSONLD_ID) && ((Map<String, Object>) value).get(JSONLD_ID) == null)) {
            if (activeCtx.mappings.containsKey(key)) {
                // if key is a keyword alias, remove it
                String kw = (String) ((Map<String, Object>) activeCtx.mappings.get(key)).get(JSONLD_ID);
                if (JSONLDUtils.isKeyword(kw)) {
                    List<String> aliases = activeCtx.keywords.get(kw);
                    aliases.remove(key);
                }
                activeCtx.mappings.remove(key);
            }
            defined.put(key, Boolean.TRUE);
            return;
        }

        if (value instanceof String) {
            if (JSONLDUtils.isKeyword((String) value)) {
                // disallow aliasing @context and @preserve
                if (JSONLD_CONTEXT.equals(value) || JSONLD_PRESERVE.equals(value)) {
                    throw new JSONLDProcessingError("Invalid JSON-LD syntax; @context and @preserve cannot be aliased");
                }
                // uniquely add key as a keyword alias and resort
                List<String> aliases = activeCtx.keywords.get(value);
                if (!aliases.contains(key)) {
                    aliases.add(key);
                    Collections.sort(aliases, new Comparator<String>() {
                        // Compares two strings first based on length and then lexicographically
                        public int compare(String a, String b) {
                            if (a.length() < b.length()) {
                                return -1;
                            } else if (b.length() < a.length()) {
                                return 1;
                            }
                            return a.compareTo(b);
                        }
                    });
                }
            } else {
                // expand value to a full IRI
                value = expandContextIri(activeCtx, ctx, (String) value, base, defined);
            }

            // define/redefine key to expanded IRI/keyword
            Map<String, Object> tmp = new HashMap<String, Object>();
            tmp.put(JSONLD_ID, value);
            activeCtx.mappings.put(key, tmp);
            defined.put(key, Boolean.TRUE);
            return;
        }

        if (!(value instanceof Map)) {
            throw new JSONLDProcessingError("Invalid JSON-LD syntax; @context property values must be strings or objects.")
                    .setDetail("context", ctx);
        }
        // create new mapping
        Map<String, Object> mapping = new HashMap();

        // helper to make accessing the value as a map easier
        Map<String, Object> val = (Map<String, Object>) value;

        if (val.containsKey(JSONLD_ID)) {
            if (!(val.get(JSONLD_ID) instanceof String)) {
                throw new JSONLDProcessingError("Invalid JSON-LD syntax; @context @id values must be strings.")
                        .setDetail("context", ctx);
            }
            String id = (String) val.get(JSONLD_ID);

            // expand @id if it is not @type
            if (!JSONLD_TYPE.equals(id)) {
                // expand @id to full IRI
                id = expandContextIri(activeCtx, ctx, id, base, defined);
            }

            // add @id to mapping
            mapping.put(JSONLD_ID, id);
        } else if (activeCtx.containsKey(JSONLD_VOCAB) && activeCtx.get(JSONLD_VOCAB) != null) {
            // NOTE: this is not implemented in javascript (which actually fails tests that need it)
            String id = activeCtx.get(JSONLD_VOCAB) + key;
            mapping.put(JSONLD_ID, id);
        } else {
            // non-IRIs *must* define @ids
            if (prefix == null) {
                throw new JSONLDProcessingError("Invalid JSON-LD syntax; @context terms must define an @id.")
                        .setDetail("context", ctx)
                        .setDetail("key", val);
            }

            if (activeCtx.mappings.containsKey(prefix)) {
                String suffix = key.substring(colon + 1);
                mapping.put(JSONLD_ID, (String) ((Map<String, Object>) activeCtx.mappings.get(prefix)).get(JSONLD_ID) + suffix);
            } else {
                mapping.put(JSONLD_ID, key);
            }
        }

        if (val.containsKey(JSONLD_TYPE)) {
            if (!(val.get(JSONLD_TYPE) instanceof String)) {
                throw new JSONLDProcessingError("Invalid JSON-LD syntax; @context @type values must be strings.")
                        .setDetail("context", ctx);
            }
            String type = (String) val.get(JSONLD_TYPE);
            if (!JSONLD_ID.equals(type)) {
                // expand @type to full IRI
                type = expandContextIri(activeCtx, ctx, type, null, defined);
            }
            // add @type to mapping
            mapping.put(JSONLD_TYPE, type);
        }

        if (val.containsKey(JSONLD_CONTAINER)) {
            Object container = val.get(JSONLD_CONTAINER);
            if (!(JSONLD_LIST.equals(container) || JSONLD_SET.equals(container) || JSONLD_LANGUAGE.equals(container))) {
                throw new JSONLDProcessingError("Invalid JSON-LD syntax; @context @container value must be \"@list\" or \"@set\".")
                        .setDetail("context", ctx);
            }
            // add @container to mapping
            mapping.put(JSONLD_CONTAINER, container);
        }

        if (val.containsKey(JSONLD_LANGUAGE)) {
            Object lang = val.get(JSONLD_LANGUAGE);
            if (lang != null && !(lang instanceof String)) {
                throw new JSONLDProcessingError("Invalid JSON-LD syntax; @context @language must be a string or null.")
                        .setDetail("context", ctx);
            }
            // add @language to mapping
            mapping.put(JSONLD_LANGUAGE, lang);
        }

        // merge onto parent mapping if one exists for a prefix
        if (prefix != null && activeCtx.mappings.containsKey(prefix)) {
            Map<String, Object> child = mapping;
            mapping = (Map<String, Object>) JSONLDUtils.clone(activeCtx.mappings.get(prefix));
            for (String k : child.keySet()) {
                mapping.put(k, child.get(k));
            }
        }

        // define key mapping
        activeCtx.mappings.put(key, mapping);
        defined.put(key, Boolean.TRUE);
    }

    /**
     * Expands a string value to a full IRI during context processing. It can be
     * assumed that the value is not a keyword.
     *
     * @param activeCtx the current active context.
     * @param ctx the local context being processed.
     * @param value the string value to expand.
     * @param base the base IRI.
     * @param defined a map for tracking cycles in context definitions.
     *
     * @return the expanded value.
     * @throws org.xbib.rdf.jsonld.JSONLDProcessingError
     */
    private String expandContextIri(ActiveContext activeCtx, Map<String, Object> ctx, String value, String base, Map<String, Boolean> defined) throws JSONLDProcessingError {
        // dependency not defined, define it
        if (ctx.containsKey(value) && defined.get(value) != Boolean.TRUE) {
            defineContextMapping(activeCtx, ctx, value, base, defined);
        }

        // recurse if value is a term
        if (activeCtx.mappings.containsKey(value)) {
            String id = ((Map<String, String>) activeCtx.mappings.get(value)).get(JSONLD_ID);
            // value is already an absolute IRI
            if (id != null && id.equals(value)) {
                return value;
            }
            return expandContextIri(activeCtx, ctx, id, base, defined);
        }

        // split value into prefix:suffix
        int colon = value.indexOf(':');
        if (colon != -1) {
            String prefix = value.substring(0, colon);
            String suffix = value.substring(colon + 1);

            // indicates the value is a blank node
            if ("_".equals(prefix)) {
                return value;
            }

            // indicates the value is an absolute IRI
            if (suffix.startsWith("//")) {
                return value;
            }

            // dependency not defined, define it
            if (ctx.containsKey(prefix) && defined.get(prefix) != Boolean.TRUE) {
                defineContextMapping(activeCtx, ctx, prefix, base, defined);
            }

            // recurse if prefix is defined
            if (activeCtx.mappings.containsKey(prefix)) {
                String id = ((Map<String, String>) activeCtx.mappings.get(prefix)).get(JSONLD_ID);
                return expandContextIri(activeCtx, ctx, id, base, defined) + suffix;
            }

            // consider the value an absolute IRI
            return value;
        }

        if (JSONLDUtils.isKeyword(value)) {
        } // prepend vocab
        else if (base == null && ctx.containsKey(JSONLD_VOCAB)) {
            value = JSONLDUtils.prependBaseAndnormalizeURI((String) ctx.get(JSONLD_VOCAB), value);
        } else if (base != null && activeCtx.containsKey(JSONLD_VOCAB)) {
            // NOTE: this fulfills the case where @vocab is in the root of the active content
            // which from the expected results of test compact-0021 is required to be used
            // over the value of base
            value = JSONLDUtils.prependBaseAndnormalizeURI((String) activeCtx.get(JSONLD_VOCAB), value);
        } else if (base != null) {
            value = JSONLDUtils.prependBaseAndnormalizeURI(base, value);
        }

        // value must now be an absolute IRI
        if (!JSONLDUtils.isAbsoluteIri(value)) {
            throw new JSONLDProcessingError("Invalid JSON-LD syntax; a @context value does not expand to an absolute IRI.")
                    .setDetail("context", ctx)
                    .setDetail("value", value);
        }

        return value;
    }

    /**
     * Processes a local context, resolving any URLs as necessary, and returns a
     * new active context in its callback.
     *
     * @param activeCtx the current active context.
     * @param localCtx the local context to process. options the options to use:
     * [resolver(url, callback(err, jsonCtx))] the URL resolver to use. callback
     * (err, ctx) called once the operation completes.
     * @throws org.xbib.rdf.jsonld.JSONLDProcessingError
     */
    public ActiveContext processContext(ActiveContext activeCtx, Object localCtx) throws JSONLDProcessingError {
        // initialize the resulting context
        ActiveContext rval = (ActiveContext) JSONLDUtils.clone(activeCtx);

        // normalize local context to an array of @context objects
        if (localCtx instanceof Map && ((Map) localCtx).containsKey(JSONLD_CONTEXT) && ((Map) localCtx).get(JSONLD_CONTEXT) instanceof List) {
            localCtx = ((Map) localCtx).get(JSONLD_CONTEXT);
        }

        List<Map<String, Object>> ctxs;
        if (localCtx instanceof List) {
            ctxs = (List<Map<String, Object>>) localCtx;
        } else {
            ctxs = new ArrayList();
            ctxs.add((Map<String, Object>) localCtx);
        }

        // process each context in order
        for (Object ctx : ctxs) {
            if (ctx == null) {
                // reset to initial context
                rval = new ActiveContext();
                continue;
            }

            // context must be an object by now, all URLs resolved before this call
            if (ctx instanceof Map) {

                // dereference @context key if present
                if (((Map<String, Object>) ctx).containsKey(JSONLD_CONTEXT)) {
                    ctx = (Map<String, Object>) ((Map<String, Object>) ctx).get(JSONLD_CONTEXT);
                }

                // define context mappings for keys in local context
                HashMap<String, Boolean> defined = new HashMap();
                for (String key : ((Map<String, Object>) ctx).keySet()) {
                    defineContextMapping(rval, (Map<String, Object>) ctx, key, opts.getBase(), defined);
                }
            } else {
                // context must be an object by now, all URLs resolved before this call
                throw new JSONLDProcessingError("@context must be an object")
                        .setDetail("context", ctx);
            }
        }

        return rval;
    }

    /**
     * Processes a local context and returns a new active context.
     *
     * @param activeCtx the current active context.
     * @param localCtx the local context to process.
     * @param opts the context processing options.
     *
     * @return the new active context.
     * @throws org.xbib.rdf.jsonld.JSONLDProcessingError
     */
    public ActiveContext processContext(ActiveContext activeCtx, Object localCtx, Options opts) throws JSONLDProcessingError {
        JSONLDProcessorImpl p = new JSONLDProcessorImpl(opts);
        if (localCtx == null) {
            return new ActiveContext();
        }

        localCtx = JSONLDUtils.clone(localCtx);
        if (localCtx instanceof Map && !((Map) localCtx).containsKey(JSONLD_CONTEXT)) {
            Map<String, Object> tmp = new HashMap();
            tmp.put(JSONLD_CONTEXT, localCtx);
            localCtx = tmp;
        }
        return p.processContext(activeCtx, localCtx);
    }

    /**
     * Expands a term into an absolute IRI. The term may be a regular term, a
     * prefix, a relative IRI, or an absolute IRI. In any case, the associated
     * absolute IRI will be returned.
     *
     * @param ctx the active context to use.
     * @param term the term to expand.
     * @param base the base IRI to use if a relative IRI is detected.
     *
     * @return the expanded term as an absolute IRI.
     */
    private String expandTerm(ActiveContext ctx, String term, String base) {
        return expandTerm(ctx, term, base, false, false);
    }

    // NOTE: adding isKey and isValueOfType flags to support points 4.3.5/6 
    private String expandTerm(ActiveContext ctx, String term, String base, Boolean isKey, Boolean isValueOfType) {
        // nothing to expand
        if (term == null) {
            return null;
        }

        // the term has a mapping, so it is a plain term
        if (ctx.mappings.containsKey(term)) {
            String id = (String) ((Map<String, Object>) ctx.mappings.get(term)).get(JSONLD_ID);
            // term is already an absolute IRI
            if (term.equals(id)) {
                return term;
            }
            return expandTerm(ctx, id, base);
        }

        // split term into prefix:suffix
        int colon = term.indexOf(':');
        if (colon != -1) {
            String prefix = term.substring(0, colon);
            String suffix = term.substring(colon + 1);

            // indicates the value is a blank node
            if ("_".equals(prefix)) {
                return term;
            }

            // indicates the value is an absolute IRI
            if (suffix.startsWith("//")) {
                return term;
            }

            // the term's prefix has a mapping, so it is a CURIE
            if (ctx.mappings.containsKey(prefix)) {
                return expandTerm(ctx, (String) ((Map<String, Object>) ctx.mappings.get(prefix)).get(JSONLD_ID), base) + suffix;
            }

            // consider term an absolute IRI
            return term;
        }


        // TODO: 5) Otherwise, if the IRI being processed does not contain a colon and is a property,
        // i.e., a key in a JSON object, or the value of @type and the active context has a @vocab 
        // mapping, join the mapped value to the suffix using textual concatenation.

        // TODO: this seems to be missing a check to see if this is a keyword
        if (JSONLDUtils.isKeyword(term)) {
            // then we just return the term
        } // use vocab // TODO: added base check, since terms that are not key's or values of @type should prioritise base before @vocab
        else if ((isKey || isValueOfType) && ctx.containsKey(JSONLD_VOCAB)) {
            term = JSONLDUtils.prependBaseAndnormalizeURI((String) ctx.get(JSONLD_VOCAB), term);
            // prepend base to term
        } else if (!isKey && base != null) {
            term = JSONLDUtils.prependBaseAndnormalizeURI(base, term);
        }

        return term;
    }

 
    private String expandTerm(ActiveContext ctx, String term) {
        return expandTerm(ctx, term, null);
    }

    /**
     * Expands the given value by using the coercion and keyword rules in the
     * given context.
     *
     * @param ctx the active context to use.
     * @param property the property the value is associated with.
     * @param value the value to expand.
     * @param base the base IRI to use.
     *
     * @return the expanded value.
     */
    private Object expandValue(ActiveContext ctx, String property, Object value, String base) {
        // nothing to expand
        if (value == null) {
            return null;
        }

        // default to simple string return value
        Object rval = value;

        // special-case expand @id and @type (skips '@id' expansion)
        String prop = expandTerm(ctx, property, base, true, false);
        if (JSONLD_ID.equals(prop)) {
            rval = expandTerm(ctx, (String) value, base, false, false);
        } else if (JSONLD_TYPE.equals(prop)) {
            rval = expandTerm(ctx, (String) value, base, false, true);
        } else {
            // get type definition from context
            Object type = ctx.getContextValue(property, JSONLD_TYPE);

            // do @id expansion (automatic for @graph)
            if (JSONLD_ID.equals(type) || JSONLD_GRAPH.equals(prop)) {
                Map<String, Object> tmp = new HashMap<String, Object>();
                tmp.put(JSONLD_ID, expandTerm(ctx, (String) value, base));
                rval = tmp;
            } else if (!JSONLDUtils.isKeyword(prop)) {
                Map<String, Object> tmp = new HashMap<String, Object>();
                tmp.put(JSONLD_VALUE, value);
                rval = tmp;

                // other type
                if (type != null) {
                    tmp.put(JSONLD_TYPE, type);
                    // check for language tagging
                } else {
                    Object language = ctx.getContextValue(property, JSONLD_LANGUAGE);
                    if (language != null) {
                        tmp.put(JSONLD_LANGUAGE, language);
                    }
                }
            }
        }
        return rval;
    }

    /**
     * Throws an exception if the given value is not a valid
     *
     * @type value.
     *
     * @param v the value to check.
     * @throws org.xbib.rdf.jsonld.JSONLDProcessingError
     */
    private boolean validateTypeValue(Object v) throws JSONLDProcessingError {
        // must be a string, subject reference, or empty object
        if (v instanceof String || (v instanceof Map && (((Map<String, Object>) v).containsKey(JSONLD_ID) || ((Map<String, Object>) v).size() == 0))) {
            return true;
        }

        // must be an array
        boolean isValid = false;
        if (v instanceof List) {
            isValid = true;
            for (Object i : (List) v) {
                if (!(i instanceof String || i instanceof Map && ((Map<String, Object>) i).containsKey(JSONLD_ID))) {
                    isValid = false;
                    break;
                }
            }
        }

        if (!isValid) {
            throw new JSONLDProcessingError(
                    "Invalid JSON-LD syntax; \"@type\" value must a string, a subject reference, an array of strings or subject references, or an empty object.")
                    .setDetail("value", v);
        }
        return true;
    }

    /**
     * Compacts an IRI or keyword into a term or prefix if it can be. If the IRI
     * has an associated value it may be passed.
     *
     * @param ctx the active context to use.
     * @param iri the IRI to compact.
     * @param value the value to check or null.
     * @param isKey if this is a key in the object map, or a value of
     * @type
     *
     * @return the compacted term, prefix, keyword alias, or the original IRI.
     */
    public static String compactIri(ActiveContext ctx, String iri, Object value, boolean isKey) {
        // can't compact null
        if (iri == null) {
            return iri;
        }

        // term is a keyword
        if (JSONLDUtils.isKeyword(iri)) {
            // return alias if available
            List<String> aliases = ctx.keywords.get(iri);
            if (aliases.size() > 0) {
                return aliases.get(0);
            } else {
                // no alias, keep original keyword
                return iri;
            }
        }

        // default value to null
        // NOTE: since there is no 'undefined' in Java, there's nothing to do here

        // find all possible term matches
        List<String> terms = new ArrayList<String>();
        int highest = 0;
        boolean listContainer = false;
        boolean isList = (value instanceof Map && ((Map<String, Object>) value).containsKey(JSONLD_LIST));
        for (String term : ctx.mappings.keySet()) {
            // skip terms with non-matching iris
            Map<String, Object> entry = (Map<String, Object>) ctx.mappings.get(term);
            if (!iri.equals(entry.get(JSONLD_ID))) {
                continue;
            }
            // skip @set containers for @lists
            if (isList && JSONLD_SET.equals(entry.get(JSONLD_CONTAINER))) {
                continue;
            }
            // skip @list containers for non-@lists
            if (!isList && JSONLD_LIST.equals(entry.get(JSONLD_CONTAINER)) && value != null) {
                continue;
            }
            // for @lists, if listContainer is set, skip non-list containers
            if (isList && listContainer && !JSONLD_LIST.equals(entry.get(JSONLD_CONTAINER))) {
                continue;
            }

            // rank term
            int rank = rankTerm(ctx, term, value);
            if (rank > 0) {
                // add 1 to rank if container is a @set
                if (JSONLD_SET.equals(entry.get(JSONLD_CONTAINER))) {
                    rank += 1;
                }

                // for @lists, give preference to @list containers
                if (isList && !listContainer && JSONLD_LIST.equals(entry.get(JSONLD_CONTAINER))) {
                    listContainer = true;
                    terms.clear();
                    highest = rank;
                    terms.add(term);
                    // only push match if rank meets current threshold
                } else if (rank >= highest) {
                    if (rank > highest) {
                        terms.clear();
                        highest = rank;
                    }
                    terms.add(term);
                }
            }
        }

        // if this is a key or the value of @type, compact with @vocab first
        // otherwise do it the otherway around
        // NOTE: this helps to satisfy test compact-0021
        // TODO: remove duplicate code

        if (isKey) {
            // NOTE: added this after the check for CURRIES to support test compact-0021
            // no matching terms, use @vocab if available
            if (terms.size() == 0 && ctx.containsKey(JSONLD_VOCAB)) {
                // determine if vocab is a prefix of the iri
                String vocab = (String) ctx.get(JSONLD_VOCAB);
                if (iri.startsWith(vocab)) {
                    // use suffix as relative iri if it is not a term in the active context
                    String suffix = iri.substring(vocab.length());
                    if (!ctx.mappings.containsKey(suffix)) {
                        return suffix;
                    }
                }
            }
            // no term matches, add possible CURIEs
            if (terms.size() == 0) {
                for (String term : ctx.mappings.keySet()) {
                    // skip terms with colons, they can't be prefixes
                    if (term.contains(":")) {
                        continue;
                    }

                    // skip entries with @ids that are not partial matches
                    Map<String, Object> entry = (Map<String, Object>) ctx.mappings.get(term);
                    String entryid = (String) entry.get(JSONLD_ID);
                    if (entryid == null || !(entryid.endsWith("/") || entryid.endsWith("#")) || iri.equals(entryid) || !iri.startsWith(entryid)) {
                        // TODO: added skip of entries that don't end with / or # since they are most likely not prefixes
                        // but this may not always be true.
                        continue;
                    }

                    // add CURIE as term if it has no mapping
                    String curie = term + ":" + iri.substring(((String) entry.get(JSONLD_ID)).length());
                    if (!(ctx.mappings.containsKey(curie))) {
                        terms.add(curie);
                    }
                }
            }
        } else {
            // no term matches, add possible CURIEs
            if (terms.size() == 0) {
                for (String term : ctx.mappings.keySet()) {
                    // skip terms with colons, they can't be prefixes
                    if (term.contains(":")) {
                        continue;
                    }

                    // skip entries with @ids that are not partial matches
                    Map<String, Object> entry = (Map<String, Object>) ctx.mappings.get(term);
                    String entryid = (String) entry.get(JSONLD_ID);
                    if (entryid == null || !(entryid.endsWith("/") || entryid.endsWith("#")) || iri.equals(entryid) || !iri.startsWith(entryid)) {
                        // TODO: added skip of entries that don't end with / or # since they are most likely not prefixes
                        // but this may not always be true.
                        continue;
                    }

                    // add CURIE as term if it has no mapping
                    String curie = term + ":" + iri.substring(((String) entry.get(JSONLD_ID)).length());
                    if (!(ctx.mappings.containsKey(curie))) {
                        terms.add(curie);
                    }
                }
            }
            // NOTE: added this after the check for CURRIES to support test compact-0021
            // no matching terms, use @vocab if available
            if (terms.size() == 0 && ctx.containsKey(JSONLD_VOCAB)) {
                // determine if vocab is a prefix of the iri
                String vocab = (String) ctx.get(JSONLD_VOCAB);
                if (iri.startsWith(vocab)) {
                    // use suffix as relative iri if it is not a term in the active context
                    String suffix = iri.substring(vocab.length());
                    if (!ctx.mappings.containsKey(suffix)) {
                        return suffix;
                    }
                }
            }

        }

        // no matching terms,
        if (terms.size() == 0) {
            // use iri
            return iri;
        }

        // return shortest and lexicographically-least term
        Collections.sort(terms, new Comparator<String>() {
            // Compares two strings first based on length and then lexicographically
            public int compare(String a, String b) {
                if (a.length() < b.length()) {
                    return -1;
                } else if (b.length() < a.length()) {
                    return 1;
                }
                return a.compareTo(b);
            }
        });

        return terms.get(0);
    }

    public static String compactIri(ActiveContext ctx, String iri) {
        return compactIri(ctx, iri, null, false);
    }

    /**
     * Ranks a term that is possible choice for compacting an IRI associated
     * with the given value.
     *
     * @param ctx the active context.
     * @param term the term to rank.
     * @param value the associated value.
     *
     * @return the term rank.
     */
    private static int rankTerm(ActiveContext ctx, String term, Object value) {
        // no term restrictions for a null value
        if (value == null) {
            return 3;
        }

        // get context entry for term
        Map<String, Object> entry = (Map<String, Object>) ctx.mappings.get(term);

        // @list rank is the sum of its values' ranks
        if (value instanceof Map && ((Map<String, Object>) value).containsKey(JSONLD_LIST)) {
            List<Object> list = (List<Object>) ((Map<String, Object>) value).get(JSONLD_LIST);
            if (list.size() == 0) {
                return JSONLD_LIST.equals(entry.get(JSONLD_CONTAINER)) ? 1 : 0;
            }
            // sum term ranks for each list value
            int sum = 0;
            for (Object i : list) {
                sum += rankTerm(ctx, term, i);
            }
            return sum;
        }

        // Note: Value must be an object that is a @value or subject/reference.

        if (value instanceof Map && ((Map<String, Object>) value).containsKey(JSONLD_VALUE)) {
            // value has a @type
            if (((Map<String, Object>) value).containsKey(JSONLD_TYPE)) {
                // @types match
                if (entry.containsKey(JSONLD_TYPE)) {
                    Object vt = ((Map<String, Object>) value).get(JSONLD_TYPE);
                    Object et = entry.get(JSONLD_TYPE);
                    if ((vt == null && et == null) || (vt != null && vt.equals(et))) {
                        return 3;
                    }
                }
                return (!entry.containsKey(JSONLD_TYPE) && !entry.containsKey(JSONLD_LANGUAGE)) ? 1 : 0;
            }

            // rank non-string value
            if (!(((Map<String, Object>) value).get(JSONLD_VALUE) instanceof String)) {
                return (!entry.containsKey(JSONLD_TYPE) && !entry.containsKey(JSONLD_LANGUAGE)) ? 2 : 1;
            }

            // value has no @type or @language
            if (!((Map<String, Object>) value).containsKey(JSONLD_LANGUAGE)) {
                if ((entry.containsKey(JSONLD_LANGUAGE) && entry.get(JSONLD_LANGUAGE) == null)
                        || (!entry.containsKey(JSONLD_TYPE) && !entry.containsKey(JSONLD_LANGUAGE) && !ctx.containsKey(JSONLD_LANGUAGE))) {
                    return 3;
                }
                return 0;
            }

            // @languages match or entry has no @type or @language but default
            // @language matches
            Object vl = ((Map<String, Object>) value).get(JSONLD_LANGUAGE);
            Object el = entry.get(JSONLD_LANGUAGE);
            Object cl = ctx.get(JSONLD_LANGUAGE);
            if ((entry.containsKey(JSONLD_LANGUAGE) && ((vl == null && el == null) || vl.equals(el)))
                    || (!entry.containsKey(JSONLD_TYPE) && !entry.containsKey(JSONLD_LANGUAGE) && (ctx.containsKey(JSONLD_LANGUAGE) && ((vl == null && cl == null) || vl
                    .equals(cl))))) {
                return 3;
            }
            return (!entry.containsKey(JSONLD_TYPE) && !entry.containsKey(JSONLD_LANGUAGE)) ? 1 : 0;
        }

        // value must be a subject/reference
        if (JSONLD_ID.equals(entry.get(JSONLD_TYPE))) {
            return 3;
        }

        return (!entry.containsKey(JSONLD_TYPE) && !entry.containsKey(JSONLD_LANGUAGE)) ? 1 : 0;
    }

    /**
     * Recursively expands an element using the given context. Any context in
     * the element will be removed. All context URLs must have been resolved
     * before calling this method.
     *
     * @param ctx the context to use.
     * @param property the property for the element, null for none.
     * @param element the element to expand.
     *
     * TODO: options the expansion options. propertyIsList true if the property
     * is a list, false if not. NOTE: not implemented in the java version as it
     * seems that it's only (and always) true if property === JSONLD_LIST
     *
     * @return the expanded value.
     * @throws org.xbib.rdf.jsonld.JSONLDProcessingError
     */
    public Object expand(ActiveContext ctx, UniqueNamer namer, String property, Object element) throws JSONLDProcessingError {
        // NOTE: undefined is not really null, and since there's no equivalent in Java we can't test this.
        // infact, it shouldn't actually be possible.
        //if (element == null) {
        //	throw new JSONLDProcessingError("Invalid JSON-LD syntax; undefined element.")
        //		.setType(JSONLDProcessingError.Error.SYNTAX_ERROR);
        //}

        // recursively expand array
        if (element instanceof List) {
            List<Object> rval = new ArrayList();
            for (Object i : (List<Object>) element) {
                // expand element
                Object e = expand(ctx, namer, property, i);
                if (e instanceof List && JSONLD_LIST.equals(property)) {
                    // lists of lists are illegal
                    throw new JSONLDProcessingError("Invalid JSON-LD syntax; lists of lists are not permitted.");
                    // drop null values
                } else if (e != null) {
                    rval.add(e);
                }
            }
            return rval;
        }

        // NOTE: HANDLING TEST-CASE 30 HERE
        // TODO: this will be incomplete as it doesn't seem to be defined yet in the spec
        // and isn't implemented in the javascript code, but as long as the tests pass I don't care!
        if (property != null && ctx.mappings.containsKey(property)
                && ctx.mappings.get(property) instanceof Map && ((Map) ctx.mappings.get(property)).containsKey(JSONLD_CONTAINER)
                && JSONLD_LANGUAGE.equals(((Map) ctx.mappings.get(property)).get(JSONLD_CONTAINER))) {
            // prob becomes @language
            // value becomes @value
            List<Object> rval = new ArrayList<Object>();
            for (String key : ((Map<String, Object>) element).keySet()) {
                Object value = ((Map<String, Object>) element).get(key);
                value = expand(ctx, namer, null, value);
                value = handleNestedLanguageContainer(value, key);
                if (value instanceof List) {
                    rval.addAll((List) value);
                } else {
                    rval.add(value);
                }
            }
            return rval;
        }

        // recursively expand object
        if (element instanceof Map) {
            // access helper
            Map<String, Object> elem = (Map<String, Object>) element;

            // if element has a context, process it
            if (elem.containsKey(JSONLD_CONTEXT)) {
                ctx = processContext(ctx, elem.get(JSONLD_CONTEXT));
                elem.remove(JSONLD_CONTEXT);
            }

            Map<String, Object> rval = new HashMap<String, Object>();
            for (String key : elem.keySet()) {
                // expand property
                String prop = expandTerm(ctx, key, null, true, false);

                // handle ignored keys
                if (opts.isIgnored(key)) {
                    //JSONLDUtils.addValue(rval, key, elem.get(key), false);
                    rval.put(key, elem.get(key));
                    continue;
                }

                // drop non-absolute IRI keys that aren't keywords
                if (!JSONLDUtils.isAbsoluteIri(prop) && !JSONLDUtils.isKeyword(prop, ctx)) {
                    continue;
                }

                // if value is null and property is not @value, continue
                Object value = elem.get(key);
                if (value == null && !JSONLD_VALUE.equals(prop)) {
                    continue;
                }

                // syntax error if @id is not a string
                if (JSONLD_ID.equals(prop) && !(value instanceof String)) {
                    throw new JSONLDProcessingError("Invalid JSON-LD syntax; \"@id\" value must a string.")
                            .setDetail("value", value);
                }

                // validate @type value
                if (JSONLD_TYPE.equals(prop)) {
                    validateTypeValue(value);
                }

                // @graph must be an array or an object
                if (JSONLD_GRAPH.equals(prop) && !(value instanceof Map || value instanceof List)) {
                    throw new JSONLDProcessingError("Invalid JSON-LD syntax; \"@graph\" value must be an object or an array.")
                            .setDetail("value", value);
                }

                // @value must not be an object or an array
                if (JSONLD_VALUE.equals(prop) && (value instanceof Map || value instanceof List)) {
                    throw new JSONLDProcessingError("Invalid JSON-LD syntax; \"@value\" value must not be an object or an array.")
                            .setDetail("value", value);
                }

                // @language must be a string
                if (JSONLD_LANGUAGE.equals(prop) && !(value instanceof String)) {
                    throw new JSONLDProcessingError("Invalid JSON-LD syntax; \"@language\" value must be a string.")
                            .setDetail("value", value);
                }

                // recurse into @list or @set keeping the active property
                if (JSONLD_LIST.equals(prop) || JSONLD_SET.equals(prop)) {
                    value = expand(ctx, namer, property, value);
                    if (JSONLD_LIST.equals(prop) && (value instanceof Map && ((Map<String, Object>) value).containsKey(JSONLD_LIST))) {
                        throw new JSONLDProcessingError("Invalid JSON-LD syntax; lists of lists are not permitted.");
                    }
                } else {
                    // update active property and recursively expand value
                    property = key;
                    value = expand(ctx, namer, property, value);
                }

                // drop null values if property is not @value (dropped below)
                if (value != null || JSONLD_VALUE.equals(prop)) {
                    // convert value to @list if container specifies it
                    if (!JSONLD_LIST.equals(prop) && !(value instanceof Map && ((Map<String, Object>) value).containsKey(JSONLD_LIST))) {
                        Object container = ctx.getContextValue(property, JSONLD_CONTAINER);
                        if (JSONLD_LIST.equals(container)) {
                            // ensure value is an array
                            Map<String, Object> tmp = new HashMap<String, Object>();
                            List<Object> tl;
                            if (value instanceof List) {
                                tl = (List) value;
                            } else {
                                tl = new ArrayList<Object>();
                                tl.add(value);
                            }
                            tmp.put(JSONLD_LIST, tl);
                            value = tmp;
                        }
                    }

                    // optimize away @id for @type
                    if (JSONLD_TYPE.equals(prop)) {
                        if (value instanceof Map && ((Map<String, Object>) value).containsKey(JSONLD_ID)) {
                            value = ((Map<String, Object>) value).get(JSONLD_ID);
                        } else if (value instanceof List) {
                            List<Object> val = new ArrayList<Object>();
                            for (Object v : (List) value) {
                                if (v instanceof Map && ((Map<String, Object>) v).containsKey(JSONLD_ID)) {
                                    val.add(((Map<String, Object>) v).get(JSONLD_ID));
                                } else {
                                    val.add(v);
                                }
                            }
                            value = val;
                        }
                    }

                    // add value, use an array if not @id, @type, @value, or @language
                    boolean useArray = !(JSONLD_ID.equals(prop) || JSONLD_TYPE.equals(prop) || JSONLD_VALUE.equals(prop) || JSONLD_LANGUAGE.equals(prop));
                    JSONLDUtils.addValue(rval, prop, value, useArray);
                }
            }

            // @value must only have @language or @type
            if (rval.containsKey(JSONLD_VALUE)) {
                if ((rval.size() == 2 && !rval.containsKey(JSONLD_TYPE) && !rval.containsKey(JSONLD_LANGUAGE)) || rval.size() > 2) {
                    throw new JSONLDProcessingError(
                            "Invalid JSON-LD syntax; an element containing \"@value\" must have at most one other property which can be \"@type\" or \"@language\".")
                            .setDetail("element", rval);
                }
                // value @type must be a string
                if (rval.containsKey(JSONLD_TYPE) && !(rval.get(JSONLD_TYPE) instanceof String)) {
                    throw new JSONLDProcessingError("Invalid JSON-LD syntax; the \"@type\" value of an element containing \"@value\" must be a string.")
                            .setDetail("element", rval);
                }
                // drop null @values
                if (rval.get(JSONLD_VALUE) == null) {
                    rval = null;
                }
                // convert @type to an array
            } else if (rval.containsKey(JSONLD_TYPE) && !(rval.get(JSONLD_TYPE) instanceof List)) {
                List<Object> tmp = new ArrayList<Object>();
                tmp.add(rval.get(JSONLD_TYPE));
                rval.put(JSONLD_TYPE, tmp);
                // handle @set and @list
            } else if (rval.containsKey(JSONLD_SET) || rval.containsKey(JSONLD_LIST)) {
                if (rval.size() != 1) {
                    throw new JSONLDProcessingError(
                            "Invalid JSON-LD syntax; if an element has the property \"@set\" or \"@list\", then it must be its only property.")
                            .setDetail("element", rval);
                }
                // optimize away @set
                if (rval.containsKey(JSONLD_SET)) {
                    return rval.get(JSONLD_SET);
                }
                // drop objects with only @language
            } else if (rval.containsKey(JSONLD_LANGUAGE) && rval.size() == 1) {
                rval = null;
            }

            if (opts.addBlankNodeIDs && JSONLDUtils.isSubject(rval) && !rval.containsKey(JSONLD_ID)) {
                rval.put(JSONLD_ID, namer.getName());
            }

            return rval;
        }

        // expand element according to value expansion rules
        return expandValue(ctx, property, element, opts.getBase());
    }

    /**
     * Used in the handling of
     *
     * @language containers
     *
     * @param value
     * @param lang
     * @return
     */
    private Object handleNestedLanguageContainer(Object value, String lang) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            // since we expand out values before we call this function, a string @value should be represented as a map with
            // an @value tag again, so we'll ignore these cases
            return value;
        }
        if (value instanceof List) {
            List<Object> rval = new ArrayList<Object>();
            for (Object v : ((List) value)) {
                rval.add(handleNestedLanguageContainer(v, lang));
            }
            return rval;
        }
        // only thing left is a map
        Map<String, Object> rval;

        // if that map already has a @value key, just add a @language tag to it
        if (((Map<String, Object>) value).containsKey(JSONLD_VALUE)) {
            rval = (Map<String, Object>) JSONLDUtils.clone(value);
            rval.put(JSONLD_LANGUAGE, lang);
            return rval;
        }

        rval = new HashMap();
        for (String key : ((Map<String, Object>) value).keySet()) {
            rval.put(key, handleNestedLanguageContainer(((Map<String, Object>) value).get(key), lang));
        }
        return rval;

    }

    /**
     * Recursively compacts an element using the given active context. All
     * values must be in expanded form before this method is called.
     *
     * @param ctx the active context to use.
     * @param property the property that points to the element, null for none.
     * @param element the element to compact.
     *
     * @return the compacted value.
     * @throws org.xbib.rdf.jsonld.JSONLDProcessingError
     */
    public Object compact(ActiveContext ctx, String property, Object element) throws JSONLDProcessingError {

        // recursively compact array
        if (element instanceof List) {
            List<Object> rval = new ArrayList<Object>();
            for (Object i : (List) element) {
                Object e = compact(ctx, property, i);
                // drop null values
                if (e != null) {
                    rval.add(e);
                }
            }
            if (rval.size() == 1) {
                // use single element if no container is specified
                Object container = ctx.getContextValue(property, JSONLD_CONTAINER);
                if (!(JSONLD_LIST.equals(container) || JSONLD_SET.equals(container))) {
                    return rval.get(0);
                }
            }
            return rval;
        }

        // recursively compact object
        if (element instanceof Map) {
            // access helper
            Map<String, Object> elem = (Map<String, Object>) element;
            // element is a @value
            if (elem.containsKey(JSONLD_VALUE)) {
                // get type and language context rules
                Object type = ctx.getContextValue(property, JSONLD_TYPE);
                Object language = ctx.getContextValue(property, JSONLD_LANGUAGE);

                // if @value is the only key
                if (elem.size() == 1) {
                    // if there is no default language or @value is not a string,
                    // return value of @value
                    // NOTE: language == null check done to make test compact-0015 pass
                    if (language == null || !(elem.get(JSONLD_VALUE) instanceof String)) {
                        return elem.get(JSONLD_VALUE);
                    }
                    // return full element, alias @value
                    Map<String, Object> rval = new HashMap<String, Object>();
                    rval.put(compactIri(ctx, JSONLD_VALUE), elem.get(JSONLD_VALUE));
                    return rval;
                }

                // matching @type specified in context, compact element
                if (type != null && elem.containsKey(JSONLD_TYPE) && type.equals(elem.get(JSONLD_TYPE))) {
                    return elem.get(JSONLD_VALUE);
                    // matching @language specified in context, compact element
                } else if (language != null && elem.containsKey(JSONLD_LANGUAGE) && language.equals(elem.get(JSONLD_LANGUAGE))) {
                    return elem.get(JSONLD_VALUE);
                } else {
                    Map<String, Object> rval = new HashMap<String, Object>();
                    if (elem.containsKey(JSONLD_TYPE)) {
                        rval.put(compactIri(ctx, JSONLD_TYPE), compactIri(ctx, (String) elem.get(JSONLD_TYPE), null, true));
                    } // alias @language
                    else if (elem.containsKey(JSONLD_LANGUAGE)) {
                        rval.put(compactIri(ctx, JSONLD_LANGUAGE), elem.get(JSONLD_LANGUAGE));
                    }
                    rval.put(compactIri(ctx, JSONLD_VALUE), elem.get(JSONLD_VALUE));
                    return rval;
                }
            }

            // compact subject references
            if (elem.size() == 1 && elem.containsKey(JSONLD_ID)) {
                Object type = ctx.getContextValue(property, JSONLD_TYPE);
                if (JSONLD_ID.equals(type) || JSONLD_GRAPH.equals(property)) {
                    return compactIri(ctx, (String) elem.get(JSONLD_ID));
                }
            }

            // recursively process element keys
            Map<String, Object> rval = new HashMap<String, Object>();
            for (String key : elem.keySet()) {
                Object value = elem.get(key);

                // handle ignored keys
                if (opts.isIgnored(key)) {
                    //JSONLDUtils.addValue(rval, key, value, false);
                    rval.put(key, value);
                    continue;
                }

                // compact @id and @type(s)
                if (JSONLD_ID.equals(key) || JSONLD_TYPE.equals(key)) {
                    if (value instanceof String) {
                        value = compactIri(ctx, (String) value, null, JSONLD_TYPE.equals(key));
                        // value must be a @type array
                    } else {
                        List<String> types = new ArrayList<String>();
                        for (String i : (List<String>) value) {
                            types.add(compactIri(ctx, i, null, JSONLD_TYPE.equals(key)));
                        }
                        value = types;
                    }

                    // compact property and add value
                    String prop = compactIri(ctx, key, null, JSONLD_TYPE.equals(key));
                    JSONLDUtils.addValue(rval, prop, value, value instanceof List && ((List) value).size() == 0);
                    continue;
                }

                // NOTE: value must be an array due to expansion algorithm

                // preserve empty arrays
                if (((List) value).size() == 0) {
                    String prop = compactIri(ctx, key, null, true);
                    JSONLDUtils.addValue(rval, prop, new ArrayList<Object>(), true);
                }

                // recusively process array values
                for (Object v : (List) value) {
                    boolean isList = (v instanceof Map && ((Map<String, Object>) v).containsKey(JSONLD_LIST));

                    // compact property
                    String prop = compactIri(ctx, key, v, true);

                    // remove @list for recursion (will be re-added if necessary)
                    if (isList) {
                        v = ((Map<String, Object>) v).get(JSONLD_LIST);
                    }

                    // recursively compact value
                    v = compact(ctx, prop, v);

                    // get container type for property
                    Object container = ctx.getContextValue(prop, JSONLD_CONTAINER);

                    // handle @list
                    if (isList && !JSONLD_LIST.equals(container)) {
                        // handle messy @list compaction
                        if (rval.containsKey(prop) && opts.getStrict()) {
                            throw new JSONLDProcessingError(
                                    "JSON-LD compact error; property has a \"@list\" @container rule but there is more than a single @list that matches the compacted term in the document. Compaction might mix unwanted items into the list.");
                        }
                        // reintroduce @list keyword
                        String kwlist = compactIri(ctx, JSONLD_LIST, null, true);
                        Map<String, Object> val = new HashMap<String, Object>();
                        val.put(kwlist, v);
                        v = val;

                    }
                    // if @container is @set or @list or value is an empty array, use
                    // an array when adding value
                    boolean isArray = (JSONLD_SET.equals(container) || JSONLD_LIST.equals(container) || (v instanceof List && ((List) v).size() == 0));

                    // add compact value
                    JSONLDUtils.addValue(rval, prop, v, isArray);
                }
            }
            return rval;
        }

        // only primitives remain which are already compact
        return element;
    }

    /**
     * Performs JSON-LD framing.
     *
     * @param input the expanded JSON-LD to frame.
     * @param frame the expanded JSON-LD frame to use. options the framing
     * options.
     *
     * @return the framed output.
     * @throws org.xbib.rdf.jsonld.JSONLDProcessingError
     */
    public Object frame(Object input, Object frame) throws JSONLDProcessingError {
        // create framing state
        FramingContext state = new FramingContext(opts);
        //Map<String,Object> state = new HashMap<String, Object>();
        //state.put("options", this.opts);
        state.graphs = new HashMap<String, Object>();
        state.graphs.put(JSONLD_DEFAULT, new HashMap<String, Object>());
        state.graphs.put("@merged", new HashMap<String, Object>());

        // produce a map of all graphs and name each bnode
        UniqueNamer namer = new UniqueNamer("_:t");
        flatten(input, state.graphs, JSONLD_DEFAULT, namer);
        namer = new UniqueNamer("_:t");
        flatten(input, state.graphs, "@merged", namer);
        // FIXME: currently uses subjects from @merged graph only
        state.subjects = (Map<String, Object>) state.graphs.get("@merged");

        // frame the subjects
        List framed = new ArrayList();
        frame(state, state.subjects.keySet(), frame, framed, null);
        return framed;
    }

    /**
     * Frames subjects according to the given frame.
     *
     * @param state the current framing state.
     * @param subjects the subjects to filter.
     * @param frame the frame.
     * @param parent the parent subject or top-level array.
     * @param property the parent property, initialized to null.
     * @throws org.xbib.rdf.jsonld.JSONLDProcessingError
     */
    private void frame(FramingContext state, Collection<String> subjects,
            Object frame, Object parent, String property) throws JSONLDProcessingError {
        // validate the frame
        validateFrame(state, frame);
        // NOTE: once validated we move to the function where the frame is specifically a map
        frame(state, subjects, (Map) ((List) frame).get(0), parent, property);
    }

    private void frame(FramingContext state, Collection<String> subjects,
            Map<String, Object> frame, Object parent, String property) throws JSONLDProcessingError {
        // filter out subjects that match the frame
        Map<String, Object> matches = filterSubjects(state, subjects, frame);

        // get flags for current frame
        Options options = state.options;
        Boolean embedOn = (frame.containsKey(JSONLD_EMBED)) ? (Boolean) ((List) frame.get(JSONLD_EMBED)).get(0) : options.embed;
        Boolean explicicOn = (frame.containsKey(JSONLD_EXPLICIT)) ? (Boolean) ((List) frame.get(JSONLD_EXPLICIT)).get(0) : options.explicit;

        // add matches to output
        for (String id : matches.keySet()) {

            // Note: In order to treat each top-level match as a compartmentalized
            // result, create an independent copy of the embedded subjects map when the
            // property is null, which only occurs at the top-level.
            if (property == null) {
                state.embeds = new HashMap<String, Object>();
            }

            // start output
            Map<String, Object> output = new HashMap<String, Object>();
            output.put(JSONLD_ID, id);

            // prepare embed meta info
            Map<String, Object> embed = new HashMap<String, Object>();
            embed.put("parent", parent);
            embed.put("property", property);

            // if embed is on and there is an existing embed
            if (embedOn && state.embeds.containsKey(id)) {
                // only overwrite an existing embed if it has already been added to its
                // parent -- otherwise its parent is somewhere up the tree from this
                // embed and the embed would occur twice once the tree is added
                embedOn = false;

                // existing embed's parent is an array
                Map<String, Object> existing = (Map<String, Object>) state.embeds.get(id);
                if (existing.get("parent") instanceof List) {
                    for (Object o : (List) existing.get("parent")) {
                        if (JSONLDUtils.compareValues(output, o)) {
                            embedOn = true;
                            break;
                        }
                    }
                    // existing embed's parent is an object
                } else if (JSONLDUtils.hasValue((Map<String, Object>) existing.get("parent"), (String) existing.get("property"), output)) {
                    embedOn = true;
                }

                // existing embed has already been added, so allow an overwrite
                if (embedOn) {
                    removeEmbed(state, id);
                }
            }

            // not embedding, add output without any other properties
            if (!embedOn) {
                addFrameOutput(state, parent, property, output);
            } else {
                // add embed meta info
                state.embeds.put(id, embed);

                // iterate over subject properties
                Map<String, Object> subject = (Map<String, Object>) matches.get(id);
                for (String prop : subject.keySet()) {

                    // handle ignored keys
                    if (opts.isIgnored(prop)) {
                        output.put(prop, JSONLDUtils.clone(subject.get(prop)));
                        continue;
                    }

                    // copy keywords to output
                    if (prop instanceof String && JSONLDUtils.isKeyword(prop)) {
                        output.put(prop, JSONLDUtils.clone(subject.get(prop)));
                        continue;
                    }

                    // if property isn't in the frame
                    if (!frame.containsKey(prop)) {
                        // if explicit is off, embed values
                        if (!explicicOn) {
                            embedValues(state, subject, prop, output);
                        }
                        continue;
                    }

                    // add objects
                    Object objects = subject.get(prop);
                    // TODO: i've done some crazy stuff here because i'm unsure if objects is always a list or if it can
                    // be a map as well
                    for (Object i : objects instanceof List ? (List) objects : ((Map) objects).keySet()) {
                        Object o = objects instanceof List ? i : ((Map) objects).get(i);

                        // recurse into list
                        if (o instanceof Map && ((Map) o).containsKey(JSONLD_LIST)) {
                            // add empty list
                            Map<String, Object> list = new HashMap();
                            list.put(JSONLD_LIST, new ArrayList());
                            addFrameOutput(state, output, prop, list);

                            // add list objects
                            List src = (List) ((Map) o).get(JSONLD_LIST);
                            for (Object n : src) {
                                // recurse into subject reference
                                if (n instanceof Map && ((Map) n).size() == 1 && ((Map) n).containsKey(JSONLD_ID)) {
                                    List tmp = new ArrayList();
                                    tmp.add(((Map) n).get(JSONLD_ID));
                                    frame(state, tmp, frame.get(prop), list, JSONLD_LIST);
                                } else {
                                    // include other values automatcially
                                    addFrameOutput(state, list, JSONLD_LIST, (Map<String, Object>) JSONLDUtils.clone(n));
                                }
                            }
                            continue;
                        }

                        // recurse into subject reference
                        if (o instanceof Map && ((Map) o).size() == 1 && ((Map) o).containsKey(JSONLD_ID)) {
                            List tmp = new ArrayList();
                            tmp.add(((Map) o).get(JSONLD_ID));
                            frame(state, tmp, frame.get(prop), output, prop);
                        } else {
                            // include other values automatically
                            addFrameOutput(state, output, prop, (Map<String, Object>) JSONLDUtils.clone(o));
                        }
                    }
                }

                // handle defaults
                List<String> props = new ArrayList<String>();
                props.addAll(frame.keySet());
                Collections.sort(props);
                for (String prop : props) {
                    // skip keywords
                    if (JSONLDUtils.isKeyword(prop)) {
                        continue;
                    }

                    // if omit default is off, then include default values for properties
                    // that appear in the next frame but are not in the matching subject
                    Map<String, Object> next = (Map<String, Object>) ((List<Object>) frame.get(prop)).get(0);
                    boolean omitDefaultOn =
                            (next.containsKey(JSONLD_OMITDEFAULT)) ? (Boolean) ((List) next.get(JSONLD_OMITDEFAULT)).get(0) : options.omitDefault;
                    if (!omitDefaultOn && !output.containsKey(prop)) {
                        Object preserve = "@null";
                        if (next.containsKey(JSONLD_DEFAULT)) {
                            preserve = JSONLDUtils.clone(next.get(JSONLD_DEFAULT));
                        }
                        Map<String, Object> tmp = new HashMap<String, Object>();
                        tmp.put(JSONLD_PRESERVE, preserve);
                        output.put(prop, tmp);
                    }
                }

                // add output to parent
                addFrameOutput(state, parent, property, output);
            }
        }
    }

    /**
     * Embeds values for the given subject and property into the given output
     * during the framing algorithm.
     *
     * @param state the current framing state.
     * @param subject the subject.
     * @param property the property.
     * @param output the output.
     */
    private void embedValues(FramingContext state,
            Map<String, Object> subject, String property, Object output) {
        // embed subject properties in output
        Object objects = subject.get(property);

        // NOTE: more crazyness due to lack of knowledge about whether objects should
        // be an array or an object
        for (Object i : objects instanceof List ? (List) objects : ((Map) objects).keySet()) {
            Object o = objects instanceof List ? i : ((Map) objects).get(i);

            // recurse into @list
            if (o instanceof Map && ((Map) o).containsKey(JSONLD_LIST)) {
                Map<String, Object> list = new HashMap();
                list.put(JSONLD_LIST, new ArrayList());
                addFrameOutput(state, output, property, list);
                embedValues(state, (Map<String, Object>) o, JSONLD_LIST, list.get(JSONLD_LIST));
                return;
            }

            // handle subject reference
            if (o instanceof Map && ((Map) o).size() == 1 && ((Map) o).containsKey(JSONLD_ID)) {
                String id = (String) ((Map<String, Object>) o).get(JSONLD_ID);

                // embed full subject if isn't already embedded
                if (!state.embeds.containsKey(id)) {
                    // add embed
                    Map<String, Object> embed = new HashMap<String, Object>();
                    embed.put("parent", output);
                    embed.put("property", property);
                    state.embeds.put(id, embed);

                    // recurse into subject
                    o = new HashMap<String, Object>();
                    Map<String, Object> s = (Map<String, Object>) state.subjects.get(id);
                    for (String prop : s.keySet()) {
                        // copy keywords
                        if (JSONLDUtils.isKeyword(prop) || opts.isIgnored(prop)) {
                            ((Map<String, Object>) o).put(prop, JSONLDUtils.clone(s.get(prop)));
                            continue;
                        }
                        embedValues(state, s, prop, o);
                    }
                }
                addFrameOutput(state, output, property, o);
            } // copy non-subject value
            else {
                addFrameOutput(state, output, property, JSONLDUtils.clone(o));
            }
        }

    }

    /**
     * Adds framing output to the given parent.
     *
     * @param state the current framing state.
     * @param parent the parent to add to.
     * @param property the parent property.
     * @param output the output to add.
     */
    private static void addFrameOutput(FramingContext state, Object parent,
            String property, Object output) {
        if (parent instanceof Map) {
            JSONLDUtils.addValue((Map<String, Object>) parent, property, output, true);
        } else {
            ((List) parent).add(output);
        }

    }

    private static void removeEmbed(FramingContext state, String id) {
        // get existing embed
        Map<String, Object> embeds = state.embeds;
        Object parent = ((Map<String, Object>) embeds.get(id)).get("parent");
        String property = (String) ((Map<String, Object>) embeds.get(id)).get("property");

        // create reference to replace embed
        Map<String, Object> subject = new HashMap<String, Object>();
        subject.put(JSONLD_ID, id);

        // remove existing embed
        if (parent instanceof List) {
            // replace subject with reference
            for (int i = 0; i < ((List) parent).size(); i++) {
                if (JSONLDUtils.compareValues(((List) parent).get(i), subject)) {
                    ((List) parent).set(i, subject);
                    break;
                }
            }
        } else {
            // replace subject with reference
            JSONLDUtils.removeValue(((Map<String, Object>) parent), property, subject, ((Map<String, Object>) parent).get(property) instanceof List);
            JSONLDUtils.addValue(((Map<String, Object>) parent), property, subject, ((Map<String, Object>) parent).get(property) instanceof List);
        }

        // recursively remove dependent dangling embeds
        removeDependents(embeds, id);
    }

    private static void removeDependents(Map<String, Object> embeds, String id) {
        Set<String> ids = embeds.keySet();
        for (String next : ids) {
            if (embeds.containsKey(next)
                    && ((Map<String, Object>) embeds.get(next)).get("parent") instanceof Map
                    && id.equals(((Map<String, Object>) ((Map<String, Object>) embeds.get(next)).get("parent")).get(JSONLD_ID))) {
                embeds.remove(next);
                removeDependents(embeds, next);
            }
        }
    }

    /**
     * Returns a map of all of the subjects that match a parsed frame.
     *
     * @param state the current framing state.
     * @param subjects the set of subjects to filter.
     * @param frame the parsed frame.
     *
     * @return all of the matched subjects.
     */
    private static Map<String, Object> filterSubjects(FramingContext state,
            Collection<String> subjects, Map<String, Object> frame) {
        // filter subjects in @id order
        Map<String, Object> rval = new HashMap<String, Object>();
        for (String id : subjects) {
            Map<String, Object> subject = (Map<String, Object>) state.subjects.get(id);
            if (filterSubject(subject, frame)) {
                rval.put(id, subject);
            }
        }
        return rval;
    }

    /**
     * Returns true if the given subject matches the given frame.
     *
     * @param subject the subject to check.
     * @param frame the frame to check.
     *
     * @return true if the subject matches, false if not.
     */
    private static boolean filterSubject(Map<String, Object> subject, Map<String, Object> frame) {
        // check @type (object value means 'any' type, fall through to ducktyping)
        Object t = frame.get(JSONLD_TYPE);
        // TODO: it seems @type should always be a list
        if (frame.containsKey(JSONLD_TYPE) && !(t instanceof List && ((List) t).size() == 1 && ((List) t).get(0) instanceof Map)) {
            for (Object i : (List) t) {
                if (JSONLDUtils.hasValue(subject, JSONLD_TYPE, i)) {
                    return true;
                }
            }
            return false;
        }

        // check ducktype
        for (String key : frame.keySet()) {
            if (JSONLD_ID.equals(key) || !JSONLDUtils.isKeyword(key) && !(subject.containsKey(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates a JSON-LD frame, throwing an exception if the frame is invalid.
     *
     * @param state the current frame state.
     * @param frame the frame to validate.
     * @throws org.xbib.rdf.jsonld.JSONLDProcessingError
     */
    private static void validateFrame(FramingContext state, Object frame) throws JSONLDProcessingError {
        if (!(frame instanceof List) || ((List) frame).size() != 1 || !(((List) frame).get(0) instanceof Map)) {
            throw new JSONLDProcessingError("Invalid JSON-LD syntax; a JSON-LD frame must be a single object.")
                    .setDetail("frame", frame);
        }
    }

    /**
     * Removes the
     *
     * @preserve keywords as the last step of the framing algorithm.
     *
     * @param ctx the active context used to compact the input.
     * @param input the framed, compacted output.
     *
     * @return the resulting output.
     */
    public Object removePreserve(ActiveContext ctx, Object input) {
        // recurse through arrays
        if (input instanceof List) {
            List<Object> l = new ArrayList<Object>();
            for (Object i : (List) input) {
                Object r = removePreserve(ctx, i);
                if (r != null) {
                    l.add(r);
                }
            }
            input = l;
        } else if (input instanceof Map) {
            // remove @preserve
            Map<String, Object> imap = (Map<String, Object>) input;
            if (imap.containsKey(JSONLD_PRESERVE)) {
                if ("@null".equals(imap.get(JSONLD_PRESERVE))) {
                    return null;
                }
                return imap.get(JSONLD_PRESERVE);
            }

            if (imap.containsKey(JSONLD_VALUE)) {
                return input;
            }

            if (imap.containsKey(JSONLD_LIST)) {
                imap.put(JSONLD_LIST, removePreserve(ctx, imap.get(JSONLD_LIST)));
                return input;
            }

            for (String key : imap.keySet()) {
                if (!opts.isIgnored(key)) {
                    Object res = removePreserve(ctx, imap.get(key));
                    Object container = ctx.getContextValue(key, JSONLD_CONTAINER);
                    if (res instanceof List && ((List) res).size() == 1 && !JSONLD_SET.equals(container) && !JSONLD_LIST.equals(container)) {
                        res = ((List<String>) res).get(0);
                    }
                    imap.put(key, res);
                }
            }
        }
        return input;
    }

    /**
     * Recursively flattens the subjects in the given JSON-LD expanded input.
     *
     * @param input the JSON-LD expanded input.
     * @param graphs a map of graph name to subject map.
     * @param graph the name of the current graph.
     * @param namer the blank node namer.
     * @param name the name assigned to the current input if it is a bnode.
     * @param list the list to append to, null for none.
     */
    private void flatten(Object input, Map<String, Object> graphs, String graph, UniqueNamer namer, String name, List<Object> list) {
        // recurse through array
        if (input instanceof List) {
            for (Object i : (List) input) {
                flatten(i, graphs, graph, namer, null, list);
            }
            return;
        }

        // add non-object or value
        if (!(input instanceof Map) || ((Map) input).containsKey(JSONLD_VALUE)) {
            if (list != null) {
                list.add(input);
            }
            return;
        }

        // TODO: isUndefined (in js this is different from === null
        // get name for subject
        if (name == null) {
            name = JSONLDUtils.isBlankNode(input) ? namer.getName((String) ((Map<String, Object>) input).get(JSONLD_ID)) : (String) ((Map<String, Object>) input).get(JSONLD_ID);
        }

        // add subject reference to list
        if (list != null) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(JSONLD_ID, name);
            list.add(map);
        }

        // create new subject or merge into existing one
        Map<String, Object> subjects = (Map<String, Object>) graphs.get(graph);
        Map<String, Object> subject;
        if (subjects.containsKey(name)) {
            subject = (Map<String, Object>) subjects.get(name);
        } else {
            subject = new HashMap<String, Object>();
            subjects.put(name, subject);
        }
        subject.put(JSONLD_ID, name);
        for (String prop : ((Map<String, Object>) input).keySet()) {
            // skip @id
            if (JSONLD_ID.equals(prop)) {
                continue;
            }

            // recurse into graph
            if (JSONLD_GRAPH.equals(prop)) {
                // add graph subjects map entry
                if (!graphs.containsKey(name)) {
                    graphs.put(name, new HashMap<String, Object>());
                }
                String g = "@merged".equals(graph) ? graph : name;
                flatten(((Map<String, Object>) input).get(prop), graphs, g, namer, null, null);
                continue;
            }

            // copy ignored keys
            if (opts.isIgnored(prop)) {
                subject.put(prop, ((Map<String, Object>) input).get(prop));
                continue;
            }

            // copy non-@type keywords
            if (!JSONLD_TYPE.equals(prop) && JSONLDUtils.isKeyword(prop)) {
                subject.put(prop, ((Map<String, Object>) input).get(prop));
                continue;
            }

            // iterate over objects
            Object objects = ((Map<String, Object>) input).get(prop);
            Object[] keys = null;
            int len = 0;
            if (objects instanceof Map) {
                keys = ((Map<String, Object>) objects).keySet().toArray();
                len = keys.length;
            } else {
                len = ((List) objects).size();
            }
            for (int i = 0; i < len; i++) {
                Object o;
                if (objects instanceof Map) {
                    o = ((Map) objects).get(keys[i]);
                } else {
                    o = ((List) objects).get(i);
                }

                if (JSONLDUtils.isSubject(o) || JSONLDUtils.isSubjectReference(o)) {
                    // rename blank node @id
                    String id = (JSONLDUtils.isBlankNode(o) ? namer.getName((String) ((Map<String, Object>) o).get(JSONLD_ID)) : (String) ((Map<String, Object>) o).get(JSONLD_ID));

                    // add reference and recurse
                    Map<String, Object> tmp = new HashMap<String, Object>();
                    tmp.put(JSONLD_ID, id);
                    JSONLDUtils.addValue(subject, prop, tmp, true);
                    flatten(o, graphs, graph, namer, id);
                } else {
                    // recurse into list
                    if (o instanceof Map && ((Map) o).containsKey(JSONLD_LIST)) {
                        List<Object> _list = new ArrayList<Object>();
                        flatten(((Map) o).get(JSONLD_LIST), graphs, graph, namer, name, _list);
                        o = new HashMap<String, Object>();
                        ((Map<String, Object>) o).put(JSONLD_LIST, _list);
                        // special-handle @type IRIs
                    } else if (JSONLD_TYPE.equals(prop) && o instanceof String && ((String) o).startsWith("_:")) {
                        o = namer.getName((String) o);
                    }

                    // add non-subject
                    JSONLDUtils.addValue(subject, prop, o, true);
                }
            }
        }
    }

    private void flatten(Object input, Map<String, Object> graphs, String graph, UniqueNamer namer, String name) {
        flatten(input, graphs, graph, namer, name, null);
    }

    private void flatten(Object input, Map<String, Object> graphs, String graph, UniqueNamer namer) {
        flatten(input, graphs, graph, namer, null, null);
    }




    /**
     * Compares two RDF statements for equality.
     *
     * @param s1 the first triple.
     * @param s2 the second triple.
     *
     * @return true if the statements are the same, false if not.
     */
    public static boolean compareRdfStatements(Map<String, Object> s1, Map<String, Object> s2) {
        for (String attr : new String[]{"subject", "property", "object"}) {
            Object s1int = ((Map<String, Object>) s1.get(attr)).get("interfaceName");
            Object s2int = ((Map<String, Object>) s2.get(attr)).get("interfaceName");
            Object s1nom = ((Map<String, Object>) s1.get(attr)).get("nominalValue");
            Object s2nom = ((Map<String, Object>) s2.get(attr)).get("nominalValue");
            if (!(s1int != null && s1int.equals(s2int) || s1int == null && s2int == null)
                    || !(s1nom != null && s1nom.equals(s2nom) || s1nom == null && s2nom == null)) {
                return false;
            }
        }
        Object s1lang = ((Map<String, Object>) s1.get("object")).get("language");
        Object s2lang = ((Map<String, Object>) s2.get("object")).get("language");
        if (!(s1lang != null && s1lang.equals(s2lang) || s1lang == null && s2lang == null)) {
            return false;
        }

        if (((Map<String, Object>) s1.get("object")).containsKey("datatype") != ((Map<String, Object>) s2.get("object")).containsKey("datatype")) {
            return false;
        }

        if (((Map<String, Object>) s1.get("object")).containsKey("datatype")) {
            Object s1int = ((Map<String, Object>) s1.get("object")).get("interfaceName");
            Object s2int = ((Map<String, Object>) s2.get("object")).get("interfaceName");
            Object s1nom = ((Map<String, Object>) s1.get("object")).get("nominalValue");
            Object s2nom = ((Map<String, Object>) s2.get("object")).get("nominalValue");
            if (!(s1int != null && s1int.equals(s2int) || s1int == null && s2int == null)
                    || !(s1nom != null && s1nom.equals(s2nom) || s1nom == null && s2nom == null)) {
                return false;
            }
        }

        Object s1name = s1.get("name");
        Object s2name = s2.get("name");
        if (!(s1name != null && s1name.equals(s2name) || s1name == null && s2name == null)) {
            return false;
        }
        return true;
    }

    public static boolean compareRdfStatements(String s1, String s2) {
        return s1 != null && s1.equals(s2) || s1 == null && s2 == null;
    }

    /**
     * Performs RDF normalization on the given JSON-LD input.
     *
     * @param input the expanded JSON-LD object to normalize. options the
     * normalization options. callback(err, normalized) called once the
     * operation completes.
     */
    public List normalize(Object input) {
        NormalizeCallback cb = new NormalizeCallback();
        internalToRDF(input, new UniqueNamer("_:t"), null, null, null, cb);
        cb.finalise();
        return null;
    }

    /**
     * Outputs the RDF statements found in the given JSON-LD element.
     *
     * @param element the JSON-LD element.
     * @param namer the UniqueNamer for assigning bnode names.
     * @param subject the active subject.
     * @param property the active property.
     * @param graph the graph name.
     * @param callback(err, triple) called when a triple is output, with
     * the last triple as null.
     */
    public void toRDF(Object element, UniqueNamer namer, String subject, String property, Object graph, JSONLDTripleCallback callback) {
        CallbackWrapper cbw = new ToRDFCallback(callback);
        internalToRDF(element, namer, subject, property, graph, cbw);
        cbw.callback(null);
    }

    /**
     * Recursively outputs the RDF statements found in the given JSON-LD
     * element.
     *
     * @param elem the JSON-LD element.
     * @param namer the UniqueNamer for assigning bnode names.
     * @param subject the active subject.
     * @param property the active property.
     * @param graph the graph name.
     * @param callback(err, triple) called when a triple is output, with
     * the last triple as null.
     */
    private void internalToRDF(Object elem, UniqueNamer namer, Object subject, Object property, Object graph, CallbackWrapper callback) {
        if (elem instanceof Map) {
            Map<String, Object> element = (Map<String, Object>) elem;
            if (element.containsKey(JSONLD_VALUE)) {
                Object value = element.get(JSONLD_VALUE);
                Object datatype = element.get(JSONLD_TYPE); // || null;
                if (value instanceof Boolean || value instanceof Number) {
                    // convert to XSD datatype
                    if (value instanceof Boolean) {
                        value = value.toString();
                        if (datatype == null) {
                            datatype = XSD_BOOLEAN;
                        }
                    } else if (value instanceof Double || value instanceof Float) {
                        DecimalFormat df = new DecimalFormat("0.0###############E0");
                        value = df.format(value);
                        if (datatype == null) {
                            datatype = XSD_DOUBLE;
                        }
                    } else {
                        DecimalFormat df = new DecimalFormat("0");
                        value = df.format(value);
                        if (datatype == null) {
                            datatype = XSD_INTEGER;
                        }
                    }
                }

                if (datatype == null) {
                    datatype = XSD_STRING;
                }

                Map<String, Object> object = new HashMap<String, Object>();
                object.put("nominalValue", value);
                object.put("interfaceName", "LiteralNode");
                Map<String, Object> objdt = new HashMap<String, Object>();
                objdt.put("nominalValue", datatype);
                objdt.put("intergaceName", "IRI");
                object.put("datatype", objdt);

                if (element.containsKey(JSONLD_LANGUAGE) && XSD_STRING.equals(datatype)) {
                    object.put("language", element.get(JSONLD_LANGUAGE));
                }

                // emit literal
                Map<String, Object> statement = new HashMap();
                statement.put("subject", JSONLDUtils.clone(subject));
                statement.put("property", JSONLDUtils.clone(property));
                statement.put("object", object);

                if (graph != null) {
                    statement.put("name", graph);
                }

                // TODO:
                callback.callback(statement);
                return;
            }

            if (element.containsKey(JSONLD_LIST)) {
                // convert @list array into embedded blank node linked list in reverse
                List<Object> list = (List<Object>) element.get(JSONLD_LIST);
                int len = list.size();
                Map<String, Object> tail = new HashMap<String, Object>();
                tail.put(JSONLD_ID, RDF_NIL);
                for (int i = len - 1; i >= 0; --i) {
                    Map<String, Object> e = new HashMap<String, Object>();
                    List<Object> f = new ArrayList<Object>();
                    f.add(list.get(i));
                    List<Object> r = new ArrayList<Object>();
                    r.add(tail);
                    e.put(RDF_FIRST, f);
                    e.put(RDF_REST, r);
                    tail = e;
                }

                internalToRDF(tail, namer, subject, property, graph, callback);
                return;
            }

            // Note: element must be a subject

            // get subject @id (generate one if it is a bnode)
            Boolean isBnode = JSONLDUtils.isBlankNode(element);
            String id = isBnode ? namer.getName((String) element.get(JSONLD_ID)) : (String) element.get(JSONLD_ID);

            // create object 
            Map<String, Object> object = new HashMap<String, Object>();
            object.put("nominalValue", id);
            object.put("interfaceName", isBnode ? "BlankNode" : "IRI");

            if (subject != null) {
                Map<String, Object> statement = new HashMap<String, Object>();
                statement.put("subject", JSONLDUtils.clone(subject));
                statement.put("property", JSONLDUtils.clone(property));
                statement.put("object", object);

                if (graph != null) {
                    statement.put("name", graph);
                }
                callback.callback(statement);
            }

            // set new active subject to object
            subject = object;

            // recurse over subject properties in order
            List<String> props = new ArrayList<String>(element.keySet());
            Collections.sort(props);
            for (String prop : props) {

                // skip ignored keys
                if (opts.isIgnored(prop)) {

                    // TODO: I need the id of the parent object, even if it's a
                    // blank node, so i'm passing the subject value here, where
                    // element makes more sense (but does not have to contain
                    // the @id, or in the cases of blank nodes, may not have the
                    // same @id as the resulting rdf.
                    callback.processIgnored(subject, id, prop, element.get(prop));

                    continue;
                }

                Object e = element.get(prop);

                // convert @type to rdf:type
                if (JSONLD_TYPE.equals(prop)) {
                    prop = RDF_TYPE;
                }

                // recurse into @graph
                if (JSONLD_GRAPH.equals(prop)) {
                    internalToRDF(e, namer, null, null, subject, callback);
                    continue;
                }

                // skip keywords
                if (JSONLDUtils.isKeyword(prop)) {
                    continue;
                }

                // create new active property
                property = new HashMap<String, Object>();
                ((Map<String, Object>) property).put("nominalValue", prop);
                ((Map<String, Object>) property).put("interfaceName", "IRI");

                // recurse into value
                internalToRDF(e, namer, subject, property, graph, callback);
            }

            return;
        }

        if (elem instanceof List) {
            // recurse into arrays
            for (Object element : (List) elem) {
                internalToRDF(element, namer, subject, property, graph, callback);
            }
            return;
        }

        // element must be an rdf:type IRI (@values covered above)
        if (elem instanceof String) {
            // emit IRI
            Map<String, Object> object = new HashMap<String, Object>();
            object.put("nominalValue", elem);
            object.put("interfaceName", "IRI");
            Map<String, Object> statement = new HashMap<String, Object>();
            statement.put("subject", JSONLDUtils.clone(subject));
            statement.put("property", JSONLDUtils.clone(property));
            statement.put("object", object);

            if (graph != null) {
                statement.put("name", graph);
            }

            callback.callback(statement);
            return;
        }
    }

    /**
     * Converts RDF statements into JSON-LD.
     *
     * @param statements the RDF statements. options the RDF conversion options.
     * callback(err, output) called once the operation completes.
     * @throws org.xbib.rdf.jsonld.JSONLDProcessingError
     */
    public Object fromRDF(List<Map<String, Object>> statements) throws JSONLDProcessingError {
        Map<String, Object> defaultGraph = new HashMap<String, Object>();
        defaultGraph.put("subjects", new HashMap<String, Object>());
        defaultGraph.put("listMap", new HashMap<String, Object>());
        Map<String, Map<String, Object>> graphs = new HashMap();
        graphs.put("", defaultGraph);

        for (Map<String, Object> statement : statements) {
            // get subject, property, object, and graph name (default to '')
            String s = (String) Obj.get(statement, "subject", "nominalValue");
            String p = (String) Obj.get(statement, "property", "nominalValue");
            Map<String, Object> o = (Map<String, Object>) statement.get("object");
            String name = statement.containsKey("name") ? (String) Obj.get(statement, "name", "nominalValue") : "";

            // create a graph entry as needed
            Map<String, Object> graph;
            if (!graphs.containsKey(name)) {
                graph = new HashMap<String, Object>();
                graph.put("subjects", new HashMap<String, Object>());
                graph.put("listMap", new HashMap<String, Object>());
                graphs.put(name, graph);
            } else {
                graph = graphs.get(name);
            }

            // handle element in @list
            if (RDF_FIRST.equals(p)) {
                // create list entry as needed
                Map<String, Object> listMap = (Map<String, Object>) graph.get("listMap");
                Map<String, Object> entry;
                if (!listMap.containsKey(s)) {
                    entry = new HashMap<String, Object>();
                    listMap.put(s, entry);
                } else {
                    entry = (Map<String, Object>) listMap.get(s);
                }
                // set object value
                entry.put("first", rdfToObject(o));
                continue;
            }

            // handle other element in @list
            if (RDF_REST.equals(p)) {
                // set next in list
                if ("BlankNode".equals(o.get("interfaceName"))) {
                    // create list entry as needed
                    Map<String, Object> listMap = (Map<String, Object>) graph.get("listMap");
                    Map<String, Object> entry;
                    if (!listMap.containsKey(s)) {
                        entry = new HashMap<String, Object>();
                        listMap.put(s, entry);
                    } else {
                        entry = (Map<String, Object>) listMap.get(s);
                    }
                    // set object value
                    entry.put("rest", o.get("nominalValue"));
                }
                continue;
            }

            // add graph subject to default graph as needed
            if (!"".equals(name) && !Obj.contains(defaultGraph, "subjects", name)) {
                Map<String, Object> tmp = new HashMap<String, Object>();
                tmp.put(JSONLD_ID, name);
                Obj.put(defaultGraph, "subjects", name, tmp);
            }

            // add subject to graph as needed
            Map<String, Object> subjects = (Map<String, Object>) graph.get("subjects");
            Map<String, Object> value;
            if (!subjects.containsKey(s)) {
                value = new HashMap<String, Object>();
                value.put(JSONLD_ID, s);
                subjects.put(s, value);
            } else {
                value = (Map<String, Object>) subjects.get(s);
            }

            // convert to @type unless options indicate to treat rdf:type as a property
            if (RDF_TYPE.equals(p) && !opts.useRdfType) {
                // add value of object as @type
                JSONLDUtils.addValue(value, JSONLD_TYPE, o.get("nominalValue"), true);
            } else {
                // add property to value as needed
                Object object = rdfToObject(o);
                JSONLDUtils.addValue(value, p, object, true);

                // a bnode might be the beginning of a list, so add it to the list map
                if ("BlankNode".equals(o.get("interfaceName"))) {
                    String id = (String) Obj.get(object, JSONLD_ID);
                    Map<String, Object> listMap = (Map<String, Object>) graph.get("listMap");
                    Map<String, Object> entry;
                    if (!listMap.containsKey(id)) {
                        entry = new HashMap<String, Object>();
                        listMap.put(id, entry);
                    } else {
                        entry = (Map<String, Object>) listMap.get(id);
                    }
                    entry.put("head", object);
                }
            }
        }

        // build @lists
        for (String name : graphs.keySet()) {
            Map<String, Object> graph = graphs.get(name);

            // find list head
            Map<String, Object> listMap = (Map<String, Object>) graph.get("listMap");
            for (String subject : listMap.keySet()) {
                Map<String, Object> entry = (Map<String, Object>) listMap.get(subject);

                // head found, build lists
                if (entry.containsKey("head") && entry.containsKey("first")) {
                    // replace bnode @id with @list
                    Obj.remove(entry, "head", JSONLD_ID);
                    List<Object> list = new ArrayList<Object>();
                    list.add(entry.get("first"));
                    Obj.put(entry, "head", JSONLD_LIST, list);
                    while (entry.containsKey("rest")) {
                        String rest = (String) entry.get("rest");
                        entry = (Map<String, Object>) listMap.get(rest);
                        if (!entry.containsKey("first")) {
                            throw new JSONLDProcessingError("Invalid RDF list entry.)")
                                    .setDetail("bnode", rest);
                        }
                        list.add(entry.get("first"));
                    }
                }
            }
        }

        // build default graph in subject @id order
        List<Object> output = new ArrayList();
        Map<String, Object> subjects = (Map<String, Object>) defaultGraph.get("subjects");
        List<String> ids = new ArrayList(subjects.keySet());
        Collections.sort(ids);
        for (String id : ids) {
            // add subject to default graph
            Map<String, Object> subject = (Map<String, Object>) subjects.get(id);
            output.add(subject);

            // output named graph in subject @id order
            if (graphs.containsKey(id)) {
                List<Object> graph = new ArrayList<Object>();
                subject.put(JSONLD_GRAPH, graph);
                Map<String, Object> _subjects = (Map<String, Object>) Obj.get(graphs, id, "subjects");
                List<String> _ids = new ArrayList<String>(_subjects.keySet());
                for (String _id : _ids) {
                    graph.add(_subjects.get(_id));
                }
            }
        }

        return output;
    }
    private final Pattern p = Pattern.compile("^[+-]?[0-9]+((?:\\.?[0-9]+((?:E?[+-]?[0-9]+)|)|))$");

    /**
     * Converts an RDF triple object to a JSON-LD object.
     *
     * @param o the RDF triple object to convert. useNativeTypes true to
     * output native types, false not to.
     *
     * @return the JSON-LD object.
     */
    private Object rdfToObject(Map<String, Object> o) {
        Map<String, Object> rval = new HashMap();
        if ("IRI".equals(o.get("interfaceName")) && RDF_NIL.equals(o.get("nominalValue"))) {
            rval.put(JSONLD_LIST, new ArrayList<Object>());
            return rval;
        }
        if ("IRI".equals(o.get("interfaceName")) || "BlankNode".equals(o.get("interfaceName"))) {
            rval.put(JSONLD_ID, o.get("nominalValue"));
            return rval;
        }

        // convert literal object to JSON-LD
        rval.put(JSONLD_VALUE, o.get("nominalValue"));

        // add datatype
        if (o.containsKey("datatype")) {
            String type = (String) Obj.get(o, "datatype", "nominalValue");
            if (opts.useNativeTypes) {
                // use native datatypes for certain xsd types
                if (XSD_BOOLEAN.equals(type)) {
                    if ("true".equals(rval.get(JSONLD_VALUE))) {
                        rval.put(JSONLD_VALUE, Boolean.TRUE);
                    } else if ("false".equals(rval.get(JSONLD_VALUE))) {
                        rval.put(JSONLD_VALUE, Boolean.FALSE);
                    }
                } else if (p.matcher((String) rval.get(JSONLD_VALUE)).matches()) {
                    try {
                        Double d = Double.parseDouble((String) rval.get(JSONLD_VALUE));
                        if (!Double.isNaN(d) && !Double.isInfinite(d)) {
                            if (XSD_INTEGER.equals(type)) {
                                Integer i = d.intValue();
                                if (i.toString().equals(rval.get(JSONLD_VALUE))) {
                                    rval.put(JSONLD_VALUE, i);
                                }
                            } else if (XSD_DOUBLE.equals(type)) {
                                rval.put(JSONLD_VALUE, d);
                            } else {
                                // we don't know the type, so we should add it to the JSON-LD
                                rval.put(JSONLD_TYPE, type);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // this should never happen since we match the value with regex
                    }
                } // do not add xsd:string type
                else if (!XSD_STRING.equals(type)) {
                    rval.put(JSONLD_TYPE, type);
                }
            } else {
                rval.put(JSONLD_TYPE, type);
            }
        }
        // add language
        if (o.containsKey("language")) {
            rval.put(JSONLD_LANGUAGE, o.get("language"));
        }
        return rval;
    }

    /**
     * Generates a unique simplified key from a URI and add it to the context
     *
     * @param key to full URI to generate the simplified key from
     * @param ctx the context to add the simplified key too
     */
    private void processKeyVal(Map<String, Object> ctx, String key, Object val) {
        int idx = key.lastIndexOf('#');
        if (idx < 0) {
            idx = key.lastIndexOf('/');
        }
        String skey = key.substring(idx + 1);
        Object keyval = key;
        Map entry = new HashMap();
        entry.put(JSONLD_ID, keyval);
        Object v = val;
        while (true) {
            if (v instanceof List && ((List) v).size() > 0) {
                // use the first entry as a reference
                v = ((List) v).get(0);
                continue;
            }
            if (v instanceof Map && ((Map) v).containsKey(JSONLD_LIST)) {
                v = ((Map) v).get(JSONLD_LIST);
                entry.put(JSONLD_CONTAINER, JSONLD_LIST);
                continue;
            }
            if (v instanceof Map && ((Map) v).containsKey(JSONLD_SET)) {
                v = ((Map) v).get(JSONLD_SET);
                entry.put(JSONLD_CONTAINER, JSONLD_SET);
                continue;
            }
            break;
        }
        if (v instanceof Map && ((Map) v).containsKey(JSONLD_ID)) {
            entry.put(JSONLD_TYPE, JSONLD_ID);
        }
        if (entry.size() == 1) {
            keyval = entry.get(JSONLD_ID);
        } else {
            keyval = entry;
        }
        while (true) {
            // check if the key is already in the frame ctx
            if (ctx.containsKey(skey)) {
                // if so, check if the values are the same
                if (JSONUtils.equals(ctx.get(skey), keyval)) {
                    // if they are, skip adding this
                    break;
                }
                // if not, add a _ to the simple key and try again
                skey += "_";
            } else {
                ctx.put(skey, keyval);
                break;
            }
        }
    }

    /**
     * Generates the context to be used by simplify.
     *
     * @param input
     * @param ctx
     */
    public void generateSimplifyContext(Object input, Map<String, Object> ctx) {
        if (input instanceof List) {
            for (Object o : (List) input) {
                generateSimplifyContext(o, ctx);
            }
        } else if (input instanceof Map) {
            Map<String, Object> o = (Map<String, Object>) input;
            Map<String, Object> localCtx = (Map<String, Object>) o.remove(JSONLD_CONTEXT);
            for (String key : o.keySet()) {
                Object val = o.get(key);
                if (key.matches("^https?://.+$")) {
                    processKeyVal(ctx, key, val);
                }
                if (JSONLD_TYPE.equals(key)) {
                    if (!(val instanceof List)) {
                        List<Object> tmp = new ArrayList<Object>();
                        tmp.add(val);
                        val = tmp;
                    }
                    for (Object t : (List<Object>) val) {
                        if (t instanceof String) {
                            processKeyVal(ctx, (String) t, new HashMap<String, Object>() {
                                {
                                    put(JSONLD_ID, "");
                                }
                            });
                        } else {
                            throw new RuntimeException("TODO: don't yet know how to handle non-string types in @type");
                        }
                    }
                } else if (val instanceof Map || val instanceof List) {
                    generateSimplifyContext(val, ctx);
                }
            }
        }
    }
}
