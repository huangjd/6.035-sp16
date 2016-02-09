package edu.mit.compilers;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.junit.rules.ExpectedException;

public class ParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private void testLegal(int n) throws Exception {
    Process process = Runtime.getRuntime().exec(new String[]{
        System.getProperty("user.dir") + "/run.sh",
        "--target=parse",
        System.getProperty("user.dir") + "/tests/parser/legal/legal-" + Integer.toString(n)});

    if (!process.waitFor(1, TimeUnit.SECONDS)) {
      process.destroy();
      throw new Exception();
    }
    assertEquals(0, process.exitValue());
  }

  private void testIllegal(int n) throws Exception {
    Process process = Runtime.getRuntime().exec(new String[]{
        System.getProperty("user.dir") + "/run.sh",
        "--target=parse",
        System.getProperty("user.dir") + "/tests/parser/illegal/illegal-" + Integer.toString(n)});

    if (!process.waitFor(1, TimeUnit.SECONDS)) {
      process.destroy();
      throw new Exception();
    }
    assertNotEquals(0, process.exitValue());
  }

  @Test
  public void legal0() throws Exception {
    throw new Exception();
    // testLegal(0);
  }

  @Test
  public void legal1() throws Exception {
    testLegal(1);
  }

  @Test
  public void legal2() throws Exception {
    testLegal(2);
  }

  @Test
  public void legal3() throws Exception {
    testLegal(3);
  }

  @Test
  public void legal4() throws Exception {
    testLegal(4);
  }

  @Test
  public void legal5() throws Exception {
    testLegal(5);
  }

  @Test
  public void legal6() throws Exception {
    testLegal(6);
  }

  @Test
  public void legal7() throws Exception {
    testLegal(7);
  }

  @Test
  public void legal8() throws Exception {
    testLegal(8);
  }

  @Test
  public void legal9() throws Exception {
    testLegal(9);
  }

  @Test
  public void legal10() throws Exception {
    testLegal(10);
  }

  @Test
  public void legal11() throws Exception {
    testLegal(11);
  }

  @Test
  public void legal12() throws Exception {
    testLegal(12);
  }

  @Test
  public void legal13() throws Exception {
    testLegal(13);
  }

  @Test
  public void legal14() throws Exception {
    testLegal(14);
  }

  @Test
  public void legal15() throws Exception {
    testLegal(15);
  }

  @Test
  public void legal16() throws Exception {
    testLegal(16);
  }

  @Test
  public void legal17() throws Exception {
    testLegal(17);
  }

  @Test
  public void legal18() throws Exception {
    testLegal(18);
  }

  @Test
  public void legal19() throws Exception {
    testLegal(19);
  }

  @Test
  public void legal20() throws Exception {
    testLegal(20);
  }

  @Test
  public void legal21() throws Exception {
    testLegal(21);
  }

  @Test
  public void legal22() throws Exception {
    testLegal(22);
  }

  @Test
  public void legal23() throws Exception {
    testLegal(23);
  }

  @Test
  public void legal24() throws Exception {
    testLegal(24);
  }

  @Test
  public void legal25() throws Exception {
    testLegal(25);
  }

  @Test
  public void legal26() throws Exception {
    testLegal(26);
  }

  @Test
  public void illegal1() throws Exception {
    testIllegal(1);
  }

  @Test
  public void illegal2() throws Exception {
    testIllegal(2);
  }

  @Test
  public void illegal3() throws Exception {
    testIllegal(3);
  }

  @Test
  public void illegal4() throws Exception {
    testIllegal(4);
  }

  @Test
  public void illegal5() throws Exception {
    testIllegal(5);
  }

  @Test
  public void illegal6() throws Exception {
    testIllegal(6);
  }

  @Test
  public void illegal7() throws Exception {
    testIllegal(7);
  }

  @Test
  public void illegal8() throws Exception {
    testIllegal(8);
  }

  @Test
  public void illegal9() throws Exception {
    testIllegal(9);
  }

  @Test
  public void illegal10() throws Exception {
    testIllegal(10);
  }

  @Test
  public void illegal11() throws Exception {
    testIllegal(11);
  }

  @Test
  public void illegal12() throws Exception {
    testIllegal(12);
  }

  @Test
  public void illegal13() throws Exception {
    testIllegal(13);
  }

  @Test
  public void illegal14() throws Exception {
    testIllegal(14);
  }

  @Test
  public void illegal15() throws Exception {
    testIllegal(15);
  }

  @Test
  public void illegal16() throws Exception {
    testIllegal(16);
  }

  @Test
  public void illegal17() throws Exception {
    testIllegal(17);
  }

  @Test
  public void illegal18() throws Exception {
    testIllegal(18);
  }

  @Test
  public void illegal19() throws Exception {
    testIllegal(19);
  }

  @Test
  public void illegal20() throws Exception {
    testIllegal(20);
  }

  @Test
  public void illegal21() throws Exception {
    testIllegal(21);
  }

  @Test
  public void illegal22() throws Exception {
    testIllegal(22);
  }

  @Test
  public void illegal23() throws Exception {
    testIllegal(23);
  }

  @Test
  public void illegal24() throws Exception {
    testIllegal(24);
  }

  @Test
  public void illegal25() throws Exception {
    testIllegal(25);
  }

  @Test
  public void illegal26() throws Exception {
    testIllegal(26);
  }

  @Test
  public void illegal27() throws Exception {
    testIllegal(27);
  }

  @Test
  public void illegal28() throws Exception {
    testIllegal(28);
  }

  @Test
  public void illegal29() throws Exception {
    testIllegal(29);
  }
}
