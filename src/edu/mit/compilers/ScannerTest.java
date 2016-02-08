package edu.mit.compilers;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.junit.rules.ExpectedException;

public class ScannerTest {

  ByteArrayOutputStream baos;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    System.out.flush();
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  }

  private void test(String fileName) throws Exception {
    Process process = Runtime.getRuntime().exec(new String[]{
        System.getProperty("user.dir") + "/run.sh",
        "--target=scan", "-o", "/dev/stderr",
        System.getProperty("user.dir") + "/tests/scanner/input/" + fileName});

    InputStream inputStream = process.getErrorStream();
    if (!process.waitFor(1, TimeUnit.SECONDS)) {
      throw new Exception();
    }

    String actual = (new java.util.Scanner(inputStream).useDelimiter("\\A")).next();
    String expected = new String(
        Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/tests/scanner/output/" + fileName + ".out")));
    assertEquals(expected, actual);
  }

  @Test
  public void char1() throws Exception {
    test("char1");
  }

  @Test
  public void char2() throws Exception {
    test("char2");
  }

  @Test
  public void char3() throws Exception {
    test("char3");
  }

  @Test
  public void char4() throws Exception {
    test("char4");
  }

  @Test
  public void char5() throws Exception {
    test("char5");
  }

  @Test
  public void char6() throws Exception {
    test("char6");
  }

  @Test
  public void char7() throws Exception {
    test("char7");
  }

  @Test
  public void char8() throws Exception {
    test("char8");
  }

  @Test
  public void char9() throws Exception {
    test("char9");
  }

  @Test
  public void hexlit1() throws Exception {
    test("hexlit1");
  }

  @Test
  public void hexlit2() throws Exception {
    test("hexlit2");
  }

  @Test
  public void hexlit3() throws Exception {
    test("hexlit3");
  }

  @Test
  public void id1() throws Exception {
    test("id1");
  }

  @Test
  public void id2() throws Exception {
    test("id2");
  }

  @Test
  public void id3() throws Exception {
    test("id3");
  }

  @Test
  public void number1() throws Exception {
    test("number1");
  }

  @Test
  public void number2() throws Exception {
    test("number2");
  }

  @Test
  public void op1() throws Exception {
    test("op1");
  }

  @Test
  public void op2() throws Exception {
    test("op2");
  }

  @Test
  public void string1() throws Exception {
    test("string1");
  }

  @Test
  public void string2() throws Exception {
    test("string2");
  }

  @Test
  public void string3() throws Exception {
    test("string3");
  }

  @Test
  public void tokens1() throws Exception {
    test("tokens1");
  }

  @Test
  public void tokens2() throws Exception {
    test("tokens2");
  }

  @Test
  public void tokens3() throws Exception {
    test("tokens3");
  }

  @Test
  public void tokens4() throws Exception {
    test("tokens4");
  }

  @Test
  public void ws1() throws Exception {
    test("ws1");
  }
}
