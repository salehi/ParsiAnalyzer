package ir.ac.sbu.parsi.plugin;

import ir.ac.sbu.parsi.charfilter.ZeroWidthNonJoinerCharFilter;
import ir.ac.sbu.parsi.plugin.compat.CompatCharFilterFactory;
import ir.ac.sbu.parsi.resource.ParsiResources;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

import java.io.Reader;
import java.util.regex.Pattern;

/**
 * Char filter "zwnj_filter". Normalizes spacing to the Zero-Width Non-Joiner in
 * the contexts required by Persian orthography, using bundled regex rules.
 */
public class ZeroWidthNonJoinerCharFilterFactory extends CompatCharFilterFactory {

    private final Pattern pattern;

    public ZeroWidthNonJoinerCharFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, environment, name, settings);
        this.pattern = ParsiResources.zwnjPattern();
    }

    @Override
    public Reader create(Reader reader) {
        return new ZeroWidthNonJoinerCharFilter(reader, pattern);
    }
}
