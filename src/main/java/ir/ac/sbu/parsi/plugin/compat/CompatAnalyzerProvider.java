package ir.ac.sbu.parsi.plugin.compat;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.AnalyzerScope;

/**
 * Version-independent base for analyzer providers. Implements the
 * {@link AnalyzerProvider} interface directly (see {@link CompatTokenFilterFactory}
 * for why). Subclasses implement {@link #get()}.
 */
public abstract class CompatAnalyzerProvider<T extends Analyzer> implements AnalyzerProvider<T> {

    private final String name;

    protected CompatAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public AnalyzerScope scope() {
        return AnalyzerScope.INDEX;
    }
}
