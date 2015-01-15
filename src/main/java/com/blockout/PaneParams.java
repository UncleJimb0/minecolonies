package com.blockout;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.util.MathHelper;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaneParams
{
    Node node;
    View parentView;

    public PaneParams(Node n)
    {
        node = n;
    }

    public String getType() { return node.getNodeName(); }

    public void setParentView(View parent) { parentView = parent; }
    public View getParentView() { return parentView; }

    public int getParentWidth() { return parentView != null ? parentView.getInteriorWidth() : 0; }
    public int getParentHeight() { return parentView != null ? parentView.getInteriorHeight() : 0; }

    public List<PaneParams> getChildren()
    {
        List<PaneParams> list = null;

        Node child = node.getFirstChild();
        while (child != null)
        {
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                if (list == null)
                {
                    list = new ArrayList<PaneParams>();
                }

                list.add(new PaneParams(child));
            }
            child = child.getNextSibling();
        }

        return list;
    }

    public String getText()
    {
        return node.getTextContent().trim();
    }

    public String getLocalizedText()
    {
        return Localize(node.getTextContent().trim());
    }

    public String getStringAttribute(String name) { return getStringAttribute(name, ""); }
    public String getStringAttribute(String name, String def)
    {
        Node attr = getAttribute(name);
        return (attr != null) ? attr.getNodeValue() : def;
    }

    public String getLocalizedStringAttribute(String name) { return getLocalizedStringAttribute(name, ""); }
    public String getLocalizedStringAttribute(String name, String def)
    {
        return Localize(getStringAttribute(name, def));
    }

    public int getIntegerAttribute(String name) { return getIntegerAttribute(name, 0); }
    public int getIntegerAttribute(String name, int def)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            try { return Integer.parseInt(attr); }
            catch (NumberFormatException ex) {}
        }
        return def;
    }

    public float getFloatAttribute(String name) { return getFloatAttribute(name, 0); }
    public float getFloatAttribute(String name, float def)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            try { return Float.parseFloat(attr) ; }
            catch (NumberFormatException ex) {}
        }
        return def;
    }

    public double getDoubleAttribute(String name) { return getDoubleAttribute(name, 0); }
    public double getDoubleAttribute(String name, double def)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            try { return Double.parseDouble(attr); }
            catch (NumberFormatException ex) {}
        }

        return def;
    }

    public boolean getBooleanAttribute(String name) { return getBooleanAttribute(name, false); }
    public boolean getBooleanAttribute(String name, boolean def)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            return Boolean.parseBoolean(attr);
        }
        return def;
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getEnumAttribute(String name, T def)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            try { return def.valueOf((Class<T>)(Object)def.getClass(), attr); }
            catch (IllegalArgumentException exc) {}
        }
        return def;
    }

    static Pattern percentagePattern = Pattern.compile("([-+]?\\d+)(%|px)?", Pattern.CASE_INSENSITIVE);
    private int parseScalableIntegerRegexMatch(Matcher m, int def, int scale)
    {
        try
        {
            int value = Integer.parseInt(m.group(1));

            if ("%".equals(m.group(2)))
            {
                value = scale * MathHelper.clamp_int(value, 0, 100) / 100;
            }
            //  DO NOT attempt to do a "value < 0" treated as (100% of parent) - abs(size)
            //  without differentiating between 'size' and 'position' value types
            //  even then, it's probably not actually necessary...

            return value;
        }
        catch (Exception ex)
        {
            //  NumberFormatException | NullPointerException | IndexOutOfBoundsException | IllegalStateException ex
        }

        return def;
    }

    public int getScalableIntegerAttribute(String name, int def, int scale)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            Matcher m = percentagePattern.matcher(attr);
            if (m.find())
            {
                return parseScalableIntegerRegexMatch(m, def, scale);
            }
        }

        return def;
    }

    public static class SizePair
    {
        public SizePair(int w, int h) { x = w; y = h; }

        public int x;
        public int y;
    }

    public SizePair getSizePairAttribute(String name, SizePair def, SizePair scale)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            int w = def != null ? def.x : 0;
            int h = def != null ? def.y : 0;

            Matcher m = percentagePattern.matcher(attr);
            if (m.find())
            {
                w = parseScalableIntegerRegexMatch(m, w, scale != null ? scale.x : 0);

                if (m.find() || m.find(0))
                {
                    //  If no second value is passed, use the first value
                    h = parseScalableIntegerRegexMatch(m, h, scale != null ? scale.y : 0);
                }
            }

            return new SizePair(w, h);
        }

        return def;
    }

    static Pattern rgbaPattern = Pattern.compile("rgba?\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*(?:,\\s*([01]\\.\\d+)\\s*)?\\)", Pattern.CASE_INSENSITIVE);

    public int getColorAttribute(String name, int def)
    {
        String attr = getStringAttribute(name, null);
        if (attr != null)
        {
            if (attr.startsWith("#"))
            {
                //  CSS Hex format: #00112233
                try{ return Integer.parseInt(attr.substring(1), 16); }
                catch (NumberFormatException ex){}
            }
            else if (attr.startsWith("rgb(") || attr.startsWith("rgba("))
            {
                //  CSS RGB format: rgb(255,0,0) and rgba(255,0,0,0.3)
                Matcher m = rgbaPattern.matcher(attr);

                if (m.find())
                {
                    try
                    {
                        int r = MathHelper.clamp_int(Integer.parseInt(m.group(1)), 0, 255);
                        int g = MathHelper.clamp_int(Integer.parseInt(m.group(2)), 0, 255);
                        int b = MathHelper.clamp_int(Integer.parseInt(m.group(3)), 0, 255);

                        int color = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

                        if (attr.startsWith("rgba"))
                        {
                            int alpha = (int) (Float.parseFloat(m.group(4)) * 255.0f);
                            color |= MathHelper.clamp_int(alpha, 0, 255) << 24;
                        }

                        return color;
                    }
                    catch (Exception ex)
                    {
                        //  NumberFormatException | NullPointerException | IndexOutOfBoundsException | IllegalStateException ex
                    }
                }
            }
            else
            {
                //  Integer
                try{ return Integer.parseInt(attr); }
                catch (NumberFormatException ex){}

                return Color.getByName(attr, def);
            }
        }
        return def;
    }

    private Node getAttribute(String name)
    {
        return node.getAttributes().getNamedItem(name);
    }

    private static String Localize(String str)
    {
        if (str == null)
        {
            return str;
        }

        int index = str.indexOf("$(");
        while (index != -1)
        {
            int endIndex = str.indexOf(")", index);

            if (endIndex == -1)
            {
                break;
            }

            String key = str.substring(index + 2, endIndex);
            String replacement = LanguageRegistry.instance().getStringLocalization(key);

            if (replacement.isEmpty())
            {
                replacement = "MISSING:" + key;
            }

            str = str.substring(0, index) + replacement + str.substring(endIndex + 1);

            index = str.indexOf("$(", index + replacement.length());
        }

        return str;
    }
}