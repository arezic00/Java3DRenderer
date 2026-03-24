import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class DemoViewer {

    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
    private static final double IMPORT_SCALE = 200.0;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // Top panel for buttons
        JPanel topPanel = new JPanel();
        JButton openButton = new JButton("Open OBJ");
        topPanel.add(openButton);
        JButton inflateButton = new JButton("Inflate");
        topPanel.add(inflateButton);
        pane.add(topPanel, BorderLayout.NORTH);

        // slider to control horizontal rotation
        JSlider horizontalSlider = new JSlider(0, 360, 180);
        pane.add(horizontalSlider, BorderLayout.SOUTH);

        // slider to control vertical rotation
        JSlider verticalSlider = new JSlider(JSlider.VERTICAL, -180, 180, 0);
        pane.add(verticalSlider, BorderLayout.EAST);

        RenderPanel renderPanel = new RenderPanel();
        renderPanel.setModel(makeDefaultTetrahedron());
        renderPanel.setRotation(horizontalSlider.getValue(), verticalSlider.getValue());

        openButton.addActionListener(e -> openObjFile(frame, renderPanel));

        inflateButton.addActionListener(e -> {
            List<Triangle> inflated = inflate(renderPanel.getModel());
            renderPanel.setModel(inflated);
            renderPanel.repaint();
        });

        horizontalSlider.addChangeListener(e -> {
            renderPanel.setRotation(horizontalSlider.getValue(), verticalSlider.getValue());
            renderPanel.repaint();
        });

        verticalSlider.addChangeListener(e -> {
            renderPanel.setRotation(horizontalSlider.getValue(), verticalSlider.getValue());
            renderPanel.repaint();
        });

        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static List<Triangle> inflate(List<Triangle> tris) {
        List<Triangle> result = new ArrayList<>();
        for (Triangle t : tris) {
            Vertex m1 =
                    new Vertex((t.v1.x + t.v2.x)/2, (t.v1.y + t.v2.y)/2, (t.v1.z + t.v2.z)/2);
            Vertex m2 =
                    new Vertex((t.v2.x + t.v3.x)/2, (t.v2.y + t.v3.y)/2, (t.v2.z + t.v3.z)/2);
            Vertex m3 =
                    new Vertex((t.v1.x + t.v3.x)/2, (t.v1.y + t.v3.y)/2, (t.v1.z + t.v3.z)/2);
            result.add(new Triangle(t.v1, m1, m3, t.color));
            result.add(new Triangle(t.v2, m1, m2, t.color));
            result.add(new Triangle(t.v3, m2, m3, t.color));
            result.add(new Triangle(m1, m2, m3, t.color));
        }
        for (Triangle t : result) {
            for (Vertex v : new Vertex[] { t.v1, t.v2, t.v3 }) {
                double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
                v.x /= l;
                v.y /= l;
                v.z /= l;
            }
        }
        return result;
    }

    private static List<Triangle> makeDefaultTetrahedron() {
        List<Triangle> tris = new ArrayList<>();

        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(-100, 100, -100),
                Color.WHITE));

        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, -100),
                Color.RED));

        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(100, 100, 100),
                Color.GREEN));

        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(-100, -100, 100),
                Color.BLUE));

        return tris;
    }

    private static void openObjFile(JFrame frame, RenderPanel renderPanel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose an OBJ file");
        chooser.setFileFilter(new FileNameExtensionFilter("OBJ files", "obj"));

        int result = chooser.showOpenDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();

        try {
            List<Triangle> loaded = ObjLoader.loadObj(selectedFile.getPath(), Color.WHITE);
            loaded = ObjLoader.centerAndScale(loaded, IMPORT_SCALE);

            renderPanel.setModel(loaded);
            renderPanel.repaint();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    frame,
                    "Failed to load OBJ file:\n" + ex.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

}
