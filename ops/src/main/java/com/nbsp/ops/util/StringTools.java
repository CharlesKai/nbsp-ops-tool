package com.nbsp.ops.util;

/**
 * @author: CharlesYan
 * @date: 2017/10/24 10:16
 */
public class StringTools {

  public static boolean isNullOrEmpty(String str) {
    return null == str || "".equals(str) || "null".equals(str);
  }

  public static boolean isNullOrEmpty(Object obj) {
    return null == obj || "".equals(obj);
  }

  // Empty checks
  // -----------------------------------------------------------------------

  /**
   * Checks if a CharSequence is empty ("") or null.
   *
   * <pre>
   * StringUtils.isEmpty(null)      = true
   * StringUtils.isEmpty("")        = true
   * StringUtils.isEmpty(" ")       = false
   * StringUtils.isEmpty("bob")     = false
   * StringUtils.isEmpty("  bob  ") = false
   * </pre>
   *
   * <p>NOTE: This method changed in Lang version 2.0. It no longer trims the CharSequence. That
   * functionality is available in isBlank().
   *
   * @param cs the CharSequence to check, may be null
   * @return {@code true} if the CharSequence is empty or null
   * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
   */
  public static boolean isEmpty(final CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  // Equals
  // -----------------------------------------------------------------------

  /**
   * Compares two CharSequences, returning {@code true} if they represent equal sequences of
   * characters.
   *
   * <p>{@code null}s are handled without exceptions. Two {@code null} references are considered to
   * be equal. The comparison is case sensitive.
   *
   * <pre>
   * StringUtils.equals(null, null)   = true
   * StringUtils.equals(null, "abc")  = false
   * StringUtils.equals("abc", null)  = false
   * StringUtils.equals("abc", "abc") = true
   * StringUtils.equals("abc", "ABC") = false
   * </pre>
   *
   * @param cs1 the first CharSequence, may be {@code null}
   * @param cs2 the second CharSequence, may be {@code null}
   * @return {@code true} if the CharSequences are equal (case-sensitive), or both {@code null}
   * @see Object#equals(Object)
   * @since 3.0 Changed signature from equals(String, String) to equals(CharSequence, CharSequence)
   */
  public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
    if (cs1 == cs2) {
      return true;
    }
    if (cs1 == null || cs2 == null) {
      return false;
    }
    if (cs1.length() != cs2.length()) {
      return false;
    }
    if (cs1 instanceof String && cs2 instanceof String) {
      return cs1.equals(cs2);
    }
    return CharSequenceUtils.regionMatches(cs1, false, 0, cs2, 0, cs1.length());
  }
}
