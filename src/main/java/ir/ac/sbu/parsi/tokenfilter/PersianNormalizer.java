package ir.ac.sbu.parsi.tokenfilter;

import java.util.Map;
import java.util.Set;

/**
 * Data-driven character normalization of Persian/Arabic text. The mapping
 * (variant -> canonical) and the set of characters to delete (diacritics,
 * kashida, hamza) are loaded from data files by
 * {@link ir.ac.sbu.parsi.resource.ParsiResources}; nothing is hard-coded here.
 */
public class PersianNormalizer {

    private final Map<Character, Character> mapping;
    private final Set<Character> deletions;

    public PersianNormalizer(Map<Character, Character> mapping, Set<Character> deletions) {
        this.mapping = mapping;
        this.deletions = deletions;
    }

    public String normalize(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (deletions.contains(c)) {
                continue;
            }
            Character mapped = mapping.get(c);
            sb.append(mapped != null ? mapped.charValue() : c);
        }
        return sb.toString();
    }
}
