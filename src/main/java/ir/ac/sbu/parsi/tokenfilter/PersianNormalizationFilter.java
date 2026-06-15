package ir.ac.sbu.parsi.tokenfilter;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

/**
 * Applies a {@link PersianNormalizer} (built from data files) to each token.
 */
public final class PersianNormalizationFilter extends TokenFilter {

    private final PersianNormalizer normalizer;
    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);

    public PersianNormalizationFilter(TokenStream input, PersianNormalizer normalizer) {
        super(input);
        this.normalizer = normalizer;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            String token = new String(termAttribute.buffer(), 0, termAttribute.length()).trim();

            if (token.length() > 0) {
                String term = normalizer.normalize(token);
                termAttribute.setEmpty();
                termAttribute.append(term);
            }

            return true;
        }

        return false;
    }
}
