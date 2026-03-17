import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class DemoViewer {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // slider to control horizontal rotation
        JSlider horizontalSlider = new JSlider(0, 360, 180);
        pane.add(horizontalSlider, BorderLayout.SOUTH);

        // slider to control vertical rotation
        JSlider verticalSlider = new JSlider(JSlider.VERTICAL, -90, 90, 0);
        pane.add(verticalSlider, BorderLayout.EAST);

        //panel to display render results
        JPanel renderPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0,0, getWidth(), getHeight());

                //A tetrahedron
                List<Triangle> triangles = new ArrayList<>();
                triangles.add(new Triangle(new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(-100, 100, -100),
                        Color.WHITE));
                triangles.add(new Triangle(new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(100, -100, -100),
                        Color.RED));
                triangles.add(new Triangle(new Vertex(-100, 100, -100),
                        new Vertex(100, -100, -100),
                        new Vertex(100, 100, 100),
                        Color.GREEN));
                triangles.add(new Triangle(new Vertex(-100, 100, -100),
                        new Vertex(100, -100, -100),
                        new Vertex(-100, -100, 100),
                        Color.BLUE));

                double yRotation = Math.toRadians(horizontalSlider.getValue());
                Matrix3 yRotationMatrix = new Matrix3(new double[] {
                   Math.cos(yRotation), 0, -Math.sin(yRotation),
                   0, 1, 0,
                   Math.sin(yRotation),0,Math.cos(yRotation)
                });

                double xRotation = Math.toRadians(verticalSlider.getValue());
                Matrix3 xRotationMatrix = new Matrix3(new double[] {
                        1, 0, 0,
                        0, Math.cos(xRotation), Math.sin(xRotation),
                        0, -Math.sin(xRotation), Math.cos(xRotation)
                });

                Matrix3 rotationMatrix = xRotationMatrix.multiply(yRotationMatrix);

                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY);

                for (Triangle t : triangles) {
                    Vertex v1 = rotationMatrix.transform(t.v1);
                    Vertex v2 = rotationMatrix.transform(t.v2);
                    Vertex v3 = rotationMatrix.transform(t.v3);

                    v1.x += (double) getWidth() / 2;
                    v1.y += (double) getHeight() / 2;
                    v2.x += (double) getWidth() / 2;
                    v2.y += (double) getHeight() / 2;
                    v3.x += (double) getWidth() / 2;
                    v3.y += (double) getHeight() / 2;

                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));

                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                    double triangleArea = (v1.x - v3.x) * (v2.y - v3.y) - (v2.x - v3.x) * (v1.y - v3.y);

                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            double b1 = ((x - v3.x) * (v2.y - v3.y) - (v2.x - v3.x) * (y - v3.y)) / triangleArea;
                            double b2 = ((v1.x - v3.x) * (y - v3.y) - (x - v3.x) * (v1.y - v3.y)) / triangleArea;
                            double b3 = ((v1.x - x) * (v2.y - y) - (v2.x - x) * (v1.y - y)) / triangleArea;

                            double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                            int zIndex = y * img.getWidth() + x;
                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1 && depth > zBuffer[zIndex]) {
                                img.setRGB(x, y, t.color.getRGB());
                                zBuffer[zIndex] = depth;
                            }
                        }
                    }
                }
                g2.drawImage(img, 0, 0, null);
            }
        };

        horizontalSlider.addChangeListener(e -> renderPanel.repaint());
        verticalSlider.addChangeListener(e -> renderPanel.repaint());

        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}
