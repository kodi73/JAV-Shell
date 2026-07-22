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
            } else if (command.equals("echo")) {
                System.out.println(arguments);
            } else if (command.equals("type")) {
                if (arguments.equals("echo") || arguments.equals("exit") || arguments.equals("type")) {
                    System.out.println(arguments + " is a shell builtin");
                } else {
                    System.out.println(arguments + ": not found");
                }
            } else {
                System.out.println(command + ": command not found");
            }
        }
        sc.close();
    }
}