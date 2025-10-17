package com.example.activityscheduler.membership;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.activityscheduler.membership.model.MembershipId;
import org.junit.jupiter.api.Test;

class MembershipIdTests {

  @Test
  void defaultConstructor_createsEmptyId() {
    MembershipId id = new MembershipId();

    assertThat(id.getOrgId()).isNull();
    assertThat(id.getUserId()).isNull();
  }

  @Test
  void parameterizedConstructor_setsFields() {
    String orgId = "org-123";
    String userId = "user-456";

    MembershipId id = new MembershipId(orgId, userId);

    assertThat(id.getOrgId()).isEqualTo(orgId);
    assertThat(id.getUserId()).isEqualTo(userId);
  }

  @Test
  void setters_updateFields() {
    MembershipId id = new MembershipId();
    String orgId = "org-789";
    String userId = "user-101";

    id.setOrgId(orgId);
    id.setUserId(userId);

    assertThat(id.getOrgId()).isEqualTo(orgId);
    assertThat(id.getUserId()).isEqualTo(userId);
  }

  @Test
  void equals_sameValues_returnsTrue() {
    MembershipId id1 = new MembershipId("org-123", "user-456");
    MembershipId id2 = new MembershipId("org-123", "user-456");

    assertThat(id1).isEqualTo(id2);
  }

  @Test
  void equals_differentValues_returnsFalse() {
    MembershipId id1 = new MembershipId("org-123", "user-456");
    MembershipId id2 = new MembershipId("org-789", "user-456");
    MembershipId id3 = new MembershipId("org-123", "user-789");

    assertThat(id1).isNotEqualTo(id2);
    assertThat(id1).isNotEqualTo(id3);
  }

  @Test
  void equals_sameInstance_returnsTrue() {
    MembershipId id = new MembershipId("org-123", "user-456");

    assertThat(id).isEqualTo(id);
  }

  @Test
  void equals_null_returnsFalse() {
    MembershipId id = new MembershipId("org-123", "user-456");

    assertThat(id).isNotEqualTo(null);
  }

  @Test
  void equals_differentClass_returnsFalse() {
    MembershipId id = new MembershipId("org-123", "user-456");
    String other = "not-a-membership-id";

    assertThat(id).isNotEqualTo(other);
  }

  @Test
  void hashCode_sameValues_returnsSameHash() {
    MembershipId id1 = new MembershipId("org-123", "user-456");
    MembershipId id2 = new MembershipId("org-123", "user-456");

    assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
  }

  @Test
  void hashCode_differentValues_returnsDifferentHash() {
    MembershipId id1 = new MembershipId("org-123", "user-456");
    MembershipId id2 = new MembershipId("org-789", "user-456");

    assertThat(id1.hashCode()).isNotEqualTo(id2.hashCode());
  }

  @Test
  void toString_containsBothIds() {
    MembershipId id = new MembershipId("org-123", "user-456");
    String toString = id.toString();

    assertThat(toString).contains("org-123");
    assertThat(toString).contains("user-456");
  }
}
