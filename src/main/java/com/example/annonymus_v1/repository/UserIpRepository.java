package com.example.annonymus_v1.repository;

import com.example.annonymus_v1.entity.UsersIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserIpRepository extends JpaRepository<UsersIp,Long> {
    Optional<UsersIp> findByReviewIdAndUserIp(UUID reviewId, String userIp);

    /**
     * Every vote this reader holds across a page of reviews, in one query.
     *
     * <p>Fetched in bulk so rendering a ten entry listing costs one round trip
     * rather than ten.
     */
    List<UsersIp> findByUserIpAndReviewIdIn(String userIp, Collection<UUID> reviewIds);
}
