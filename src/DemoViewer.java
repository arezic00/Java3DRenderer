import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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



    private static class RenderPanel extends JPanel {
        private List<Triangle> model = new ArrayList<>();
        private double yRotationDegrees = 180;
        private double xRotationDegrees = 0;

        public void setModel(List<Triangle> model) {
            this.model = model;
        }

        public void setRotation(double yRotationDegrees, double xRotationDegrees) {
            this.yRotationDegrees = yRotationDegrees;
            this.xRotationDegrees = xRotationDegrees;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            double[] zBuffer = new double[img.getWidth() * img.getHeight()];
            Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY);

            Matrix3 rotationMatrix = createRotationMatrix(yRotationDegrees, xRotationDegrees);

            for (Triangle t : model) {
                renderTriangle(img, zBuffer, t, rotationMatrix, getWidth(), getHeight());
            }

            g2.drawImage(img, 0, 0, null);
        }
    }

    public static Color getShade(Color color, double shade) {
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1/2.4);
        int green = (int) Math.pow(greenLinear, 1/2.4);
        int blue = (int) Math.pow(blueLinear, 1/2.4);

        return new Color(red, green, blue);
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

    private static Matrix3 createRotationMatrix(double yRotationDegrees, double xRotationDegrees) {
        double yRotation = Math.toRadians(yRotationDegrees);
        Matrix3 yRotationMatrix = new Matrix3(new double[] {
                Math.cos(yRotation), 0, -Math.sin(yRotation),
                0, 1, 0,
                Math.sin(yRotation), 0, Math.cos(yRotation)
        });

        double xRotation = Math.toRadians(xRotationDegrees);
        Matrix3 xRotationMatrix = new Matrix3(new double[] {
                1, 0, 0,
                0, Math.cos(xRotation), Math.sin(xRotation),
                0, -Math.sin(xRotation), Math.cos(xRotation)
        });

        return xRotationMatrix.multiply(yRotationMatrix);
    }

    private static Vertex toScreenSpace(Vertex vertex, int width, int height) {
        return new Vertex(
                vertex.x + width / 2.0,
                -vertex.y + height / 2.0,
                vertex.z
        );
    }

    private static Vertex calculateNormal(Vertex v1, Vertex v2, Vertex v3) {
        Vertex ab = new Vertex(
                v2.x - v1.x,
                v2.y - v1.y,
                v2.z - v1.z
        );

        Vertex ac = new Vertex(
                v3.x - v1.x,
                v3.y - v1.y,
                v3.z - v1.z
        );

        Vertex norm = new Vertex(
                ab.y * ac.z - ab.z * ac.y,
                ab.z * ac.x - ab.x * ac.z,
                ab.x * ac.y - ab.y * ac.x
        );

        double normLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
        if (normLength == 0.0) {
            return new Vertex(0, 0, 0);
        }

        norm.x /= normLength;
        norm.y /= normLength;
        norm.z /= normLength;

        return norm;
    }

    private static double edgeFunction(Vertex a, Vertex b, double x, double y) {
        return (x - a.x) * (b.y - a.y) - (y - a.y) * (b.x - a.x);
    }

    private static void renderTriangle(
            BufferedImage img,
            double[] zBuffer,
            Triangle triangle,
            Matrix3 rotationMatrix,
            int panelWidth,
            int panelHeight
    ) {
        Vertex v1 = rotationMatrix.transform(triangle.v1);
        Vertex v2 = rotationMatrix.transform(triangle.v2);
        Vertex v3 = rotationMatrix.transform(triangle.v3);

        v1 = toScreenSpace(v1, panelWidth, panelHeight);
        v2 = toScreenSpace(v2, panelWidth, panelHeight);
        v3 = toScreenSpace(v3, panelWidth, panelHeight);

        Vertex norm = calculateNormal(v1, v2, v3);
        double angleCos = Math.abs(norm.z);

        int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
        int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
        int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
        int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

        double triangleArea = edgeFunction(v1, v2, v3.x, v3.y);
        if (triangleArea == 0.0) {
            return;
        }

        int shadedRgb = getShade(triangle.color, angleCos).getRGB();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double b1 = edgeFunction(v2, v3, x, y) / triangleArea;
                double b2 = edgeFunction(v3, v1, x, y) / triangleArea;
                double b3 = edgeFunction(v1, v2, x, y) / triangleArea;

                double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                int zIndex = y * img.getWidth() + x;

                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1 && depth > zBuffer[zIndex]) {
                    img.setRGB(x, y, shadedRgb);
                    zBuffer[zIndex] = depth;
                }
            }
        }
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
