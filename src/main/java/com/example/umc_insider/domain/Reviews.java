package com.example.umc_insider.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "reviews")

public class Reviews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id; // PK

    @ManyToOne
    @JoinColumn(name = "goods_id")
    private Goods goods_id; // PK, FK

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Users buyer_id; // PK, FK

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer point;

    @Column(nullable = false)
    private Timestamp created_at;

    public Reviews createReviews(String content, Integer point){
        this.content = content;
        this.point = point;
        this.created_at = new Timestamp(System.currentTimeMillis());

        return this;
    }
}
