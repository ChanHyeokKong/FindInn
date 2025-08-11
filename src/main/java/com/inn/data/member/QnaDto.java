package com.inn.data.member;

import jakarta.persistence.*;
import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name="qna")
public class QnaDto {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable=false)
    private String question;
    @Column(nullable=true)
    private String  answer;

    @Column
    private boolean status= false;

    @ManyToMany
    @JoinTable(
            name="qna_category",
            joinColumns= @JoinColumn(name = "qna_idx", referencedColumnName = "idx"),
            inverseJoinColumns = @JoinColumn(name = "category_idx", referencedColumnName = "idx")
    )
    @EqualsAndHashCode.Exclude
    private Set<CategoriesDto> categories = new HashSet<>();


}
