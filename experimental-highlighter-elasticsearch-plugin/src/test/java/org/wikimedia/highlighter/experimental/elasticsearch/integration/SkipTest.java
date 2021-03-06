package org.wikimedia.highlighter.experimental.elasticsearch.integration;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.*;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.junit.Test;
import org.wikimedia.highlighter.experimental.elasticsearch.AbstractExperimentalHighlighterIntegrationTestBase;

/**
 * Tests for skipping highlighting.
 */
public class SkipTest extends AbstractExperimentalHighlighterIntegrationTestBase {
    @Test
    public void skipIfLastMatched() throws IOException {
        buildIndex();
        indexTestData();

        Map<String, Object> skipIf = new HashMap<>();
        skipIf.put("skip_if_last_matched", true);

        // A single chain and a single extra highlight not in the chain
        SearchRequestBuilder search = testSearch(termQuery("a", "test")).setSize(1000)
                .addHighlightedField(new HighlightBuilder.Field("a").options(skipIf))
                .addHighlightedField(new HighlightBuilder.Field("b").options(skipIf))
                .addHighlightedField(new HighlightBuilder.Field("c").options(skipIf))
                .addHighlightedField(new HighlightBuilder.Field("d"));
        SearchResponse response = search.get();
        assertHighlight(response, 0, "a", 0, equalTo("<em>test</em> a"));
        assertNotHighlighted(response, 0, "b");
        assertNotHighlighted(response, 0, "c");
        assertHighlight(response, 0, "d", 0, equalTo("<em>test</em> foo d"));

        assertHighlight(response, 1, "a", 0, equalTo("<em>test</em> a"));
        assertNotHighlighted(response, 1, "b");
        assertNotHighlighted(response, 1, "c");
        assertHighlight(response, 1, "d", 0, equalTo("<em>test</em> foo d"));

        // Support for multiple "chains"
        search = testSearch(termQuery("b", "foo")).setSize(1000)
                .addHighlightedField(new HighlightBuilder.Field("a").options(skipIf))
                .addHighlightedField(new HighlightBuilder.Field("b").options(skipIf))
                .addHighlightedField(new HighlightBuilder.Field("c"))
                .addHighlightedField(new HighlightBuilder.Field("d").options(skipIf));
        response = search.get();
        assertNotHighlighted(response, 0, "a");
        assertHighlight(response, 0, "b", 0, equalTo("test <em>foo</em> b"));
        assertNotHighlighted(response, 0, "c");
        assertHighlight(response, 0, "d", 0, equalTo("test <em>foo</em> d"));

        assertNotHighlighted(response, 1, "a");
        assertHighlight(response, 1, "b", 0, equalTo("test <em>foo</em> b"));
        assertNotHighlighted(response, 1, "c");
        assertHighlight(response, 1, "d", 0, equalTo("test <em>foo</em> d"));

        // Everything is a single chain
        search = testSearch(termQuery("a", "test")).setSize(1000)
                .addHighlightedField(new HighlightBuilder.Field("a").options(skipIf))
                .addHighlightedField(new HighlightBuilder.Field("b").options(skipIf))
                .addHighlightedField(new HighlightBuilder.Field("c").options(skipIf))
                .addHighlightedField(new HighlightBuilder.Field("d").options(skipIf));
        response = search.get();
        assertHighlight(response, 0, "a", 0, equalTo("<em>test</em> a"));
        assertNotHighlighted(response, 0, "b");
        assertNotHighlighted(response, 0, "c");
        assertNotHighlighted(response, 0, "d");

        assertHighlight(response, 1, "a", 0, equalTo("<em>test</em> a"));
        assertNotHighlighted(response, 1, "b");
        assertNotHighlighted(response, 1, "c");
        assertNotHighlighted(response, 0, "d");
    }

    protected void indexTestData() {
        client().prepareIndex("test", "test", "1")
                .setSource("a", "test a", "b", "test foo b", "c", "test c", "d", "test foo d")
                .get();
        client().prepareIndex("test", "test", "2")
                .setSource("a", "test a", "b", "test foo b", "c", "test c", "d", "test foo d")
                .get();
        refresh();
    }
}
