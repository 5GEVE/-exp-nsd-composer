package it.cnit.nfvmano;

/**
 * Hello world!
 */
public class App {

  public static void main(String[] args) throws InterruptedException {
    int i = 0;
    while (i < 100) {
      System.out.println("Hello World!");
      Thread.sleep(1000);
      i++;
    }
  }
}
