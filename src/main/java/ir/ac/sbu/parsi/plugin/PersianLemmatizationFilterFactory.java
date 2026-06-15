package ir.ac.sbu.parsi.plugin;

import ir.ac.sbu.parsi.plugin.compat.CompatTokenFilterFactory;
import ir.ac.sbu.parsi.resource.ParsiResources;
import ir.ac.sbu.parsi.tokenfilter.PersianLemmatizationFilter;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

import java.util.Map;

/**
 * Token filter "parsi_lemmatizer". Replaces known surface forms with their lemma
 * from the bundled dictionary and marks them as keywords so a following stemmer
 * leaves them untouched.
 */
public class PersianLemmatizationFilterFactory extends CompatTokenFilterFactory {

    private final Map<String, String> lemmas;

    public PersianLemmatizationFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, environment, name, settings);
        this.lemmas = ParsiResources.lemmas();
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new PersianLemmatizationFilter(tokenStream, lemmas);
    }
}
