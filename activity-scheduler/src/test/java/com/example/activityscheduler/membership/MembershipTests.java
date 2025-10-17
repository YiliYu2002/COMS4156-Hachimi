package com.example.activityscheduler.membership;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.activityscheduler.membership.model.Membership;
import com.example.activityscheduler.membership.model.MembershipStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MembershipTests {

  @Test
  void defaultConstructor_setsDefaults() {
    Membership membership = new Membership();

    assertThat(membership.getOrgId()).isNull();
    assertThat(membership.getUserId()).isNull();
    assertThat(membership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    assertThat(membership.getCreatedAt()).isNotNull();
  }

  @Test
  void parameterizedConstructor_setsFields() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipStatus status = MembershipStatus.INVITED;

    Membership membership = new Membership(orgId, userId, status);

    assertThat(membership.getOrgId()).isEqualTo(orgId);
    assertThat(membership.getUserId()).isEqualTo(userId);
    assertThat(membership.getStatus()).isEqualTo(status);
    assertThat(membership.getCreatedAt()).isNotNull();
  }

  @Test
  void setters_updateFields() {
    Membership membership = new Membership();
    String orgId = "org-789";
    String userId = "user-101";
    MembershipStatus status = MembershipStatus.SUSPENDED;
    LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

    membership.setOrgId(orgId);
    membership.setUserId(userId);
    membership.setStatus(status);
    membership.setCreatedAt(createdAt);

    assertThat(membership.getOrgId()).isEqualTo(orgId);
    assertThat(membership.getUserId()).isEqualTo(userId);
    assertThat(membership.getStatus()).isEqualTo(status);
    assertThat(membership.getCreatedAt()).isEqualTo(createdAt);
  }

  @Test
  void createdAt_canBeOverridden() {
    Membership membership = new Membership();
    LocalDateTime customTime = LocalDateTime.of(2023, 1, 1, 10, 0);
    membership.setCreatedAt(customTime);

    assertThat(membership.getCreatedAt()).isEqualTo(customTime);
  }
}
