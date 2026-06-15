package ir.ac.sbu.parsi.plugin;

import ir.ac.sbu.parsi.analysis.ParsiAnalyzer;
import ir.ac.sbu.parsi.plugin.compat.CompatTokenFilterFactory;
import ir.ac.sbu.parsi.resource.ParsiResources;
import ir.ac.sbu.parsi.tokenfilter.PersianStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

import java.util.List;

/**
 * Token filter "parsi_stem_filter". Light, single-longest-suffix stemmer with a
 * configurable minimum stem length ({@code min_stem_length}, default 3).
 */
public class PersianStemFilterFactory extends CompatTokenFilterFactory {

    private final List<String> suffixes;
    private final int minStemLength;

    public PersianStemFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, environment, name, settings);
        this.suffixes = ParsiResources.suffixes();
        this.minStemLength = settings.getAsInt("min_stem_length", ParsiAnalyzer.DEFAULT_MIN_STEM_LENGTH);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new PersianStemFilter(tokenStream, suffixes, minStemLength);
    }
}
