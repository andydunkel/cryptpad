import de.dasoftware.cryptpad.model.DataModel;
import de.dasoftware.cryptpad.model.EntryTreeNode;
import de.dasoftware.cryptpad.model.IDataModel;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

/**
 * Uses the real DataModel/XMLManager classes to prove .cryptpad file
 * interoperability with the FreePascal port. Not part of the shipped app.
 *
 * Usage:
 *   java -cp .;<target/classes> XmlModelInteropTest create-file <password> <outFile>
 *   java -cp .;<target/classes> XmlModelInteropTest dump-file <password> <inFile> <outDumpFile>
 */
public class XmlModelInteropTest {
    public static void main(String[] args) throws Exception {
        String cmd = args[0];
        String password = args[1];

        if (cmd.equals("create-file")) {
            DataModel model = new DataModel();
            model.setPassword(password);

            EntryTreeNode bank = model.addNode(null, "Bankkonten");
            bank.setContent("IBAN: DE12 3456 7890, PIN: 1234");
            EntryTreeNode umlaut = model.addNode(null, "Umlaut-Test äöüß€");
            umlaut.setContent("Inhalt mit Umlauten: äöüß, Euro: €, Zeilen\nzweite Zeile");
            EntryTreeNode sub = model.addNode(bank, "Unterknoten");
            sub.setContent("verschachtelter Inhalt");

            model.saveFile(args[2]);
            System.out.println("wrote " + args[2]);
        } else if (cmd.equals("dump-file")) {
            DataModel model = new DataModel();
            model.setPassword(password);
            model.loadFile(args[2]);

            try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get(args[3]), StandardCharsets.UTF_8))) {
                dump(model.getRootNode(), 0, pw);
            }
            System.out.println("wrote " + args[3]);
        } else {
            System.err.println("unknown command: " + cmd);
            System.exit(1);
        }
    }

    private static void dump(EntryTreeNode node, int indent, PrintWriter pw) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) sb.append("  ");
        pw.println(sb + "title=" + node.toString() + " content=" + node.getContent());

        Enumeration<?> children = node.children();
        while (children.hasMoreElements()) {
            EntryTreeNode child = (EntryTreeNode) children.nextElement();
            dump(child, indent + 1, pw);
        }
    }
}
