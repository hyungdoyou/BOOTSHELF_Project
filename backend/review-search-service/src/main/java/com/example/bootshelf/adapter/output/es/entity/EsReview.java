package com.example.bootshelf.adapter.output.es.entity;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Document(indexName = "review")
public class EsReview {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Integer)
    private Integer user;

    @Field(type = FieldType.Integer)
    private Integer reviewCategory;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String reviewTitle;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String reviewContent;

    @Field(type = FieldType.Integer)
    private Integer viewCnt;

    @Field(type = FieldType.Integer)
    private Integer upCnt;

    @Field(type = FieldType.Integer)
    private Integer scrapCnt;

    @Field(type = FieldType.Integer)
    private Integer commentCnt;

    @Field(type = FieldType.Boolean)
    private Boolean status;

    private String createdAt;

    private String updatedAt;

    @Field(type = FieldType.Text)
    private String courseName;

    @Field(type = FieldType.Integer)
    private Integer courseEvaluation;

    @Field(type = FieldType.Text)
    private String nickName;

    @Field(type = FieldType.Text)
    private String profileImage;

    @Field(type = FieldType.Text)
    private String reviewImage;


}
