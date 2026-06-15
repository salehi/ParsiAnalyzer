package ir.ac.sbu.parsi.charfilter;

import org.apache.lucene.analysis.pattern.PatternReplaceCharFilter;

import java.io.Reader;
import java.util.regex.Pattern;

/**
 * Normalizes spacing to the Zero-Width Non-Joiner (نیم‌فاصله, U+200C) in the
 * contexts where Persian orthography requires it, and strips non-standard
 * zero-width characters. The patterns are supplied by the factory (loaded from
 * a data file).
 */
public class ZeroWidthNonJoinerCharFilter extends PatternReplaceCharFilter {

    public static final String ZWNJ = "‌";

    public ZeroWidthNonJoinerCharFilter(Reader in, Pattern pattern) {
        super(pattern, ZWNJ, in);
    }
}
