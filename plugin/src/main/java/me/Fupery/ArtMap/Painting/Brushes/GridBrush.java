package me.Fupery.ArtMap.Painting.Brushes;

import me.Fupery.ArtMap.ArtMap;
import me.Fupery.ArtMap.Painting.Brush;
import me.Fupery.ArtMap.Painting.CachedPixel;
import me.Fupery.ArtMap.Painting.CanvasRenderer;
import me.Fupery.ArtMap.api.Colour.ArtDye;
import me.Fupery.ArtMap.api.Colour.SimpleDye;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class GridBrush extends Brush {
    private final ArrayList<CachedPixel> lastFill; // Вообще нигде не используется, нужно только для return'а в paint
    private int axisLength;
    private Dropper dropper;
    private boolean[][] coloured;
    private byte[] lastPixel;

    public GridBrush(CanvasRenderer renderer, Player player, Dropper dropper) {
        super(renderer, player);
        lastFill = new ArrayList<>();
        this.axisLength = getAxisLength();
        this.dropper = dropper;
        coloured = new boolean[axisLength][axisLength];
    }

    @Override
    public List<CachedPixel> paint(BrushAction action, ItemStack bucket, long strokeTime) {
        byte[] pixel = getCurrentPixel();
        if (pixel == null) return lastFill;
        ArtDye dyeColor = ArtMap.instance().getDyePalette().getDye(this.player.getInventory().getItemInOffHand());
        if (dyeColor == null) {
            if (dropper.checkMaterial(this.player.getInventory().getItemInOffHand())) {
                if (action == BrushAction.LEFT_CLICK) {
                    dropper.paint(action, bucket, strokeTime);
                    return lastFill;
                }
                if (dropper.getColour() != null)
                    dyeColor = new SimpleDye(dropper.getColour());
                else
                    return lastFill;
            }
            else return lastFill;
        }

        String nameDigits = bucket.getItemMeta().getDisplayName().replaceAll("[\\D]", "");
        int gridSize = nameDigits.isEmpty() ? 1 : max(Integer.parseInt(nameDigits), 1);
        pixel[0] /= (byte) gridSize;
        pixel[1] /= (byte) gridSize;

        if (strokeTime > 250 || action == BrushAction.LEFT_CLICK) clean();
        if (lastPixel != null) {
            flowBrush(lastPixel[0], lastPixel[1], pixel[0], pixel[1], dyeColor, gridSize);
        }
        fillArea(pixel[0], pixel[1], dyeColor, gridSize);
        lastPixel = pixel;
        return this.lastFill;
    }

    @Override
    public boolean checkMaterial(ItemStack bucket) {
        return bucket.getType() == Material.HONEYCOMB;
    }

    @Override
    public void clean() {
        lastFill.clear();
        coloured = new boolean[axisLength][axisLength];
        lastPixel = null;
    }

    private void fillArea(int x, int y, ArtDye dye, int gridSize) {
        for (int i = x * gridSize; i < min(axisLength, (x + 1) * gridSize); i++)
            for (int j = max(0, y * gridSize); j < min(axisLength, (y + 1) * gridSize); j++) {
                if (!coloured[i][j]) {
                    dye.apply(getPixelAt(i, j));
                    coloured[i][j] = true;
                }
            }
    }

    private void flowBrush(int x, int y, int x2, int y2, ArtDye dye, int gridSize) {

        int w = x2 - x;
        int h = y2 - y;

        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;

        if (w != 0) {
            dx1 = (w > 0) ? 1 : -1;
            dx2 = (w > 0) ? 1 : -1;
        }

        if (h != 0) {
            dy1 = (h > 0) ? 1 : -1;
        }

        int longest = Math.abs(w);
        int shortest = Math.abs(h);

        if (!(longest > shortest)) {
            longest = Math.abs(h);
            shortest = Math.abs(w);

            if (h < 0) {
                dy2 = -1;

            } else if (h > 0) {
                dy2 = 1;
            }
            dx2 = 0;
        }
        int numerator = longest >> 1;

        for (int i = 0; i <= longest; i++) {
            fillArea(x, y, dye, gridSize);
            numerator += shortest;

            if (!(numerator < longest)) {
                numerator -= longest;
                x += dx1;
                y += dy1;

            } else {
                x += dx2;
                y += dy2;
            }
        }
    }
}
