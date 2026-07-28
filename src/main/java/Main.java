import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

            String[] inputParts = parseCommand(input);
            if (inputParts.length == 0) continue;

            String outputFile = null;
            String errorFile = null;
            boolean appendOutput = false;
            List<String> argList = new ArrayList<>();

            for (int i = 0; i < inputParts.length; i++) {
                if ((inputParts[i].equals(">") || inputParts[i].equals("1>")) && i + 1 < inputParts.length) {
                    outputFile = inputParts[i + 1];
                    i++;
                } else if (inputParts[i].equals("2>") && i + 1 < inputParts.length) {
                    errorFile = inputParts[i + 1];
                    i++;
                } else if ((inputParts[i].equals(">>") || inputParts[i].equals("1>>")) && i + 1 < inputParts.length) {
                    outputFile = inputParts[i + 1];
                    appendOutput = true;
                    i++;
                } else {
                    argList.add(inputParts[i]);
                }
            }

            String[] parts = argList.toArray(new String[0]);
            if (parts.length == 0) continue;
            String cmd = parts[0];

            if (outputFile != null && !appendOutput) new FileWriter(outputFile, false).close();
            if (errorFile != null) new FileWriter(errorFile, false).close();

            if (cmd.equals("exit")) {
                break;
            } else if (cmd.equals("cd")) {
                String targetDir = parts.length > 1 ? parts[1] : System.getenv("HOME");
                
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
                    writeError("cd: " + targetDir + ": No such file or directory", errorFile);
                }
            } else if (cmd.equals("pwd")) {
                writeOutput(System.getProperty("user.dir"), outputFile, appendOutput);
            } else if (cmd.equals("echo")) {
                String out = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                writeOutput(out, outputFile, appendOutput);
            } else if (cmd.equals("type")) {
                String typeArg = parts.length > 1 ? parts[1] : "";
                writeOutput(type(typeArg), outputFile, appendOutput);
            } else {
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
                    runExternal(parts, outputFile,errorFile);
                } else {
                    writeError(inputParts[0] + ": command not found", errorFile);
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

    private static void runExternal(String[] args, String outputFile, String errorFile) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(Arrays.asList(args));
        pb.inheritIO();

        if (outputFile != null) {
            pb.redirectOutput(new File(outputFile));
        }
        if (errorFile != null) {
            pb.redirectError(new File(errorFile));
        }

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
            } else if (ch == '\\' && inDoubleQuote) {
                if (i + 1 < input.length()) {
                    char next = input.charAt(i + 1);

                    if (next == '"' || next == '\\') {
                        current.append(next);
                        i++;
                    }
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

    private static void writeOutput(String text, String outputFile, boolean appendOutput) throws IOException {
        if (outputFile != null) {
                FileWriter fw = new FileWriter(outputFile, appendOutput);
                fw.write(text+System.lineSeparator());
                fw.close();
        } else {
                System.out.println(text);
        }
    }

    private static void writeError(String text, String errorFile) throws IOException {
        if (errorFile != null) {
            FileWriter fw = new FileWriter(errorFile, false);
            fw.write(text+System.lineSeparator());
            fw.close();
        } else {
            System.err.println(text);
        }
    }

}
