package org.xbib.rdf.jsonld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xbib.rdf.jsonld.utils.ActiveContext;
import org.xbib.rdf.jsonld.utils.JSONLDUtils;
import org.xbib.rdf.jsonld.utils.Options;
import org.xbib.rdf.jsonld.utils.UniqueNamer;

public class API implements JsonLd {


    public static Object compact(Object input, Object context) throws JSONLDProcessingError {
        return compact(input, context, new Options("", true));
    }

    /**
     * Performs JSON-LD compaction.
     *
     * @param input the JSON-LD input to compact.
     * @param ctx   the context to compact with.
     * @param opts  options to use:
     *              [base] the base IRI to use.
     *              [strict] use strict mode (default: true).
     *              [optimize] true to optimize the compaction (default: false).
     *              [graph] true to always output a top-level graph (default: false).
     *              [resolver(url, callback(err, jsonCtx))] the URL resolver to use.
     *              callback(err, compacted, ctx) called once the operation completes.
     * @throws JSONLDProcessingError
     */
    public static Object compact(Object input, Object ctx, Options opts) throws JSONLDProcessingError {
        // nothing to compact
        if (input == null) {
            return null;
        }
        if (opts.getBase() == null) {
            opts.setBase("");
        }
        if (opts.getStrict() == null) {
            opts.setStrict(true);
        }
        if (opts.graph == null) {
            opts.graph = false;
        }
        if (opts.optimize == null) {
            opts.optimize = false;
        }
        JSONLDProcessorImpl p = new JSONLDProcessorImpl(opts);

        // expand input then do compaction
        Object expanded;
        try {
            expanded = p.expand(new ActiveContext(), new UniqueNamer("_:t"), null, input);
        } catch (JSONLDProcessingError e) {
            throw new JSONLDProcessingError("Could not expand input before compaction")
                    .setDetail("cause", e);
        }
        // process context
        ActiveContext activeCtx = new ActiveContext();
        try {
            activeCtx = processContext(activeCtx, ctx, opts);
        } catch (JSONLDProcessingError e) {
            throw new JSONLDProcessingError("Could not process context before compaction")
                    .setDetail("cause", e);
        }
        if (opts.optimize) {
            opts.optimizeCtx = new HashMap<String, Object>();
        }
        // do compaction
        Object compacted = p.compact(activeCtx, null, expanded);
        // cleanup
        if (!opts.graph && compacted instanceof List && ((List<Object>) compacted).size() == 1) {
            compacted = ((List<Object>) compacted).get(0);
        } else if (opts.graph && compacted instanceof Map) {
            List<Object> tmp = new ArrayList<Object>();
            tmp.add(compacted);
            compacted = tmp;
        }
        if (ctx instanceof Map && ((Map<String, Object>) ctx).containsKey(JSONLD_CONTEXT)) {
            ctx = ((Map<String, Object>) ctx).get(JSONLD_CONTEXT);
        }

        ctx = JSONLDUtils.clone(ctx);
        if (!(ctx instanceof List)) {
            List<Object> lctx = new ArrayList();
            lctx.add(ctx);
            ctx = lctx;
        }
        // TODO: i need some cases where ctx is a list!

        if (opts.optimize) {
            ((List<Object>) ctx).add(opts.optimizeCtx);
        }

        List<Object> tmp = (List<Object>) ctx;
        ctx = new ArrayList();
        for (Object i : tmp) {
            if (!(i instanceof Map) || ((Map) i).size() > 0) {
                ((List<Object>) ctx).add(i);
            }
        }

        boolean hasContext = ((List) ctx).size() > 0;
        if (((List) ctx).size() == 1) {
            ctx = ((List) ctx).get(0);
        }

        if (hasContext || opts.graph) {
            if (compacted instanceof List) {
                String kwgraph = JSONLDProcessorImpl.compactIri(activeCtx, JSONLD_GRAPH);
                Object graph = compacted;
                compacted = new HashMap<String, Object>();
                if (hasContext) {
                    ((Map<String, Object>) compacted).put(JSONLD_CONTEXT, ctx);
                }
                ((Map<String, Object>) compacted).put(kwgraph, graph);
            } else if (compacted instanceof Map) {
                Map<String, Object> graph = (Map<String, Object>) compacted;
                compacted = new HashMap<String, Object>();
                ((Map) compacted).put(JSONLD_CONTEXT, ctx);
                for (String key : graph.keySet()) {
                    ((Map<String, Object>) compacted).put(key, graph.get(key));
                }
            }
        }

        return compacted;
    }

