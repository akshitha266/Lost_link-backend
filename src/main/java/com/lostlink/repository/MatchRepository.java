package com.lostlink.repository;

import com.lostlink.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByLostItemId(Long lostItemId);
    List<Match> findByFoundItemId(Long foundItemId);
}
