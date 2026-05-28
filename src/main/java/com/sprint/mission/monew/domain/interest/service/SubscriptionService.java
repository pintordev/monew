package com.sprint.mission.monew.domain.interest.service;

import com.sprint.mission.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.mission.monew.domain.interest.repository.InterestRepository;
import com.sprint.mission.monew.domain.interest.repository.SubscriptionRepository;
import com.sprint.mission.monew.domain.user.exception.UserNotFoundException;
import com.sprint.mission.monew.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubscriptionService {

  private final InterestRepository interestRepository;
  private final UserRepository userRepository;
  private final SubscriptionRepository subscriptionRepository;

  @Transactional
  public Object subscribe(UUID interestId, UUID userId) {
    interestRepository.findById(interestId)
        .orElseThrow(() -> InterestNotFoundException.withId(interestId));
    userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
    return null;
  }
}