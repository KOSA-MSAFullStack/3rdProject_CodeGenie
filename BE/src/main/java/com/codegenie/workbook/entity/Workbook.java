package com.codegenie.workbook.entity;

import com.codegenie.member.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workbooks")
public class Workbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workbook_id")
    private Integer id;

    // ✅ FK: members(member_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "language", nullable = false)
    private String language;


    @Column(name = "level", nullable = false)
    private String level;

    @Column(name = "style", nullable = false)
    private String style;

    @Lob
    @Column(name = "request_detail", nullable = false, columnDefinition = "TEXT")
    private String requestDetail;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "is_upload", nullable = false)
    private Boolean isUpload = Boolean.FALSE;
}
