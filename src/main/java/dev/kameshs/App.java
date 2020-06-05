package dev.kameshs;

import javax.inject.Inject;
import dev.kameshs.commands.ApplyCommand;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;


/**
 * jo
 */
@QuarkusMain
@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true,
    subcommands = {ApplyCommand.class})
public class App implements QuarkusApplication {

  @Inject
  CommandLine.IFactory factory;

  public int run(String... args) {
    return new CommandLine(this, factory).execute(args);
  }

  public static void main(String ... args) {
    System.out.println("Running main method");
    Quarkus.run(args);
  }
}
