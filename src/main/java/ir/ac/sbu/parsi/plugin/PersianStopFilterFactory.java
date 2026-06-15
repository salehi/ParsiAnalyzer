package ir.ac.sbu.parsi.plugin;

import ir.ac.sbu.parsi.plugin.compat.CompatTokenFilterFactory;
import ir.ac.sbu.parsi.resource.ParsiResources;
import ir.ac.sbu.parsi.tokenfilter.PersianStopFilter;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

import java.util.List;

/**
 * Token filter "parsi_stop_filter". Uses the bundled Persian stop list by
 * default; override with a {@code stopwords} array in the index settings.
 */
public class PersianStopFilterFactory extends CompatTokenFilterFactory {

    private final CharArraySet stopWords;

    public PersianStopFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, environment, name, settings);
        List<String> custom = settings.getAsList("stopwords");
        this.stopWords = (custom == null || custom.isEmpty())
                ? ParsiResources.stopWordSet()
                : PersianStopFilter.makeStopSet(custom);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new PersianStopFilter(tokenStream, stopWords);
    }
}
