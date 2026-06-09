package com.herbarium.common.util;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;
import java.util.List;

public class ImageUtils {

    private ImageUtils() {
    }

    public static class LeafFeatures {
        public double leafLength;
        public double leafWidth;
        public double leafArea;
        public double leafPerimeter;
        public double aspectRatio;
        public double circularity;
        public double rectangularity;
        public String leafShape;
        public String leafMargin;
        public String leafApex;
        public String leafBase;
        public Map<String, Object> colorFeatures;
        public double roughness;
        public double contrast;
        public String texture;
        public List<Double> featureVector;
    }

    public static LeafFeatures extractLeafFeatures(BufferedImage image) {
        LeafFeatures features = new LeafFeatures();

        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage grayImage = toGrayscale(image);
        byte[] grayPixels = ((DataBufferByte) grayImage.getRaster().getDataBuffer()).getData();

        int threshold = otsuThreshold(grayPixels, width, height);
        boolean[] binaryMask = createBinaryMask(grayPixels, width, height, threshold);

        binaryMask = ensureLeafIsForeground(binaryMask, width, height);

        binaryMask = keepLargestConnectedComponent(binaryMask, width, height);

        binaryMask = removeSmallNoise(binaryMask, width, height, 3);

        List<Point> contour = extractContour(binaryMask, width, height);
        if (contour.size() < 10) {
            setDefaultFeatures(features, width, height);
            return features;
        }

        int leafPixelCount = countForegroundPixels(binaryMask);
        double pixelArea = leafPixelCount;
        double perimeter = calculatePerimeter(contour);

        double[] majorAxis = findMajorAxis(contour);
        double leafLengthPx = majorAxis[2];
        double leafWidthPx = calculateMaxWidthPerpendicular(contour, majorAxis);

        double scale = 1.0;

        features.leafLength = leafLengthPx * scale;
        features.leafWidth = leafWidthPx * scale;
        features.leafArea = pixelArea * scale * scale;
        features.leafPerimeter = perimeter * scale;
        features.aspectRatio = leafWidthPx > 0 ? leafLengthPx / leafWidthPx : 1.0;
        features.circularity = perimeter > 0 ? (4 * Math.PI * pixelArea) / (perimeter * perimeter) : 0;

        Rectangle boundingBox = findBoundingBox(binaryMask, width, height);
        double rectArea = (double) boundingBox.width * boundingBox.height;
        features.rectangularity = rectArea > 0 ? pixelArea / rectArea : 0;

        features.leafShape = classifyLeafShape(features.aspectRatio, features.circularity, features.rectangularity);
        features.leafMargin = analyzeLeafMargin(contour, binaryMask, width, height);
        features.leafApex = analyzeLeafApex(contour, majorAxis);
        features.leafBase = analyzeLeafBase(contour, majorAxis);

        features.colorFeatures = extractColorFeatures(image, binaryMask);

        double[] textureFeatures = extractTextureFeatures(grayImage, binaryMask, width, height);
        features.roughness = textureFeatures[0];
        features.contrast = textureFeatures[1];
        features.texture = classifyTexture(features.roughness, features.contrast);

        features.featureVector = generateFeatureVector(features);

        return features;
    }

