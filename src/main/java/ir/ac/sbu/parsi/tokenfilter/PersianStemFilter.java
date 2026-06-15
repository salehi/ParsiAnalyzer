package ir.ac.sbu.parsi.tokenfilter;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Light Persian stemmer: strips at most one longest-matching suffix, never
 * reducing a token below {@link #minStemLength} characters. This avoids the
 * cumulative over-stemming (down to empty/garbage tokens) of the original
 * implementation.
 *
 * The suffix list is supplied by the factory (loaded from a data file).
 */
public final class PersianStemFilter extends TokenFilter {

    private final List<String> suffixes;
    private final int minStemLength;
    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAttribute = addAttribute(KeywordAttribute.class);

    public PersianStemFilter(TokenStream in, List<String> suffixes, int minStemLength) {
        super(in);
        // Longest-first so the most specific suffix wins (e.g. "ترین" before "تر").
        this.suffixes = new ArrayList<>(suffixes);
        this.suffixes.sort(Comparator.comparingInt(String::length).reversed());
        this.minStemLength = minStemLength;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }

        if (keywordAttribute.isKeyword()) {
            return true; // already lemmatized; leave untouched
        }

        String token = new String(termAttribute.buffer(), 0, termAttribute.length()).trim();
        if (token.isEmpty()) {
            return true;
        }

        for (String suffix : suffixes) {
            if (suffix.isEmpty() || token.length() <= suffix.length()) {
                continue;
            }
            if (token.endsWith(suffix) && token.length() - suffix.length() >= minStemLength) {
                token = token.substring(0, token.length() - suffix.length());
                break; // strip a single, longest matching suffix only
            }
        }

        termAttribute.setEmpty();
        termAttribute.append(token);
        return true;
    }
}
