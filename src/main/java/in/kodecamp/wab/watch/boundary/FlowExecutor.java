
package in.kodecamp.wab.watch.boundary;

import in.kodecamp.wab.watch.control.Builder;
import in.kodecamp.wab.watch.control.FolderWatchService;
import in.kodecamp.wab.watch.control.TerminalColors;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 *
 * @author airhacks.com
 */
public class FlowExecutor {
  private static final List<Long> buildTimes = new ArrayList<>();

  private static final AtomicLong successCounter = new AtomicLong();
  private static final AtomicLong buildErrorCounter = new AtomicLong();
  private static final Path dir = Paths.get("./src/main/");
  private static final Builder builder = new Builder();

  public static void start() throws IOException {
    // Runnable Change Listener Task
    Runnable changeListener = () -> buildAndDeploy();
    changeListener.run();
    registerEnterListener(changeListener);
    FolderWatchService.listenForChanges(dir, changeListener);
  }

  static void registerEnterListener(Runnable listener) {
    InputStream in = System.in;
    Runnable task = () -> {
      int c;
      try {
        while ((c = in.read()) != -1) {
          listener.run();
        }
      } catch (IOException ex) {
      }
    };
    new Thread(task).start();
  }

  static void buildAndDeploy() {
    long start = System.currentTimeMillis();
    try {
      System.out.printf("[%s%s%s]", TerminalColors.TIME.value(),
          currentFormattedTime(), TerminalColors.RESET.value());
      InvocationResult result = builder.build();
      if (result.getExitCode() == 0) {
        System.out.printf("[%d]", successCounter.incrementAndGet());
        System.out.print("\uD83D\uDC4D");
        long buildTime = (System.currentTimeMillis() - start);
        buildTimes.add(buildTime);
        System.out.println(" built in " + buildTime + " ms");
        start = System.currentTimeMillis();
        System.out.print("\uD83D\uDE80 ");
        if (buildTimes.size() % 10 == 0) {
          printStatistics();
        }
      } else {
        System.out.printf("[%d] ", buildErrorCounter.incrementAndGet());
        System.out.println("\uD83D\uDC4E ");
      }
    } catch (MavenInvocationException ex) {
      System.err.println(ex.getClass()
          .getName() + " " + ex.getMessage());
    }
  }

  static String currentFormattedTime() {
    DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2)
        .optionalStart()
        .appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2)
        .toFormatter();

    return LocalTime.now()
        .format(timeFormatter);
  }

  public static LongSummaryStatistics buildTimeStatistics() {
    return buildTimes.stream()
        .mapToLong(t -> t)
        .summaryStatistics();
  }

  static String statisticsSummary() {

    LongSummaryStatistics buildTimeStatistics = buildTimeStatistics();
    long maxTime = buildTimeStatistics.getMax();
    long minTime = buildTimeStatistics.getMin();
    long totalTime = buildTimeStatistics.getSum();
    String buildTimeStats = String.format(
        "Build times: min %d ms, max %d ms, total %d ms\n", minTime, maxTime,
        totalTime);

    String failureStats;
    long failedBuilds = buildErrorCounter.get();
    if (failedBuilds == 0) {
      failureStats = "Great! Every build was a success!";
    } else {
      failureStats = String.format("%d builds failed", buildErrorCounter.get());
    }
    return buildTimeStats + failureStats;
  }

  static void printStatistics() {
    System.out.println(statisticsSummary());
  }

}
