package com.example.umc_insider.repository;

import com.example.umc_insider.domain.Exchanges;
import com.example.umc_insider.domain.Goods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangesRepository extends JpaRepository<Exchanges, Long> {
    Exchanges save(Exchanges exchange);
    List<Exchanges> findAllBy();
}