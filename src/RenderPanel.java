import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RenderPanel extends JPanel {
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

    private Matrix3 createRotationMatrix(double yRotationDegrees, double xRotationDegrees) {
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

    private void renderTriangle(
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

    private Vertex toScreenSpace(Vertex vertex, int width, int height) {
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

    private double edgeFunction(Vertex a, Vertex b, double x, double y) {
        return (x - a.x) * (b.y - a.y) - (y - a.y) * (b.x - a.x);
    }

    public Color getShade(Color color, double shade) {
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1/2.4);
        int green = (int) Math.pow(greenLinear, 1/2.4);
        int blue = (int) Math.pow(blueLinear, 1/2.4);

        return new Color(red, green, blue);
    }
}
