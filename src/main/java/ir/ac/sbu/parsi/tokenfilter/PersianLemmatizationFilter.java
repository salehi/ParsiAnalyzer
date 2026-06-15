package ir.ac.sbu.parsi.tokenfilter;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

import java.io.IOException;
import java.util.Map;

/**
 * Dictionary-based Persian lemmatizer. When a token's surface form is found in
 * the bundled {@code lemmas.tsv} map it is replaced by its lemma and marked as a
 * keyword, so a downstream {@link PersianStemFilter} leaves it untouched. Tokens
 * not in the dictionary pass through unchanged and fall back to light stemming.
 */
public final class PersianLemmatizationFilter extends TokenFilter {

    private final Map<String, String> lemmas;
    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAttribute = addAttribute(KeywordAttribute.class);

    public PersianLemmatizationFilter(TokenStream in, Map<String, String> lemmas) {
        super(in);
        this.lemmas = lemmas;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }

        String token = new String(termAttribute.buffer(), 0, termAttribute.length());
        String lemma = lemmas.get(token);
        if (lemma != null) {
            termAttribute.setEmpty();
            termAttribute.append(lemma);
            keywordAttribute.setKeyword(true);
        }
        return true;
    }
}
