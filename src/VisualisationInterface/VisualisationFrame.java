package VisualisationInterface;

import Sensor.Data;
import Sensor.InSensor;
import Sensor.OutSensor;
import Sensor.SensorType;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class VisualisationFrame extends JFrame implements TreeSelectionListener, ActionListener {

    private JTabbedPane tabbed_panel;
    private JScrollPane scroll_panel;
    private JSplitPane main_split_panel;
    private JSplitPane split_panel;

    private JTextArea dialog_area;

    private JMenuBar menuBar;
    private JMenu menu;
    private JMenu menu_data;
    private JMenuItem connection;
    private JMenuItem refresh;
    private JMenuItem signIn;
    private JMenuItem signOut;

    private JTree tree;
    private DefaultMutableTreeNode top = new DefaultMutableTreeNode("");

    private int nb_Sensor = 0;
    private JLabel status;
    private JLabel nb_Sensor_label;

    private Set<InSensor> inSensors = new TreeSet<>();
    private Set<OutSensor> outSensors = new TreeSet<>();
    private Map<String, List<Data>> data = new HashMap<>();

    public VisualisationFrame(){
        super("Visualisation des capteurs");
        setSize(850, 700);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        initialize();
        place();
        setListener();
        setResizable(false);
        setVisible(true);
    }

    private void initialize() {
        readDataFile();
        createJTree();

        menuBar = new JMenuBar();
        menu = new JMenu("Menu");
        menu_data = new JMenu("Données");
        signIn = new JMenuItem("Inscription aux capteurs");
        refresh = new JMenuItem("Actualiser");
        signOut = new JMenuItem("Désinscription aux capteurs");
        connection = new JMenuItem("Connexion au serveur");
        status = new JLabel("   Status : Déconnecté    ");
        nb_Sensor_label = new JLabel("Nb capteurs : "+nb_Sensor);
        tabbed_panel = new JTabbedPane(SwingConstants.TOP);
        scroll_panel = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        split_panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll_panel, tabbed_panel);
        dialog_area = new JTextArea(100, 20);
        main_split_panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dialog_area, split_panel);
    }

    private void place() {
        Container content = getContentPane();
        menu.add(connection);
        menu.add(signIn);
        menu.add(signOut);
        menuBar.add(menu);
        menu_data.add(refresh);
        menuBar.add(menu_data);
        menuBar.add(status);
        menuBar.add(nb_Sensor_label);
        setJMenuBar(menuBar);
        content.add(main_split_panel);
    }

    public void switchText() {
        if (connection.getText() == "Connexion au serveur") {
            connection.setText("Déconnexion du serveur");
            status.setText("   Status : Connecté      ");
        }
        else {
            connection.setText("Connexion au serveur");
            status.setText("   Status : Déconnecté    ");
        }
    }

    private void setListener() {
        tree.addTreeSelectionListener(this);
        signOut.addActionListener(this);
        signIn.addActionListener(this);
        connection.addActionListener(this);
        refresh.addActionListener(this);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        TreeNode node = nextNode(top, e.getNewLeadSelectionPath().getLastPathComponent().toString());
        if (node.isLeaf()) {
            //TODO - Affichage courbe Bastien
            tabbed_panel.add(node.toString(), new JPanel());
            System.out.println("Courbe");
        } else if (!((DefaultMutableTreeNode)node).isRoot()) {
            //TODO - Affichage tableau Nahor
            tabbed_panel.add(node.toString(), new JPanel());
            System.out.println("Tableau");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connection) {
            if (connection.getText() == "Connexion au serveur") new ConnectionFrame();
            else {

            }
            switchText(); // à placer ailleurs
        }
        if (e.getSource() == signIn) { // gestion erreur si déjà inscrit à tous les capteurs ?
            nb_Sensor++;
            nb_Sensor_label.setText("Nb capteurs : "+nb_Sensor);
        }
        if (e.getSource() == signOut) {
            if (nb_Sensor > 0) {
                nb_Sensor--;
                nb_Sensor_label.setText("Nb capteurs : "+nb_Sensor);
            } // sinon erreur
        }
    }

    private void readDataFile() {
        String path = System.getProperty("user.dir");
        String name = "data.txt";

        File dataFile = new File(path + "/" + name);
        try {
            BufferedReader br = new BufferedReader(new FileReader(dataFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(";");
                if (tokens.length == 3) {
                    String id = tokens[0];
                    Date date = Date.from(Instant.parse(tokens[1]));
                    Double value = Double.valueOf(tokens[2]);

                    java.util.List<Data> sensorValues = new ArrayList<>();
                    if (data.containsKey(id))
                        sensorValues = data.get(id);
                    sensorValues.add(new Data(value, date));

                    if (data.containsKey(id))
                        data.replace(id, sensorValues);
                    else
                        data.put(id, sensorValues);
                } else {
                    SensorType type = SensorType.STRINGTOTYPE(tokens[1]);
                    switch (tokens[2]) {
                        case "Intérieur":
                            inSensors.add(new InSensor(tokens[0], type, tokens[3],
                                    tokens[4], tokens[5], tokens[6]));
                            break;
                        case "Extérieur":
                            outSensors.add(new OutSensor(tokens[0], type, tokens[3], tokens[4]));
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createJTree() {
        if (!inSensors.isEmpty()) {
            DefaultMutableTreeNode in = new DefaultMutableTreeNode("Intérieur");
            top.add(in);

            for (InSensor inSensor: inSensors) {
                TreeNode building = nextNode(in, inSensor.getBuilding());
                in.add((DefaultMutableTreeNode)building);

                TreeNode floor = nextNode(building, inSensor.getFloor());
                ((DefaultMutableTreeNode)building).add((DefaultMutableTreeNode)floor);

                TreeNode room = nextNode(floor, inSensor.getRoom());
                ((DefaultMutableTreeNode)floor).add((DefaultMutableTreeNode)room);

                DefaultMutableTreeNode sensor = new DefaultMutableTreeNode(inSensor.toString());
                ((DefaultMutableTreeNode)room).add(sensor);
            }
        }

        if (!outSensors.isEmpty()) {
            DefaultMutableTreeNode out = new DefaultMutableTreeNode("Extérieur");
            top.add(out);

            for (OutSensor outSensor: outSensors) {
                DefaultMutableTreeNode sensor = new DefaultMutableTreeNode(outSensor.toString());
                out.add(sensor);
            }
        }

        tree = new JTree(top);
    }

    private TreeNode nextNode(TreeNode node, String child) {
        for (int i = 0; i < node.getChildCount(); i++) {
            if (node.getChildAt(i).toString().equals(child))
                return node.getChildAt(i);
        }
        return new DefaultMutableTreeNode(child);
    }
}
