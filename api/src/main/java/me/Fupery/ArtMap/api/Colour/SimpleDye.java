package me.Fupery.ArtMap.api.Colour;

import me.Fupery.ArtMap.api.Painting.Pixel;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class SimpleDye extends ArtDye{
    /**
     * Durability value of -1 indicates that items of any durability will be accepted
     *
     * @param localizedName
     * @param englishName
     * @param chatColor
     * @param material
     */

    private final byte color;
    protected SimpleDye(String localizedName, String englishName, ChatColor chatColor, Material material, int color) {
        super(localizedName, englishName, chatColor, material);
        this.color = (byte) color;
    }

    public SimpleDye(int color){
        super("simpleDye", "simpleDye", ChatColor.WHITE, Material.POISONOUS_POTATO);
        this.color = (byte) color;
    }

    @Override
    public void apply(Pixel pixel) {
        pixel.setColour(this.color);
    }

    @Override
    public byte getDyeColour(byte currentPixelColour) {
        return this.color;
    }

    @Override
    public byte getColour() {
        return this.color;
    }
}
