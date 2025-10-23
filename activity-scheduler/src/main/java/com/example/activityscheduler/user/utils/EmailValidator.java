package com.example.activityscheduler.user.utils;

import java.net.IDN;
import java.util.regex.Pattern;

/** Validates email addresses according to RFC 5322 and RFC 1034. */
public final class EmailValidator {

  // Local-part: dot-atom per RFC 5322 (simplified), no leading/trailing dot, no ".."
  private static final Pattern LOCAL_PART =
      Pattern.compile("^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*$");

  // Domain label: letters/digits, hyphen allowed inside, 1–63 chars
  private static final Pattern DNS_LABEL =
      Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?$");

  // TLD: letters only, 2–63 chars (prevents 1-char and numeric-only TLDs)
  private static final Pattern TLD = Pattern.compile("^[A-Za-z]{2,63}$");

  private EmailValidator() {}

  /** Returns true if the string looks like a valid email address. */
  public static boolean isValidEmail(String input) {
    if (input == null) {
      return false;
    }
    String email = input.trim();
    if (email.isEmpty() || email.length() > 254) {
      return false;
    }

    int at = email.indexOf('@');
    if (at <= 0 || at != email.lastIndexOf('@')) {
      return false;
    }

    String local = email.substring(0, at);
    String domain = email.substring(at + 1);
    if (local.length() > 64 || domain.isEmpty()) {
      return false;
    }

    if (!LOCAL_PART.matcher(local).matches()) {
      return false;
    }

    final String asciiDomain;
    try {
      asciiDomain = IDN.toASCII(domain, IDN.ALLOW_UNASSIGNED);
    } catch (IllegalArgumentException e) {
      return false;
    }
    if (asciiDomain.length() > 253) {
      return false;
    }

    String[] labels = asciiDomain.split("\\.");
    if (labels.length < 2) {
      return false;
    }

    for (int i = 0; i < labels.length; i++) {
      String label = labels[i];
      if (label.isEmpty() || label.length() > 63) {
        return false;
      }
      if (!DNS_LABEL.matcher(label).matches()) {
        return false;
      }
      if (i == labels.length - 1 && !TLD.matcher(label).matches()) {
        return false;
      }
    }
    return true;
  }
}
