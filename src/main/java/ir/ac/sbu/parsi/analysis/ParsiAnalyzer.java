package ir.ac.sbu.parsi.analysis;

import ir.ac.sbu.parsi.charfilter.ZeroWidthNonJoinerCharFilter;
import ir.ac.sbu.parsi.resource.ParsiResources;
import ir.ac.sbu.parsi.tokenfilter.PersianLemmatizationFilter;
import ir.ac.sbu.parsi.tokenfilter.PersianNormalizationFilter;
import ir.ac.sbu.parsi.tokenfilter.PersianNormalizer;
import ir.ac.sbu.parsi.tokenfilter.PersianStemFilter;
import ir.ac.sbu.parsi.tokenfilter.PersianStopFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.DecimalDigitFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Persian analyzer with three reduction modes, all sharing the same
 * normalization pipeline. All Persian data is loaded from bundled files.
 *
 * <pre>
 * ZWNJ char filter -&gt; StandardTokenizer -&gt; LowerCase -&gt; normalize(+arabic)
 *   -&gt; DecimalDigit -&gt; stop -&gt; {tail}
 *
 *   LEMMATIZE : dictionary lemmatization, then light stemming for the rest  (analyzer "parsi")
 *   LIGHT     : light stemming only                                          (analyzer "parsi_light")
 *   NONE      : normalization only                                           (analyzer "parsi_standard")
 * </pre>
 */
public final class ParsiAnalyzer extends Analyzer {

    public enum Mode {
        LEMMATIZE, LIGHT, NONE
    }

    /** Default minimum stem length; tokens are never stripped below this. */
    public static final int DEFAULT_MIN_STEM_LENGTH = 3;

    private final Mode mode;
    private final Pattern zwnjPattern;
    private final PersianNormalizer normalizer;
    private final CharArraySet stopWords;
    private final List<String> suffixes;
    private final int minStemLength;
    private final Map<String, String> lemmas;

    public ParsiAnalyzer(Mode mode) {
        this.mode = mode;
        this.zwnjPattern = ParsiResources.zwnjPattern();
        this.normalizer = ParsiResources.normalizer();
        this.stopWords = ParsiResources.stopWordSet();
        this.suffixes = ParsiResources.suffixes();
        this.minStemLength = DEFAULT_MIN_STEM_LENGTH;
        this.lemmas = ParsiResources.lemmas();
    }

    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        return new ZeroWidthNonJoinerCharFilter(super.initReader(fieldName, reader), zwnjPattern);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new StandardTokenizer();
        TokenStream result = new LowerCaseFilter(source);
        result = new PersianNormalizationFilter(result, normalizer);
        result = new DecimalDigitFilter(result);
        result = new PersianStopFilter(result, stopWords);

        switch (mode) {
            case LEMMATIZE:
                result = new PersianLemmatizationFilter(result, lemmas);
                result = new PersianStemFilter(result, suffixes, minStemLength);
                break;
            case LIGHT:
                result = new PersianStemFilter(result, suffixes, minStemLength);
                break;
            case NONE:
            default:
                break;
        }
        return new TokenStreamComponents(source, result);
    }
}
