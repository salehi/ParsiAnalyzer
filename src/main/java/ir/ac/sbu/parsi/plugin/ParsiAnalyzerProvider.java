package ir.ac.sbu.parsi.plugin;

import ir.ac.sbu.parsi.analysis.ParsiAnalyzer;
import ir.ac.sbu.parsi.plugin.compat.CompatAnalyzerProvider;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

/** Provider for analyzer "parsi": normalization + dictionary lemmatization with light-stem fallback. */
public class ParsiAnalyzerProvider extends CompatAnalyzerProvider<ParsiAnalyzer> {

    private final ParsiAnalyzer analyzer;

    public ParsiAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, environment, name, settings);
        this.analyzer = new ParsiAnalyzer(ParsiAnalyzer.Mode.LEMMATIZE);
    }

    @Override
    public ParsiAnalyzer get() {
        return analyzer;
    }
}
