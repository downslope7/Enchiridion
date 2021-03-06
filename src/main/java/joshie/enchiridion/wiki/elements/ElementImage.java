package joshie.enchiridion.wiki.elements;

import static java.io.File.separator;
import static joshie.enchiridion.helpers.OpenGLHelper.disable;
import static joshie.enchiridion.helpers.OpenGLHelper.enable;
import static joshie.enchiridion.helpers.OpenGLHelper.end;
import static joshie.enchiridion.helpers.OpenGLHelper.fixColors;
import static joshie.enchiridion.helpers.OpenGLHelper.scale;
import static joshie.enchiridion.helpers.OpenGLHelper.start;
import static org.lwjgl.opengl.GL11.GL_BLEND;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import joshie.enchiridion.EConfig;
import joshie.enchiridion.ELogger;
import joshie.enchiridion.Enchiridion;
import joshie.enchiridion.helpers.ClientHelper;
import joshie.enchiridion.helpers.OpenGLHelper;
import joshie.enchiridion.wiki.WikiHelper;
import joshie.enchiridion.wiki.WikiPage;
import joshie.enchiridion.wiki.gui.popups.PageEditResource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

import com.google.gson.annotations.Expose;

public class ElementImage extends Element {
    private DynamicTexture texture;
    private ResourceLocation resource;
    private boolean isDynamic;

    @Expose
    public String path;
    public int img_width;
    public int img_height;

    public ElementImage setPath(String fileName, boolean resource) {
        WikiPage page = WikiHelper.getPage();
        this.width = 100;
        this.height = 100;
        this.path = fileName;
        if (!resource && !EConfig.DEFAULT_DIR.equals("")) {
            this.path = "mod@" + EConfig.DEFAULT_DIR + "@" + path;
        }

        loadImage(WikiHelper.getPage());
        this.markDirty();
        return this;
    }

    //Loads the image in to memory
    public void loadImage(WikiPage page) {
        if (!path.contains(":")) {
            try {
                BufferedImage img = null;
                if (path.startsWith("root.")) {
                    ImageIO.read(new File(Enchiridion.root + separator + "wiki" + separator + path.replace("root.", "")));
                } else {
                    if (path.startsWith("mod@")) {
                        String[] split = path.split("@");
                        String mod = page.getCategory().getTab().getMod().getKey();
                        String tab = page.getCategory().getTab().getKey();
                        String cat = page.getCategory().getKey();
                        String article = page.getKey();
                        String image = "/assets/" + split[1] + "/wiki/" + mod + "/" + tab + "/" + cat + "/" + article + "/" + split[2];
                        img = ImageIO.read(Enchiridion.class.getResourceAsStream(image));
                    } else img = ImageIO.read(new File(new File(page.getPath()).getParentFile(), path)); //If We fail to read the image from the live directory, let's grab it from resources
                }

                texture = new DynamicTexture(img);
                resource = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(path, texture);
                isDynamic = true;
                img_width = img.getWidth();
                img_height = img.getHeight();
            } catch (Exception e) {
                ELogger.log(Level.ERROR, "Enchiridion 2 failed to read in the image at the following path: ");
                ELogger.log(Level.ERROR, path);

                e.printStackTrace();
            }
        } else {
            String[] split = path.split(":");
            if (split.length == 2 || split.length == 3 || split.length == 4) resource = new ResourceLocation(split[0], split[1]);
            try {
                double splitX = split.length >= 3 ? Double.parseDouble(split[2]) : 1D;
                double splitY = split.length == 4 ? Double.parseDouble(split[3]) : 1D;
                BufferedImage image = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream());
                img_width = (int) (image.getWidth() / splitX);
                img_height = (int) (image.getHeight() / splitY);
            } catch (Exception e) {
                ELogger.log(Level.ERROR, "Enchiridion 2 failed to read in the image at the following resource: ");
                ELogger.log(Level.ERROR, page.getPath() + separator + resource);
            }

            isDynamic = false;
        }
    }

    @Override
    public ElementImage setToDefault() {
        this.width = 100;
        this.height = 100;
        this.path = "enchiridion:textures/wiki/enchiridion_logo.png:2.5";
        loadImage(WikiHelper.getPage());
        return this;
    }

    @Override
    public void display(boolean isEditMode) {
        OpenGLHelper.fixColors();

        if (isDynamic) {
            start();
            enable(GL_BLEND);
            texture.updateDynamicTexture();
            Tessellator tessellator = Tessellator.instance;
            ClientHelper.getMinecraft().getTextureManager().bindTexture(resource);
            fixColors();
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(WikiHelper.getScaledX(BASE_X + left), WikiHelper.getScaledY(BASE_Y + top + (height * 2)), 0, 0.0, 1.0);
            tessellator.addVertexWithUV(WikiHelper.getScaledX(BASE_X + left + (width * 2)), WikiHelper.getScaledY(BASE_Y + top + (height * 2)), 0, 1.0, 1.0);
            tessellator.addVertexWithUV(WikiHelper.getScaledX(BASE_X + left + (width * 2)), WikiHelper.getScaledY(BASE_Y + top), 0, 1.0, 0.0);
            tessellator.addVertexWithUV(WikiHelper.getScaledX(BASE_X + left), WikiHelper.getScaledY(BASE_Y + top), 0, 0.0, 0.0);
            tessellator.draw();
            disable(GL_BLEND);
            end();
        } else if (resource != null) {
            ClientHelper.getMinecraft().getTextureManager().bindTexture(resource);
            scaleTexture(BASE_X + left, BASE_Y + top, (float) width / 125F, (float) height / 125F);
        } else if (resource == null) {
            loadImage(WikiHelper.getPage());
        }
    }

    private void scaleTexture(int x, int y, float scaleX, float scaleY) {
        start();
        enable(GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ClientHelper.bindTexture(resource);
        scale(scaleX, scaleY);
        WikiHelper.drawTexture(WikiHelper.getScaledX(x, scaleX), WikiHelper.getScaledY(y, scaleY), 0, 0, img_width, img_height);
        disable(GL_BLEND);
        end();
    }

    @Override
    public void onSelected(int x, int y, int button) {
        if (!isDynamic) {
            ((PageEditResource) (WikiHelper.getInstance(PageEditResource.class))).setEditing(this);
        }
    }
}
