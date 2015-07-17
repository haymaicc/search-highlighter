package org.apache.lucene.util.automaton;

import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.RunAutomaton;



/**
 * RunAutomaton that returns the end offset of the matching string.
 */
public class OffsetReturningRunAutomaton extends RunAutomaton {
    public OffsetReturningRunAutomaton(Automaton a, boolean utf8) {
        super(a, Character.MAX_CODE_POINT, true);
    }

    /**
     * Does s match the automaton?
     *
     * @param s string to check
     * @param offset offset to start checking
     * @param end end offset to end checking
     * @return the end offset of the matching string or -1 if no match
     */
    public int run(String s, int offset, int end) {
        int p = initial;
        int i;
        int cp;
        int lastMatch = -1;
        for (i = offset, cp = 0; i < end; i += Character.charCount(cp)) {
            cp = s.codePointAt(i);
            p = step(p, cp);
            if (p == -1) {
                // We're off the automaton so no new positions will match. If we
                // have a last match then that was it - otherwise we're out of
                // luck.
                return lastMatch;
            }
            if (accept[p]) {
                // We're matching right now so if we ever fail to match the rest
                // of the string then we can roll back to here.
                lastMatch = i + 1;
            }
        }
        // We're at the end of the string and p is the last state we hit - if
        // its not acceptable then we're half way through a potential match that
        // we'll never finish. If we have a last match then that was it -
        // otherwise no match.
        return accept[p] ? i : lastMatch;
    }
}
