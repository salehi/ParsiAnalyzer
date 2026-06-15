package ir.ac.sbu.parsi.plugin;

import ir.ac.sbu.parsi.analysis.ParsiAnalyzer;
import ir.ac.sbu.parsi.plugin.compat.CompatAnalyzerProvider;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

/** Provider for analyzer "parsi_standard": normalization only (no stemming/lemmatization). */
public class ParsiStandardAnalyzerProvider extends CompatAnalyzerProvider<ParsiAnalyzer> {

    private final ParsiAnalyzer analyzer;

    public ParsiStandardAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, environment, name, settings);
        this.analyzer = new ParsiAnalyzer(ParsiAnalyzer.Mode.NONE);
    }

    @Override
    public ParsiAnalyzer get() {
        return analyzer;
    }
}
