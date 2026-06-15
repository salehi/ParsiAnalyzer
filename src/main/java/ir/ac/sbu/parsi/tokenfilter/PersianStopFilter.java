package ir.ac.sbu.parsi.tokenfilter;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.util.Arrays;
import java.util.Collection;

/**
 * Removes Persian stop words. The stop set is supplied by the factory (loaded
 * from a data file), not hard-coded here.
 */
public final class PersianStopFilter extends FilteringTokenFilter {

    private final CharArraySet stopWords;
    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);

    public PersianStopFilter(TokenStream in, CharArraySet stopWords) {
        super(in);
        this.stopWords = stopWords;
    }

    public static CharArraySet makeStopSet(String... stopWords) {
        return makeStopSet(Arrays.asList(stopWords));
    }

    public static CharArraySet makeStopSet(Collection<String> stopWords) {
        CharArraySet stopSet = new CharArraySet(stopWords.size(), true);
        stopSet.addAll(stopWords);
        return stopSet;
    }

    @Override
    protected boolean accept() {
        String token = new String(termAttribute.buffer(), 0, termAttribute.length()).trim();
        return !stopWords.contains(token);
    }
}
