package me.Fupery.ArtMap.Painting.Brushes;

import me.Fupery.ArtMap.ArtMap;
import me.Fupery.ArtMap.Painting.Brush;
import me.Fupery.ArtMap.Painting.CachedPixel;
import me.Fupery.ArtMap.Painting.CanvasRenderer;
import me.Fupery.ArtMap.api.Colour.ArtDye;
import me.Fupery.ArtMap.api.Colour.SimpleDye;
import me.Fupery.ArtMap.api.Painting.Pixel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class BigBrush extends Brush {
    private final ArrayList<CachedPixel> lastFill;
    private int axisLength;
    private Dropper dropper;
    private boolean[][] coloured;
    private byte[] lastPixel;

    public BigBrush(CanvasRenderer renderer, Player player, Dropper dropper) {
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
        int brushSize = nameDigits.isEmpty() ? 1 : max(Integer.parseInt(nameDigits), 1);


        if (strokeTime > 250 || action == BrushAction.LEFT_CLICK) clean();
        if (lastPixel != null){
            flowBrush(lastPixel[0], lastPixel[1], pixel[0], pixel[1], dyeColor, brushSize);
        }
        fillArea(pixel[0], pixel[1], dyeColor, brushSize);
        lastPixel = pixel;
        return lastFill;
    }

    @Override
    public boolean checkMaterial(ItemStack bucket) {
        return bucket.getType() == Material.BRUSH;
    }

    @Override
    public void clean() {
        lastFill.clear();
        coloured = new boolean[axisLength][axisLength];
        lastPixel = null;
    }

    private void fillArea(int x, int y, ArtDye dye, int brushSize) {
        for (int i = max(0, x - brushSize / 2); i < min(axisLength, x + brushSize / 2 + brushSize % 2); i++)
            for (int j = max(0, y - brushSize / 2); j < min(axisLength, y + brushSize / 2 + brushSize % 2); j++) {
                if (!coloured[i][j]) {
                    lastFill.add(new CachedPixel(i, j, getPixel(i, j)));
                    dye.apply(getPixelAt(i, j));
                    coloured[i][j] = true;
                }
            }
    }

    private void flowBrush(int x, int y, int x2, int y2, ArtDye dye, int brushSize) {

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
            fillArea(x, y, dye, brushSize);
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
