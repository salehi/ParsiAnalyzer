package ir.ac.sbu.parsi.plugin.compat;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.TokenFilterFactory;

/**
 * Version-independent base for token filter factories. Implements the
 * {@link TokenFilterFactory} interface directly instead of extending the
 * {@code AbstractTokenFilterFactory} base class, whose constructor signature
 * changed across Elasticsearch 7.x / 8.x / 9.x. The {@code name()}/{@code create()}
 * interface contract has been stable across all of them, so a single source tree
 * builds against every version.
 */
public abstract class CompatTokenFilterFactory implements TokenFilterFactory {

    private final String name;

    protected CompatTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }
}