    public static Object expand(Object input) throws JSONLDProcessingError {
        return expand(input, new Options(""));
    }

    /**
     * Performs JSON-LD expansion.
     *
     * @param input the JSON-LD input to expand.
     * @throws JSONLDProcessingError
     */
    public static Object expand(Object input, Options opts) throws JSONLDProcessingError {
        if (opts.getBase() == null) {
            opts.setBase("");
        }

        // resolve all @context URLs in the input
        input = JSONLDUtils.clone(input);
        JSONLDUtils.resolveContextUrls(input);

        // do expansion
        JSONLDProcessorImpl p = new JSONLDProcessorImpl(opts);
        UniqueNamer namer = new UniqueNamer("_:t");
        Object expanded = p.expand(new ActiveContext(), namer, null, input);

        // optimize away @graph with no other properties
        if (expanded instanceof Map && ((Map) expanded).containsKey(JSONLD_GRAPH) && ((Map) expanded).size() == 1) {
            expanded = ((Map<String, Object>) expanded).get(JSONLD_GRAPH);
        }

        // normalize to an array
        if (!(expanded instanceof List)) {
            List<Object> tmp = new ArrayList<Object>();
            tmp.add(expanded);
            expanded = tmp;
        }
        return expanded;
    }


    public static Object frame(Object input, Object frame) throws JSONLDProcessingError {
        return frame(input, frame, new Options(""));
    }
    /**
     * Performs JSON-LD framing.
     *
     * @param input the JSON-LD input to frame.
     * @param frame the JSON-LD frame to use.
     * @param opts  the framing options.
     *              [base] the base IRI to use.
     *              [embed] default @embed flag (default: true).
     *              [explicit] default @explicit flag (default: false).
     *              [omitDefault] default @omitDefault flag (default: false).
     *              [optimize] optimize when compacting (default: false).
     *              [resolver(url, callback(err, jsonCtx))] the URL resolver to use.
     * @throws JSONLDProcessingError
     */
    public static Object frame(Object input, Object frame, Options opts) throws JSONLDProcessingError {
        if (input == null) {
            return null;
        }
        if (opts.embed == null) {
            opts.embed = true;
        }
        if (opts.optimize == null) {
            opts.optimize = false;
        }
        if (opts.explicit == null) {
            opts.explicit = false;
        }
        if (opts.omitDefault == null) {
            opts.omitDefault = false;
        }


        JSONLDProcessorImpl p = new JSONLDProcessorImpl(opts);

        // preserve frame context
        ActiveContext ctx = new ActiveContext();
        Map<String, Object> fctx;
        if (frame instanceof Map && ((Map<String, Object>) frame).containsKey(JSONLD_CONTEXT)) {
            fctx = (Map<String, Object>) ((Map<String, Object>) frame).get(JSONLD_CONTEXT);
            ctx = p.processContext(ctx, fctx, opts);
        } else {
            fctx = new HashMap<String, Object>();
        }

        // expand input
        Object _input = API.expand(input, opts);
        Object _frame = API.expand(frame, opts);

        Object framed = p.frame(_input, _frame);

        opts.graph = true;

        Map<String, Object> compacted = (Map<String, Object>) API.compact(framed, fctx, opts);
        String graph = JSONLDProcessorImpl.compactIri(ctx, JSONLD_GRAPH);
        compacted.put(graph, p.removePreserve(ctx, compacted.get(graph)));
        return compacted;
    }

    private static ActiveContext processContext(ActiveContext activeCtx, Object localCtx, Options opts) throws JSONLDProcessingError {
        JSONLDProcessorImpl p = new JSONLDProcessorImpl(opts);
        if (localCtx == null) {
            return new ActiveContext();
        }
        localCtx = JSONLDUtils.clone(localCtx);
        if (localCtx instanceof Map && !((Map) localCtx).containsKey(JSONLD_CONTEXT)) {
            Object tmp = localCtx;
            localCtx = new HashMap<String, Object>();
            ((HashMap<String, Object>) localCtx).put(JSONLD_CONTEXT, tmp);
        }
        JSONLDUtils.resolveContextUrls(localCtx);
        return p.processContext(activeCtx, localCtx);
    }

