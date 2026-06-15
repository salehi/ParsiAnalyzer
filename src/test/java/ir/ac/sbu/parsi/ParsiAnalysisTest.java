package ir.ac.sbu.parsi;

import ir.ac.sbu.parsi.analysis.ParsiAnalyzer;
import ir.ac.sbu.parsi.charfilter.ZeroWidthNonJoinerCharFilter;
import ir.ac.sbu.parsi.resource.ParsiResources;
import ir.ac.sbu.parsi.tokenfilter.PersianNormalizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fast, container-free unit tests for the analysis pipeline. */
class ParsiAnalysisTest {

    private static final String ZWNJ = "‌";

    private static List<String> analyze(ParsiAnalyzer.Mode mode, String text) throws IOException {
        List<String> out = new ArrayList<>();
        try (Analyzer analyzer = new ParsiAnalyzer(mode);
             TokenStream ts = analyzer.tokenStream("f", text)) {
            CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                out.add(term.toString());
            }
            ts.end();
        }
        return out;
    }

    // Normalization is tested on the normalizer directly: the full pipeline also
    // removes stop words, and many short normalized forms (کی، اره) are stop words.

    @Test
    void normalizerUnifiesArabicLetters() {
        PersianNormalizer n = ParsiResources.normalizer();
        // Arabic Kaf (0643) -> Keheh, Arabic Yeh (064A) -> Farsi Yeh.
        assertEquals("کی", n.normalize("كي"));
        assertEquals("کتاب", n.normalize("كتاب"));
    }

    @Test
    void normalizerRemovesDiacriticsAndKashida() {
        PersianNormalizer n = ParsiResources.normalizer();
        // "اَرّه" (fatha + shadda) -> "اره"
        assertEquals("اره", n.normalize("اَرّه"));
        // kashida-stretched word -> collapsed
        assertEquals("بادبادک", n.normalize("بادبـــادک"));
    }

    @Test
    void decimalDigitsAreFolded() throws IOException {
        // Persian digits ۱۲۳ -> 123
        assertEquals(List.of("123"), analyze(ParsiAnalyzer.Mode.NONE, "۱۲۳"));
    }

    @Test
    void stopWordsAreRemoved() throws IOException {
        // "کتاب از خانه" -> drop "از"
        List<String> tokens = analyze(ParsiAnalyzer.Mode.NONE, "کتاب از خانه");
        assertEquals(List.of("کتاب", "خانه"), tokens);
    }

    @Test
    void standardModeDoesNotStem() throws IOException {
        // "کتابها" stays whole in NONE mode
        assertEquals(List.of("کتابها"),
                analyze(ParsiAnalyzer.Mode.NONE, "کتابها"));
    }

    @Test
    void lightStemStripsPlural() throws IOException {
        // "کتابها" -> "کتاب"
        assertEquals(List.of("کتاب"),
                analyze(ParsiAnalyzer.Mode.LIGHT, "کتابها"));
    }

    @Test
    void lightStemStripsSuperlative() throws IOException {
        // "زیباترین" -> "زیبا" (single longest suffix "ترین")
        assertEquals(List.of("زیبا"),
                analyze(ParsiAnalyzer.Mode.LIGHT, "زیباترین"));
    }

    @Test
    void lightStemGuardsShortStems() throws IOException {
        // "تری" must NOT be stripped to "" or below 3 chars; stays "تری".
        assertEquals(List.of("تری"),
                analyze(ParsiAnalyzer.Mode.LIGHT, "تری"));
    }

    @Test
    void lemmatizesIrregularPlural() throws IOException {
        // "کتب" (broken plural) -> "کتاب"
        assertEquals(List.of("کتاب"),
                analyze(ParsiAnalyzer.Mode.LEMMATIZE, "کتب"));
        // "اعضا" -> "عضو"
        assertEquals(List.of("عضو"),
                analyze(ParsiAnalyzer.Mode.LEMMATIZE, "اعضا"));
    }

    @Test
    void lemmaDictionaryCoversSuppletiveComparatives() {
        // Suppletive comparatives map to their base form. (In the default `parsi`
        // pipeline these are also stop words, so this checks the dictionary directly.)
        assertEquals("خوب", ParsiResources.lemmas().get("بهتر"));
        assertEquals("کم", ParsiResources.lemmas().get("کمتر"));
    }

    @Test
    void zwnjCharFilterGluesPresentContinuous() throws IOException {
        // " می رود" -> " می‌رود" (space before the verb becomes ZWNJ).
        // A non-letter must precede می, so the input is not at string start.
        String input = " می رود";
        String expected = " می" + ZWNJ + "رود";
        assertEquals(expected, readFully(new ZeroWidthNonJoinerCharFilter(new StringReader(input), ParsiResources.zwnjPattern())));
    }

    @Test
    void resourcesLoad() {
        assertTrue(ParsiResources.stopWords().size() > 100, "stop word list should be comprehensive");
        assertTrue(ParsiResources.suffixes().contains("ها"), "suffixes should include ها");
        assertTrue(ParsiResources.lemmas().containsKey("کتب"), "lemmas should map کتب");
    }

    private static String readFully(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[256];
        int n;
        while ((n = reader.read(buf)) != -1) {
            sb.append(buf, 0, n);
        }
        return sb.toString();
    }
}
