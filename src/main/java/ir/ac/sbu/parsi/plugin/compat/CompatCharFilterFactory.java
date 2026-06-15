package ir.ac.sbu.parsi.plugin.compat;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.CharFilterFactory;

/**
 * Version-independent base for char filter factories. Implements the
 * {@link CharFilterFactory} interface directly (see {@link CompatTokenFilterFactory}
 * for why).
 */
public abstract class CompatCharFilterFactory implements CharFilterFactory {

    private final String name;

    protected CompatCharFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }
}