    /**
     * Performs RDF normalization on the given JSON-LD input. The output is
     * a sorted array of RDF statements unless the 'format' option is used.
     *
     * @param input the JSON-LD input to normalize.
     * @param opts  the options to use:
     *              [base] the base IRI to use.
     *              [format] the format if output is a string:
     *              'application/nquads' for N-Quads.
     *              [resolver(url, callback(err, jsonCtx))] the URL resolver to use.
     */
    public static Object normalize(Object input, Options opts) throws JSONLDProcessingError {
        if (opts.getBase() == null) {
            opts.setBase("");
        }

        Object expanded = API.expand(input, opts);
        return new JSONLDProcessorImpl(opts).normalize(expanded);
    }

    /**
     * Outputs the RDF statements found in the given JSON-LD object.
     *
     * @param input         the JSON-LD input.
     * @param opts          the options to use:
     *                      [base] the base IRI to use.
     *                      [format] the format to use to output a string:
     *                      'application/nquads' for N-Quads (default).
     *                      [collate] true to output all statements at once (in an array
     *                      or as a formatted string), false to output one triple at
     *                      a time (default).
     *                      [resolver(url, callback(err, jsonCtx))] the URL resolver to use.
     * @param callback(err,triple) called when a triple is output, with the
     *                      last triple as null.
     */
    public static void toRDF(Object input, Options opts, JSONLDTripleCallback callback) throws JSONLDProcessingError {
        if (opts == null) {
            return;
        }
        if (opts.getBase() == null) {
            opts.setBase("");
        }
        if (opts.collate == null) {
            opts.collate = false;
        }

        if (opts.collate) {
            // TODO:
        }

        Object expanded = API.expand(input, opts);
        JSONLDProcessorImpl p = new JSONLDProcessorImpl(opts);
        UniqueNamer namer = new UniqueNamer("_:t");
        p.toRDF(expanded, namer, null, null, null, callback);
    }

    public static void toRDF(Object input, JSONLDTripleCallback callback) throws JSONLDProcessingError {
        toRDF(input, new Options(""), callback);
    }

    public static Object fromRDF(Object input, JSONLDSerializer serializer) throws JSONLDProcessingError {
        return fromRDF(input, new Options(""), serializer);
    }
    
    public static Object fromRDF(Object input, Options opts, JSONLDSerializer serializer) throws JSONLDProcessingError {
        if (opts == null) {
            return null;
        }
        if (opts.useRdfType == null) {
            opts.useRdfType = false;
        }
        if (opts.useNativeTypes == null) {
            opts.useNativeTypes = true;
        }
        serializer.parse(input);
        Object rval = new JSONLDProcessorImpl(opts).fromRDF(serializer.getStatements());
        rval = serializer.finalize(rval);
        return rval;
    }


    public static Object simplify(Object input) throws JSONLDProcessingError {
        return simplify(input, new Options(""));
    }

    public static Object simplify(Object input, Options opts) throws JSONLDProcessingError {
        if (opts == null) {
            return null;
        }
        if (opts.getBase() == null) {
            opts.setBase("");
        }
        JSONLDProcessorImpl processor = new JSONLDProcessorImpl(opts);
        return simplify(processor, opts, input);
    }

    /**
     * Automatically builds a context which attempts to simplify the keys and values as much as possible
     * and uses that context to compact the input
     */
    public static Object simplify(JSONLDProcessorImpl processor, Options opts, Object input) throws JSONLDProcessingError {

        Object expanded = expand(input, opts);
        Map<String, Object> ctx = new HashMap();
        processor.generateSimplifyContext(expanded, ctx);

        Map<String,Object> tmp = new HashMap();
        tmp.put(JSONLD_CONTEXT, ctx);

        // add optimize flag to opts (clone the opts so we don't change the flag for the base processor)
        Options opts1 = opts.clone();
        opts1.optimize = true;
        return compact(input, tmp, opts1);
    }

}
