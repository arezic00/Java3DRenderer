import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
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

                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.WHITE);
                for (Triangle t : triangles) {
                    Vertex v1 = rotationMatrix.transform(t.v1);
                    Vertex v2 = rotationMatrix.transform(t.v2);
                    Vertex v3 = rotationMatrix.transform(t.v3);
                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x, v1.y);
                    path.lineTo(v2.x, v2.y);
                    path.lineTo(v3.x, v3.y);
                    path.closePath();
                    g2.draw(path);
                }
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
