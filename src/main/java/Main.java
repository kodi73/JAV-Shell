import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();

            if (input.startsWith("exit")) {
                break;
            } else if (input.startsWith("cd ")) {
                String targetDir = input.substring(3);
                if (targetDir.equals("~")) {
                    targetDir = System.getenv("HOME");
                }
                File file;

                if (targetDir.startsWith("/")) {
                    file = new File(targetDir);
                } else {
                    file = new File(System.getProperty("user.dir"), targetDir).getCanonicalFile();
                }

                if (file.exists() && file.isDirectory()) {
                    System.setProperty("user.dir", file.getAbsolutePath());
                } else {
                    System.out.println("cd: " + targetDir + ": No such file or directory");
                }
            } else if (input.startsWith("pwd")) {
                System.out.println(System.getProperty("user.dir"));
            } else if (input.startsWith("echo")) {
                String[] parts = parseCommand(input);
                System.out.println(String.join(" ", Arrays.copyOfRange(parts, 1, parts.length)));
            } else if (input.startsWith("type")) {
                System.out.println(type(input.substring(5)));
            } else {
                String[] inputParts = parseCommand(input);
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
                    System.out.println(inputParts[0] + ": command not found");
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

    private static String[] parseCommand(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            if (ch == '\\' && !inSingleQuote && !inDoubleQuote) {
                if (i + 1 < input.length()) {
                    current.append(input.charAt(i + 1));
                    i++;
                }
            } else if (ch == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (ch == '\"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (Character.isWhitespace(ch) && !inSingleQuote && !inDoubleQuote) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(ch);
            }
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens.toArray(new String[0]);
    }
}
