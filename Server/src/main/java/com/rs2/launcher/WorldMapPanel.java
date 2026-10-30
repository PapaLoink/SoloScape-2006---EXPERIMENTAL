package com.rs2.launcher;

import com.rs2.launcher.TransparentColorFilter;
import com.rs2.model.Entity;
import com.rs2.model.World;
import com.rs2.model.player.Player;
import com.rs2.model.skill.crafting.CraftingHandler;
import com.rs2.util.FileUtil;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.net.URLConnection;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class WorldMapPanel
extends JPanel {
    /*
     * The November 2006 downloadable RuneScape map is a 31 x 28 region image
     * rendered at 2 pixels per game tile:
     *   x: 2048 .. 4031
     *   y: 2496 .. 4287
     */
    private static final int WORLD_MIN_X = 2048;
    private static final int WORLD_MIN_Y = 2496;
    private static final int WORLD_MAX_X_EXCLUSIVE = 4032;
    private static final int WORLD_MAX_Y_EXCLUSIVE = 4288;
    private static final int HD_PIXELS_PER_TILE = 2;
    private static final int HD_MAP_WIDTH = (WORLD_MAX_X_EXCLUSIVE - WORLD_MIN_X) * HD_PIXELS_PER_TILE;
    private static final int HD_MAP_HEIGHT = (WORLD_MAX_Y_EXCLUSIVE - WORLD_MIN_Y) * HD_PIXELS_PER_TILE;

    private static final String HD_MAP_CACHE_PATH = "./data/launcher/world_map_2006.jpg";
    private static final String CACHE_RENDERED_TILE_DIRECTORY = "../Client/runtime/cache/world_map_control_panel_tiles";
    private static final int CACHE_WORLD_MIN_X = 2112;
    private static final int CACHE_WORLD_MIN_Y = 2624;
    private static final int CACHE_WORLD_MAX_X_EXCLUSIVE = 3776;
    private static final int CACHE_WORLD_MAX_Y_EXCLUSIVE = 4032;
    private static final String[] HD_MAP_URLS = new String[]{
        "https://www.runerebels.com/map/2006map.jpg",
        "https://runescape.wiki/w/Special:Redirect/file/Rsmap_21_November_2006.jpg",
        "https://img6.imageshack.us/img6/6315/rsmap20061121.jpg"
    };

    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 3.0;
    private double mapScale = 1.0;
    private int baseMapWidthPixels = HD_MAP_WIDTH;
    private int baseMapHeightPixels = HD_MAP_HEIGHT;
    private int mapWorldMinX = WORLD_MIN_X;
    private int mapWorldMinY = WORLD_MIN_Y;
    private int mapWorldMaxXExclusive = WORLD_MAX_X_EXCLUSIVE;
    private int mapWorldMaxYExclusive = WORLD_MAX_Y_EXCLUSIVE;
    public int mapWidthPixels = HD_MAP_WIDTH;
    public int mapHeightPixels = HD_MAP_HEIGHT;

    private final CraftingHandler mapIndex = new CraftingHandler();
    private final Toolkit toolkit = Toolkit.getDefaultToolkit();
    private static Image[][] regionImages;
    private static int[] regionBaseXs;
    private static int[] regionBaseYs;
    private BufferedImage hdWorldMap;
    private volatile boolean hdMapDownloadAttempted;
    private volatile boolean usingCacheRenderedMap;
    private volatile boolean cacheRenderedMapLoadInProgress;
    private long lastCacheRenderedMapCheckMillis;
    private volatile boolean usingCacheRenderedTiles;
    private int cacheTilePixelsPerGameTile;
    private int cacheTileGameSize;
    private int cacheTileColumnCount;
    private int cacheTileRowCount;
    private final Map<String, BufferedImage> cacheRenderedTileCache = new LinkedHashMap<String, BufferedImage>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
            if (this.size() > 4) {
                BufferedImage image = eldest.getValue();
                if (image != null) {
                    image.flush();
                }
                return true;
            }
            return false;
        }
    };
    public boolean showPlayerNames = true;

    public WorldMapPanel() {
        this.setFocusable(true);
        this.setBackground(Color.black);
        this.setDoubleBuffered(false);
        this.setSize(this.mapWidthPixels, this.mapHeightPixels);
        this.setPreferredSize(new java.awt.Dimension(this.mapWidthPixels, this.mapHeightPixels));

        this.loadCachedHdWorldMap();

        if (this.hdWorldMap == null && !this.usingCacheRenderedTiles) {
            try {
                this.mapIndex.loadMapIndex();
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }

            regionImages = new Image[this.mapIndex.mapIndexEntryCount][4];
            regionBaseXs = new int[this.mapIndex.mapIndexEntryCount];
            regionBaseYs = new int[this.mapIndex.mapIndexEntryCount];
            this.loadMapRegionSprites();
            this.downloadHdWorldMapAsync();
        }
    }

    public final Point worldToMapPoint(int worldX, int worldY) {
        double pixelsPerTileX = (double)this.baseMapWidthPixels / (double)(this.mapWorldMaxXExclusive - this.mapWorldMinX);
        double pixelsPerTileY = (double)this.baseMapHeightPixels / (double)(this.mapWorldMaxYExclusive - this.mapWorldMinY);
        return new Point(
            (int)Math.round((worldX - this.mapWorldMinX + 0.5) * pixelsPerTileX * this.mapScale),
            (int)Math.round((this.mapWorldMaxYExclusive - worldY - 0.5) * pixelsPerTileY * this.mapScale)
        );
    }

    public final double getZoom() {
        return this.mapScale;
    }

    public final void setZoom(double zoom) {
        if (zoom < MIN_ZOOM) {
            zoom = MIN_ZOOM;
        }
        if (zoom > MAX_ZOOM) {
            zoom = MAX_ZOOM;
        }
        this.mapScale = zoom;
        this.mapWidthPixels = (int)Math.round(this.baseMapWidthPixels * this.mapScale);
        this.mapHeightPixels = (int)Math.round(this.baseMapHeightPixels * this.mapScale);
        java.awt.Dimension mapSize = new java.awt.Dimension(this.mapWidthPixels, this.mapHeightPixels);
        this.setPreferredSize(mapSize);
        this.setSize(mapSize);
        this.revalidate();
        this.repaint();
    }

    private boolean isOnHdWorldMap(int worldX, int worldY) {
        return worldX >= this.mapWorldMinX
            && worldX < this.mapWorldMaxXExclusive
            && worldY >= this.mapWorldMinY
            && worldY < this.mapWorldMaxYExclusive;
    }


    private void clearCacheRenderedTileCache() {
        for (BufferedImage image : this.cacheRenderedTileCache.values()) {
            if (image != null) {
                image.flush();
            }
        }
        this.cacheRenderedTileCache.clear();
    }

    private void releaseLegacyMapSprites() {
        if (regionImages != null) {
            for (int index = 0; index < regionImages.length; ++index) {
                if (regionImages[index] == null) {
                    continue;
                }
                for (int plane = 0; plane < regionImages[index].length; ++plane) {
                    Image image = regionImages[index][plane];
                    if (image != null) {
                        image.flush();
                    }
                }
            }
        }
        regionImages = null;
        regionBaseXs = null;
        regionBaseYs = null;
    }

    private BufferedImage toCompactRgb(BufferedImage source) {
        if (source == null || source.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return source;
        }

        BufferedImage compact = new BufferedImage(
            source.getWidth(),
            source.getHeight(),
            BufferedImage.TYPE_3BYTE_BGR
        );
        Graphics2D graphics2D = compact.createGraphics();
        try {
            graphics2D.drawImage(source, 0, 0, null);
        }
        finally {
            graphics2D.dispose();
        }
        source.flush();
        return compact;
    }

    private void applyHdWorldMap(BufferedImage image, int worldMinX, int worldMinY, int worldMaxXExclusive, int worldMaxYExclusive, boolean cacheRenderedMap) {
        if (image == null) {
            return;
        }
        if (this.hdWorldMap != null && this.hdWorldMap != image) {
            this.hdWorldMap.flush();
        }
        this.clearCacheRenderedTileCache();
        this.releaseLegacyMapSprites();
        this.hdWorldMap = image;
        this.baseMapWidthPixels = image.getWidth();
        this.baseMapHeightPixels = image.getHeight();
        this.mapWorldMinX = worldMinX;
        this.mapWorldMinY = worldMinY;
        this.mapWorldMaxXExclusive = worldMaxXExclusive;
        this.mapWorldMaxYExclusive = worldMaxYExclusive;
        this.usingCacheRenderedMap = cacheRenderedMap;
        this.usingCacheRenderedTiles = false;
        this.mapWidthPixels = (int)Math.round(this.baseMapWidthPixels * this.mapScale);
        this.mapHeightPixels = (int)Math.round(this.baseMapHeightPixels * this.mapScale);
        java.awt.Dimension mapSize = new java.awt.Dimension(this.mapWidthPixels, this.mapHeightPixels);
        this.setPreferredSize(mapSize);
        this.setSize(mapSize);
        this.revalidate();
        java.awt.Container parentContainer = this.getParent();
        if (parentContainer != null) {
            parentContainer.setPreferredSize(mapSize);
            parentContainer.revalidate();
        }
        this.repaint();
    }


    private boolean loadCacheRenderedTileManifest() {
        File tileDirectory = new File(CACHE_RENDERED_TILE_DIRECTORY);
        File manifestFile = new File(tileDirectory, "manifest.txt");
        if (!manifestFile.exists()) {
            return false;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(manifestFile));
            String line = reader.readLine();
            if (line == null) {
                return false;
            }
            String[] values = line.trim().split(";");
            if (values.length < 8) {
                return false;
            }

            int worldMinX = Integer.parseInt(values[0]);
            int worldMinY = Integer.parseInt(values[1]);
            int worldWidth = Integer.parseInt(values[2]);
            int worldHeight = Integer.parseInt(values[3]);
            int pixelsPerGameTile = Integer.parseInt(values[4]);
            int tileGameSize = Integer.parseInt(values[5]);
            int columnCount = Integer.parseInt(values[6]);
            int rowCount = Integer.parseInt(values[7]);
            if (worldWidth <= 0 || worldHeight <= 0 || pixelsPerGameTile <= 0
                || tileGameSize <= 0 || columnCount <= 0 || rowCount <= 0) {
                return false;
            }

            this.mapWorldMinX = worldMinX;
            this.mapWorldMinY = worldMinY;
            this.mapWorldMaxXExclusive = worldMinX + worldWidth;
            this.mapWorldMaxYExclusive = worldMinY + worldHeight;
            this.cacheTilePixelsPerGameTile = pixelsPerGameTile;
            this.cacheTileGameSize = tileGameSize;
            this.cacheTileColumnCount = columnCount;
            this.cacheTileRowCount = rowCount;
            this.baseMapWidthPixels = worldWidth * pixelsPerGameTile;
            this.baseMapHeightPixels = worldHeight * pixelsPerGameTile;
            this.mapWidthPixels = (int)Math.round(this.baseMapWidthPixels * this.mapScale);
            this.mapHeightPixels = (int)Math.round(this.baseMapHeightPixels * this.mapScale);
            if (this.hdWorldMap != null) {
                this.hdWorldMap.flush();
                this.hdWorldMap = null;
            }
            this.releaseLegacyMapSprites();
            this.clearCacheRenderedTileCache();
            this.usingCacheRenderedTiles = true;
            this.usingCacheRenderedMap = true;

            java.awt.Dimension mapSize = new java.awt.Dimension(this.mapWidthPixels, this.mapHeightPixels);
            this.setPreferredSize(mapSize);
            this.setSize(mapSize);
            this.revalidate();
            java.awt.Container parentContainer = this.getParent();
            if (parentContainer != null) {
                parentContainer.setPreferredSize(mapSize);
                parentContainer.revalidate();
            }
            this.repaint();
            System.out.println(
                "Loaded lossless tiled cache map: " + this.baseMapWidthPixels + "x" + this.baseMapHeightPixels
                    + " (" + columnCount + "x" + rowCount + " tiles)"
            );
            return true;
        }
        catch (Exception exception) {
            System.err.println("Unable to load tiled cache-map manifest: " + exception.getMessage());
            return false;
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception ignored) {
                }
            }
        }
    }

    private void loadCachedHdWorldMap() {
        try {
            if (this.loadCacheRenderedTileManifest()) {
                return;
            }

            File mapFile = new File(HD_MAP_CACHE_PATH);
            if (!mapFile.exists()) {
                return;
            }
            BufferedImage loadedImage = ImageIO.read(mapFile);
            if (loadedImage != null) {
                this.applyHdWorldMap(
                    loadedImage,
                    WORLD_MIN_X,
                    WORLD_MIN_Y,
                    WORLD_MAX_X_EXCLUSIVE,
                    WORLD_MAX_Y_EXCLUSIVE,
                    false
                );
                System.out.println(
                    "Loaded HD 2006 world map: " + loadedImage.getWidth() + "x" + loadedImage.getHeight()
                );
            }
        }
        catch (Exception exception) {
            System.err.println("Unable to load cached HD world map: " + exception.getMessage());
        }
    }

    private void checkForCacheRenderedMapAsync() {
        if (this.usingCacheRenderedTiles) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastCacheRenderedMapCheckMillis < 5000L) {
            return;
        }
        this.lastCacheRenderedMapCheckMillis = now;
        if (this.loadCacheRenderedTileManifest()) {
            System.out.println("Switched to lossless tiled cache-rendered world map.");
        }
    }

    private void downloadHdWorldMapAsync() {
        if (this.usingCacheRenderedTiles || this.hdMapDownloadAttempted) {
            return;
        }
        this.hdMapDownloadAttempted = true;

        Thread downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedImage downloadedImage = null;

                for (int urlIndex = 0; urlIndex < HD_MAP_URLS.length && downloadedImage == null; ++urlIndex) {
                    if (WorldMapPanel.this.usingCacheRenderedTiles) {
                        return;
                    }
                    InputStream inputStream = null;
                    try {
                        URLConnection connection = new URL(HD_MAP_URLS[urlIndex]).openConnection();
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(15000);
                        connection.setRequestProperty("User-Agent", "ProgressiveSP-ControlPanel/1.0");
                        inputStream = connection.getInputStream();
                        BufferedImage candidateImage = ImageIO.read(inputStream);
                        if (candidateImage != null) {
                            double expectedAspect = (double)HD_MAP_WIDTH / (double)HD_MAP_HEIGHT;
                            double candidateAspect = (double)candidateImage.getWidth() / (double)candidateImage.getHeight();
                            double aspectDifference = Math.abs(candidateAspect - expectedAspect) / expectedAspect;
                            if (candidateImage.getWidth() >= 1500
                                && candidateImage.getHeight() >= 1200
                                && aspectDifference <= 0.08) {
                                downloadedImage = candidateImage;
                            } else {
                                System.err.println(
                                    "Ignoring unsuitable world map from " + HD_MAP_URLS[urlIndex]
                                        + ": " + candidateImage.getWidth() + "x" + candidateImage.getHeight()
                                );
                            }
                        }
                    }
                    catch (Exception exception) {
                        System.err.println(
                            "Unable to download HD world map from " + HD_MAP_URLS[urlIndex] + ": " + exception.getMessage()
                        );
                    }
                    finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            }
                            catch (Exception ignored) {
                            }
                        }
                    }
                }

                if (downloadedImage == null) {
                    if (!WorldMapPanel.this.usingCacheRenderedTiles) {
                        System.err.println("HD world map unavailable; using legacy live-map tiles.");
                    }
                    return;
                }

                try {
                    File mapFile = new File(HD_MAP_CACHE_PATH);
                    File parentFile = mapFile.getParentFile();
                    if (parentFile != null && !parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    ImageIO.write(downloadedImage, "jpg", mapFile);
                }
                catch (Exception exception) {
                    System.err.println("Unable to cache HD world map: " + exception.getMessage());
                }

                final BufferedImage finalImage = downloadedImage;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!WorldMapPanel.this.usingCacheRenderedMap) {
                            WorldMapPanel.this.applyHdWorldMap(
                                finalImage,
                                WORLD_MIN_X,
                                WORLD_MIN_Y,
                                WORLD_MAX_X_EXCLUSIVE,
                                WORLD_MAX_Y_EXCLUSIVE,
                                false
                            );
                            System.out.println(
                                "HD 2006 world map ready: " + finalImage.getWidth() + "x" + finalImage.getHeight()
                            );
                        }
                    }
                });
            }
        }, "WorldMapDownloader");

        downloadThread.setDaemon(true);
        downloadThread.start();
    }

    private void loadMapRegionSprites() {
        int index = 0;
        while (index < this.mapIndex.mapIndexEntryCount) {
            String path = "./data/launcher/sprites/0/" + index + " " + this.mapIndex.regionIds[index] + ".png";
            if (FileUtil.exists(path)) {
                Image image = this.toolkit.getImage(path);
                Color transparentColor = new Color(0xFF00FF);
                ImageFilter imageFilter = new TransparentColorFilter(transparentColor);
                FilteredImageSource filteredImageSource = new FilteredImageSource(image.getSource(), imageFilter);
                regionImages[index][0] = Toolkit.getDefaultToolkit().createImage(filteredImageSource);

                int regionId = this.mapIndex.regionIds[index];
                regionBaseXs[index] = regionId >> 8 << 6;
                regionBaseYs[index] = (regionId & 0xFF) << 6;
            }
            ++index;
        }
    }

    private void drawLegacyMap(Graphics graphics) {
        if (regionImages == null || regionBaseXs == null || regionBaseYs == null) {
            return;
        }
        int index = 0;
        while (index < this.mapIndex.mapIndexEntryCount) {
            Image regionImage = regionImages[index][0];
            int regionX = regionBaseXs[index];
            int regionY = regionBaseYs[index];
            if (regionImage != null
                && regionX >= WORLD_MIN_X
                && regionX < WORLD_MAX_X_EXCLUSIVE
                && regionY >= WORLD_MIN_Y
                && regionY < WORLD_MAX_Y_EXCLUSIVE) {
                int drawX = (int)Math.round((regionX - WORLD_MIN_X) * HD_PIXELS_PER_TILE * this.mapScale);
                int drawY = (int)Math.round((WORLD_MAX_Y_EXCLUSIVE - regionY - 64) * HD_PIXELS_PER_TILE * this.mapScale);
                int regionSize = (int)Math.round(64 * HD_PIXELS_PER_TILE * this.mapScale);
                graphics.drawImage(regionImage, drawX, drawY, regionSize, regionSize, null);
            }
            ++index;
        }
    }


    private BufferedImage getCacheRenderedTile(int tileX, int tileY) {
        String key = tileX + "_" + tileY;
        BufferedImage image = this.cacheRenderedTileCache.get(key);
        if (image != null) {
            return image;
        }
        try {
            File tileFile = new File(
                CACHE_RENDERED_TILE_DIRECTORY,
                "tile_" + tileX + "_" + tileY + ".png"
            );
            if (!tileFile.exists()) {
                return null;
            }
            image = ImageIO.read(tileFile);
            if (image != null) {
                image = this.toCompactRgb(image);
                this.cacheRenderedTileCache.put(key, image);
            }
            return image;
        }
        catch (Exception exception) {
            System.err.println("Unable to load cache-map tile " + key + ": " + exception.getMessage());
            return null;
        }
    }

    private void drawCacheRenderedTiles(Graphics2D graphics2D) {
        if (!this.usingCacheRenderedTiles || this.cacheTilePixelsPerGameTile <= 0 || this.cacheTileGameSize <= 0) {
            return;
        }

        Object oldInterpolation = graphics2D.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        graphics2D.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        java.awt.Rectangle clip = graphics2D.getClipBounds();
        if (clip == null) {
            clip = this.getVisibleRect();
        }

        int nativeTilePixels = this.cacheTileGameSize * this.cacheTilePixelsPerGameTile;
        double scaledTilePixels = nativeTilePixels * this.mapScale;
        int startTileX = Math.max(0, (int)Math.floor(clip.x / scaledTilePixels));
        int startTileY = Math.max(0, (int)Math.floor(clip.y / scaledTilePixels));
        int endTileX = Math.min(
            this.cacheTileColumnCount - 1,
            (int)Math.floor((clip.x + Math.max(0, clip.width - 1)) / scaledTilePixels)
        );
        int endTileY = Math.min(
            this.cacheTileRowCount - 1,
            (int)Math.floor((clip.y + Math.max(0, clip.height - 1)) / scaledTilePixels)
        );

        for (int tileY = startTileY; tileY <= endTileY; ++tileY) {
            for (int tileX = startTileX; tileX <= endTileX; ++tileX) {
                BufferedImage tileImage = this.getCacheRenderedTile(tileX, tileY);
                if (tileImage == null) {
                    continue;
                }

                int sourceX = tileX * nativeTilePixels;
                int sourceY = tileY * nativeTilePixels;
                int drawX = (int)Math.round(sourceX * this.mapScale);
                int drawY = (int)Math.round(sourceY * this.mapScale);
                int drawX2 = (int)Math.round((sourceX + tileImage.getWidth()) * this.mapScale);
                int drawY2 = (int)Math.round((sourceY + tileImage.getHeight()) * this.mapScale);
                graphics2D.drawImage(
                    tileImage,
                    drawX,
                    drawY,
                    drawX2 - drawX,
                    drawY2 - drawY,
                    null
                );
            }
        }

        if (oldInterpolation != null) {
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        }
    }

    private void drawHdMap(Graphics2D graphics2D) {
        if (this.usingCacheRenderedTiles) {
            this.drawCacheRenderedTiles(graphics2D);
            return;
        }
        if (this.hdWorldMap == null) {
            this.drawLegacyMap(graphics2D);
            return;
        }

        Object oldInterpolation = graphics2D.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        graphics2D.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );
        graphics2D.drawImage(
            this.hdWorldMap,
            0,
            0,
            this.mapWidthPixels,
            this.mapHeightPixels,
            null
        );
        if (oldInterpolation != null) {
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        }
    }

    private void drawPlayerMarker(Graphics2D graphics2D, Player player) {
        int worldX = player.getPosition().getX();
        int worldY = player.getPosition().getY();
        int plane = player.getPosition().getPlane();

        if (plane != 0 || !this.isOnHdWorldMap(worldX, worldY)) {
            return;
        }

        Point mapPoint = this.worldToMapPoint(worldX, worldY);
        int markerRadius = player.isBot ? 4 : 5;

        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setStroke(new BasicStroke(1.0f));
        graphics2D.setColor(new Color(0, 0, 0, 190));
        graphics2D.fillOval(
            mapPoint.x - markerRadius - 1,
            mapPoint.y - markerRadius - 1,
            markerRadius * 2 + 2,
            markerRadius * 2 + 2
        );

        graphics2D.setColor(player.isBot ? new Color(255, 170, 45) : new Color(70, 235, 110));
        graphics2D.fillOval(
            mapPoint.x - markerRadius,
            mapPoint.y - markerRadius,
            markerRadius * 2,
            markerRadius * 2
        );
        graphics2D.setColor(Color.white);
        graphics2D.drawOval(
            mapPoint.x - markerRadius,
            mapPoint.y - markerRadius,
            markerRadius * 2,
            markerRadius * 2
        );

        if (!this.showPlayerNames) {
            return;
        }

        String username = player.getUsername();
        Font font = new Font("Calibri", Font.BOLD, 12);
        FontMetrics fontMetrics = this.getFontMetrics(font);
        graphics2D.setFont(font);

        int textX = mapPoint.x - fontMetrics.stringWidth(username) / 2;
        int textY = mapPoint.y - markerRadius - 4;

        graphics2D.setColor(Color.black);
        graphics2D.drawString(username, textX - 1, textY);
        graphics2D.drawString(username, textX + 1, textY);
        graphics2D.drawString(username, textX, textY - 1);
        graphics2D.drawString(username, textX, textY + 1);
        graphics2D.setColor(Color.white);
        graphics2D.drawString(username, textX, textY);
    }

    @Override
    protected final void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        this.checkForCacheRenderedMapAsync();

        Graphics2D graphics2D = (Graphics2D)graphics.create();
        try {
            this.drawHdMap(graphics2D);

            Player[] players = World.getPlayers();
            for (int playerIndex = 0; playerIndex < players.length; ++playerIndex) {
                Player player = players[playerIndex];
                if (player != null) {
                    this.drawPlayerMarker(graphics2D, player);
                }
            }
        }
        finally {
            graphics2D.dispose();
        }
    }
}
