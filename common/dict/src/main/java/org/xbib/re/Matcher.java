package org.xbib.re;

import java.util.Arrays;
import java.util.regex.MatchResult;

public abstract class Matcher implements MatchResult {

    protected Pattern pattern;
    protected Sub[] match;
    protected CharSequence input;
    protected int regionStart;
    protected int regionEnd;
    protected int appendPosition;
    protected boolean matched;

    public Matcher(Pattern pattern) {
        usePattern(pattern);
    }

    public Matcher reset() {
        regionStart = 0;
        regionEnd = input.length();
        appendPosition = 0;
        matched = false;
        return this;
    }

    public Matcher reset(CharSequence input) {
        this.input = input;
        return reset();
    }

    public Matcher appendReplacement(StringBuilder sb, String replacement) {
        requireMatch();
        int start = start();
        for (int i = appendPosition; i < start; i++) {
            sb.append(input.charAt(i));
        }
        sb.append(replacement); // TODO: references
        appendPosition = end();
        return this;
    }

    public StringBuilder appendTail(StringBuilder sb) {
        for (int i = appendPosition, len = input.length(); i < len; i++) {
            sb.append(input.charAt(i));
        }
        return sb;
    }

    public String replaceAll(String replacement) {
        reset();
        StringBuilder sb = new StringBuilder();
        while (find()) {
            appendReplacement(sb, replacement);
        }
        appendTail(sb);
        return sb.toString();
    }

    public String replaceFirst(String replacement) {
        reset();
        if (!find()) {
            return input.toString();
        }
        StringBuilder sb = new StringBuilder();
        appendReplacement(sb, replacement);
        appendTail(sb);
        return sb.toString();
    }

    public Matcher region(int start, int end) {
        if (start < 0 || end < 0
                || start > input.length()
                || end > input.length()
                || start > end) {
            throw new IndexOutOfBoundsException("start=" + start + " end=" + end);
        }
        reset();
        regionStart = start;
        regionEnd = end;
        return this;
    }

    public int regionStart() {
        return regionStart;
    }

    public int regionEnd() {
        return regionEnd;
    }

    public MatchResult toMatchResult() {
        requireMatch();
        return new MatchResultCapture(input, match.clone());
    }

    public Matcher usePattern(Pattern pattern) {
        this.pattern = pattern;
        match = new Sub[pattern.parenCount()];
        return this;
    }

    public Pattern pattern() {
        return pattern;
    }

    public boolean matches() {
        matched = matchHelper(0) && end() == regionEnd;
        return matched;
    }

    public boolean lookingAt() {
        matchHelper(0);
        return matched;
    }

    public boolean find() {
        return matchHelper(matched ? match[0].ep : regionStart);
    }

    public boolean find(int start) {
        reset();
        return matchHelper(start); // ???
    }

    private boolean matchHelper(int p) {
        matched = false;
        Arrays.fill(match, null);
        match(p);
        matched = match[0] != null && match[0].sp >= 0;
        //matched = p < match.length &&  match[p] != null && match[p].sp >= 0;
        return matched;
    }

    private void requireMatch() {
        if (!matched) {
            throw new IllegalStateException("no current match");
        }
    }

    @Override
    public String group() {
        return group(0);
    }

    @Override
    public int groupCount() {
        requireMatch();
        return match.length - 1;
    }

    @Override
    public String group(int group) {
        requireMatch();
        if (match[group] == null) {
            throw new IndexOutOfBoundsException("no group");
        }
        return input.subSequence(match[group].sp, match[group].ep).toString();
    }

    @Override
    public int start() {
        return start(0);
    }

    @Override
    public int end() {
        return end(0);
    }

    @Override
    public int start(int group) {
        requireMatch();
        return match[group].sp;
    }

    @Override
    public int end(int group) {
        requireMatch();
        return match[group].ep;
    }

    public Object groupLabel() {
        return groupLabel(0);
    }

    public Object groupLabel(int group) {
        requireMatch();
        return match[group].getLabel();
    }

    protected abstract void match(int p);

    private class MatchResultCapture implements MatchResult {

        private final CharSequence input;
        private final Sub[] match;

        MatchResultCapture(CharSequence input, Sub[] match) {
            this.input = input;
            this.match = match;
        }

        @Override
        public String group() {
            return group(0);
        }

        @Override
        public int groupCount() {
            return match.length - 1;
        }

        @Override
        public String group(int group) {
            return input.subSequence(match[group].sp, match[group].ep).toString();
        }

        @Override
        public int start() {
            return start(0);
        }

        @Override
        public int end() {
            return end(0);
        }

        @Override
        public int start(int group) {
            return match[group].sp;
        }

        @Override
        public int end(int group) {
            return match[group].ep;
        }
    }
}
