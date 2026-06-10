package ru.practicum.ewm.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.user.model.UserSubscription;
import ru.practicum.ewm.user.model.UserSubscriptionId;

import java.util.List;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UserSubscriptionId> {
    boolean existsBySubscriberIdAndTargetUserId(Long subscriberId, Long targetUserId);

    List<UserSubscription> findAllBySubscriberId(Long subscriberId);

    @Modifying
    @Query("delete from UserSubscription us " +
            "where us.targetUser.id = :targetUserId")
    void deleteAllByTargetUserId(@Param("targetUserId") Long targetUserId);
}
