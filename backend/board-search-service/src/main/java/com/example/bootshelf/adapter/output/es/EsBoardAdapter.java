package com.example.bootshelf.adapter.output.es;

import com.example.bootshelf.adapter.output.es.entity.EsBoard;
import com.example.bootshelf.application.port.output.GetListBoardPort;
import com.example.bootshelf.common.ExternalSystemAdapter;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ExternalSystemAdapter
public class EsBoardAdapter implements GetListBoardPort {

    private final ElasticsearchOperations operations;

    @Override
    public SearchHits<EsBoard> titleContentSearch(Integer categoryIdx, String sortField, String title, Pageable pageable) {

        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(title,
                "boardtitle", "boardcontent");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("boardcategory_idx", categoryIdx));

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("boardtitle"); // 하이라이팅을 적용할 필드 지정
        highlightBuilder.field("boardcontent"); // 하이라이팅을 적용할 필드 지정
        highlightBuilder.preTags("<em>"); // 하이라이트 시작 태그
        highlightBuilder.postTags("</em>"); // 하이라이트 종료 태그


        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(multiMatchQueryBuilder)
                .withFilter(boolQueryBuilder)
                .withHighlightBuilder(highlightBuilder) // 하이라이트 설정 추가
                .withPageable(pageable)
                .withSort(SortBuilders.fieldSort(sortField).order(SortOrder.ASC))
                .build();

        return operations.search(build, EsBoard.class);
    }

    // search after 적용 ver
    @Override
    public SearchHits<EsBoard> searchAfterBoard(Integer categoryIdx, String sortField, String title, int size, List<Object> searchAfter) {
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(title, "boardTitle", "boardContent");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(multiMatchQueryBuilder)
                .filter(QueryBuilders.termQuery("boardcategory_idx", categoryIdx));

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withSort(SortBuilders.fieldSort(sortField).order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC)) // 중복값이 있을 것을 대비해 id로 한 번 더 sorting
                .withPageable(PageRequest.of(0, size));

        if (searchAfter != null && !searchAfter.isEmpty()) {
            queryBuilder.withSearchAfter(searchAfter);
        }

        Query searchQuery = queryBuilder.build();
        return operations.search(searchQuery, EsBoard.class);
    }

}
