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
                    vertices.add(parseVertex(line));
                } else if (line.startsWith("f ")) {
                    int[] faceVertexIndices = parseFace(line);
                    addTrianglesFromFace(vertices, triangles, faceVertexIndices, color);
                }
            }
        }

        return triangles;
    }

    private static Vertex parseVertex(String line) {
        String[] parts = line.split("\\s+");
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        return new Vertex(x, y, z);
    }

    private static int[] parseFace(String line) {
        String[] parts = line.split("\\s+");
        int[] faceVertexIndices = new int[parts.length - 1];

        for (int i = 1; i < parts.length; i++) {
            String[] subParts = parts[i].split("/");
            int vertexIndex = Integer.parseInt(subParts[0]);
            faceVertexIndices[i - 1] = vertexIndex - 1;
        }

        return faceVertexIndices;
    }

    private static void addTrianglesFromFace(
            List<Vertex> vertices,
            List<Triangle> triangles,
            int[] faceVertexIndices,
            Color color
    ) {
        if (faceVertexIndices.length < 3) {
            return;
        }

        for (int i = 1; i < faceVertexIndices.length - 1; i++) {
            int index1 = faceVertexIndices[0];
            int index2 = faceVertexIndices[i];
            int index3 = faceVertexIndices[i + 1];

            if (!isValidVertexIndex(vertices, index1)
                    || !isValidVertexIndex(vertices, index2)
                    || !isValidVertexIndex(vertices, index3)) {
                continue;
            }

            Vertex v1 = vertices.get(index1).copy();
            Vertex v2 = vertices.get(index2).copy();
            Vertex v3 = vertices.get(index3).copy();

            triangles.add(new Triangle(v1, v2, v3, color));
        }
    }

    private static boolean isValidVertexIndex(List<Vertex> vertices, int index) {
        return index >= 0 && index < vertices.size();
    }

    public static List<Triangle> centerAndScale(List<Triangle> tris, double scale) {
        if (tris.isEmpty()) {
            return tris;
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (Triangle t : tris) {
            minX = Math.min(minX, Math.min(t.v1.x, Math.min(t.v2.x, t.v3.x)));
            minY = Math.min(minY, Math.min(t.v1.y, Math.min(t.v2.y, t.v3.y)));
            minZ = Math.min(minZ, Math.min(t.v1.z, Math.min(t.v2.z, t.v3.z)));

            maxX = Math.max(maxX, Math.max(t.v1.x, Math.max(t.v2.x, t.v3.x)));
            maxY = Math.max(maxY, Math.max(t.v1.y, Math.max(t.v2.y, t.v3.y)));
            maxZ = Math.max(maxZ, Math.max(t.v1.z, Math.max(t.v2.z, t.v3.z)));
        }

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double centerZ = (minZ + maxZ) / 2.0;

        double sizeX = maxX - minX;
        double sizeY = maxY - minY;
        double sizeZ = maxZ - minZ;
        double maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));

        if (maxSize == 0.0) {
            return tris;
        }

        double factor = scale / maxSize;

        List<Triangle> result = new ArrayList<>(tris.size());
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