package VisualisationInterface;


import Sensor.InSensor;
import Sensor.OutSensor;
import Sensor.Sensor;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;

public class VisualisationTabPanel extends JPanel implements Comparable<VisualisationTabPanel> {

    private String name;
    private List<Sensor> sensors;
    private List<Double> values;
    private String[][] table;
    private Object[][] data;
    private JTable sensors_array;

    public VisualisationTabPanel(String name, List<Sensor> sensors, List<Double> values){

        this.name = name;
        this.sensors = sensors;
        this.values = values;

        boolean inside = sensors.get(0).isIn();

        String[] title;
        Sensor temp;

        if(inside) {
            title = new String[]{"Id", "Type de capteur", "Batiment", "Étage", "Salle", "Description","Valeur"};
            table =new String[sensors.size()][7];
            for(int i=0;i<sensors.size();i++) {
                temp = sensors.get(i);
                table[i][0] = temp.getId();
                table[i][1] = temp.getSensorType().getType();
                table[i][2] = ((InSensor) temp).getBuilding();
                table[i][3] = ((InSensor) temp).getFloor();
                table[i][4] = ((InSensor) temp).getRoom();
                table[i][5] = ((InSensor) temp).getDescription();
                if (values.get(i) == 5000.0)
                    table[i][6] = "";
                else
                    table[i][6] = (""+values.get(i));

            }
        } else {
            title = new String[]{"Id", "Type de capteur", "Longitude","Latitude","Valeur"};
            table = new String[sensors.size()][5];
            for(int i=0;i<sensors.size();i++) {
                temp = sensors.get(i);
                table[i][0] = temp.getId();
                table[i][1] = temp.getSensorType().getType();
                table[i][2] = ((OutSensor) temp).getLongitude();
                table[i][3] = ((OutSensor) temp).getLatitude();
                if (values.get(i) == 5000.0)
                    table[i][4] = "";
                else
                    table[i][4] = (""+values.get(i));
            }
        }

        sensors_array = new JTable(table, title);
        sensors_array.setDefaultEditor(Object.class,null);
        sensors_array.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        sensors_array.getColumnModel().getColumn(0).setPreferredWidth(100);
        sensors_array.getColumnModel().getColumn(1).setPreferredWidth(200);

        if(inside)
            sensors_array.getColumnModel().getColumn(5).setPreferredWidth(200);

        sensors_array.setPreferredScrollableViewportSize(sensors_array.getPreferredSize());
        sensors_array.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(sensors_array);
        add(scrollPane);
    }

    public void update(String id, double data, boolean alert) {
        for (int i = 0; i < sensors.size(); i++) {
            if (sensors.get(i).getId().equals(id)) {
                values.set(i, data);
                TableModel model = sensors_array.getModel();
                model.setValueAt(String.valueOf(data), i, model.getColumnCount() - 1);
                sensors_array.setModel(model);
                if (alert) {
                    sensors_array.changeSelection(i, 0, false, false);
                    sensors_array.changeSelection(i, sensors_array.getColumnCount() - 1, false, true);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(VisualisationTabPanel o) {
        return sensors.get(0).compareTo(o.sensors.get(0));
    }
}