    private static void setDefaultFeatures(LeafFeatures features, int width, int height) {
        features.leafLength = height;
        features.leafWidth = width;
        features.leafArea = (double) width * height * 0.5;
        features.leafPerimeter = 2.0 * (width + height) * 0.7;
        features.aspectRatio = width > 0 ? (double) height / width : 1.0;
        features.circularity = 0.5;
        features.rectangularity = 0.6;
        features.leafShape = "椭圆形";
        features.leafMargin = "全缘";
        features.leafApex = "急尖";
        features.leafBase = "楔形";
        features.colorFeatures = new HashMap<>();
        features.roughness = 0.3;
        features.contrast = 0.4;
        features.texture = "纸质";
        features.featureVector = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            features.featureVector.add(0.0);
        }
    }

    public static BufferedImage toGrayscale(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = grayImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayImage;
    }

    private static int otsuThreshold(byte[] pixels, int width, int height) {
        int totalPixels = width * height;
        int[] histogram = new int[256];

        for (byte pixel : pixels) {
            int value = pixel & 0xFF;
            histogram[value]++;
        }

        int sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        int sumB = 0;
        int wB = 0;
        int wF = 0;

        double maxVariance = 0;
        int threshold = 128;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;

            wF = totalPixels - wB;
            if (wF == 0) break;

            sumB += i * histogram[i];

            double mB = (double) sumB / wB;
            double mF = (double) (sum - sumB) / wF;

            double variance = (double) wB * wF * (mB - mF) * (mB - mF);

            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = i;
            }
        }

        return threshold;
    }

    private static boolean[] createBinaryMask(byte[] grayPixels, int width, int height, int threshold) {
        int totalPixels = width * height;
        boolean[] mask = new boolean[totalPixels];

        for (int i = 0; i < totalPixels; i++) {
            int value = grayPixels[i] & 0xFF;
            mask[i] = value < threshold;
        }

        return mask;
    }

    private static boolean[] ensureLeafIsForeground(boolean[] mask, int width, int height) {
        int totalPixels = width * height;
        int foregroundCount = 0;

        int borderPixels = 0;
        int borderForeground = 0;

        for (int x = 0; x < width; x++) {
            borderPixels++;
            if (mask[x]) borderForeground++;
            borderPixels++;
            if (mask[(height - 1) * width + x]) borderForeground++;
        }
        for (int y = 1; y < height - 1; y++) {
            borderPixels++;
            if (mask[y * width]) borderForeground++;
            borderPixels++;
            if (mask[y * width + width - 1]) borderForeground++;
        }

        for (boolean b : mask) {
            if (b) foregroundCount++;
        }

        double borderForegroundRatio = borderPixels > 0 ? (double) borderForeground / borderPixels : 0;

        if (borderForegroundRatio > 0.5 || foregroundCount > totalPixels * 0.6) {
            boolean[] inverted = new boolean[totalPixels];
            for (int i = 0; i < totalPixels; i++) {
                inverted[i] = !mask[i];
            }
            return inverted;
        }

        return mask;
    }

    private static boolean[] keepLargestConnectedComponent(boolean[] mask, int width, int height) {
        boolean[] result = new boolean[width * height];
        boolean[] visited = new boolean[width * height];

        List<Point> largestRegion = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                if (mask[idx] && !visited[idx]) {
                    List<Point> region = floodFill(mask, visited, width, height, x, y);
                    if (region.size() > largestRegion.size()) {
                        largestRegion = region;
                    }
                }
            }
        }

        for (Point p : largestRegion) {
            result[p.y * width + p.x] = true;
        }

        return result;
    }

    private static boolean[] removeSmallNoise(boolean[] mask, int width, int height, int minSize) {
        boolean[] result = new boolean[width * height];
        boolean[] visited = new boolean[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                if (mask[idx] && !visited[idx]) {
                    List<Point> region = floodFill(mask, visited, width, height, x, y);
                    if (region.size() >= minSize) {
                        for (Point p : region) {
                            result[p.y * width + p.x] = true;
                        }
                    }
                }
            }
        }

        return result;
    }

    private static List<Point> floodFill(boolean[] mask, boolean[] visited, int width, int height, int startX, int startY) {
        List<Point> region = new ArrayList<>();
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startX, startY));
        visited[startY * width + startX] = true;

        int[] dx = {-1, 1, 0, 0, -1, -1, 1, 1};
        int[] dy = {0, 0, -1, 1, -1, 1, -1, 1};

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            region.add(p);

            for (int i = 0; i < 8; i++) {
                int nx = p.x + dx[i];
                int ny = p.y + dy[i];
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    int nidx = ny * width + nx;
                    if (mask[nidx] && !visited[nidx]) {
                        visited[nidx] = true;
                        queue.add(new Point(nx, ny));
                    }
                }
            }
        }

        return region;
    }

    private static int countForegroundPixels(boolean[] mask) {
        int count = 0;
        for (boolean b : mask) {
            if (b) count++;
        }
        return count;
    }

    private static List<Point> extractContour(boolean[] mask, int width, int height) {
        List<Point> contour = new ArrayList<>();
        boolean[] isContour = new boolean[width * height];

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int idx = y * width + x;
                if (mask[idx]) {
                    for (int k = 0; k < 4; k++) {
                        int nx = x + dx[k];
                        int ny = y + dy[k];
                        if (!mask[ny * width + nx]) {
                            isContour[idx] = true;
                            break;
                        }
                    }
                }
            }
        }

        Point start = null;
        for (int y = 0; y < height && start == null; y++) {
            for (int x = 0; x < width && start == null; x++) {
                if (isContour[y * width + x]) {
                    start = new Point(x, y);
                }
            }
        }

        if (start == null) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (isContour[y * width + x]) {
                        contour.add(new Point(x, y));
                    }
                }
            }
            return contour;
        }

        boolean[] visited = new boolean[width * height];
        Point current = start;
        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
        int startDir = 0;

        int safety = 0;
        int maxIterations = width * height;

        while (safety < maxIterations) {
            contour.add(current);
            visited[current.y * width + current.x] = true;

            boolean found = false;
            for (int i = 0; i < 8; i++) {
                int dirIdx = (startDir + i) % 8;
                int nx = current.x + directions[dirIdx][0];
                int ny = current.y + directions[dirIdx][1];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    int nidx = ny * width + nx;
                    if (isContour[nidx] && !visited[nidx]) {
                        current = new Point(nx, ny);
                        startDir = (dirIdx + 6) % 8;
                        found = true;
                        break;
                    }
                }
            }

            if (!found || (current.x == start.x && current.y == start.y && contour.size() > 10)) {
                break;
            }
            safety++;
        }

        return contour;
    }

    private static double calculatePerimeter(List<Point> contour) {
        if (contour.size() < 2) return 0;

        double perimeter = 0;
        for (int i = 0; i < contour.size(); i++) {
            Point p1 = contour.get(i);
            Point p2 = contour.get((i + 1) % contour.size());
            perimeter += p1.distance(p2);
        }
        return perimeter;
    }

    private static double[] findMajorAxis(List<Point> contour) {
        if (contour.size() < 2) return new double[]{0, 0, 0, 0, 0};

        double maxDist = 0;
        Point p1Max = contour.get(0);
        Point p2Max = contour.get(0);

        int step = Math.max(1, contour.size() / 100);
        for (int i = 0; i < contour.size(); i += step) {
            Point p1 = contour.get(i);
            for (int j = i + 1; j < contour.size(); j += step) {
                Point p2 = contour.get(j);
                double dist = p1.distance(p2);
                if (dist > maxDist) {
                    maxDist = dist;
                    p1Max = p1;
                    p2Max = p2;
                }
            }
        }

        double angle = Math.atan2(p2Max.y - p1Max.y, p2Max.x - p1Max.x);

        return new double[]{p1Max.x, p1Max.y, maxDist, p2Max.x, p2Max.y, angle};
    }

    private static double calculateMaxWidthPerpendicular(List<Point> contour, double[] majorAxis) {
        if (contour.isEmpty()) return 0;

        double angle = majorAxis[5];
        double perpAngle = angle + Math.PI / 2;

        double maxWidth = 0;

        Point2D.Double center = new Point2D.Double(majorAxis[0] + majorAxis[3] / 2, majorAxis[1] + majorAxis[4] / 2);

        double[] projections = new double[contour.size()];
        for (int i = 0; i < contour.size(); i++) {
            Point p = contour.get(i);
            double dx = p.x - center.x;
            double dy = p.y - center.y;
            projections[i] = dx * Math.cos(perpAngle) + dy * Math.sin(perpAngle);
        }

        double minProj = Double.MAX_VALUE;
        double maxProj = -Double.MAX_VALUE;
        for (double proj : projections) {
            if (proj < minProj) minProj = proj;
            if (proj > maxProj) maxProj = proj;
        }

        maxWidth = maxProj - minProj;

        return maxWidth;
    }

    private static Rectangle findBoundingBox(boolean[] mask, int width, int height) {
        int minX = width, minY = height, maxX = 0, maxY = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (mask[y * width + x]) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private static String classifyLeafShape(double aspectRatio, double circularity, double rectangularity) {
        if (aspectRatio < 1.2 && circularity > 0.8) {
            return "圆形";
        }
        if (aspectRatio >= 1.2 && aspectRatio < 1.8) {
            if (circularity > 0.7) return "卵形";
            if (circularity > 0.5) return "椭圆形";
            return "菱形";
        }
        if (aspectRatio >= 1.8 && aspectRatio < 3.0) {
            if (circularity > 0.6) return "椭圆形";
            if (rectangularity > 0.7) return "披针形";
            return "卵形";
        }
        if (aspectRatio >= 3.0 && aspectRatio < 5.0) {
            return "披针形";
        }
        if (aspectRatio >= 5.0) {
            return "线形";
        }
        if (circularity < 0.3 && rectangularity < 0.5) {
            return "心形";
        }
        return "椭圆形";
    }

    private static String analyzeLeafMargin(List<Point> contour, boolean[] mask, int width, int height) {
        if (contour.size() < 10) return "全缘";

        double[] angles = new double[contour.size()];
        int window = 5;

        for (int i = 0; i < contour.size(); i++) {
            int prevIdx = (i - window + contour.size()) % contour.size();
            int nextIdx = (i + window) % contour.size();

            Point prev = contour.get(prevIdx);
            Point curr = contour.get(i);
            Point next = contour.get(nextIdx);

            double angle1 = Math.atan2(curr.y - prev.y, curr.x - prev.x);
            double angle2 = Math.atan2(next.y - curr.y, next.x - curr.x);
            double angleDiff = Math.abs(angle2 - angle1);
            if (angleDiff > Math.PI) angleDiff = 2 * Math.PI - angleDiff;

            angles[i] = angleDiff;
        }

        double meanAngle = 0;
        for (double angle : angles) {
            meanAngle += angle;
        }
        meanAngle /= angles.length;

        int sharpChanges = 0;
        double threshold = 0.3;
        for (double angle : angles) {
            if (angle > meanAngle + threshold) {
                sharpChanges++;
            }
        }

        double changeRate = (double) sharpChanges / angles.length;

        if (changeRate < 0.02) {
            return "全缘";
        } else if (changeRate < 0.05) {
            return "波状";
        } else if (changeRate < 0.1) {
            return "钝齿";
        } else if (changeRate < 0.2) {
            return "锯齿";
        } else {
            return "重锯齿";
        }
    }

    private static String analyzeLeafApex(List<Point> contour, double[] majorAxis) {
        if (contour.size() < 3) return "急尖";

        Point apexPoint = findApexPoint(contour, majorAxis, true);
        double apexAngle = calculatePointAngle(contour, apexPoint, 10);

        if (apexAngle < 0.5) {
            return "渐尖";
        } else if (apexAngle < 1.0) {
            return "急尖";
        } else if (apexAngle < 1.8) {
            return "钝形";
        } else if (apexAngle < 2.5) {
            return "圆形";
        } else {
            return "微凹";
        }
    }

    private static String analyzeLeafBase(List<Point> contour, double[] majorAxis) {
        if (contour.size() < 3) return "楔形";

        Point basePoint = findApexPoint(contour, majorAxis, false);
        double baseAngle = calculatePointAngle(contour, basePoint, 10);

        if (baseAngle < 0.8) {
            return "楔形";
        } else if (baseAngle < 1.5) {
            return "渐狭";
        } else if (baseAngle < 2.2) {
            return "圆形";
        } else if (baseAngle < 2.8) {
            return "心形";
        } else {
            return "截形";
        }
    }

    private static Point findApexPoint(List<Point> contour, double[] majorAxis, boolean isApex) {
        double angle = majorAxis[5];
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        Point result = contour.get(0);
        double extremeProj = isApex ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (Point p : contour) {
            double proj = p.x * cos + p.y * sin;
            if (isApex) {
                if (proj > extremeProj) {
                    extremeProj = proj;
                    result = p;
                }
            } else {
                if (proj < extremeProj) {
                    extremeProj = proj;
                    result = p;
                }
            }
        }

        return result;
    }

    private static double calculatePointAngle(List<Point> contour, Point target, int window) {
        int targetIdx = -1;
        double minDist = Double.MAX_VALUE;

        for (int i = 0; i < contour.size(); i++) {
            double dist = contour.get(i).distance(target);
            if (dist < minDist) {
                minDist = dist;
                targetIdx = i;
            }
        }

        if (targetIdx < 0) return Math.PI;

        int prevIdx = (targetIdx - window + contour.size()) % contour.size();
        int nextIdx = (targetIdx + window) % contour.size();

        Point prev = contour.get(prevIdx);
        Point next = contour.get(nextIdx);

        double dx1 = target.x - prev.x;
        double dy1 = target.y - prev.y;
        double dx2 = next.x - target.x;
        double dy2 = next.y - target.y;

        double dot = dx1 * dx2 + dy1 * dy2;
        double mag1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
        double mag2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

        if (mag1 == 0 || mag2 == 0) return Math.PI;

        double cosAngle = dot / (mag1 * mag2);
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle));

        return Math.acos(cosAngle);
    }

    private static Map<String, Object> extractColorFeatures(BufferedImage image, boolean[] mask) {
        Map<String, Object> colorFeatures = new HashMap<>();
        int width = image.getWidth();
        int height = image.getHeight();

        int[] rgbHistogram = new int[256 * 3];
        int[] hueHistogram = new int[12];
        int[] saturationHistogram = new int[8];
        int[] valueHistogram = new int[8];

        double sumR = 0, sumG = 0, sumB = 0;
        double sumH = 0, sumS = 0, sumV = 0;
        double sumSqH = 0, sumSqS = 0, sumSqV = 0;
        double sumCuH = 0, sumCuS = 0, sumCuV = 0;
        int pixelCount = 0;
        int greenPixelCount = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (mask[y * width + x]) {
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;

                    rgbHistogram[r]++;
                    rgbHistogram[256 + g]++;
                    rgbHistogram[512 + b]++;

                    float[] hsv = new float[3];
                    java.awt.Color.RGBtoHSB(r, g, b, hsv);

                    int hueBin = (int) (hsv[0] * 12);
                    if (hueBin >= 12) hueBin = 11;
                    hueHistogram[hueBin]++;

                    int satBin = (int) (hsv[1] * 8);
                    if (satBin >= 8) satBin = 7;
                    saturationHistogram[satBin]++;

                    int valBin = (int) (hsv[2] * 8);
                    if (valBin >= 8) valBin = 7;
                    valueHistogram[valBin]++;

                    sumR += r;
                    sumG += g;
                    sumB += b;
                    sumH += hsv[0] * 360;
                    sumS += hsv[1];
                    sumV += hsv[2];

                    double hNorm = hsv[0] * 360;
                    double sNorm = hsv[1];
                    double vNorm = hsv[2];
                    sumSqH += hNorm * hNorm;
                    sumSqS += sNorm * sNorm;
                    sumSqV += vNorm * vNorm;

                    if (hsv[0] * 360 >= 60 && hsv[0] * 360 <= 170 && hsv[1] > 0.2 && hsv[2] > 0.2) {
                        greenPixelCount++;
                    }

                    pixelCount++;
                }
            }
        }

        if (pixelCount == 0) {
            colorFeatures.put("greenRatio", 0.0);
            colorFeatures.put("dominantColors", new HashMap<>());
            colorFeatures.put("colorMoments", new HashMap<>());
            colorFeatures.put("histogram", new HashMap<>());
            return colorFeatures;
        }

        double meanR = sumR / pixelCount;
        double meanG = sumG / pixelCount;
        double meanB = sumB / pixelCount;
        double meanH = sumH / pixelCount;
        double meanS = sumS / pixelCount;
        double meanV = sumV / pixelCount;

        double varianceH = sumSqH / pixelCount - meanH * meanH;
        double varianceS = sumSqS / pixelCount - meanS * meanS;
        double varianceV = sumSqV / pixelCount - meanV * meanV;

        double greenRatio = (double) greenPixelCount / pixelCount;

        Map<String, double[]> dominantColors = new HashMap<>();
        dominantColors.put("green", new double[]{meanR, meanG, meanB});

        int maxHueBin = 0;
        for (int i = 1; i < hueHistogram.length; i++) {
            if (hueHistogram[i] > hueHistogram[maxHueBin]) maxHueBin = i;
        }
        double mainHue = (maxHueBin + 0.5) * 30;
        dominantColors.put("mainHue", new double[]{mainHue, meanS, meanV});

        Map<String, Double> colorMoments = new HashMap<>();
        colorMoments.put("meanHue", meanH);
        colorMoments.put("meanSaturation", meanS);
        colorMoments.put("meanValue", meanV);
        colorMoments.put("varianceHue", Math.sqrt(Math.max(0, varianceH)));
        colorMoments.put("varianceSaturation", Math.sqrt(Math.max(0, varianceS)));
        colorMoments.put("varianceValue", Math.sqrt(Math.max(0, varianceV)));
        colorMoments.put("meanRed", meanR);
        colorMoments.put("meanGreen", meanG);
        colorMoments.put("meanBlue", meanB);

        Map<String, Object> histogram = new HashMap<>();
        List<Double> hueHistList = new ArrayList<>();
        for (int value : hueHistogram) {
            hueHistList.add((double) value / pixelCount);
        }
        histogram.put("hue", hueHistList);

        List<Double> satHistList = new ArrayList<>();
        for (int value : saturationHistogram) {
            satHistList.add((double) value / pixelCount);
        }
        histogram.put("saturation", satHistList);

        List<Double> valHistList = new ArrayList<>();
        for (int value : valueHistogram) {
            valHistList.add((double) value / pixelCount);
        }
        histogram.put("value", valHistList);

        colorFeatures.put("dominantColors", dominantColors);
        colorFeatures.put("colorMoments", colorMoments);
        colorFeatures.put("histogram", histogram);
        colorFeatures.put("greenRatio", greenRatio);

        return colorFeatures;
    }

    private static double[] extractTextureFeatures(BufferedImage grayImage, boolean[] mask, int width, int height) {
        byte[] pixels = ((DataBufferByte) grayImage.getRaster().getDataBuffer()).getData();

        double sumDiff = 0;
        double edgeCount = 0;
        double totalPixelPairs = 0;
        double sumSqDiff = 0;

        int sobelThreshold = 30;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int idx = y * width + x;
                if (mask[idx]) {
                    int center = pixels[idx] & 0xFF;
                    int right = pixels[idx + 1] & 0xFF;
                    int bottom = pixels[idx + width] & 0xFF;

                    int dx = right - center;
                    int dy = bottom - center;
                    double gradient = Math.sqrt(dx * dx + dy * dy);

                    sumDiff += gradient;
                    sumSqDiff += gradient * gradient;
                    totalPixelPairs++;

                    int gx = (-1 * (pixels[(y-1)*width + (x-1)] & 0xFF) + 1 * (pixels[(y-1)*width + (x+1)] & 0xFF)
                            + -2 * (pixels[y*width + (x-1)] & 0xFF) + 2 * (pixels[y*width + (x+1)] & 0xFF)
                            + -1 * (pixels[(y+1)*width + (x-1)] & 0xFF) + 1 * (pixels[(y+1)*width + (x+1)] & 0xFF));

                    int gy = (-1 * (pixels[(y-1)*width + (x-1)] & 0xFF) + -2 * (pixels[(y-1)*width + x] & 0xFF) + -1 * (pixels[(y-1)*width + (x+1)] & 0xFF)
                            + 1 * (pixels[(y+1)*width + (x-1)] & 0xFF) + 2 * (pixels[(y+1)*width + x] & 0xFF) + 1 * (pixels[(y+1)*width + (x+1)] & 0xFF));

                    double magnitude = Math.sqrt(gx * gx + gy * gy);
                    if (magnitude > sobelThreshold) {
                        edgeCount++;
                    }
                }
            }
        }

        if (totalPixelPairs == 0) return new double[]{0, 0};

        double roughness = edgeCount / totalPixelPairs;
        double meanDiff = sumDiff / totalPixelPairs;
        double contrast = Math.sqrt(sumSqDiff / totalPixelPairs - meanDiff * meanDiff);

        roughness = Math.min(1.0, roughness * 3);
        contrast = Math.min(1.0, contrast / 100);

        return new double[]{roughness, contrast};
    }

    private static String classifyTexture(double roughness, double contrast) {
        if (roughness < 0.15 && contrast < 0.3) {
            return "膜质";
        } else if (roughness < 0.25 && contrast < 0.4) {
            return "纸质";
        } else if (roughness < 0.35 && contrast < 0.5) {
            return "草质";
        } else if (roughness > 0.5 && contrast > 0.6) {
            return "革质";
        } else if (roughness > 0.4 && contrast > 0.5) {
            return "粗糙";
        } else {
            return "纸质";
        }
    }

    private static List<Double> generateFeatureVector(LeafFeatures features) {
        List<Double> vector = new ArrayList<>();

        vector.add(normalize(features.leafLength, 0, 2000));
        vector.add(normalize(features.leafWidth, 0, 1000));
        vector.add(normalize(features.leafArea, 0, 2000000));
        vector.add(normalize(features.leafPerimeter, 0, 10000));
        vector.add(normalize(features.aspectRatio, 0, 10));
        vector.add(features.circularity);
        vector.add(features.rectangularity);
        vector.add(features.roughness);
        vector.add(features.contrast);

        Map<String, Object> colorFeatures = features.colorFeatures;
        if (colorFeatures != null && colorFeatures.containsKey("colorMoments")) {
            @SuppressWarnings("unchecked")
            Map<String, Double> moments = (Map<String, Double>) colorFeatures.get("colorMoments");
            vector.add(normalize(moments.getOrDefault("meanHue", 0.0), 0, 360));
            vector.add(moments.getOrDefault("meanSaturation", 0.0));
            vector.add(moments.getOrDefault("meanValue", 0.0));
            vector.add(normalize(moments.getOrDefault("varianceHue", 0.0), 0, 180));
            vector.add(normalize(moments.getOrDefault("varianceSaturation", 0.0), 0, 1));
            vector.add(normalize(moments.getOrDefault("varianceValue", 0.0), 0, 1));
        } else {
            for (int i = 0; i < 6; i++) vector.add(0.0);
        }

        if (colorFeatures != null && colorFeatures.containsKey("histogram")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> hist = (Map<String, Object>) colorFeatures.get("histogram");
            @SuppressWarnings("unchecked")
            List<Double> hueHist = (List<Double>) hist.getOrDefault("hue", new ArrayList<>());
            for (int i = 0; i < 12; i++) {
                vector.add(i < hueHist.size() ? hueHist.get(i) : 0.0);
            }
            @SuppressWarnings("unchecked")
            List<Double> satHist = (List<Double>) hist.getOrDefault("saturation", new ArrayList<>());
            for (int i = 0; i < 8; i++) {
                vector.add(i < satHist.size() ? satHist.get(i) : 0.0);
            }
        } else {
            for (int i = 0; i < 20; i++) vector.add(0.0);
        }

        int shapeCode = encodeShape(features.leafShape);
        int marginCode = encodeMargin(features.leafMargin);
        int apexCode = encodeApex(features.leafApex);
        int baseCode = encodeBase(features.leafBase);
        int textureCode = encodeTexture(features.texture);

        for (int i = 0; i < 10; i++) {
            vector.add(i == shapeCode ? 1.0 : 0.0);
        }
        for (int i = 0; i < 10; i++) {
            vector.add(i == marginCode ? 1.0 : 0.0);
        }
        for (int i = 0; i < 10; i++) {
            vector.add(i == apexCode ? 1.0 : 0.0);
        }
        for (int i = 0; i < 10; i++) {
            vector.add(i == baseCode ? 1.0 : 0.0);
        }
        for (int i = 0; i < 10; i++) {
            vector.add(i == textureCode ? 1.0 : 0.0);
        }

        while (vector.size() < 128) {
            vector.add(0.0);
        }

        return vector;
    }

    private static double normalize(double value, double min, double max) {
        double range = max - min;
        if (range == 0) return 0;
        return Math.max(0, Math.min(1, (value - min) / range));
    }

    private static int encodeShape(String shape) {
        return switch (shape) {
            case "圆形" -> 0;
            case "卵形" -> 1;
            case "椭圆形" -> 2;
            case "披针形" -> 3;
            case "心形" -> 4;
            case "线形" -> 5;
            case "菱形" -> 6;
            case "匙形" -> 7;
            case "戟形" -> 8;
            case "箭形" -> 9;
            default -> 2;
        };
    }

    private static int encodeMargin(String margin) {
        return switch (margin) {
            case "全缘" -> 0;
            case "锯齿" -> 1;
            case "重锯齿" -> 2;
            case "齿状" -> 3;
            case "波状" -> 4;
            case "浅裂" -> 5;
            case "深裂" -> 6;
            case "全裂" -> 7;
            case "睫毛状" -> 8;
            case "钝齿" -> 9;
            default -> 0;
        };
    }

    private static int encodeApex(String apex) {
        return switch (apex) {
            case "渐尖" -> 0;
            case "急尖" -> 1;
            case "钝形" -> 2;
            case "圆形" -> 3;
            case "微凹" -> 4;
            case "倒心形" -> 5;
            case "尾尖" -> 6;
            case "芒尖" -> 7;
            case "凸尖" -> 8;
            case "截形" -> 9;
            default -> 1;
        };
    }

    private static int encodeBase(String base) {
        return switch (base) {
            case "楔形" -> 0;
            case "圆形" -> 1;
            case "心形" -> 2;
            case "箭形" -> 3;
            case "戟形" -> 4;
            case "渐狭" -> 5;
            case "偏斜" -> 6;
            case "耳垂形" -> 7;
            case "盾形" -> 8;
            case "截形" -> 9;
            default -> 0;
        };
    }

    private static int encodeTexture(String texture) {
        return switch (texture) {
            case "革质" -> 0;
            case "纸质" -> 1;
            case "肉质" -> 2;
            case "膜质" -> 3;
            case "草质" -> 4;
            case "木栓质" -> 5;
            case "海绵质" -> 6;
            case "粗糙" -> 7;
            case "光滑" -> 8;
            case "被毛" -> 9;
            default -> 1;
        };
    }

    public static BufferedImage loadImage(String imagePath) throws Exception {
        return javax.imageio.ImageIO.read(new java.io.File(imagePath));
    }
}
