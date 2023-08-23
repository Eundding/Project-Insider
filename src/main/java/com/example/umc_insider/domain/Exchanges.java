package com.example.umc_insider.domain;


import jakarta.persistence.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.sql.Timestamp;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "exchanges")
public class Exchanges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id; // PK

    @ManyToOne
    @JoinColumn(name = "mine_goods")
    private Goods mineGoods; // FK

    @ManyToOne
    @JoinColumn(name = "yours_goods")
    private Goods yoursGoods; // FK

    @Column(name="status")
    private String status;

    @Column(name="exchange_item")
    private String exchangeItem;

    public Exchanges registerExchanges(Goods mineGoods, String exchangeItem, String status){
        this.mineGoods = mineGoods;
        this.exchangeItem = exchangeItem;
        this.status="교환";
        return this;
    }

    public Exchanges completionExchanges(String status){
        this.status = "교환완료";
        return this;
    }
}
