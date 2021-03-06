
package in.kodecamp.wab.watch.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author airhacks.com
 */
public interface FolderWatchService {

  static long POLLING_INTERVALL = 500;

  static String POM = "pom.xml";

  public static void listenForChanges(Path dir, Runnable listener)
      throws IOException {
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    checkForChanges(scheduler, dir, listener);
  }

  static void checkForChanges(ScheduledExecutorService scheduler, Path dir,
      Runnable changeListener) {
    long initialStamp = getProjectModificationId(dir);
    boolean changeDetected = false;
    while (true) {
      try {
        final long previous = initialStamp;
        changeDetected = scheduler
            .schedule(() -> detectModification(dir, previous),
                POLLING_INTERVALL, TimeUnit.MILLISECONDS)
            .get();
      } catch (InterruptedException | ExecutionException ex) {
        throw new IllegalStateException("Scheduler error", ex);
      }
      if (changeDetected) {
        changeListener.run();
        initialStamp = getProjectModificationId(dir);
      }
    }
  }

  static long getPomModificationStamp() {
    return getFileSize(Paths.get(POM));
  }

  static boolean detectModification(Path dir, long previousStamp) {
    long currentStamp = getProjectModificationId(dir);
    return previousStamp != currentStamp;
  }

  static long getProjectModificationId(Path dir) {
    try {
      long modificationId = Files.walk(dir)
          .filter(Files::isRegularFile)
          .mapToLong(FolderWatchService::getFileSize)
          .sum();
      modificationId += getPomModificationStamp();
      return modificationId;
    } catch (IOException ex) {
      throw new IllegalStateException("Cannot list files", ex);
    }
  }

  static long getFileSize(Path p) {
    try {
      return Files.size(p);
    } catch (IOException ex) {
      throw new IllegalStateException("Cannot obtain FileTime", ex);
    }
  }

}
