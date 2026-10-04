package com.benbenlaw.routers.manager;

import com.benbenlaw.routers.manager.ManagerSnapshot.Edge;

import java.util.ArrayList;
import java.util.List;

// Left-to-right layered layout: exporters sit to the left of the importers they feed. Links that
// would loop back (two Importer Exporters feeding each other, say) are ignored for placement so
// the chart stays readable, and each disconnected group of routers is stacked below the last.
public class ManagerLayout {

    public static final int NODE_WIDTH = 118;
    public static final int NODE_HEIGHT = 28;
    public static final int GAP_X = 44;
    public static final int GAP_Y = 14;

    public final int[] x;
    public final int[] y;
    public final int width;
    public final int height;

    private ManagerLayout(int[] x, int[] y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static ManagerLayout compute(int nodeCount, List<Edge> edges) {
        int[] x = new int[nodeCount];
        int[] y = new int[nodeCount];
        if (nodeCount == 0) return new ManagerLayout(x, y, 0, 0);

        List<List<Integer>> out = new ArrayList<>();
        List<List<Integer>> in = new ArrayList<>();
        int[] parent = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            out.add(new ArrayList<>());
            in.add(new ArrayList<>());
            parent[i] = i;
        }

        for (Edge edge : edges) {
            if (edge.from() == edge.to()) continue;
            if (!out.get(edge.from()).contains(edge.to())) {
                out.get(edge.from()).add(edge.to());
                in.get(edge.to()).add(edge.from());
            }
            parent[find(parent, edge.from())] = find(parent, edge.to());
        }

        boolean[][] back = new boolean[nodeCount][];
        for (int i = 0; i < nodeCount; i++) back[i] = new boolean[out.get(i).size()];

        // depth-first search to find the links that point backwards into a loop
        int[] state = new int[nodeCount];
        List<Integer> starts = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) if (in.get(i).isEmpty()) starts.add(i);
        for (int i = 0; i < nodeCount; i++) if (!starts.contains(i)) starts.add(i);
        for (int start : starts) {
            if (state[start] == 0) markBackEdges(start, out, state, back);
        }

        // longest-path layering over the remaining forward links
        int[] layer = new int[nodeCount];
        int[] inDegree = new int[nodeCount];
        for (int from = 0; from < nodeCount; from++) {
            for (int k = 0; k < out.get(from).size(); k++) {
                if (!back[from][k]) inDegree[out.get(from).get(k)]++;
            }
        }
        List<Integer> ready = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) if (inDegree[i] == 0) ready.add(i);
        for (int head = 0; head < ready.size(); head++) {
            int from = ready.get(head);
            for (int k = 0; k < out.get(from).size(); k++) {
                if (back[from][k]) continue;
                int to = out.get(from).get(k);
                layer[to] = Math.max(layer[to], layer[from] + 1);
                if (--inDegree[to] == 0) ready.add(to);
            }
        }

        int[] componentOf = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) componentOf[i] = find(parent, i);

        List<Integer> componentRoots = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) if (!componentRoots.contains(componentOf[i])) componentRoots.add(componentOf[i]);

        int cursorY = 0;
        int maxRight = 0;

        for (int root : componentRoots) {
            List<Integer> members = new ArrayList<>();
            int maxLayer = 0;
            for (int i = 0; i < nodeCount; i++) {
                if (componentOf[i] != root) continue;
                members.add(i);
                maxLayer = Math.max(maxLayer, layer[i]);
            }

            List<List<Integer>> columns = new ArrayList<>();
            for (int l = 0; l <= maxLayer; l++) columns.add(new ArrayList<>());
            for (int member : members) columns.get(layer[member]).add(member);

            double[] row = new double[nodeCount];
            for (List<Integer> column : columns) for (int i = 0; i < column.size(); i++) row[column.get(i)] = i;

            // a few barycentre sweeps to cut down crossing links
            for (int sweep = 0; sweep < 4; sweep++) {
                boolean forward = sweep % 2 == 0;
                for (int c = forward ? 1 : maxLayer - 1; forward ? c <= maxLayer : c >= 0; c += forward ? 1 : -1) {
                    List<Integer> column = columns.get(c);
                    double[] weight = new double[nodeCount];
                    for (int node : column) {
                        List<Integer> neighbours = forward ? in.get(node) : out.get(node);
                        double sum = 0;
                        int count = 0;
                        for (int neighbour : neighbours) {
                            if (layer[neighbour] == c + (forward ? -1 : 1)) {
                                sum += row[neighbour];
                                count++;
                            }
                        }
                        weight[node] = count == 0 ? row[node] : sum / count;
                    }
                    column.sort((a, b) -> Double.compare(weight[a], weight[b]));
                    for (int i = 0; i < column.size(); i++) row[column.get(i)] = i;
                }
            }

            int tallest = 0;
            for (List<Integer> column : columns) tallest = Math.max(tallest, column.size());

            for (int c = 0; c <= maxLayer; c++) {
                List<Integer> column = columns.get(c);
                int offset = (tallest - column.size()) * (NODE_HEIGHT + GAP_Y) / 2;
                for (int i = 0; i < column.size(); i++) {
                    int node = column.get(i);
                    x[node] = c * (NODE_WIDTH + GAP_X);
                    y[node] = cursorY + offset + i * (NODE_HEIGHT + GAP_Y);
                    maxRight = Math.max(maxRight, x[node] + NODE_WIDTH);
                }
            }

            cursorY += tallest * (NODE_HEIGHT + GAP_Y) + GAP_Y * 2;
        }

        return new ManagerLayout(x, y, maxRight, Math.max(cursorY - GAP_Y * 2 - GAP_Y, NODE_HEIGHT));
    }

    private static void markBackEdges(int node, List<List<Integer>> out, int[] state, boolean[][] back) {
        state[node] = 1;
        for (int k = 0; k < out.get(node).size(); k++) {
            int to = out.get(node).get(k);
            if (state[to] == 1) {
                back[node][k] = true;
            } else if (state[to] == 0) {
                markBackEdges(to, out, state, back);
            }
        }
        state[node] = 2;
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }
}
