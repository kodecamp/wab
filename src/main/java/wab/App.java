
package wab;

import in.kodecamp.wab.watch.boundary.FlowExecutor;
import java.io.IOException;

/**
 *
 * @author airhacks.com
 */
public class App {

  /**
   * Main Method
   **/
  public static void main(String[] args) throws IOException {
    System.out.println("### Watch and build service started ...");
    FlowExecutor.start();
  }

}
