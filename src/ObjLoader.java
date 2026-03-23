import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjLoader {

    public static List<Triangle> loadObj(String path, Color color) throws IOException {
        List<Vertex> vertices = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("v ")) {
                    String[] parts = line.split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);

                    vertices.add(new Vertex(x, y, z));
                }

                else if (line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");

                    // supports:
                    // f 1 2 3
                    // f 1/1/1 2/2/2 3/3/3
                    // f 1//1 2//2 3//3
                    // also triangulates quads/ngons with a fan
                    int[] faceVertexIndices = new int[parts.length - 1];

                    for (int i = 1; i < parts.length; i++) {
                        String token = parts[i];
                        String[] subParts = token.split("/");
                        int vertexIndex = Integer.parseInt(subParts[0]);

                        // OBJ is 1-based
                        faceVertexIndices[i - 1] = vertexIndex - 1;
                    }

                    // triangulate polygon as fan:
                    // (0,1,2), (0,2,3), (0,3,4), ...
                    for (int i = 1; i < faceVertexIndices.length - 1; i++) {
                        Vertex v1 = copy(vertices.get(faceVertexIndices[0]));
                        Vertex v2 = copy(vertices.get(faceVertexIndices[i]));
                        Vertex v3 = copy(vertices.get(faceVertexIndices[i + 1]));

                        triangles.add(new Triangle(v1, v2, v3, color));
                    }
                }
            }
        }

        return triangles;
    }

    private static Vertex copy(Vertex v) {
        return new Vertex(v.x, v.y, v.z);
    }

    public static List<Triangle> centerAndScale(List<Triangle> tris, double scale) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (Triangle t : tris) {
            for (Vertex v : new Vertex[]{t.v1, t.v2, t.v3}) {
                minX = Math.min(minX, v.x);
                minY = Math.min(minY, v.y);
                minZ = Math.min(minZ, v.z);
                maxX = Math.max(maxX, v.x);
                maxY = Math.max(maxY, v.y);
                maxZ = Math.max(maxZ, v.z);
            }
        }

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double centerZ = (minZ + maxZ) / 2.0;

        double sizeX = maxX - minX;
        double sizeY = maxY - minY;
        double sizeZ = maxZ - minZ;
        double maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));

        double factor = scale / maxSize;

        List<Triangle> result = new ArrayList<>();
        for (Triangle t : tris) {
            result.add(new Triangle(
                    new Vertex((t.v1.x - centerX) * factor, (t.v1.y - centerY) * factor, (t.v1.z - centerZ) * factor),
                    new Vertex((t.v2.x - centerX) * factor, (t.v2.y - centerY) * factor, (t.v2.z - centerZ) * factor),
                    new Vertex((t.v3.x - centerX) * factor, (t.v3.y - centerY) * factor, (t.v3.z - centerZ) * factor),
                    t.color
            ));
        }

        return result;
    }
}
