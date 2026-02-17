/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.watcher.transport.action;

import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.searchafter.SearchAfterBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.test.AbstractXContentSerializingTestCase;
import org.elasticsearch.xcontent.NamedXContentRegistry;
import org.elasticsearch.xcontent.XContentParser;
import org.elasticsearch.xpack.core.watcher.transport.actions.QueryWatchesAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryWatchesRequestTests extends AbstractXContentSerializingTestCase<QueryWatchesAction.Request> {

    @Override
    protected QueryWatchesAction.Request doParseInstance(XContentParser parser) throws IOException {
        return QueryWatchesAction.Request.fromXContent(parser);
    }

    @Override
    protected Writeable.Reader<QueryWatchesAction.Request> instanceReader() {
        return QueryWatchesAction.Request::new;
    }

    @Override
    protected QueryWatchesAction.Request createTestInstance() {
        QueryBuilder query = null;
        if (randomBoolean()) {
            query = randomQuery();
        }
        List<FieldSortBuilder> sorts = null;
        if (randomBoolean()) {
            sorts = randomSorts();
        }
        SearchAfterBuilder searchAfter = null;
        if (randomBoolean()) {
            searchAfter = randomSearchAfter();
        }
        return new QueryWatchesAction.Request(
            randomBoolean() ? randomIntBetween(0, 10000) : null,
            randomBoolean() ? randomIntBetween(0, 10000) : null,
            query,
            sorts,
            searchAfter
        );
    }

    @Override
    protected QueryWatchesAction.Request mutateInstance(QueryWatchesAction.Request instance) {
        Integer from = instance.getFrom();
        Integer size = instance.getSize();
        QueryBuilder query = instance.getQuery();
        List<FieldSortBuilder> sorts = instance.getSorts();
        SearchAfterBuilder searchAfter = instance.getSearchAfter();

        switch (randomIntBetween(0, 4)) {
            case 0 -> from = from == null ? randomIntBetween(0, 10000) : randomValueOtherThan(from, () -> randomIntBetween(0, 10000));
            case 1 -> size = size == null ? randomIntBetween(0, 10000) : randomValueOtherThan(size, () -> randomIntBetween(0, 10000));
            case 2 -> {
                if (query == null) {
                    query = randomQuery();
                } else if (randomBoolean()) {
                    query = null;
                } else {
                    query = randomValueOtherThan(query, this::randomQuery);
                }
            }
            case 3 -> {
                if (sorts == null) {
                    sorts = randomSorts();
                } else if (randomBoolean()) {
                    sorts = null;
                } else {
                    sorts = randomValueOtherThan(sorts, this::randomSorts);
                }
            }
            case 4 -> {
                if (searchAfter == null) {
                    searchAfter = randomSearchAfter();
                } else if (randomBoolean()) {
                    searchAfter = null;
                } else {
                    searchAfter = randomValueOtherThan(searchAfter, this::randomSearchAfter);
                }
            }
            default -> throw new IllegalStateException("unexpected mutation branch");
        }

        return new QueryWatchesAction.Request(from, size, query, sorts, searchAfter);
    }

    private QueryBuilder randomQuery() {
        return QueryBuilders.termQuery(randomAlphaOfLengthBetween(5, 20), randomAlphaOfLengthBetween(5, 20));
    }

    private List<FieldSortBuilder> randomSorts() {
        int numSorts = randomIntBetween(1, 3);
        List<FieldSortBuilder> sorts = new ArrayList<>(numSorts);
        for (int i = 0; i < numSorts; i++) {
            sorts.add(SortBuilders.fieldSort(randomAlphaOfLengthBetween(5, 20)).order(randomFrom(SortOrder.values())));
        }
        return sorts;
    }

    private SearchAfterBuilder randomSearchAfter() {
        SearchAfterBuilder searchAfter = new SearchAfterBuilder();
        searchAfter.setSortValues(new Object[] { randomInt() });
        return searchAfter;
    }

    @Override
    protected NamedWriteableRegistry getNamedWriteableRegistry() {
        SearchModule searchModule = new SearchModule(Settings.EMPTY, Collections.emptyList());
        return new NamedWriteableRegistry(searchModule.getNamedWriteables());
    }

    @Override
    protected NamedXContentRegistry xContentRegistry() {
        SearchModule searchModule = new SearchModule(Settings.EMPTY, Collections.emptyList());
        return new NamedXContentRegistry(searchModule.getNamedXContents());
    }
}
