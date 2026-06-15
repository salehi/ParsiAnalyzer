package ir.ac.sbu.parsi.plugin;

import ir.ac.sbu.parsi.plugin.compat.CompatTokenFilterFactory;
import ir.ac.sbu.parsi.resource.ParsiResources;
import ir.ac.sbu.parsi.tokenfilter.PersianNormalizationFilter;
import ir.ac.sbu.parsi.tokenfilter.PersianNormalizer;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

/**
 * Token filter "parsi_normalizer". Unifies Arabic/Persian letter variants and
 * removes diacritics/kashida/hamza, all driven by bundled data files.
 */
public class PersianNormalizationFilterFactory extends CompatTokenFilterFactory {

    private final PersianNormalizer normalizer;

    public PersianNormalizationFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, environment, name, settings);
        this.normalizer = ParsiResources.normalizer();
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new PersianNormalizationFilter(tokenStream, normalizer);
    }
}
