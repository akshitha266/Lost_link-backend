package com.lostlink.repository;

import com.lostlink.entity.Claim;
import com.lostlink.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByClaimantId(Long claimantId);
    List<Claim> findByItemId(Long itemId);
    Optional<Claim> findByItemIdAndClaimantIdAndStatus(Long itemId, Long claimantId, ClaimStatus status);
    long countByStatus(ClaimStatus status);
}
