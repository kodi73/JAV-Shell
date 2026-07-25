import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();

            int spaceIndex = input.indexOf(' ');
            String command = spaceIndex == -1 ? input : input.substring(0, spaceIndex);
            String arguments = spaceIndex == -1 ? "" : input.substring(spaceIndex + 1).trim();

            if (command.equals("exit")) {
                break;
            } else if (command.equals("pwd")) {
                String currentDir = new File(".").getAbsolutePath();
                System.out.println(currentDir.substring(0, currentDir.length() - 2));
            } else if (command.equals("echo")) {
                System.out.println(arguments);
            } else if (command.equals("type")) {
                System.out.println(type(arguments));
            } else {
                String[] inputParts = input.split(" ");
                String path = System.getenv("PATH");
                String[] pathDirs = path.split(File.pathSeparator);
                boolean programExistsAndExecutable = false;

                for (int i = 0; i < pathDirs.length; i++) {
                    File file = new File(pathDirs[i], inputParts[0]);

                    if (file.exists() && file.canExecute()) {
                        programExistsAndExecutable = true;
                        break;
                    }
                }

                if (programExistsAndExecutable) {
                    runExternal(inputParts);
                } else {
                    System.out.println(command + ": command not found");
                }
            }
        }
        sc.close();
    }

    private static String type(String command) {
        String[] builtIns = { "echo", "exit", "type", "pwd" };

        for (int i = 0; i < builtIns.length; i++) {
            if (command.equals(builtIns[i])) {
                return command + " is a shell builtin";
            }
        }

        String path = System.getenv("PATH");
        String[] pathDirs = path.split(File.pathSeparator);

        for (int i = 0; i < pathDirs.length; i++) {
            File file = new File(pathDirs[i], command);

            if (file.exists() && file.canExecute()) {
                return command + " is " + file.getAbsolutePath();
            }
        }

        return command + ": not found";
    }

    private static void runExternal(String[] parts) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(Arrays.asList(parts));
        pb.inheritIO();
        Process process = pb.start();
        process.waitFor();
    }
}
