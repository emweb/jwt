package eu.webtoolkit.jwt;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.EnumSet;

public class RefEncoderTest {

  void checkAllRefEncoderOptionResults(String input, String expected)
  {
    WString result = RefEncoder.EncodeRefs(input, EnumSet.noneOf(RefEncoderOption.class));

    assertEquals(expected, result.toString());

    result = RefEncoder.EncodeRefs(input, EnumSet.of(RefEncoderOption.EncodeInternalPaths));

    assertEquals(expected, result.toString());

    result = RefEncoder.EncodeRefs(input, EnumSet.of(RefEncoderOption.EncodeRedirectTrampoline));

    assertEquals(expected, result.toString());

    result = RefEncoder.EncodeRefs(input, EnumSet.of(RefEncoderOption.EncodeInternalPaths, RefEncoderOption.EncodeRedirectTrampoline));

    assertEquals(expected, result.toString());

  }

  @Test
  public void testEncodePlainText() {
    String content = "this is some simple content";
    checkAllRefEncoderOptionResults(content, content);
  }

  @Test
  public void testEncodeHTMLText() {
    String content = "<p>this is some html content</p>";
    checkAllRefEncoderOptionResults(content, content);
  }

  @Test
  public void testEncodeUnbalancedHTMLText() {
    String content = "<p>this is some html content";
    checkAllRefEncoderOptionResults(content, content);
  }

  @Test
  public void testEncodeAttributedHTMLText() {
    String content = "<p class=\"class\" style=\"width: 100%; color: red;\">this is some html content</p>";
    checkAllRefEncoderOptionResults(content, content);
  }

  @Test
  public void testEncodeAttributedQuoteHTMLText() {
    String content = "<p class='class'>this is some html content</p>";
    String quotedContent = "<p class=\"class\">this is some html content</p>";
    checkAllRefEncoderOptionResults(content, quotedContent);
  }

  @Test
  public void testEncodeHREFAttributeNoParamsHTMLText() {
    String content = "<p href=\"www.emweb.be\" class=\"class\">this is some html content</p>";
    checkAllRefEncoderOptionResults(content, content);
  }

  @Test
  public void testEncodeHREFAttributeWithParamsHTMLText() {
    String content = "<p href=\"www.emweb.be?param1=1&param2=2\" class=\"class\">this is some html content</p>";
    checkAllRefEncoderOptionResults(content, content);
  }

  @Test
  public void testEncodeHREFAttributeWithParamsAndRegularHTMLText() {
    String content = "<p href=\"www.emweb.be?param1=1&param2=2\" class=\"class\">this is some html content, with an &</p>";
    checkAllRefEncoderOptionResults(content, content);
  }
}
