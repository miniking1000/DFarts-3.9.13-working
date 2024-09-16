package me.Fupery.ArtMap.Painting.Brushes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.Fupery.ArtMap.api.Colour.SimpleDye;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.Fupery.ArtMap.ArtMap;
import me.Fupery.ArtMap.Painting.Brush;
import me.Fupery.ArtMap.Painting.CachedPixel;
import me.Fupery.ArtMap.Painting.CanvasRenderer;
import me.Fupery.ArtMap.api.Colour.ArtDye;

public class Fill extends Brush {
    private final ArrayList<CachedPixel> lastFill;
    private final int axisLength;
    private final Dropper dropper;

    public Fill(CanvasRenderer renderer, Player player, Dropper dropper) {
        super(renderer, player);
        lastFill = new ArrayList<>();
        this.axisLength = getAxisLength();
        this.dropper = dropper;
        cooldownMilli = 350;
    }

    @Override
    public List<CachedPixel> paint(BrushAction action, ItemStack bucket, long strokeTime) {

        if (action == BrushAction.LEFT_CLICK) {
            ArtDye colour = ArtMap.instance().getDyePalette().getDye(this.player.getInventory().getItemInOffHand());
            if (colour == null && dropper.checkMaterial(this.player.getInventory().getItemInOffHand()) && dropper.getColour() != null) {
                colour = new SimpleDye(dropper.getColour());
            }

            if (colour != null) {
                clean();
                fillPixel(colour);
            }

        } else if (!lastFill.isEmpty()) {
            for (CachedPixel cachedPixel : lastFill) {
                addPixel(cachedPixel.x, cachedPixel.y, cachedPixel.dye);
            }
        }
        return this.lastFill;
    }

    @Override
    public boolean checkMaterial(ItemStack bucket) {
        return bucket.getType() == Material.BUCKET;
    }

    @Override
    public void clean() {
        lastFill.clear();
    }

    private void fillPixel(ArtDye colour) {
        final byte[] pixel = getCurrentPixel();
        if (pixel == null) return;

        final LinkedList<byte[]> queue = new LinkedList<>();
        final boolean[][] coloured = new boolean[axisLength][axisLength];
        final byte clickedColour = getPixelBuffer()[pixel[0]][pixel[1]];

        queue.add(pixel);
        while (!queue.isEmpty()){
            fillBucket(coloured, queue.poll(), clickedColour, colour, queue);
        }
    }

    private void fillBucket(boolean[][] coloured, byte[] pixel, byte sourceColour, ArtDye dye, LinkedList<byte[]> queue) {
        byte x = pixel[0];
        byte y = pixel[1];

        if (x < 0 || y < 0) return;
        if (x >= axisLength || y >= axisLength) return;
        if (coloured[x][y]) return;
        if (getPixelBuffer()[x][y] != sourceColour) return;

        coloured[x][y] = true;
        lastFill.add(new CachedPixel(x, y, sourceColour));
        dye.apply(getPixelAt(x, y));

        queue.add(new byte[]{(byte) (x - 1), y});
        queue.add(new byte[]{(byte) (x + 1), y});
        queue.add(new byte[]{x, (byte) (y - 1)});
        queue.add(new byte[]{x, (byte) (y + 1)});
    }
}
