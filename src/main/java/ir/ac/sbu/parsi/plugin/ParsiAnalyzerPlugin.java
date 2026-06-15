package ir.ac.sbu.parsi.plugin;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.CharFilterFactory;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Entry point. Registers the Persian analyzers and the building-block
 * char/token filters so users can also compose their own analyzers.
 */
public class ParsiAnalyzerPlugin extends Plugin implements AnalysisPlugin {

    @Override
    public Map<String, AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> analyzers = new HashMap<>();
        analyzers.put("parsi", ParsiAnalyzerProvider::new);
        analyzers.put("parsi_light", ParsiLightAnalyzerProvider::new);
        analyzers.put("parsi_standard", ParsiStandardAnalyzerProvider::new);
        return analyzers;
    }

    @Override
    public Map<String, AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisProvider<TokenFilterFactory>> tokenFilters = new HashMap<>();
        tokenFilters.put("parsi_normalizer", PersianNormalizationFilterFactory::new);
        tokenFilters.put("parsi_stem_filter", PersianStemFilterFactory::new);
        tokenFilters.put("parsi_stop_filter", PersianStopFilterFactory::new);
        tokenFilters.put("parsi_lemmatizer", PersianLemmatizationFilterFactory::new);
        return tokenFilters;
    }

    @Override
    public Map<String, AnalysisProvider<CharFilterFactory>> getCharFilters() {
        Map<String, AnalysisProvider<CharFilterFactory>> charFilters = new HashMap<>();
        charFilters.put("zwnj_filter", ZeroWidthNonJoinerCharFilterFactory::new);
        return charFilters;
    }
}
