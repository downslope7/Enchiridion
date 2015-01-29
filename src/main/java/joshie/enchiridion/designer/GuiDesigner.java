package joshie.enchiridion.designer;

import static java.io.File.separator;
import static joshie.enchiridion.helpers.OpenGLHelper.color;
import static joshie.enchiridion.helpers.OpenGLHelper.fixColors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import joshie.enchiridion.Enchiridion;
import joshie.enchiridion.designer.BookRegistry.BookData;
import joshie.enchiridion.wiki.WikiHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GuiDesigner extends GuiScreen {
    public int mouseX = 0;
    public int mouseY = 0;

    private ResourceLocation cover_left;
    private ResourceLocation cover_right;
    private ResourceLocation page_left;
    private ResourceLocation page_right;

    protected float red, green, blue;
    protected int leftX = 212;
    protected int rightX = 218;
    protected int ySize = 217;
    public boolean canEdit;

    public BookData bookData; //List of all pages in the book
    public DesignerCanvas canvas; //The current canvas we are displaying
    public static HashMap<String, Integer> page_number = new HashMap(); //The current page number for this book_id

    //The books id being initialised with this, use this to grab the books data
    public GuiDesigner(BookData data, boolean isEditMode) {
        red = (data.color >> 16 & 255) / 255.0F;
        green = (data.color >> 8 & 255) / 255.0F;
        blue = (data.color & 255) / 255.0F;
        bookData = data;
        canEdit = isEditMode;
        if (page_number.get(bookData.uniqueName) == null) {
            page_number.put(bookData.uniqueName, 0);
        }

        if (data.book.size() > 0) {
            canvas = bookData.book.get(page_number.get(bookData.uniqueName));
        }

        if (data.showBackground) {
            cover_left = new ResourceLocation("enchiridion", "textures/books/guide_cover_left.png");
            cover_right = new ResourceLocation("enchiridion", "textures/books/guide_cover_right.png");
            page_left = new ResourceLocation("enchiridion", "textures/books/guide_page_left.png");
            page_right = new ResourceLocation("enchiridion", "textures/books/guide_page_right.png");
        }
    }
    
    @Override
    public void initGui() {
        super.initGui();
        
        Keyboard.enableRepeatEvents(true);
    }
    
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        
        //If we were in edit mode, save the book
        if(canEdit) {
            try {
                File example = new File(Enchiridion.root + separator + "books", bookData.uniqueName.replace(":", "_").replace(".", "_") + ".json");
                Writer writer = new OutputStreamWriter(new FileOutputStream(example), "UTF-8");
                writer.write(WikiHelper.getGson().toJson(bookData));
                writer.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void drawLeftPage(int x, int y) {
        //Cover
        if (cover_left != null) {
            color(red, green, blue);
            mc.getTextureManager().bindTexture(cover_left);
            drawTexturedModalRect(x - 9, y, 35, 0, leftX + 9, ySize);
            fixColors();

            //Page
            mc.getTextureManager().bindTexture(page_left);
            drawTexturedModalRect(x, y, 44, 0, leftX, ySize);
        }

        if(bookData.showArrows || canEdit) {
            //Arrows
            drawTexturedModalRect(x + 21, y + 200, 0, 246, 18, 10);
            if (mouseX >= -192 && mouseX <= -174 && mouseY >= 100 && mouseY <= 110) {
                drawTexturedModalRect(x + 21, y + 200, 23, 246, 18, 10);
            }
        }
    }

    public void drawRightPage(int x, int y) {
        // Cover
        if (cover_right != null) {
            color(red, green, blue);
            mc.getTextureManager().bindTexture(cover_right);
            drawTexturedModalRect(x, y, 0, 0, rightX + 9, ySize);
            fixColors();

            //Page
            mc.getTextureManager().bindTexture(page_right);
            drawTexturedModalRect(x, y, 0, 0, rightX, ySize);
        }

        //Arrows
        if(bookData.showArrows || canEdit) {
            drawTexturedModalRect(x + 175, y + 200, 0, 246, 18, 10);
            if (mouseX >= 175 && mouseX <= 192 && mouseY >= 100 && mouseY <= 110) {
                drawTexturedModalRect(x + 175, y + 200, 23, 246, 18, 10);
            }
        }

        //Draw Page
        if(canvas != null) {
            canvas.draw(this, x, y);
        }

        //Numbers
        if(bookData.showNumber || canEdit) {
            mc.fontRenderer.drawString("" + (page_number.get(bookData.uniqueName) + 1), x + 124, y + 202, 0);
        }
    }

    public void drawScreen(int i, int j, float f) {
        fixColors();
        int x = width / 2;
        int y = 8;

        drawLeftPage(x - 212, y);
        drawRightPage(x, y);
        super.drawScreen(i, j, f);
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        super.mouseClicked(par1, par2, par3);
        
        if(canvas != null) {
            canvas.clicked(mouseX, mouseY, canEdit);
        }
        
        boolean clicked = false;
        if(bookData.showArrows || canEdit) {
            if (mouseX >= -192 && mouseX <= -174 && mouseY >= 100 && mouseY <= 110) {
                clicked = true;
            }
    
            if (mouseX >= 175 && mouseX <= 192 && mouseY >= 100 && mouseY <= 110) {
                clicked = true;
            }
        }

        if (clicked) {
            //TODO: Validate the Page Number
        }
    }

    @Override
    public void handleMouseInput() {
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        mouseX = x - (this.width / 2);
        mouseY = y - (ySize / 2);

        super.handleMouseInput();
    }
}