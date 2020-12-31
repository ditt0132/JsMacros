package xyz.wagyourtail.jsmacros.client.api.helpers;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

/**
 * @author Wagyourtail
 * @since 1.0.8
 */
public class TextHelper extends BaseHelper<Text> {
    
    public TextHelper(String json) {
        super(Text.Serializer.fromJson(json));
    }
    
    public TextHelper(Text t) {
        super(t);
    }
    
    /**
     * replace the text in this class with JSON data.
     * @since 1.0.8
     * @param json
     * @return
     */
    public TextHelper replaceFromJson(String json) {
        base = Text.Serializer.fromJson(json);
        return this;
    }
    
    /**
     * replace the text in this class with {@link java.lang.String String} data.
     * @since 1.0.8
     * @param content
     * @return
     */
    public TextHelper replaceFromString(String content) {
        base = new LiteralText(content);
        return this;
    }
    
    /**
     * @since 1.2.7
     * @return JSON data representation.
     */
    public String getJson() {
        return Text.Serializer.toJson(base);
    }

    /**
     * @since 1.2.7
     * @return the text content.
     */
    public String getString() {
        return base.getString();
    }
    
    
    /**
     * @since 1.0.8
     * @deprecated confusing name.
     * @return
     */
    public String toJson() {
        return getJson();
    }

    /**
     * @since 1.0.8, this used to do the same as {@link #getString}
     * @return String representation of text helper.
     */
    public String toString() {
        return String.format("TextHelper:{\"text\": \"%s\"}", base.getString());
    }
}
